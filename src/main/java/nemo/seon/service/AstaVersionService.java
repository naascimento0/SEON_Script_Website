package nemo.seon.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import nemo.seon.model.AstaVersion;
import nemo.seon.model.dto.AstaVersionView;

/**
 * Keeps a history of uploaded {@code .asta} files so an admin can tell an old model from a
 * current one and roll back to an earlier upload.
 *
 * <p>Same shape as {@link PublicationService}: no database. Each upload is archived under the
 * versions directory ({@code seon.astah.versions.dir}) and described in a {@code versions.json}
 * index in that same directory, so the history survives a rebuild/redeploy. The file the rest of
 * the app reads ({@code seon.astah.filepath}) stays exactly where it was — this service only
 * decides which archived version is copied into it.
 *
 * <p>A candidate file is always parsed <em>before</em> it replaces the active one. A corrupt or
 * unreadable upload therefore leaves the running site untouched, which the previous
 * overwrite-then-reload flow could not guarantee.
 */
@Service
public class AstaVersionService {

    private static final Logger logger = LoggerFactory.getLogger(AstaVersionService.class);
    private static final String INDEX_FILENAME = "versions.json";
    private static final int MAX_NOTE_LENGTH = 200;
    private static final DateTimeFormatter FILENAME_STAMP =
            DateTimeFormatter.ofPattern("uuuuMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<AstaVersion> versions = new ArrayList<>();
    private final DiagramsService diagramsService;
    private final OntologyService ontologyService;

    private String activeId;

    @Value("${seon.astah.versions.dir}")
    private String versionsDirName;

    public AstaVersionService(DiagramsService diagramsService, OntologyService ontologyService) {
        this.diagramsService = diagramsService;
        this.ontologyService = ontologyService;
    }

    /** Outcome of an upload, so the controller can word its message. */
    public record UploadOutcome(AstaVersion version, boolean duplicate) {}

    /** The index as stored on disk. */
    private record VersionIndex(String activeId, List<AstaVersion> versions) {}

    @PostConstruct
    public synchronized void initialize() {
        Path index = indexPath();
        try {
            Files.createDirectories(versionsDir());
            if (Files.exists(index)) {
                VersionIndex loaded = objectMapper.readValue(index.toFile(), VersionIndex.class);
                versions.addAll(loaded.versions() == null ? List.of() : loaded.versions());
                activeId = loaded.activeId();
                dropMissingFiles();
                logger.info("Loaded {} .asta version(s) from {}", versions.size(), index);
            } else {
                seedFromActiveFile();
            }
        } catch (IOException e) {
            // A broken history must not keep the site from starting: the model itself is already loaded.
            logger.error("Could not read the .asta version index at {}: {}", index, e.getMessage(), e);
        }
    }

    /** The full history, newest upload first. */
    public synchronized List<AstaVersionView> findAll() {
        List<AstaVersion> sorted = new ArrayList<>(versions);
        sorted.sort(Comparator.comparing(AstaVersion::uploadedAt).reversed());
        List<AstaVersionView> views = new ArrayList<>(sorted.size());
        for (AstaVersion version : sorted) {
            views.add(toView(version));
        }
        return List.copyOf(views);
    }

    /**
     * Archives an uploaded file and makes it the active model.
     *
     * <p>If the bytes are identical to the active version nothing is archived and the outcome is
     * flagged as a duplicate, so repeated uploads of the same file do not clutter the history.
     *
     * @throws IllegalArgumentException if the file cannot be parsed as a SEON model
     * @throws IllegalStateException    if the file cannot be written
     */
    public synchronized UploadOutcome upload(MultipartFile file, String note, String username) {
        String cleanedNote = cleanNote(note); // rejected up front, before anything is written
        Path staged;
        try {
            Files.createDirectories(versionsDir());
            staged = Files.createTempFile(versionsDir(), "upload", ".asta.tmp");
            Files.copy(file.getInputStream(), staged, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("Could not store the uploaded file", e);
        }

        try {
            String sha256 = sha256(staged);
            AstaVersion active = activeVersion();
            if (active != null && active.sha256().equals(sha256)) {
                return new UploadOutcome(active, true);
            }

            Instant now = Instant.now();
            String id = UUID.randomUUID().toString();
            String storedFilename = FILENAME_STAMP.format(now) + "__" + id + ".asta";
            Path stored = versionsDir().resolve(storedFilename);
            Files.move(staged, stored, StandardCopyOption.REPLACE_EXISTING);

            AstaVersion version;
            try {
                int[] counts = promote(stored);
                version = new AstaVersion(
                        id,
                        storedFilename,
                        file.getOriginalFilename(),
                        now.toString(),
                        username,
                        Files.size(stored),
                        sha256,
                        cleanedNote,
                        counts[0],
                        counts[1]);
            } catch (RuntimeException | IOException e) {
                // The candidate was rejected (or could not be measured): leave no orphan behind.
                deleteQuietly(stored);
                throw e;
            }

            versions.add(version);
            String previousActive = activeId;
            activeId = id;
            try {
                persist();
            } catch (IOException e) {
                versions.remove(version);
                activeId = previousActive;
                deleteQuietly(stored);
                throw new IllegalStateException("Could not save the version history", e);
            }
            logger.info("Archived and activated .asta version {} ({})", id, file.getOriginalFilename());
            return new UploadOutcome(version, false);
        } catch (IOException e) {
            throw new IllegalStateException("Could not archive the uploaded file", e);
        } finally {
            deleteQuietly(staged);
        }
    }

    /**
     * Makes an archived version the active model again: re-parses it, copies it over the active
     * file and regenerates the diagrams.
     *
     * @throws NoSuchElementException   if no version has that id
     * @throws IllegalArgumentException if the archived file is missing or no longer parses
     */
    public synchronized AstaVersionView activate(String id) {
        AstaVersion version = require(id);
        Path stored = versionsDir().resolve(version.storedFilename());
        if (Files.notExists(stored)) {
            throw new IllegalArgumentException("The archived file for this version is no longer on disk.");
        }

        promote(stored);
        String previousActive = activeId;
        activeId = id;
        try {
            persist();
        } catch (IOException e) {
            activeId = previousActive;
            throw new IllegalStateException("Could not save the version history", e);
        }
        logger.info("Activated .asta version {} ({})", id, version.originalFilename());
        return toView(version);
    }

    /**
     * Replaces the free-text note of a version.
     *
     * @throws NoSuchElementException if no version has that id
     */
    public synchronized AstaVersionView updateNote(String id, String note) {
        int index = indexOf(id);
        AstaVersion previous = versions.get(index);
        AstaVersion updated = previous.withNote(cleanNote(note));
        versions.set(index, updated);
        try {
            persist();
        } catch (IOException e) {
            versions.set(index, previous);
            throw new IllegalStateException("Could not save the version history", e);
        }
        return toView(updated);
    }

    /**
     * Removes an archived version and its file.
     *
     * @throws NoSuchElementException   if no version has that id
     * @throws IllegalArgumentException if the version is the active one
     */
    public synchronized void delete(String id) {
        int index = indexOf(id);
        if (id.equals(activeId)) {
            throw new IllegalArgumentException("The active version cannot be deleted. Activate another one first.");
        }
        AstaVersion removed = versions.remove(index);
        try {
            persist();
        } catch (IOException e) {
            versions.add(index, removed);
            throw new IllegalStateException("Could not save the version history", e);
        }
        deleteQuietly(versionsDir().resolve(removed.storedFilename()));
        logger.info("Deleted .asta version {} ({})", id, removed.originalFilename());
    }

    /**
     * Path of an archived file, for download.
     *
     * @throws NoSuchElementException   if no version has that id
     * @throws IllegalArgumentException if the archived file is missing
     */
    public synchronized Path fileOf(String id) {
        AstaVersion version = require(id);
        Path stored = versionsDir().resolve(version.storedFilename());
        if (Files.notExists(stored)) {
            throw new IllegalArgumentException("The archived file for this version is no longer on disk.");
        }
        return stored;
    }

    /** Metadata of a version, for naming the download. */
    public synchronized AstaVersion find(String id) {
        return require(id);
    }

    /**
     * Parses a candidate file and, only if it reads back as a SEON model, copies it over the
     * active {@code .asta} and regenerates the diagrams.
     *
     * @return {@code [ontologyCount, conceptCount]} of the promoted model
     */
    private int[] promote(Path candidate) {
        try {
            ontologyService.reloadFrom(candidate.toString());
        } catch (RuntimeException e) {
            logger.error("Rejected .asta candidate {}: {}", candidate.getFileName(), e.getMessage(), e);
            restoreActiveModel();
            throw new IllegalArgumentException(
                    "This file could not be read as a SEON model, so nothing was changed. "
                            + "The site is still serving the previous version.");
        }

        int ontologyCount = ontologyService.getOntologyCount();
        int conceptCount = ontologyService.getConceptCount();
        if (ontologyCount == 0) {
            logger.error("Rejected .asta candidate {}: no ontologies found", candidate.getFileName());
            restoreActiveModel();
            throw new IllegalArgumentException(
                    "No ontologies were found in this file, so nothing was changed. "
                            + "The site is still serving the previous version.");
        }

        try {
            Path active = Paths.get(diagramsService.getAstahFilePath());
            Files.createDirectories(active.toAbsolutePath().getParent());
            Files.copy(candidate, active, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            restoreActiveModel();
            throw new IllegalStateException("Could not replace the active .asta file", e);
        }

        // Re-read the very same bytes, but through the active path. Astah names the root package
        // after the file it opened, and both the diagram export folder and
        // OntologyViewService.buildImagePath assume that name is the active file's ("astah_seon").
        // Serving the model parsed from the archived copy would put the version's filename into
        // every diagram URL, and the images would 404.
        try {
            ontologyService.reloadOntologies();
        } catch (RuntimeException e) {
            throw new IllegalStateException(
                    "The .asta file was replaced but could not be read back from its active path", e);
        }

        diagramsService.exportAstahDiagrams();
        return new int[] { ontologyCount, conceptCount };
    }

    /** Puts the previously active file back in memory after a rejected candidate. */
    private void restoreActiveModel() {
        try {
            ontologyService.reloadOntologies();
        } catch (RuntimeException e) {
            logger.error("Could not reload the active .asta file after a rejected upload: {}", e.getMessage(), e);
        }
    }

    /** Records the file already on the server as version 1, so there is something to roll back to. */
    private void seedFromActiveFile() throws IOException {
        Path active = Paths.get(diagramsService.getAstahFilePath());
        if (Files.notExists(active)) {
            logger.info("No .asta file at {} yet; the version history starts empty.", active);
            return;
        }

        Instant now = Instant.now();
        String id = UUID.randomUUID().toString();
        String storedFilename = FILENAME_STAMP.format(now) + "__" + id + ".asta";
        Files.createDirectories(versionsDir());
        Files.copy(active, versionsDir().resolve(storedFilename), StandardCopyOption.REPLACE_EXISTING);

        AstaVersion seed = new AstaVersion(
                id,
                storedFilename,
                active.getFileName().toString(),
                now.toString(),
                "system",
                Files.size(active),
                sha256(active),
                "First version recorded — the file already on the server when versioning was enabled.",
                ontologyService.getOntologyCount(),
                ontologyService.getConceptCount());
        versions.add(seed);
        activeId = id;
        persist();
        logger.info("Seeded the .asta version history from {}", active);
    }

    /** Drops entries whose archived file was removed by hand, so the history matches the disk. */
    private void dropMissingFiles() {
        List<AstaVersion> missing = new ArrayList<>();
        for (AstaVersion version : versions) {
            if (Files.notExists(versionsDir().resolve(version.storedFilename()))) {
                missing.add(version);
            }
        }
        if (missing.isEmpty()) {
            return;
        }
        versions.removeAll(missing);
        for (AstaVersion version : missing) {
            logger.warn("Archived file {} is missing; dropped version {} from the history",
                    version.storedFilename(), version.id());
            if (version.id().equals(activeId)) {
                activeId = null;
            }
        }
        try {
            persist();
        } catch (IOException e) {
            logger.error("Could not rewrite the version index after dropping missing files: {}", e.getMessage(), e);
        }
    }

    private AstaVersion activeVersion() {
        if (activeId == null) {
            return null;
        }
        for (AstaVersion version : versions) {
            if (version.id().equals(activeId)) {
                return version;
            }
        }
        return null;
    }

    private AstaVersion require(String id) {
        return versions.get(indexOf(id));
    }

    private int indexOf(String id) {
        for (int i = 0; i < versions.size(); i++) {
            if (versions.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new NoSuchElementException("No .asta version with id " + id);
    }

    private AstaVersionView toView(AstaVersion version) {
        return new AstaVersionView(
                version.id(),
                version.originalFilename(),
                version.uploadedAt(),
                version.uploadedBy(),
                version.sizeBytes(),
                version.sha256(),
                version.note(),
                version.ontologyCount(),
                version.conceptCount(),
                version.id().equals(activeId));
    }

    private String cleanNote(String note) {
        if (note == null || note.isBlank()) {
            return null;
        }
        String trimmed = note.trim();
        if (trimmed.length() > MAX_NOTE_LENGTH) {
            throw new IllegalArgumentException("The note must be at most " + MAX_NOTE_LENGTH + " characters.");
        }
        return trimmed;
    }

    /** Writes to a sibling temp file and moves it into place, so a crash can't truncate the history. */
    private void persist() throws IOException {
        Path index = indexPath();
        Path parent = index.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, "versions", ".json.tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(temp.toFile(), new VersionIndex(activeId, versions));
            Files.move(temp, index, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private static String sha256(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path);
             DigestInputStream digest = new DigestInputStream(in, MessageDigest.getInstance("SHA-256"))) {
            byte[] buffer = new byte[8192];
            while (digest.read(buffer) != -1) {
                // reading is what updates the digest
            }
            StringBuilder hex = new StringBuilder();
            for (byte b : digest.getMessageDigest().digest()) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available in this JVM", e);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            logger.warn("Could not delete {}: {}", path, e.getMessage());
        }
    }

    private Path versionsDir() {
        return Paths.get(System.getProperty("user.dir")).resolve(versionsDirName);
    }

    private Path indexPath() {
        return versionsDir().resolve(INDEX_FILENAME);
    }
}

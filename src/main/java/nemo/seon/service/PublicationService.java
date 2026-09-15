package nemo.seon.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Year;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import nemo.seon.model.Publication;
import nemo.seon.model.PublicationCategory;
import nemo.seon.model.dto.PublicationRequest;

/**
 * Keeps the publication list in memory and persists it as JSON on the filesystem.
 *
 * <p>Same shape as the rest of the app: no database. The file lives next to the {@code .asta}
 * in the working directory, so a redeploy (git pull + rebuild) never overwrites entries added
 * by an admin. On first start the bundled seed at {@code classpath:/publications.json} is
 * copied out to that path.
 */
@Service
public class PublicationService {

    private static final Logger logger = LoggerFactory.getLogger(PublicationService.class);
    private static final String SEED_RESOURCE = "/publications.json";
    private static final int EARLIEST_YEAR = 1900;
    private static final int MAX_REFERENCE_LENGTH = 2000;
    private static final int MAX_ONTOLOGY_LENGTH = 200;
    private static final int MAX_LINK_LENGTH = 2000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<Publication> publications = new ArrayList<>();

    @Value("${seon.publications.filepath}")
    private String publicationsFileName;

    @PostConstruct
    public synchronized void initialize() {
        Path path = resolvePath();
        try {
            if (Files.notExists(path)) {
                seed(path);
            }
            List<Publication> loaded = objectMapper.readValue(path.toFile(), new TypeReference<>() {
            });
            publications.clear();
            publications.addAll(loaded);
            logger.info("Loaded {} publications from {}", publications.size(), path);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load publications from " + path, e);
        }
    }

    /** All publications, oldest first; ties keep the order in which they were added. */
    public synchronized List<Publication> findAll() {
        List<Publication> sorted = new ArrayList<>(publications);
        sorted.sort(Comparator.comparingInt(Publication::year));
        return List.copyOf(sorted);
    }

    /**
     * Validates and appends a publication, then rewrites the JSON file.
     *
     * @throws IllegalArgumentException if the request is incomplete or malformed
     */
    public synchronized Publication add(PublicationRequest request) {
        Publication publication = toPublication(request, UUID.randomUUID().toString());
        publications.add(publication);
        try {
            persist();
        } catch (IOException e) {
            publications.remove(publication);
            throw new IllegalStateException("Failed to save the publication file", e);
        }
        logger.info("Added publication {} ({}, {})", publication.id(), publication.year(), publication.category());
        return publication;
    }

    /**
     * Validates and replaces the publication with the given id, keeping its position in the list.
     *
     * @throws NoSuchElementException   if no publication has that id
     * @throws IllegalArgumentException if the request is incomplete or malformed
     */
    public synchronized Publication update(String id, PublicationRequest request) {
        int index = indexOf(id);
        Publication previous = publications.get(index);
        Publication updated = toPublication(request, previous.id());
        publications.set(index, updated);
        try {
            persist();
        } catch (IOException e) {
            publications.set(index, previous);
            throw new IllegalStateException("Failed to save the publication file", e);
        }
        logger.info("Updated publication {}", id);
        return updated;
    }

    /**
     * Removes the publication with the given id.
     *
     * @throws NoSuchElementException if no publication has that id
     */
    public synchronized void delete(String id) {
        int index = indexOf(id);
        Publication removed = publications.remove(index);
        try {
            persist();
        } catch (IOException e) {
            publications.add(index, removed);
            throw new IllegalStateException("Failed to save the publication file", e);
        }
        logger.info("Deleted publication {}", id);
    }

    private int indexOf(String id) {
        for (int i = 0; i < publications.size(); i++) {
            if (publications.get(i).id().equals(id)) {
                return i;
            }
        }
        throw new NoSuchElementException("No publication with id " + id);
    }

    private Publication toPublication(PublicationRequest request, String id) {
        if (request == null) {
            throw new IllegalArgumentException("Missing request body.");
        }

        PublicationCategory category = parseCategory(request.category());

        if (request.year() == null) {
            throw new IllegalArgumentException("Year is required.");
        }
        int maxYear = Year.now().getValue() + 2;
        if (request.year() < EARLIEST_YEAR || request.year() > maxYear) {
            throw new IllegalArgumentException("Year must be between " + EARLIEST_YEAR + " and " + maxYear + ".");
        }

        String reference = request.reference() == null ? "" : request.reference().trim();
        if (reference.isEmpty()) {
            throw new IllegalArgumentException("Reference is required.");
        }
        if (reference.length() > MAX_REFERENCE_LENGTH) {
            throw new IllegalArgumentException("Reference must be at most " + MAX_REFERENCE_LENGTH + " characters.");
        }

        String ontology = request.ontology() == null ? "" : request.ontology().trim();
        if (ontology.length() > MAX_ONTOLOGY_LENGTH) {
            throw new IllegalArgumentException("Ontology must be at most " + MAX_ONTOLOGY_LENGTH + " characters.");
        }

        String link = request.link() == null ? "" : request.link().trim();
        if (link.length() > MAX_LINK_LENGTH) {
            throw new IllegalArgumentException("Link must be at most " + MAX_LINK_LENGTH + " characters.");
        }

        return new Publication(
                id,
                category,
                request.year(),
                ontology.isEmpty() ? null : PublicationSanitizer.sanitizeText(ontology),
                PublicationSanitizer.sanitizeReference(reference),
                link.isEmpty() ? null : PublicationSanitizer.sanitizeLink(link));
    }

    private PublicationCategory parseCategory(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category is required.");
        }
        try {
            return PublicationCategory.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Category must be SEON_ONTOLOGY or GENERAL.");
        }
    }

    /** Writes to a sibling temp file and moves it into place, so a crash can't truncate the list. */
    private void persist() throws IOException {
        Path path = resolvePath();
        Path parent = path.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        Path temp = Files.createTempFile(parent, "publications", ".json.tmp");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), publications);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    private void seed(Path path) throws IOException {
        try (InputStream seed = PublicationService.class.getResourceAsStream(SEED_RESOURCE)) {
            if (seed == null) {
                throw new IOException("Seed resource " + SEED_RESOURCE + " not found on the classpath");
            }
            Files.createDirectories(path.toAbsolutePath().getParent());
            Files.copy(seed, path, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Seeded publications file at {}", path);
        }
    }

    private Path resolvePath() {
        return Paths.get(System.getProperty("user.dir")).resolve(publicationsFileName);
    }
}

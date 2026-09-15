package nemo.seon.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.web.multipart.MultipartFile;

import nemo.seon.model.dto.AstaVersionView;

/**
 * The .asta history is a file-based store, so these tests drive the real filesystem in a temp
 * directory and only mock the two collaborators that would open Astah.
 */
public class AstaVersionServiceTest {

    @TempDir
    Path workDir;

    private Path activeFile;
    private DiagramsService diagramsService;
    private OntologyService ontologyService;

    @BeforeEach
    void setUp() {
        activeFile = workDir.resolve("astah_seon.asta");
        diagramsService = mock(DiagramsService.class);
        ontologyService = mock(OntologyService.class);
        when(diagramsService.getAstahFilePath()).thenReturn(activeFile.toString());
        when(ontologyService.getOntologyCount()).thenReturn(3);
        when(ontologyService.getConceptCount()).thenReturn(42);
    }

    @Test
    void seedsTheHistoryFromTheFileAlreadyOnTheServer() throws IOException {
        writeActiveFile("first model");

        AstaVersionService service = newService();

        List<AstaVersionView> versions = service.findAll();
        assertEquals(1, versions.size());
        assertTrue(versions.get(0).active());
        assertEquals(3, versions.get(0).ontologyCount());
        assertEquals(42, versions.get(0).conceptCount());
        assertArrayEquals("first model".getBytes(StandardCharsets.UTF_8),
                Files.readAllBytes(service.fileOf(versions.get(0).id())));
    }

    @Test
    void startsEmptyWhenThereIsNoAstaFileYet() {
        assertTrue(newService().findAll().isEmpty());
    }

    @Test
    void archivesAnUploadAndMakesItActive() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();

        service.upload(upload("seon-v2.asta", "second model"), "SBES version", "admin");

        List<AstaVersionView> versions = service.findAll();
        assertEquals(2, versions.size());
        assertEquals("seon-v2.asta", versions.get(0).originalFilename());
        assertEquals("SBES version", versions.get(0).note());
        assertTrue(versions.get(0).active(), "the newest upload becomes the active version");
        assertFalse(versions.get(1).active());
        assertEquals("second model", Files.readString(activeFile));
        verify(diagramsService).exportAstahDiagrams();
    }

    /**
     * Astah names the root package after the file it opened, and the diagram URLs are built from
     * that name, so the model that ends up live has to come from the active path — not from the
     * archived copy that was parsed to validate it. Otherwise every figure 404s after an upload.
     */
    @Test
    void leavesTheLiveModelReadFromTheActivePathNotTheArchivedCopy() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();

        service.upload(upload("seon-v2.asta", "second model"), null, "admin");

        var order = inOrder(ontologyService, diagramsService);
        order.verify(ontologyService).reloadFrom(argThat(path -> path.contains("asta-versions")));
        order.verify(ontologyService).reloadOntologies();
        order.verify(diagramsService).exportAstahDiagrams();
    }

    @Test
    void activateAlsoRereadsTheModelFromTheActivePath() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        service.upload(upload("seon-v2.asta", "second model"), null, "admin");
        String firstId = service.findAll().get(1).id();
        clearInvocations(ontologyService, diagramsService);

        service.activate(firstId);

        var order = inOrder(ontologyService, diagramsService);
        order.verify(ontologyService).reloadFrom(argThat(path -> path.contains("asta-versions")));
        order.verify(ontologyService).reloadOntologies();
        order.verify(diagramsService).exportAstahDiagrams();
    }

    @Test
    void ignoresAnUploadIdenticalToTheActiveVersion() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();

        var outcome = service.upload(upload("again.asta", "first model"), null, "admin");

        assertTrue(outcome.duplicate());
        assertEquals(1, service.findAll().size(), "a re-upload of the same bytes adds no entry");
    }

    @Test
    void keepsTheRunningSiteWhenAnUploadCannotBeParsed() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        doThrow(new RuntimeException("not an Astah file")).when(ontologyService).reloadFrom(anyString());

        assertThrows(IllegalArgumentException.class,
                () -> service.upload(upload("broken.asta", "garbage"), null, "admin"));

        assertEquals(1, service.findAll().size(), "a rejected upload is not archived");
        assertEquals("first model", Files.readString(activeFile), "the active file is untouched");
        verify(ontologyService).reloadOntologies();
    }

    @Test
    void rejectsAnUploadThatParsesToNoOntologies() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        when(ontologyService.getOntologyCount()).thenReturn(0);

        assertThrows(IllegalArgumentException.class,
                () -> service.upload(upload("empty.asta", "empty model"), null, "admin"));

        assertEquals(1, service.findAll().size());
        assertEquals("first model", Files.readString(activeFile));
    }

    @Test
    void activatePutsAnOlderFileBackOnTheSite() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        service.upload(upload("seon-v2.asta", "second model"), null, "admin");
        String firstId = service.findAll().get(1).id();

        service.activate(firstId);

        assertEquals("first model", Files.readString(activeFile));
        assertTrue(service.find(firstId).id().equals(firstId));
        assertTrue(service.findAll().stream().filter(AstaVersionView::active)
                .allMatch(v -> v.id().equals(firstId)));
    }

    @Test
    void refusesToDeleteTheActiveVersion() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        String activeId = service.findAll().get(0).id();

        assertThrows(IllegalArgumentException.class, () -> service.delete(activeId));
        assertEquals(1, service.findAll().size());
    }

    @Test
    void deleteRemovesTheEntryAndItsArchivedFile() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        String oldId = service.findAll().get(0).id();
        Path archived = service.fileOf(oldId);
        service.upload(upload("seon-v2.asta", "second model"), null, "admin");

        service.delete(oldId);

        assertEquals(1, service.findAll().size());
        assertFalse(Files.exists(archived));
        assertThrows(NoSuchElementException.class, () -> service.find(oldId));
    }

    @Test
    void updatesTheNoteOfAVersion() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        String id = service.findAll().get(0).id();

        assertEquals("Reviewed", service.updateNote(id, "  Reviewed  ").note());
        assertEquals(null, service.updateNote(id, "   ").note());
    }

    @Test
    void readsTheHistoryBackAfterARestart() throws IOException {
        writeActiveFile("first model");
        AstaVersionService service = newService();
        service.upload(upload("seon-v2.asta", "second model"), "SBES version", "admin");
        String activeId = service.findAll().get(0).id();

        List<AstaVersionView> reloaded = newService().findAll();

        assertEquals(2, reloaded.size());
        assertEquals(activeId, reloaded.get(0).id());
        assertTrue(reloaded.get(0).active());
        assertEquals("SBES version", reloaded.get(0).note());
    }

    private void writeActiveFile(String content) throws IOException {
        Files.writeString(activeFile, content);
    }

    /** A service wired to the temp directory, with its {@code @Value} field set by hand. */
    private AstaVersionService newService() {
        AstaVersionService service = new AstaVersionService(diagramsService, ontologyService);
        try {
            var field = AstaVersionService.class.getDeclaredField("versionsDirName");
            field.setAccessible(true);
            field.set(service, workDir.resolve("asta-versions").toString());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        service.initialize();
        return service;
    }

    private MultipartFile upload(String filename, String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return new MultipartFile() {
            @Override public String getName() { return "file"; }
            @Override public String getOriginalFilename() { return filename; }
            @Override public String getContentType() { return "application/octet-stream"; }
            @Override public boolean isEmpty() { return bytes.length == 0; }
            @Override public long getSize() { return bytes.length; }
            @Override public byte[] getBytes() { return bytes; }
            @Override public InputStream getInputStream() { return new ByteArrayInputStream(bytes); }
            @Override public void transferTo(java.io.File dest) throws IOException {
                try (OutputStream out = Files.newOutputStream(dest.toPath())) {
                    out.write(bytes);
                }
            }
        };
    }
}

package nemo.seon.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.PathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nemo.seon.model.AstaVersion;
import nemo.seon.model.dto.AstaVersionView;
import nemo.seon.service.AstaVersionService;

/**
 * Admin-only history of uploaded {@code .asta} files. Every route here requires ROLE_ADMIN
 * (enforced in {@code SecurityConfig}); nothing about the version history is public.
 */
@RestController
@RequestMapping(value = "/api/asta/versions", produces = MediaType.APPLICATION_JSON_VALUE)
public class AstaVersionController {

    private static final Logger logger = LoggerFactory.getLogger(AstaVersionController.class);

    private final AstaVersionService versionService;

    public AstaVersionController(AstaVersionService versionService) {
        this.versionService = versionService;
    }

    /** Body of {@code PATCH /api/asta/versions/{id}}. */
    public record NoteRequest(String note) {}

    @GetMapping
    public List<AstaVersionView> listVersions() {
        return versionService.findAll();
    }

    @PostMapping("/{id}/activate")
    public AstaVersionView activate(@PathVariable String id) {
        return versionService.activate(id);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public AstaVersionView updateNote(@PathVariable String id, @RequestBody NoteRequest request) {
        return versionService.updateNote(id, request == null ? null : request.note());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        versionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> download(@PathVariable String id) throws IOException {
        AstaVersion version = versionService.find(id);
        Path file = versionService.fileOf(id);
        String filename = version.originalFilename() == null ? "astah_seon.asta" : version.originalFilename();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(Files.size(file))
                .body(new PathResource(file));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> onUnknownVersion(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "This version no longer exists. Reload the page."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> onInvalidRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> onStorageFailure(IllegalStateException e) {
        logger.error("Version storage failure: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Could not update the version history. Please try again."));
    }
}

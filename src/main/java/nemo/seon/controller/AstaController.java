package nemo.seon.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import nemo.seon.model.AstaVersion;
import nemo.seon.model.dto.UploadResponse;
import nemo.seon.service.AstaVersionService;
import nemo.seon.service.AstaVersionService.UploadOutcome;

@RestController
public class AstaController {

    private static final Logger logger = LoggerFactory.getLogger(AstaController.class);
    private final AstaVersionService versionService;

    public AstaController(AstaVersionService versionService) {
        this.versionService = versionService;
    }

    /**
     * Archives the uploaded file as a new version and makes it the active model.
     *
     * <p>The file is parsed before it replaces the current one, so a bad upload leaves the running
     * site untouched. See {@link AstaVersionService} for the history itself.
     */
    @PostMapping(value = "/upload-asta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadAstaFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "note", required = false) String note,
            Authentication authentication) {
        String filename = file.getOriginalFilename();
        if (file.isEmpty() || filename == null || !filename.endsWith(".asta")) {
            logger.error("Invalid file: Must be a non-empty .asta file.");
            return ResponseEntity.badRequest()
                    .body(UploadResponse.error("Invalid file: Please upload a valid .asta file."));
        }

        String username = authentication != null ? authentication.getName() : "unknown";
        try {
            UploadOutcome outcome = versionService.upload(file, note, username);
            AstaVersion version = outcome.version();
            if (outcome.duplicate()) {
                return ResponseEntity.ok(UploadResponse.ok(
                        "This file is identical to the current version, so nothing was changed. "
                                + "No new entry was added to the history."));
            }
            return ResponseEntity.ok(UploadResponse.ok(String.format(
                    "File processed successfully: %d ontologies and %d concepts. "
                            + "Diagrams regenerated, ontology data reloaded, and the upload archived as a new version.",
                    version.ontologyCount(), version.conceptCount())));

        } catch (IllegalArgumentException e) {
            logger.warn("Rejected .asta upload: {}", e.getMessage());
            return ResponseEntity.badRequest().body(UploadResponse.error(e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(UploadResponse.error("Error processing file: " + e.getMessage()));
        }
    }
}

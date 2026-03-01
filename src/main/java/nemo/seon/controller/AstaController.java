package nemo.seon.controller;

import nemo.seon.service.DiagramsService;
import nemo.seon.service.OntologyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@RestController
public class AstaController {

    private static final Logger logger = LoggerFactory.getLogger(AstaController.class);
    private final DiagramsService diagramsService;
    private final OntologyService ontologyService;

    @Autowired
    public AstaController(DiagramsService diagramsService, OntologyService ontologyService) {
        this.diagramsService = diagramsService;
        this.ontologyService = ontologyService;
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload-asta", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UploadResponse> uploadAstaFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            String filename = file.getOriginalFilename();
            if (file.isEmpty() || filename == null || !filename.endsWith(".asta")) {
                logger.error("Invalid file: Must be a non-empty .asta file.");
                return ResponseEntity.badRequest()
                        .body(UploadResponse.error("Invalid file: Please upload a valid .asta file."));
            }

            // Save file to the expected location
            Path filePath = Paths.get(diagramsService.getAstahFilePath());
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved uploaded file to: {}", filePath);

            // Export diagrams using DiagramsService
            diagramsService.exportAstahDiagrams();
            logger.info("Exported Astah diagrams successfully.");

            // Reload ontologies data for dynamic pages
            ontologyService.reloadOntologies();
            logger.info("Reloaded ontologies data successfully.");

            return ResponseEntity.ok(
                    UploadResponse.ok("File processed successfully. Diagrams regenerated and ontology data reloaded."));

        } catch (IOException e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(UploadResponse.error("Error processing file: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(UploadResponse.error("Unexpected error: " + e.getMessage()));
        }
    }
}

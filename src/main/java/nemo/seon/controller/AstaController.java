package nemo.seon.controller;

import nemo.seon.model.Package;
import nemo.seon.parser.ModelReader;
import nemo.seon.parser.Parser;
import nemo.seon.writer.PageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/upload-asta", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> uploadAstaFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".asta")) {
                logger.error("Invalid file: Must be a non-empty .asta file.");
                return ResponseEntity.badRequest()
                        .body("Invalid file: Please upload a valid .asta file.".getBytes());
            }

            // Save file to the expected location
            Path filePath = Paths.get(Parser.astahFilePath);
            Files.createDirectories(filePath.getParent());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Saved uploaded file to: {}", filePath);

            // Export images using astah-command.sh script
            Parser.exportAstahImages();
            logger.info("Exported Astah images successfully.");

            // Process file using the parser
            ModelReader modelReader = new ModelReader();
            Package seonNetwork = modelReader.parseAstah2Seon(Parser.astahFilePath);
            logger.info("Parsed .asta file successfully.");

            // Generate HTML pages
            PageWriter pageWriter = new PageWriter();
            pageWriter.generateSeonPages(seonNetwork);
            logger.info("Generated SEON pages successfully.");

            return ResponseEntity.ok("File processed successfully. Images and pages generated.".getBytes());

        } catch (IOException e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(("Error processing file: " + e.getMessage()).getBytes());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }
}

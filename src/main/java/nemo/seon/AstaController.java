package nemo.seon;

import nemo.seon.model.Package;
import nemo.seon.parser.ModelReader;
import nemo.seon.writer.PageWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static nemo.seon.parser.Parser.astahFilePath;
import static org.apache.commons.io.FileUtils.deleteDirectory;

@RestController
public class AstaController {

    private static final Logger logger = LoggerFactory.getLogger(AstaController.class);

    @PostMapping(value = "/upload-asta", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> uploadAstaFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty() || !file.getOriginalFilename().endsWith(".asta")) {
                logger.error("Invalid file: Must be a non-empty .asta file.");
                return ResponseEntity.badRequest()
                        .body("Invalid file: Please upload a valid .asta file.".getBytes());
            }
            // Create temporary directory and save file
            Path tempDir = Files.createTempDirectory("seon-");
            Path tempFile = tempDir.resolve("astah_seon.asta");
            file.transferTo(tempFile.toFile());
            logger.info("Saved uploaded file to: {}", tempFile);

            // Process file using the parser
            ModelReader modelReader = new ModelReader();
            Package seonNetwork = modelReader.parseAstah2Seon(tempFile.toString());
            logger.info("Parsed .asta file successfully.");

            // Generate HTML pages
            Path outputDir = tempDir.resolve("page");
            Files.createDirectories(outputDir);
            System.setProperty("user.dir", tempDir.toString()); // Set working directory for PageWriter
            PageWriter pageWriter = new PageWriter();
            pageWriter.generateSeonPages(seonNetwork);
            logger.info("Generated HTML pages in: {}", outputDir);

            // Create a ZIP file containing the generated HTML pages
            byte[] zipBytes = createZipFromDirectory(outputDir.toFile());
            logger.info("Created ZIP file with {} bytes.", zipBytes.length);

            // Clean up temporary directory
            deleteDirectory(tempDir.toFile());
            logger.info("Cleaned up temporary directory: {}", tempDir);

            // Return the ZIP file
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=seon_pages.zip")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (IOException e) {
            logger.error("Error processing file: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(("Error processing file: " + e.getMessage()).getBytes());
        } catch (RuntimeException e) {
            logger.error("Error parsing .asta file: {}", e.getMessage(), e);
            String message = e.getCause() instanceof com.change_vision.jude.api.inf.exception.LicenseNotFoundException
                    ? "Astah license not found. Please ensure a valid license is configured."
                    : "Invalid or corrupted .asta file: " + e.getMessage();
            return ResponseEntity.status(500)
                    .body(message.getBytes());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(("Unexpected error: " + e.getMessage()).getBytes());
        }
    }

    private byte[] createZipFromDirectory(File directory) throws IOException {
        logger.info("Zipping directory: {}", directory);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        ZipEntry entry = new ZipEntry(file.getName());
                        zos.putNextEntry(entry);
                        zos.write(Files.readAllBytes(file.toPath()));
                        zos.closeEntry();
                        logger.debug("Added file to ZIP: {}", file.getName());
                    }
                }
            } else {
                logger.warn("No files found in directory: {}", directory);
            }
        }
        return baos.toByteArray();
    }

    private void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        if (directory.delete()) {
            logger.debug("Deleted: {}", directory);
        } else {
            logger.warn("Failed to delete: {}", directory);
        }
    }
}

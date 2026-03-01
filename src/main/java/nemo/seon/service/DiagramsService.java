package nemo.seon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class DiagramsService {

    private static final Logger logger = LoggerFactory.getLogger(DiagramsService.class);
    private final Path basePath = Paths.get(System.getProperty("user.dir"));

    @Value("${seon.astah.filepath}")
    private String astahFileName;

    @Value("${seon.astah.script}")
    private String astahScriptPath;

    @Value("${seon.images.output}")
    private String imagesOutputDir;

    public String getAstahFilePath() {
        return basePath.resolve(astahFileName).toString();
    }

    /**
     * Exports the astah images to the static/images directory for web access.
     */
    public void exportAstahDiagrams() {
        String scriptPath = basePath.resolve(astahScriptPath).toString();
        String outputDir = basePath.resolve(imagesOutputDir).toString();
        String astahFilePath = getAstahFilePath();

        // Verifies if the astah-command.sh script exists
        if (!new File(scriptPath).exists()) {
            logger.error("The file '{}' was not found.", scriptPath);
            return;
        }

        // Verifies if the astah file exists
        if (!new File(astahFilePath).exists()) {
            logger.error("Astah file not found: {}", astahFilePath);
            return;
        }

        try {
            logger.info("Starting Astah diagram export...");
            int exitCode = executeAstahCommand(scriptPath, astahFilePath, outputDir);
            if (exitCode == 0) {
                logger.info("Astah images exported successfully to: {}", outputDir);
            } else {
                logger.error("Astah command failed with exit code: {}", exitCode);
            }
        } catch (Exception e) {
            logger.error("Error while executing Astah command: {}", e.getMessage(), e);
        }
    }

    /**
     * Executes the astah-command.sh script to export diagrams.
     * @param scriptPath Path to the astah-command.sh script
     * @param astahFilePath Path to the .asta file
     * @param outputDir Output directory for the images
     * @return The exit code of the process
     */
    private int executeAstahCommand(String scriptPath, String astahFilePath, String outputDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            scriptPath,
                "-image", "cl",
                "-f", astahFilePath,
                "-o", outputDir
        );

        // Set working directory
        processBuilder.directory(basePath.toFile());

        logger.debug("Executing: {}", String.join(" ", processBuilder.command()));
        
        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to finish
        return process.waitFor();
    }
}

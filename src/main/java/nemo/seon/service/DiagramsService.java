package nemo.seon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
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

    public void exportAstahDiagrams() {
        String scriptPath = basePath.resolve(astahScriptPath).toString();
        String outputDir = basePath.resolve(imagesOutputDir).toString();
        String astahFilePath = getAstahFilePath();

        if (!new File(scriptPath).exists()) {
            logger.error("The file '{}' was not found.", scriptPath);
            return;
        }

        if (!new File(astahFilePath).exists()) {
            logger.error("Astah file not found: {}", astahFilePath);
            return;
        }

        try {
            clearPngFiles(outputDir);
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

    private void clearPngFiles(String outputDir) {
        File dir = new File(outputDir);
        if (!dir.exists()) return;
        File[] pngs = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".png"));
        if (pngs == null) return;
        for (File png : pngs) {
            try {
                Files.delete(png.toPath());
            } catch (IOException e) {
                logger.warn("Could not delete old diagram: {}", png.getName());
            }
        }
        logger.info("Cleared {} old diagram(s) from: {}", pngs.length, outputDir);
    }

    private int executeAstahCommand(String scriptPath, String astahFilePath, String outputDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            scriptPath,
                "-image", "cl",
                "-f", astahFilePath,
                "-o", outputDir
        );
        processBuilder.directory(basePath.toFile());
        processBuilder.redirectErrorStream(true);

        logger.debug("Executing: {}", String.join(" ", processBuilder.command()));
        Process process = processBuilder.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.debug("[astah-command] {}", line);
            }
        }

        return process.waitFor();
    }
}

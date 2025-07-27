package nemo.seon.service;

import nemo.seon.parser.Parser;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class DiagramsService {

    public final String astahFilePath = System.getProperty("user.dir") + "/" + "astah_seon.asta";
    /**
     * Exports the astah images to the static/images directory for web access.
     */
    public void exportAstahDiagrams() {
        String scriptPath = System.getProperty("user.dir") + "/jars/astah-command.sh";
        String outputDir = System.getProperty("user.dir") + "/src/main/resources/static/images";

        // Verifies if the astah-command.sh script exists
        if (!new File(scriptPath).exists()) {
            System.err.println("Error: The file '" + scriptPath + "' was not found.");
            return;
        }

        // Verifies if the astah file exists
        if (!new File(astahFilePath).exists()) {
            System.err.println("Error: Astah file not found: " + astahFilePath);
            return;
        }

        try {
            System.out.println("Starting Astah diagram export...");
            int exitCode = executeAstahCommand(scriptPath, outputDir);
            if (exitCode == 0) {
                System.out.println("Astah images exported successfully to: " + outputDir);
            } else {
                System.err.println("Astah command failed with exit code: " + exitCode);
            }
        } catch (Exception e) {
            System.err.println("Error while executing Astah command: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Executes the astah-command.sh script to export diagrams.
     * @param scriptPath Path to the astah-command.sh script
     * @param outputDir Output directory for the images
     * @return The exit code of the process
     */
    private int executeAstahCommand(String scriptPath, String outputDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                scriptPath,                // Path to the script
                "-image",                  // Export images
                "-f", astahFilePath, // Astah file
                "-o", outputDir,           // Output directory
                "-t", "png"                // Image type (PNG)
        );

        // Set working directory
        processBuilder.directory(new File(Parser.PATH));

        System.out.println("Executing: " + String.join(" ", processBuilder.command()));
        
        // Start the process
        Process process = processBuilder.start();

        // Wait for the process to finish
        return process.waitFor();
    }
}

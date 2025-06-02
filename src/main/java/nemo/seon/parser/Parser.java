package nemo.seon.parser;

import nemo.seon.model.Package;
import nemo.seon.writer.PageWriter;

import java.io.File;
import java.io.IOException;

public class Parser {
    public static final String PATH = System.getProperty("user.dir");  // Current working directory
    public static final String astahFilePath = PATH + "/" + "astah_seon.asta"; // Astah file name should always be "astah_seon.asta"
    public static void main(String[] args) {
        exportAstahImages();

       ModelReader modelReader = new ModelReader();
       Package seonNetwork = modelReader.parseAstah2Seon(astahFilePath);

       PageWriter pageWriter = new PageWriter();
       pageWriter.generateSeonPages(seonNetwork);

    }

    public static void exportAstahImages() {
        String outputDir = Parser.PATH + "/page/images";  // Output directory for the images
        File outputDirFile = new File(outputDir);
        if (!outputDirFile.exists()) {
            outputDirFile.mkdirs();
        }

        try {
            int exitCode = runAstahCommandInDocker(outputDir);
            if (exitCode == 0) {
                System.out.println("The astah images exporting was executed successfully.");
            } else {
                System.err.println("The Docker command has failed. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            // Prints the error message
            System.err.println("Error while executing Docker command: " + e.getMessage());
        }
    }

    /**
     * Executes the Astah command inside a Docker container.
     * @param outputDir Output directory for the images
     * @return The exit code of the process
     */
    private static int runAstahCommandInDocker(String outputDir) throws IOException, InterruptedException {
        // Build the docker run command
        ProcessBuilder processBuilder = new ProcessBuilder(
                "docker", "run", "--rm",
                "-v", astahFilePath + ":/app/data/astah_seon.asta:ro",  // Mount the .asta file (read-only)
                "-v", outputDir + ":/app/output",  // Mount the output directory
                "seon-astah:latest",  // Docker image name
                "-image", "cl",  // Argument to export only class diagrams
                "-f", "/app/data/astah_seon.asta",  // Path to the .asta file inside the container
                "-o", "/app/output"  // Output directory inside the container
        );

        // Start the process
        Process process = processBuilder.start();

        // Optionally redirect output streams for debugging
        new Thread(() -> {
            try (java.util.Scanner s = new java.util.Scanner(process.getInputStream())) {
                while (s.hasNextLine()) {
                    System.out.println("Docker: " + s.nextLine());
                }
            }
        }).start();

        new Thread(() -> {
            try (java.util.Scanner s = new java.util.Scanner(process.getErrorStream())) {
                while (s.hasNextLine()) {
                    System.err.println("Docker error: " + s.nextLine());
                }
            }
        }).start();

        // Wait for the process to finish
        return process.waitFor();
    }
}
package nemo.seon.parser;

import nemo.seon.model.Package;

import java.io.File;
import java.io.IOException;

public class Parser {
    public static final String PATH = System.getProperty("user.dir");  // Current working directory
    public static final String astahFilePath = PATH + "/" + "astah_seon.asta"; // Astah file name should always be "astah_seon.asta"
    public static void main(String[] args) {
        exportAstahImages();

        ModelReader modelReader = new ModelReader();
        Package seonNetwork = modelReader.parseAstah2Seon(astahFilePath);

    }

    /**
     * Exports the astah images to the images directory.
     * astah-command.sh script usage:
     *     -f,--file <target file>    target file
     *     -image                     export documents to image
     *     -o,--output <output dir>   output dir
     *     -t,--type <image type>     png/jpg/emf (emf is supported by uml or professional only.)
     *     -resized                   resized export image's font(Main purpose:Exporting at other OS)
     *     -diff <base astah project file> <reference astah project file>   jude/asta
     */
    private static void exportAstahImages() {
        String scriptPath = Parser.PATH + "/jars/astah-command.sh";  // astah-command.sh script is the command line tool for Astah
        String outputDir = Parser.PATH + "/images";  // Output directory for the images

        // Verifies if the astah-command.sh script exists
        if (!new File(scriptPath).exists()) {
            System.err.println("Error: The file '" + scriptPath + "' was not found.");
            return;
        }

        try {
            int exitCode = getExitCode(scriptPath, outputDir);
            if (exitCode == 0) {
                System.out.println("The astah images exporting was executed successfully.");
            } else {
                System.err.println("The command has failed. " + exitCode);
            }
        } catch (Exception e) {
            // Prints the error message
            System.err.println("Error while executing command: " + e.getMessage());
        }
    }

    /**
     * Executes the command to export the Astah images.
     * @param scriptPath Path to the script
     * @param outputDir Output directory
     * @return The exit code of the process
     */
    private static int getExitCode(String scriptPath, String outputDir) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                scriptPath, // Path to the script
                "-image", "cl", // Argument to export only class diagrams
                "-f", astahFilePath, // Astah file
                "-o", outputDir  // Output directory
        );

        // Defines the working directory
        processBuilder.directory(new File(Parser.PATH));

        // Starts the process
        Process process = processBuilder.start();

        // Waits for the process to finish
        return process.waitFor();
    }
}

package nemo.seon.writer;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class Utils {

    /**
     * Reads a file and returns its content as a string.
     * @param filename the file name
     * @return the file content as a string
     */
    public static String fileToString(String filename) {
        String text = null;
        try {
            text = FileUtils.readFileToString(new File(filename), "UTF-8");
            //text = FileUtils.readFileToString(new File(Utils.class.getResource(filename).toURI()), "UTF-8");
        } catch (IOException e) {
            System.out.println("Error while reading file: " + filename + "at OntologiesWriter.fileToString()");
            e.printStackTrace();
        }
        return text;
    }

    /**
     * Writes a string to a file.
     * @param filename the file name
     * @param text the text to write
     */
    public static void stringToFile(String filename, String text) {
        try {
            FileUtils.writeStringToFile(new File(filename), text, "UTF-8");
        } catch (IOException e) {
            System.out.println("Error while writing file: " + filename + " at OntologiesWriter.stringToFile()");
            e.printStackTrace();
        }
    }
}

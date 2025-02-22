import nemo.seon.parser.Parser;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ParserTest {
    @Test
    void testExportAstahImages_FileNotFound() {
        // Mock the script path to a non-existing file
        String invalidScriptPath = Parser.PATH + "/invalid_script.sh";
        assertFalse(new File(invalidScriptPath).exists());
    }

    @Test
    void testExportAstahImages_FileFound() {
        // Mock the script path to an existing file
        String validScriptPath = Parser.PATH + "/jars/astah-command.sh";
        assertTrue(new File(validScriptPath).exists());
    }
}

import nemo.seon.model.SeonRegistry;
import nemo.seon.parser.ModelReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ModelReaderTest {
    private ModelReader modelReader;

    @BeforeEach
    void setup() {
        modelReader = new ModelReader(new SeonRegistry());
    }

    @Test
    void testParseAstah2Seon() {

    }

    @Test
    void testParsePackages() {

    }
}

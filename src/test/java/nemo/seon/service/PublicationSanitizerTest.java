package nemo.seon.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PublicationSanitizerTest {

    @Test
    void keepsInlineFormattingTags() {
        assertEquals("A <em>title</em> here",
                PublicationSanitizer.sanitizeReference("A <em>title</em> here"));
    }

    @Test
    void escapesEverythingOutsideTheAllowlist() {
        assertEquals("&lt;script&gt;alert(1)&lt;/script&gt;",
                PublicationSanitizer.sanitizeReference("<script>alert(1)</script>"));
        assertEquals("&lt;em onclick=&quot;x&quot;&gt;",
                PublicationSanitizer.sanitizeReference("<em onclick=\"x\">"));
    }

    @Test
    void escapesAmpersandsInReferences() {
        assertEquals("Guizzardi, G. &amp; Wagner, G.",
                PublicationSanitizer.sanitizeReference("Guizzardi, G. & Wagner, G."));
    }

    @Test
    void plainTextFieldsKeepNoMarkup() {
        assertEquals("&lt;em&gt;UFO&lt;/em&gt;", PublicationSanitizer.sanitizeText("<em>UFO</em>"));
    }

    @Test
    void acceptsHttpLinksOnly() {
        assertEquals("https://nemo.inf.ufes.br/a.pdf",
                PublicationSanitizer.sanitizeLink("  https://nemo.inf.ufes.br/a.pdf  "));
        assertThrows(IllegalArgumentException.class,
                () -> PublicationSanitizer.sanitizeLink("javascript:alert(1)"));
        assertThrows(IllegalArgumentException.class,
                () -> PublicationSanitizer.sanitizeLink("nemo.inf.ufes.br/a.pdf"));
    }
}

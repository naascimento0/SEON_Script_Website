package nemo.seon.service;

import java.net.URI;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Cleans admin-submitted publication fields.
 *
 * <p>The frontend renders the {@code reference} field as raw HTML (it carries {@code <em>}
 * markup for titles and journals), so submitted text is escaped first and only the inline
 * formatting tags on the allowlist are restored afterwards.
 */
public final class PublicationSanitizer {

    private static final Pattern ALLOWED_TAGS =
            Pattern.compile("&lt;(/?)(em|i|b|strong|sup|sub)&gt;", Pattern.CASE_INSENSITIVE);

    private PublicationSanitizer() {
    }

    /** Escapes every markup character, then restores the inline tags on the allowlist. */
    public static String sanitizeReference(String value) {
        String escaped = escape(value);
        return ALLOWED_TAGS.matcher(escaped)
                .replaceAll(match -> "<" + match.group(1) + match.group(2).toLowerCase(Locale.ROOT) + ">");
    }

    /** Escapes a field that must never carry markup, such as the ontology name. */
    public static String sanitizeText(String value) {
        return escape(value);
    }

    /**
     * Accepts only absolute http(s) URLs, so a link can never smuggle in a {@code javascript:} URL.
     *
     * @throws IllegalArgumentException if the value is not a valid http(s) URL
     */
    public static String sanitizeLink(String value) {
        URI uri;
        try {
            uri = new URI(value.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Link must be a valid URL.");
        }
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
        if (!scheme.equals("http") && !scheme.equals("https")) {
            throw new IllegalArgumentException("Link must start with http:// or https://.");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("Link must include a host name.");
        }
        return uri.toString();
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}

package nemo.seon.model.dto;

/**
 * DTO for rendering a dependency row in the ontology page.
 */
public record DependencyView(
        String shortName,
        String fullName,
        String url,
        boolean openInNewTab,
        String description,
        String level
) {}

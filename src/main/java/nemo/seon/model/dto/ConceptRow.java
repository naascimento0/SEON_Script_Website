package nemo.seon.model.dto;

import java.util.List;

/**
 * DTO for rendering a concept row in the Concepts Definition table.
 */
public record ConceptRow(
        String name,
        /** CSS class for styling: "fst-italic", "fst-italic fw-bold", or "fw-bold" */
        String styleClass,
        /** Anchor ID for this concept (e.g., "SPO_Artifact+Participation") */
        String label,
        /** Link to the detailed section */
        String detailLabel,
        String definition,
        /** Example text, or null if none */
        String example,
        /** Source definition text, or null if none */
        String source
) {}

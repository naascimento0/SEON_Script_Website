package nemo.seon.model.dto;

import java.util.List;

/**
 * DTO for rendering a detailed concept card in the Detailed Concepts section.
 */
public record ConceptDetail(
        String fullName,
        String name,
        /** Stereotype text (e.g., "kind", "relator"), or null if none */
        String stereotype,
        /** Anchor ID for the detail (e.g., "SPO_Artifact+Participation_detail") */
        String detailLabel,
        String definition,
        /** Example text, or null if none */
        String example,
        /** Source definition text, or null if none */
        String source,
        /** List of generalization full names (unique, ordered) */
        List<String> generalizations,
        /** List of relation strings (unique, ordered) */
        List<String> relations
) {}

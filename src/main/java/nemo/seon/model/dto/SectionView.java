package nemo.seon.model.dto;

import java.util.List;

/**
 * DTO for a subpackage section within Ontology Models (e.g., "3.1. Standard Process Structure").
 */
public record SectionView(
        /** Section number prefix, e.g. "3.1." */
        String sectionNumber,
        /** HTML heading level (3-6) */
        int headingLevel,
        /** The section anchor id */
        String sectionRef,
        /** The section title */
        String sectionName,
        /** Description HTML (may contain line breaks) */
        String description,
        /** Diagrams within this section */
        List<DiagramView> diagrams,
        /** Nested subsections */
        List<SectionView> subsections
) {}

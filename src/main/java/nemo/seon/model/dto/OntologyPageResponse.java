package nemo.seon.model.dto;

import java.util.List;

/**
 * Aggregated payload for a single ontology page (GET /api/ontologies/{name}).
 * Mirrors what PageController.ontologyPage() used to push into the Thymeleaf model.
 */
public record OntologyPageResponse(
        String title,
        String shortName,
        String fullName,
        String ontoLevelIcon,
        String ontoLevelText,
        String description,
        List<DependencyView> dependencies,
        List<DiagramView> diagrams,
        List<SectionView> sections,
        List<ConceptRow> conceptRows,
        List<ConceptDetail> conceptDetails
) {}

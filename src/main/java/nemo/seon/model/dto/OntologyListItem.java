package nemo.seon.model.dto;

/**
 * Lightweight item for the ontology list endpoint (GET /api/ontologies).
 */
public record OntologyListItem(
        String name,
        String shortName,
        String fullName,
        String level,
        String network
) {}

package nemo.seon.model.dto;

/** Payload accepted by {@code POST /api/publications}. */
public record PublicationRequest(
        String category,
        Integer year,
        String ontology,
        String reference,
        String link
) {}

package nemo.seon.model;

/**
 * A research paper listed on the Publications page.
 *
 * <p>Publications are not parsed from the Astah model: they live in a JSON file on disk
 * (see {@code nemo.seon.service.PublicationService}) so that an admin can add new ones at
 * runtime without a rebuild.
 */
public record Publication(
        String id,
        PublicationCategory category,
        int year,
        String ontology,
        String reference,
        String link
) {}

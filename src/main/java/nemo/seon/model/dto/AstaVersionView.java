package nemo.seon.model.dto;

/** DTO for the admin version history: an {@code AstaVersion} plus which one is currently live. */
public record AstaVersionView(
        String id,
        String originalFilename,
        String uploadedAt,
        String uploadedBy,
        long sizeBytes,
        String sha256,
        String note,
        int ontologyCount,
        int conceptCount,
        boolean active
) {}

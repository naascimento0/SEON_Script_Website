package nemo.seon.model.dto;

/**
 * DTO for an image map area (clickable region over a diagram image).
 */
public record MapArea(
        String coords,
        String href,
        String target,
        String alt
) {}

package nemo.seon.model.dto;

import java.util.List;

/**
 * DTO for rendering a diagram in the ontology page.
 */
public record DiagramView(
        int figureNumber,
        String introText,
        String labelText,
        String description,
        /** Image source path (e.g., "/images/astah_seon/UFO/UFO.png") */
        String imageSrc,
        /** Image width from the Astah diagram bounds */
        int imageWidth,
        /** Name used for the usemap attribute and map name */
        String mapName,
        /** The clickable areas on the image map */
        List<MapArea> mapAreas,
        /** True if this is a simple "other" type image (no image map) */
        boolean simpleImage
) {}

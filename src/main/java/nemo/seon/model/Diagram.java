package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IDiagram;

/** Represents a diagram inside a Package, parsed from the Astah file. */
public class Diagram {

    private Package pack;
    private final String name;
    private final String definition;
    private final DiagType type;
    private final IDiagram object;

    public Diagram(String name, String definition, DiagType type, String network, IDiagram object) {
        this.name = name;
        this.definition = definition;
        this.type = type;
        this.object = object;
    }

    public enum DiagType {
        PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE
    }

    /**
     * Maps an Astah Tagged Value string to a DiagType.
     * Returns CONCEPTUALMODEL as default for null or unrecognized values.
     */
    public static DiagType getDiagramType(String type) {
        if (type == null) return DiagType.CONCEPTUALMODEL;
        return switch (type) {
            case "CM" -> DiagType.CONCEPTUALMODEL;
            case "Package" -> DiagType.PACKAGE;
            case "Other" -> DiagType.OTHER;
            case "Ignore" -> DiagType.IGNORE;
            default -> DiagType.CONCEPTUALMODEL;
        };
    }

    public String getName() { return this.name; }
    public String getDefinition() { return this.definition; }
    public DiagType getType() { return this.type; }
    public IDiagram getDiagramAstahObject() { return this.object; }
    public Package getPack() { return pack; }
    public void setPack(Package pack) { this.pack = pack; }
}

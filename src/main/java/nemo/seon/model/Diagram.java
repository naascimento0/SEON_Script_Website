package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IDiagram;

public class Diagram {

    private Package	pack;
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

    public static DiagType getDiagramType(String type) {
        if (type != null) {
            switch (type) {
                case "CM":
                    return DiagType.CONCEPTUALMODEL;
                case "Package":
                    return DiagType.PACKAGE;
                case "Other":
                    return DiagType.OTHER;
                case "Ignore":
                    return DiagType.IGNORE;
            }
        }
        return DiagType.CONCEPTUALMODEL;
    }

    public String getName() {
        return this.name;
    }

    public String getDefinition() {
        return this.definition;
    }

    public DiagType getType() {
        return this.type;
    }

    /**
     * @return the Astah object
     */
    public IDiagram getDiagramAstahObject() {
        return this.object;
    }

    /**
     * @return the pack of the diagram
     */
    public Package getPack() {
        return pack;
    }

    public void setPack(Package pack) {
        this.pack = pack;
    }
}

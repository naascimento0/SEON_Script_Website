package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IDiagram;

/**
 * Represents a diagram in the SEON ontology model.
 * 
 * Each diagram corresponds to a diagram in the Astah file and is associated with a package.
 * Diagrams are used to visualize conceptual models, package structures, and other aspects
 * of the ontology defined in the Astah project.
 * 
 * Attributes:
 * - String name: the diagram name
 * - String definition: the definition or description of the diagram
 * - DiagType type: the diagram type (PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE),
 *   determined by a Tagged Value defined by the developer in Astah
 * - Package pack: the package in which this diagram is located
 * - IDiagram object: the Astah IDiagram object representing this diagram in the model file
 * 
 * @see Package#getDiagrams()
 * @see DiagType
 */
public class Diagram {

    private Package	pack;
    private final String name;
    private final String definition;
    private final DiagType type;
    private final IDiagram object;

    /**
     * Creates a diagram with the specified attributes.
     * 
     * @param name the diagram name.
     * @param definition a description or definition of the diagram.
     * @param type the DiagType (PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE).
     * @param network the network name or context (not currently used).
     * @param object the underlying Astah IDiagram object for this diagram.
     */
    public Diagram(String name, String definition, DiagType type, String network, IDiagram object) {
        this.name = name;
        this.definition = definition;
        this.type = type;
        this.object = object;
    }

    /**
     * Enumeration of diagram types in SEON.
     * 
     * PACKAGE: diagram representing a package structure
     * CONCEPTUALMODEL: diagram showing concepts and their relationships (CM)
     * OTHER: diagram that does not fit other categories
     * IGNORE: diagram that should be ignored in processing
     */
    public enum DiagType {
        PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE
    }

    /**
     * Converts a string diagram type (from Astah Tagged Value) to the corresponding DiagType enum.
     * 
     * Mapping:
     * - "CM" -> CONCEPTUALMODEL
     * - "Package" -> PACKAGE
     * - "Other" -> OTHER
     * - "Ignore" -> IGNORE
     * - null or unrecognized -> CONCEPTUALMODEL (default)
     * 
     * @param type the diagram type string from Astah (or null).
     * @return the corresponding DiagType enum value, or CONCEPTUALMODEL if not recognized.
     */
    public static DiagType getDiagramType(String type) {
        return switch (type) {
            case "CM" -> DiagType.CONCEPTUALMODEL;
            case "Package" -> DiagType.PACKAGE;
            case "Other" -> DiagType.OTHER;
            case "Ignore" -> DiagType.IGNORE;
            default -> DiagType.CONCEPTUALMODEL;
        };
    }

    /**
     * Returns the name of this diagram.
     * @return The diagram name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the definition or description of this diagram.
     * @return The definition string, or null if not defined.
     */
    public String getDefinition() {
        return this.definition;
    }

    /**
     * Returns the type of this diagram.
     * @return The DiagType enum value (PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE).
     */
    public DiagType getType() {
        return this.type;
    }

    /**
     * Returns the Astah IDiagram object that represents this diagram.
     * This is the underlying representation used for accessing diagram properties and content.
     * 
     * @return the IDiagram object from Astah.
     */
    public IDiagram getDiagramAstahObject() {
        return this.object;
    }

    /**
     * Returns the package in which this diagram is located.
     * 
     * @return the Package containing this diagram, or null if not set.
     */
    public Package getPack() {
        return pack;
    }

    /**
     * Sets the package in which this diagram is located.
     * 
     * @param pack the Package to associate with this diagram.
     */
    public void setPack(Package pack) {
        this.pack = pack;
    }
}

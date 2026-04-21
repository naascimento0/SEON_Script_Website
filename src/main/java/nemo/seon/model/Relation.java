package nemo.seon.model;

/**
 * Represents a relationship or association between two concepts in the SEON ontology.
 * 
 * A Relation captures the structural and semantic connections between two UML classes (concepts)
 * in the Astah model. Relations define how concepts interact, depend on, or relate to each other.
 * 
 * Attributes:
 * - String name: the relation name or label (e.g., "hasPart", "isComposedOf")
 * - String definition: the definition or semantic explanation of the relation
 * - String stereotype: the relation stereotype defined in Astah (e.g., "aggregation", "association")
 * - boolean composition: indicates if this is a UML Composition relation (marked with '<>' symbol)
 *   See: https://www.uml-diagrams.org/composition.html
 * - Concept source: the source concept (origin of the relation)
 * - Concept target: the target concept (destination of the relation)
 * - String sourceMult: the multiplicity on the source side (e.g., "1", "0..1", "0..*")
 * - String targetMult: the multiplicity on the target side (e.g., "1", "0..1", "0..*")
 * 
 * Bidirectional Navigation:
 * Each Relation is also added to the 'relations' list of the source Concept,
 * allowing efficient bidirectional navigation through the model.
 * 
 */
public class Relation {
    private final Concept source;
    private final Concept target;
    private final String name;
    private final boolean composition;
    private final String sourceMult;
    private final String targetMult;
    private final String stereotype;
    private final String definition;

    /**
     * Creates a relation between two concepts.
     * 
     * The relation will automatically be added to the source concept's relation list
     * for bidirectional navigation.
     * 
     * @param name the relation name or label (e.g., "hasPart", "isComposedOf").
     * @param definition the semantic explanation or definition of this relation.
     * @param stereotype the relation stereotype defined in Astah (e.g., "aggregation", "association").
     * @param composition true if this is a UML composition relation (marked with '<>' symbol),
     *                    false for simple association or aggregation.
     * @param pack the package containing this relation (currently not used, kept for context).
     * @param source the source concept of this relation.
     * @param target the target concept of this relation.
     * @param smult the multiplicity on the source side (format: "1", "0..1", "0..*", etc.).
     * @param tmult the multiplicity on the target side (format: "1", "0..1", "0..*", etc.).
     */
    public Relation(String name, String definition, String stereotype, boolean composition, Package pack, Concept source, Concept target, String smult, String tmult) {
        this.source = source;
        this.target = target;
        this.name = name;
        this.composition = composition;
        this.sourceMult = smult;
        this.targetMult = tmult;
        this.stereotype = stereotype;
        this.definition = definition;
    }

    /**
     * Returns the source concept of this relation.
     * @return The source Concept.
     */
    public Concept getSource() {
        return this.source;
    }

    /**
     * Returns the target concept of this relation.
     * @return The target Concept.
     */
    public Concept getTarget() {
        return this.target;
    }

    /**
     * Returns the name or label of this relation.
     * @return The relation name, or null if not defined.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the definition or semantic meaning of this relation.
     * @return The relation definition, or null if not defined.
     */
    public String getDefinition() {
        return this.definition;
    }

    /**
     * Returns the stereotype of this relation (e.g., "aggregation", "association").
     * @return The stereotype string, or null if not defined.
     */
    public String getStereotype() {
        return this.stereotype;
    }

    /**
     * Returns the multiplicity on the source side of this relation.
     * Format: "1", "0..1", "0..*", etc.
     * @return The source multiplicity, or empty string if not defined.
     */
    public String getSourceMultiplicity() {
        return this.sourceMult;
    }

    /**
     * Returns the multiplicity on the target side of this relation.
     * Format: "1", "0..1", "0..*", etc.
     * @return The target multiplicity, or empty string if not defined.
     */
    public String getTargetMultiplicity() {
        return this.targetMult;
    }

    /**
     * Checks if this is a composition relation (has '<>' symbol).
     * @return true if composition, false if simple association.
     */
    public boolean isComposition() {
        return this.composition;
    }

    /**
     * Returns a string representation of this relation in a human-readable UML-like format.
     * 
     * Format: "[SourceName] [multiplicity] [relationName] [multiplicity] [TargetName] [stereotype] [definition]"
     * 
     * Example: "Process (1) hasPart (0..*) Activity <<aggregation>> [defines the structure]"
     * 
     * Composition relations are prefixed with "<>--" to indicate the composition symbol.
     * 
     * @return the relation as a formatted string.
     */
    @Override
    public String toString() {
        String rname = name;
        String smult = " ";
        String tmult = " ";
        String ster = "";
        String def = "";
        if (composition) rname = "<>--" + name;
        if (!sourceMult.isEmpty()) smult = " (" + sourceMult + ") ";
        if (!targetMult.isEmpty()) tmult = " (" + targetMult + ") ";
        if (stereotype != null) ster = "  &lt&lt" + stereotype + "&gt&gt";
        if (!definition.isEmpty()) def = " [" + def + "]";
        return source.getName() + smult + rname + tmult + target.getName() + ster + def;
    }
}

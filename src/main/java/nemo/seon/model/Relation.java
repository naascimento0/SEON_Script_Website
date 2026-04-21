package nemo.seon.model;

/**
 * Represents a UML association between two Concepts parsed from the Astah file.
 * Composition relations are flagged separately and rendered with a "&lt;&gt;--" prefix.
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

    /** The {@code pack} parameter is accepted for context but not stored. */
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

    public Concept getSource() { return this.source; }
    public Concept getTarget() { return this.target; }
    public String getName() { return this.name; }
    public String getDefinition() { return this.definition; }
    public String getStereotype() { return this.stereotype; }
    public String getSourceMultiplicity() { return this.sourceMult; }
    public String getTargetMultiplicity() { return this.targetMult; }
    public boolean isComposition() { return this.composition; }

    /**
     * Renders the relation in a UML-like format:
     * "SourceName (mult) relationName (mult) TargetName &lt;&lt;stereotype&gt;&gt; [definition]"
     * Composition relations are prefixed with "&lt;&gt;--".
     */
    @Override
    public String toString() {
        String rname = composition ? "<>--" + name : name;
        String smult = sourceMult.isEmpty() ? " " : " (" + sourceMult + ") ";
        String tmult = targetMult.isEmpty() ? " " : " (" + targetMult + ") ";
        String ster = stereotype != null ? "  &lt&lt" + stereotype + "&gt&gt" : "";
        String def = definition.isEmpty() ? "" : " [" + definition + "]";
        return source.getName() + smult + rname + tmult + target.getName() + ster + def;
    }
}

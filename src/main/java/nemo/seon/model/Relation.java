package nemo.seon.model;

/**
 * Represents a UML association between two Concepts parsed from the Astah file.
 * Each end carries its own {@link AggregationKind}; whole-part relations (aggregation or
 * composition) are rendered with a "&lt;&gt;--" prefix.
 */
public class Relation {
    private final Concept source;
    private final Concept target;
    private final String name;
    private final AggregationKind sourceAggregation;
    private final AggregationKind targetAggregation;
    private final String sourceMult;
    private final String targetMult;
    private final String stereotype;
    private final String definition;

    /** The {@code pack} parameter is accepted for context but not stored. */
    public Relation(String name, String definition, String stereotype, AggregationKind sourceAggregation, AggregationKind targetAggregation, Package pack, Concept source, Concept target, String smult, String tmult) {
        this.source = source;
        this.target = target;
        this.name = name;
        this.sourceAggregation = sourceAggregation;
        this.targetAggregation = targetAggregation;
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
    public AggregationKind getSourceAggregation() { return this.sourceAggregation; }
    public AggregationKind getTargetAggregation() { return this.targetAggregation; }

    /** True when either end has a diamond, hollow (aggregation) or filled (composition). */
    public boolean isWholePart() {
        return sourceAggregation != AggregationKind.NONE || targetAggregation != AggregationKind.NONE;
    }

    /**
     * Renders the relation in a UML-like format:
     * "SourceName (mult) relationName (mult) TargetName &lt;&lt;stereotype&gt;&gt; [definition]"
     * Whole-part relations are prefixed with "&lt;&gt;--".
     */
    @Override
    public String toString() {
        String rname = isWholePart() ? "<>--" + name : name;
        String smult = sourceMult.isEmpty() ? " " : " (" + sourceMult + ") ";
        String tmult = targetMult.isEmpty() ? " " : " (" + targetMult + ") ";
        String ster = stereotype != null ? "  &lt&lt" + stereotype + "&gt&gt" : "";
        String def = definition.isEmpty() ? "" : " [" + definition + "]";
        return source.getName() + smult + rname + tmult + target.getName() + ster + def;
    }
}

package nemo.seon.model;

/**
 * The diamond drawn on one end of an association, as in UML/OntoUML.
 * {@code SHARED} is the hollow diamond (aggregation), {@code COMPOSITE} the filled one (composition).
 * It sits on the end typed by the whole.
 */
public enum AggregationKind {
    NONE, SHARED, COMPOSITE
}

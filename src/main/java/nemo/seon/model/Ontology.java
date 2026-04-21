package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/**
 * Represents a SEON ontology — a Package that contains Concepts.
 * An Ontology is classified as FOUNDATIONAL, CORE, or DOMAIN based on its position
 * in the package hierarchy. Full name and short name come from Tagged Values in Astah.
 */
public class Ontology extends Package {

    private final List<Concept> concepts;
    private final String fullName;
    private final String shortName;

    public Ontology(String name, String fullName, String shortName, String definition, PackType type, int order, IPackage pack) {
        super(name, definition, type, order, pack);
        this.concepts = new ArrayList<>();
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public enum OntoLevel {
        FOUNDATIONAL(0), CORE(1), DOMAIN(2);

        private final int value;

        OntoLevel(final int value) { this.value = value; }

        public int getValue() { return value; }
    }

    public String getFullName() { return this.fullName; }
    public String getShortName() { return this.shortName; }
    public List<Concept> getConcepts() { return this.concepts; }

    public void addConcept(Concept concept) { this.concepts.add(concept); }
}

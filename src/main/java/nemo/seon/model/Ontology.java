package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.List;

public class Ontology extends Package{

    private final List<Concept> concepts;
    private final String fullName;
    private final String shortName;

    public Ontology(String name, String fullName, String shortName, String definition, PackType type, int order, IPackage pack) {
        super(name, definition, type, order, pack);
        this.concepts = new ArrayList<>();
        this.fullName = fullName;
        this.shortName = shortName;
    }

    public static enum OntoLevel {
        FOUNDATIONAL(1), CORE(1), DOMAIN(2);

        private final int value;

        private OntoLevel(final int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public String getFullName() {
        return this.fullName;
    }

    public String getShortName() {
        return this.shortName;
    }

    public List<Concept> getConcepts() {
        return this.concepts;
    }

    public void addConcept(Concept concept) {
        this.concepts.add(concept);
    }
}

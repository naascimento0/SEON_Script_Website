package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.List;

public class Ontology extends Package{

    private final List<Concept> concepts;

    public Ontology(String name, String fullName, String shortName, String definition, PackType type, int order, IPackage pack) {
        super(name, definition, type, order, pack);
        this.concepts = new ArrayList<>();
    }

    public void addConcept(Concept concept) {
        this.concepts.add(concept);
    }
}

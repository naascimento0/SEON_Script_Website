package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IClass;

public class Concept {

    private Ontology ontology;

    public Concept(String name, String definition, String stereotype, IClass object) {
    }

    public void setOntology(Ontology onto) {
        this.ontology = onto;
    }
}

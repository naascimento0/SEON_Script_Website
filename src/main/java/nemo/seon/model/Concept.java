package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Concept {

    private Ontology ontology;
    private final IClass object;
    private static final Map<IClass, Concept> conceptMap	= new HashMap<IClass, Concept>();
    private final List<Concept>	generalizations;


    public Concept(String name, String definition, String stereotype, IClass object) {
        this.object = object;
        conceptMap.put(object, this);
        this.generalizations = new ArrayList<>();
    }

    public void setOntology(Ontology onto) {
        this.ontology = onto;
    }

    public IClass getAstahConceptObject() {
        return this.object;
    }

    public static Concept getConceptObjectByItsIClass(IClass object) {
        return conceptMap.get(object);
    }

    public static List<Concept> getAllConcepts() {
        return new ArrayList<>(conceptMap.values());
    }

    public void addGeneralization(Concept concept) {
        this.generalizations.add(concept);
    }
}

package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Concept implements Comparable<Concept> {

    private Ontology ontology;
    private final IClass object;
    private static final Map<IClass, Concept> conceptMap	= new HashMap<IClass, Concept>();
    private final List<Concept>	generalizations;
    private final String name;
    private final String definition;
    private String example;
    private String sourceDefinition;
    private final String stereotype;

    private final List<Relation> relations = new ArrayList<>();

    public void addRelation(Relation relation) {
        if (relation != null && !relations.contains(relation)) {
            relations.add(relation);
        }
    }

    public List<Relation> getRelations() {
        return new ArrayList<>(relations);
    }


    public Concept(String name, String definition, String stereotype, IClass object) {
        this.object = object;
        conceptMap.put(object, this);
        this.generalizations = new ArrayList<>();
        this.name = name;
        this.definition = definition;
        this.stereotype = stereotype;
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

    public Ontology getOntology() {
        return this.ontology;
    }

    public String getName() {
        return this.name;
    }

    public String getDefinition() {
        return this.definition;
    }

    public IClass getConceptAstahObject() {
        return this.object;
    }

    /* Returns the Concept with the full name parameter ("::" separator). */
    public static Concept getConceptByFullName(String fullName) {
        for (Concept concept : conceptMap.values()) {
            if (concept.getConceptAstahObject().getFullName("::").equals(fullName)) {
                return concept;
            }
        }
        return null;
    }

    /**
     * Returns the string used for referencing this concept in the HTML.
     * Example: "SPO.html#SPO_Artifact+Participation"
     */
    public String getReference() {
        if (ontology == null || name == null || name.trim().isEmpty()) {
            return "#"; // Referência padrão para casos inválidos
        }

        Ontology mainOntology = ontology.getMainOntology();
        String shortName = mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() : "Unknown";
        String formattedName = name.trim().replaceAll("\\s+", "+");
        return String.format("%s.html#%s_%s", shortName, shortName, formattedName);
    }

    /* Returns the string used for labeling this concept in the html. Ex.: SPO_Artifact+Participation */
    public String getLabel() {
        return ontology.getMainOntology().getShortName() + "_" + name.replace(' ', '+');
    }

    public String getExample() {
        return this.example;
    }

    public String getSourceDefinition() {
        return this.sourceDefinition;
    }

    public List<Concept> getGeneralizations() {
        return this.generalizations;
    }

    public String getStereotype() {
        return this.stereotype;
    }

    public String getFullName() {
        return ontology.getMainOntology().getShortName() + "::" + this.name;
    }

    public Ontology getMainOntology() {
        return ontology.getMainOntology();
    }

    public void addGeneralization(Concept concept) {
        this.generalizations.add(concept);
    }

    @Override
    public int compareTo(Concept o) {
        return this.name.compareTo(o.name);
    }
}

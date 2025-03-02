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

    /**
     * Set the ontology to which this concept belongs.
     * @param onto The ontology to which this concept belongs.
     */
    public void setOntology(Ontology onto) {
        this.ontology = onto;
    }

    /**
     * Returns the Astah object that represents this concept.
     * @return The Astah object that represents this concept.
     */
    public IClass getAstahConceptObject() {
        return this.object;
    }

    /**
     * Returns the Concept object that corresponds to the given Astah object.
     * @param object The Astah object that represents the concept.
     * @return The Concept object that corresponds to the given Astah object.
     */
    public static Concept getConceptObjectByItsIClass(IClass object) {
        return conceptMap.get(object);
    }

    /**
     * Return all concepts.
     * @return A list with all concepts.
     */
    public static List<Concept> getAllConcepts() {
        return new ArrayList<>(conceptMap.values());
    }

    /**
     * Returns the ontology to which this concept belongs.
     * @return The ontology to which this concept belongs.
     */
    public Ontology getOntology() {
        return this.ontology;
    }

    public String getName() {
        return this.name;
    }

    public String getDefinition() {
        return this.definition;
    }

    /**
     * Returns the Concept with the full name parameter ("::" separator).
     * @param fullName The full name of the concept.
     * @return The concept with the given full name.
     */
    public static Concept getConceptByFullName(String fullName) {
        for (Concept concept : conceptMap.values()) {
            if (concept.getAstahConceptObject().getFullName("::").equals(fullName)) {
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

    /**
     * Returns the string used for labeling this concept in the HTML.
     * Example: "SPO_Artifact+Participation"
     */
    public String getLabel() {
        return ontology.getMainOntology().getShortName() + "_" + name.replace(' ', '+');
    }

    /**
     * Returns the example of this concept.
     * @return The example of this concept.
     */
    public String getExample() {
        return this.example;
    }

    public String getSourceDefinition() {
        return this.sourceDefinition;
    }

    /**
     * Get the generalizations of this concept.
     * @return The generalizations of this concept.
     */
    public List<Concept> getGeneralizations() {
        return this.generalizations;
    }

    public String getStereotype() {
        return this.stereotype;
    }

    public String getFullName() {
        return ontology.getMainOntology().getShortName() + "::" + this.name;
    }

    /**
     * Returns the main ontology to which this concept belongs.
     * @return The main ontology to which this concept belongs.
     */
    public Ontology getMainOntology() {
        return ontology.getMainOntology();
    }

    /**
     * Add a generalization to this concept.
     * @param concept The concept that generalizes this concept.
     * @return The concept that generalizes this concept.
     */
    public void addGeneralization(Concept concept) {
        this.generalizations.add(concept);
    }

    /**
     * Compares this concept with another concept.
     * @param o the object to be compared.
     * @return a negative integer, zero, or a positive integer as this object is less than, equal to, or greater than the specified object.
     */
    @Override
    public int compareTo(Concept o) {
        return this.name.compareTo(o.name);
    }
}

package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.change_vision.jude.api.inf.model.IClass;

/**
 * Represents a UML class (concept) within a SEON ontology.
 * Contains the concept's definition, stereotype, example, source, and its
 * generalization and association relationships parsed from the Astah file.
 */
public class Concept implements Comparable<Concept> {

    private Ontology ontology;
    private final IClass object;
    private final List<Concept> generalizations;
    private final String name;
    private final String definition;
    private String example;
    private String sourceDefinition;
    private final String stereotype;

    private final List<Relation> relations = new ArrayList<>();

    public Concept(String name, String definition, String stereotype, IClass object) {
        this.object = object;
        this.generalizations = new ArrayList<>();
        this.name = name;
        this.definition = definition;
        this.stereotype = stereotype;
    }

    /** Adds a relation if not already present (prevents duplicates from bidirectional parsing). */
    public void addRelation(Relation relation) {
        if (relation != null && !relations.contains(relation)) {
            relations.add(relation);
        }
    }

    public List<Relation> getRelations() { return new ArrayList<>(relations); }

    public void setOntology(Ontology onto) { this.ontology = onto; }
    public void addGeneralization(Concept concept) { this.generalizations.add(concept); }

    public IClass getAstahConceptObject() { return this.object; }
    public Ontology getOntology() { return this.ontology; }
    public String getName() { return this.name; }
    public String getDefinition() { return this.definition; }
    public String getExample() { return this.example; }
    public String getSourceDefinition() { return this.sourceDefinition; }
    public String getStereotype() { return this.stereotype; }
    public List<Concept> getGeneralizations() { return this.generalizations; }

    public void setExample(String example) { this.example = example; }
    public void setSourceDefinition(String sourceDefinition) { this.sourceDefinition = sourceDefinition; }

    /** Returns an HTML anchor reference: "ShortName.html#ShortName_Concept+Name" */
    public String getReference() {
        if (ontology == null || name == null || name.trim().isEmpty()) return "#";
        Ontology mainOntology = ontology.getMainOntology();
        String shortName = mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() : "Unknown";
        String formattedName = name.trim().replaceAll("\\s+", "+");
        return String.format("%s.html#%s_%s", shortName, shortName, formattedName);
    }

    /** Returns an HTML id: "ShortName_Concept+Name" */
    public String getLabel() {
        return ontology.getMainOntology().getShortName() + "_" + name.replace(' ', '+');
    }

    /** Returns "ShortName::ConceptName" */
    public String getFullName() {
        return ontology.getMainOntology().getShortName() + "::" + this.name;
    }

    public Ontology getMainOntology() {
        return ontology.getMainOntology();
    }

    @Override
    public int compareTo(Concept o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return Objects.equals(name, concept.name) && Objects.equals(ontology, concept.ontology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, ontology);
    }
}

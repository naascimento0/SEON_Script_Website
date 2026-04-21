package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.change_vision.jude.api.inf.model.IClass;

/**
 * Represents a concept (class) within a SEON ontology.
 * 
 * A Concept corresponds to a UML class in the Astah file and belongs to a specific Ontology.
 * For example, the ontology SysSwO contains concepts like Complex Computer System, Computer System,
 * Controller, and others. Each concept encapsulates semantic meaning, stereotypes, definitions,
 * examples, and relationships with other concepts.
 * 
 * Attributes:
 * - String name: the concept name
 * - String definition: the concept definition
 * - String stereotype: the stereotype defined in Astah
 *   (e.g., kind, role, mode, subkind, event, phase, category, rolemixin, 2ndOT, etc.)
 * - String example: example usage of the concept
 * - String sourceDefinition: source of the definition (reference, book, paper, etc.)
 * - Ontology ontology: the ontology to which this concept belongs
 * - IClass object: the Astah IClass object representing this class
 * - List<Concept> generalizations: generalizations of this concept
 *   (see UML Generalization relationships)
 * - List<Relation> relations: relationships in which this concept participates
 */
public class Concept implements Comparable<Concept> {

    private Ontology ontology;
    private final IClass object;
    private final List<Concept>	generalizations;
    private final String name;
    private final String definition;
    private String example;
    private String sourceDefinition;
    private final String stereotype;

    private final List<Relation> relations = new ArrayList<>();

    /**
     * Adds a relationship to this concept if it does not already exist.
     * Prevents duplicate relations from being added.
     * 
     * @param relation the Relation to add. If null, it is ignored.
     */
    public void addRelation(Relation relation) {
        if (relation != null && !relations.contains(relation)) {
            relations.add(relation);
        }
    }

    /**
     * Returns a defensive copy of all relationships in which this concept participates.
     * 
     * @return a list copy of all Relation objects for this concept.
     */
    public List<Relation> getRelations() {
        return new ArrayList<>(relations);
    }

    /**
     * Creates a new concept with the specified attributes.
     * 
     * @param name the concept name (from the Astah class).
     * @param definition the definition or description of the concept.
     * @param stereotype the stereotype assigned in Astah (e.g., "kind", "role", "mode").
     * @param object the Astah IClass object representing this concept.
     */
    public Concept(String name, String definition, String stereotype, IClass object) {
        this.object = object;
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
     * Returns the ontology to which this concept belongs.
     * @return The ontology to which this concept belongs.
     */
    public Ontology getOntology() {
        return this.ontology;
    }

    /**
     * Returns the name of this concept.
     * @return the concept name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the definition of this concept.
     * @return The definition string (may be null or empty).
     */
    public String getDefinition() {
        return this.definition;
    }

    /**
     * Returns the example of this concept.
     * @return The example string, or null if not defined.
     */
    public String getExample() {
        return this.example;
    }
    
    /**
     * Returns the source of the definition (reference, book, paper, etc.).
     * @return The source definition string, or null if not defined.
     */
    public String getSourceDefinition() {
        return this.sourceDefinition;
    }
    
    /**
     * Returns the stereotype of this concept (e.g., abstract, interface).
     * @return The stereotype string, or null if not defined.
     */
    public String getStereotype() {
        return this.stereotype;
    }

    /**
     * Returns all concepts that generalize this concept (parent concepts in the UML hierarchy).
     * A generalization represents an "is-a" relationship, where this concept specializes a parent concept.
     * 
     * @return a list of Concept objects that generalize this concept.
     */
    public List<Concept> getGeneralizations() {
        return this.generalizations;
    }

    /**
     * Returns the string used for referencing this concept in the HTML.
     * Format: "[OntologyShortName].html#[ShortName]_[ConceptName]"
     * Example: "SPO.html#SPO_Artifact+Participation"
     * 
     * @return HTML reference string, or "#" if ontology is null or name is empty.
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
     * Format: "[OntologyShortName]_[ConceptName]"
     * Example: "SPO_Artifact+Participation"
     * 
     * @return HTML label string.
     */
    public String getLabel() {
        return ontology.getMainOntology().getShortName() + "_" + name.replace(' ', '+');
    }

    /**
     * Returns the fully qualified name of this concept.
     * Format: "[OntologyShortName]::[ConceptName]"
     * Example: "SPO::Artifact"
     * 
     * @return The fully qualified concept name.
     */
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
     * Adds a concept as a generalization of this concept.
     * This establishes an "is-a" relationship where this concept specializes the given concept.
     * 
     * @param concept the concept that generalizes this concept (the parent in the UML hierarchy).
     */
    public void addGeneralization(Concept concept) {
        this.generalizations.add(concept);
    }

    /**
     * Compares this concept with another concept alphabetically by name.
     * Used for sorting concepts.
     * 
     * @param o the concept to compare with.
     * @return a negative integer, zero, or a positive integer if this concept's name is
     *         less than, equal to, or greater than the specified concept's name.
     */
    @Override
    public int compareTo(Concept o) {
        return this.name.compareTo(o.name);
    }

    /**
     * Compares this concept to another for equality.
     * Two concepts are equal if they have the same name and belong to the same ontology.
     * 
     * @param o the object to compare with.
     * @return true if this concept equals the specified object, false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Concept concept = (Concept) o;
        return Objects.equals(name, concept.name)
                && Objects.equals(ontology, concept.ontology);
    }

    /**
     * Computes the hash code for this concept based on its name and ontology.
     * Ensures correct behavior in hash-based collections (HashMap, HashSet, etc.).
     * 
     * @return the hash code for this concept.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, ontology);
    }
}
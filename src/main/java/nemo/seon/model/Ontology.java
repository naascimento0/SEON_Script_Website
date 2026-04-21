package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/**
 * Represents a SEON ontology, which is a specialized Package that contains Concepts.
 * 
 * An Ontology extends Package and stores a collection of Concepts that define the semantic
 * entities and their relationships within that ontology. A package is classified as an Ontology
 * if it has the Tagged Value "Ontology" or "Subontology" defined in the Astah file.
 * 
 * Ontologies are further classified into three levels based on their scope and generality:
 * - FOUNDATIONAL: highly reusable, domain-independent concepts (base for other ontologies)
 * - CORE: common concepts used across multiple domain ontologies
 * - DOMAIN: domain-specific concepts tailored for particular application areas
 * 
 * Attributes (in addition to those inherited from Package):
 * - String fullName: the fully qualified ontology name (Tagged Value from Astah)
 *   Example: "SEON::Process::Foundational::SPO"
 * - String shortName: the abbreviated identifier for the ontology (Tagged Value from Astah)
 *   Example: "SPO"
 * - List<Concept> concepts: the concepts that compose this ontology
 * - OntoLevel: enumeration with values FOUNDATIONAL(0), CORE(1), DOMAIN(2)
 * 
 * Note:
 * Full name and short name are Tagged Values that must be defined by the developer in Astah Professional.
 * The Astah Community and Astah UML versions do not support the definition of Tagged Values.
 */
public class Ontology extends Package {

    private final List<Concept> concepts;
    private final String fullName;
    private final String shortName;

    /**
     * Creates an ontology with full and short names.
     * 
     * @param name the ontology name (from Astah package).
     * @param fullName the fully qualified ontology name (e.g., "SEON::Process::Foundational::SPO").
     * @param shortName the short identifier for the ontology (e.g., "SPO").
     * @param definition the ontology definition or description.
     * @param type the PackType (ONTOLOGY or SUBONTOLOGY).
     * @param order the ordering value for sorting ontologies.
     * @param pack the underlying Astah IPackage object.
     */
    public Ontology(String name, String fullName, String shortName, String definition, PackType type, int order, IPackage pack) {
        super(name, definition, type, order, pack);
        this.concepts = new ArrayList<>();
        this.fullName = fullName;
        this.shortName = shortName;
    }

    /**
     * Enumeration of ontology levels in SEON, representing the scope and generality of concepts.
     * 
     * FOUNDATIONAL (0): Highly reusable, domain-independent concepts that serve as the foundation
     *                   for other ontologies. Examples: upper-level ontology concepts.
     * CORE (1): Common concepts applicable across multiple domain ontologies.
     *           Concepts that are more general than domain-specific but more specific than foundational.
     * DOMAIN (2): Domain-specific concepts tailored for particular application areas or industries.
     *             Specialized concepts with restricted scope to a specific domain.
     * 
     * The numeric values (0, 1, 2) are used for ordering and comparison.
     */
    public enum OntoLevel {
        FOUNDATIONAL(0), CORE(1), DOMAIN(2);

        /** The numeric value associated with this ontology level (used for ordering). */
        private final int value;

        /**
         * Creates an ontology level with the specified numeric value.
         * 
         * @param value the numeric value (0 for FOUNDATIONAL, 1 for CORE, 2 for DOMAIN).
         */
        OntoLevel(final int value) {
            this.value = value;
        }

        /**
         * Returns the numeric value associated with this ontology level.
         * 
         * @return the level value: 0 (FOUNDATIONAL), 1 (CORE), or 2 (DOMAIN).
         */
        public int getValue() {
            return value;
        }
    }

    /**
     * Returns the fully qualified name of this ontology.
     * This is the complete hierarchical path of the ontology within the SEON network.
     * 
     * Example: "SEON::Process::Foundational::SPO"
     * 
     * @return the full ontology name (from Tagged Value in Astah).
     */
    public String getFullName() {
        return this.fullName;
    }

    /**
     * Returns the short identifier or abbreviation for this ontology.
     * This is a concise abbreviation used in references, labels, and HTML identifiers.
     * 
     * Example: "SPO" (Standard Process Ontology)
     * 
     * @return the short ontology abbreviation (from Tagged Value in Astah).
     */
    public String getShortName() {
        return this.shortName;
    }

    /**
     * Returns all concepts defined in this ontology.
     * The returned list contains the concepts that are direct members of this ontology.
     * 
     * @return a list of Concept objects in this ontology (may be empty if no concepts are defined).
     */
    public List<Concept> getConcepts() {
        return this.concepts;
    }

    /**
     * Adds a concept to this ontology.
     * The concept will be included in the list of concepts returned by getConcepts().
     * 
     * @param concept the Concept to add to this ontology.
     */
    public void addConcept(Concept concept) {
        this.concepts.add(concept);
    }
}

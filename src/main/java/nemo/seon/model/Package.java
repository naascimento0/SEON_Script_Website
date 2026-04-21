package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/**
 * Represents a package in the SEON ontology model.
 * 
 * A Package is a container that stores attributes from an Astah IPackage instance.
 * Every folder in the .asta file is represented as a Package. Packages form a hierarchical
 * structure with parent-child relationships (subpackages) and can contain diagrams and concepts.
 * 
 * Package Classification:
 * The type of a package is determined by a Tagged Value defined by the developer in Astah
 * and can be one of: NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, or IGNORE.
 * 
 * Attributes:
 * - String name: the package name (retrieved from Astah)
 * - PackType type: the package type (determined by Tagged Value in Astah)
 * - int order: the ordering value for sorting and displaying packages
 * - IPackage pack: the Astah IPackage object representing this package
 * - List<Package> subpacks: child packages (subfolders)
 * - Package parent: the parent package (folder)
 * - List<Diagram> diagrams: diagrams contained in this package
 * - List<Dependency> dependencies: dependencies that this package has with other packages
 * 
 * Important Note:
 * The definition of a package is obtained dynamically via pack.getDefinition().
 * It is NOT stored as a field; instead, it is retrieved directly from the Astah object.
 */
public class Package implements Comparable<Package> {
    /**
     * Enumeration of package types in SEON, determined by Tagged Values in Astah.
     * 
     * NETWORK: the root network containing all ontologies
     * SUBNETWORK: a division or subset of the network
     * LEVEL: a hierarchical level grouping (e.g., "Foundational", "Core", "Domain")
     * PACKAGE: a general-purpose package or subpackage
     * ONTOLOGY: a top-level ontology (extends Package as Ontology class)
     * SUBONTOLOGY: a sub-ontology within a parent ontology
     * IGNORE: a package that should be ignored in processing
     */
    public enum PackType {
        NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, IGNORE
    }

    private final List<Package> subpacks;
    private final List<Dependency> dependencies;
    private Package parent;
    private final IPackage pack;
    private final List<Diagram> diagrams;
    private final PackType type;
    private final String name;
    private final int order;


    /**
     * Creates a package with the specified attributes.
     * 
     * @param name the package name (from Astah).
     * @param definition the package definition or description (from Astah).
     * @param type the PackType (NETWORK, ONTOLOGY, PACKAGE, etc.).
     * @param order the ordering value for sorting packages.
     * @param pack the Astah IPackage object representing this package.
     */
    public Package(String name, String definition, PackType type, int order, IPackage pack) {
        this.pack = pack;
        this.subpacks = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.diagrams = new ArrayList<>();
        this.type = type;
        this.name = name;
        this.order = order;
    }


    /**
     * Returns the name of this package (from Astah).
     * @return the package name.
     */
    public String getName() {
        return this.pack.getName();
    }

    /**
     * Returns the Astah IPackage object that represents this package.
     * @return the IPackage object.
     */
    public IPackage getAstahPack() {
        return this.pack;
    }

    /**
     * Converts a string package type (from Astah Tagged Value) to the corresponding PackType enum.
     * 
     * Mapping:
     * - "Level" -> LEVEL
     * - "Subnetwork" -> SUBNETWORK
     * - "Package" -> PACKAGE
     * - "Ontology" -> ONTOLOGY
     * - "Subontology" -> SUBONTOLOGY
     * - "Ignore" -> IGNORE
     * - null or unrecognized -> PACKAGE (default)
     * 
     * @param givenType the package type string from Astah (or null).
     * @return the corresponding PackType enum value, or PACKAGE if not recognized.
     */
    public static PackType getPackType(String givenType) {
        return switch (givenType) {
            case "Level" -> PackType.LEVEL;
            case "Subnetwork" -> PackType.SUBNETWORK;
            case "Package" -> PackType.PACKAGE;
            case "Ontology" -> PackType.ONTOLOGY;
            case "Subontology" -> PackType.SUBONTOLOGY;
            case "Ignore" -> PackType.IGNORE;
            default -> PackType.PACKAGE;
        };
    }

    /**
     * Sets the parent package of this package.
     * @param parent the parent Package, or null if this is a root package.
     */
    public void setParent(Package parent) {
        this.parent = parent;
    }

    /**
     * Adds a dependency to this package.
     * @param dependency the Dependency to add.
     */
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    /**
     * Adds a subpackage (child package) to this package.
     * @param pack the Package to add as a subpackage.
     */
    public void addSubPack(Package pack) {
        this.subpacks.add(pack);
    }

    /**
     * Adds a diagram to this package.
     * @param diagram the Diagram to add.
     */
    public void addDiagram(Diagram diagram) {
        this.diagrams.add(diagram);
    }

    /**
     * Returns the subpackages (child packages) of this package.
     * @return a list of Package objects representing subfolders.
     */
    public List<Package> getSubpacks() {
        return subpacks;
    }

    /**
     * Returns the type of this package (NETWORK, ONTOLOGY, PACKAGE, etc.).
     * @return the PackType enum value.
     */
    public PackType getPackageType() {
        return this.type;
    }

    /**
     * Returns the parent package of this package.
     * @return the parent Package object, or null if this is a root package.
     */
    public Package getParent() {
        return this.parent;
    }

    /**
     * Returns the network name of this package (from "Network" Tagged Value in Astah).
     * @return the network name, or null if not defined.
     */
    public String getNetwork() {
        return this.pack.getTaggedValue("Network");
    }

    /**
     * Returns the dependencies of this package.
     * @return a list of Dependency objects.
     */
    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    /**
     * Returns the definition of this package (from Astah).
     * Note: This is retrieved dynamically from the Astah object, not stored as a field.
     * 
     * @return the definition string, or null if not defined.
     */
    public String getDefinition() {
        return this.pack.getDefinition();
    }

    /**
     * Returns the diagrams of this package.
     * @return a list of Diagram objects contained in this package.
     */
    public List<Diagram> getDiagrams() {
        return this.diagrams;
    }

    /**
     * Returns the main (root) ontology that this package belongs to.
     * Traverses the package hierarchy upward until an Ontology (PackType.ONTOLOGY) is found.
     * 
     * @return the main Ontology object, or null if not found or this is not part of an ontology.
     */
    public Ontology getMainOntology() {
        Package current = this;
        while (current != null && current.type != PackType.ONTOLOGY) {
            current = current.getParent();
        }
        return current instanceof Ontology ? (Ontology) current : null;
    }

    /**
     * Returns the string used for referencing this package in HTML.
     * Format: "[OntologyShortName].html#[ShortName]_[PackageName]"
     * Example: "SPO.html#SPO_Standard+Process+Structure"
     * 
     * If no ontology is found, returns just the formatted package name.
     * If the package name is empty, returns "#".
     * 
     * @return HTML reference string for linking to this package.
     */
    public String getReference() {
        if (name == null || name.trim().isEmpty()) {
            return "#"; // Referência padrão para casos inválidos
        }

        Ontology mainOntology = getMainOntology();
        String shortName = mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() : "Unknown";
        String formattedName = name.trim().replaceAll("\\s+", "+");
        return mainOntology != null ?
                String.format("%s.html#%s_%s", shortName, shortName, formattedName) :
                formattedName; // Apenas o nome formatado se não houver ontologia
    }

    /**
     * Returns a unique label for this package used in HTML identification.
     * Format: "[OntologyShortName]_[PackageName]" (with spaces replaced by '+')
     * Example: "SPO_Standard+Process"
     * 
     * If no ontology is found, returns just the formatted package name.
     * If the package name is empty, returns "Unnamed".
     * 
     * @return HTML label string for identification and referencing.
     */
    public String getLabel() {
        String formattedName = name != null && !name.trim().isEmpty() ?
                name.trim().replaceAll("\\s+", "+") : "Unnamed";
        Ontology mainOntology = getMainOntology();
        return mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() + "_" + formattedName : formattedName;
    }

    /**
     * Calculates the hierarchical depth of this package in the package tree.
     * Counts the number of parent packages by traversing upward until reaching the root.
     * 
     * @return the depth level: 0 for root packages, 1 for direct children of root, etc.
     */
    public int getPackLevel() {
        int level = 0;
        Package current = this;
        while (current.parent != null) {
            current = current.parent;
            level++;
        }
        return level;
    }

    /**
     * Returns the ontology level of this package (FOUNDATIONAL, CORE, or DOMAIN).
     * Determined by examining the name of the LEVEL or NETWORK package in the hierarchy.
     * 
     * Looks for keywords in the package names:
     * - "Foundational" -> OntoLevel.FOUNDATIONAL
     * - "Core" -> OntoLevel.CORE
     * - "Domain" -> OntoLevel.DOMAIN
     * 
     * @return the OntoLevel enum value, or null if not found or indeterminable.
     */
    public Ontology.OntoLevel getLevel() {
        Package current = this;
        while (current.getPackageType() != PackType.LEVEL && current.getPackageType() != PackType.NETWORK)
            current = current.getParent();
        if (current.getName().contains("Foundational"))
            return Ontology.OntoLevel.FOUNDATIONAL;
        else if (current.getName().contains("Core"))
            return Ontology.OntoLevel.CORE;
        else if (current.getName().contains("Domain"))
            return Ontology.OntoLevel.DOMAIN;
        return null;
    }

    /**
     * Returns all concepts from this package and its subpackages recursively.
     * If this package is an Ontology, its concepts are included.
     * Subpackages are traversed recursively to collect all nested concepts.
     * 
     * @return a list of all Concept objects in this package and subpackages.
     */
    public List<Concept> getAllConcepts() {
        List<Concept> concepts = new ArrayList<>();
        if (this instanceof Ontology ontology) {
            concepts.addAll(ontology.getConcepts());
        }
        for (Package subPack : this.subpacks) {
            concepts.addAll(subPack.getAllConcepts());
        }
        return concepts;
    }


    // ========== Comparable and Sorting ==========

    /**
     * Compares this package to another package based on strength (ontology level and order).
     * Packages are sorted primarily by ontology level (DOMAIN > CORE > FOUNDATIONAL),
     * and secondarily by order value within the same level.
     * 
     * @param pack the package to compare with.
     * @return a negative integer if this package has lower strength,
     *         zero if equal strength,
     *         a positive integer if this package has higher strength;
     *         returns 1 if the specified package is null.
     */
    @Override
    public int compareTo(Package pack) {
        if (pack == null) return 1; // Considera este pacote maior se o outro for null
        return getStrength(this) - getStrength(pack);
    }

    /**
     * Calculates the precedence strength of a package based on its ontology level and order.
     * Uses exponential weighting for levels: DOMAIN (100^3 = 1,000,000), CORE (100^2 = 10,000),
     * FOUNDATIONAL (100^1 = 100). The order value is added to the level weight to break ties.
     * 
     * This allows packages to be sorted by level first, then by order within the same level.
     * Example: A DOMAIN package with order 5 has strength 1,000,005.
     * 
     * @param pack The package to evaluate, or null.
     * @return The strength value (higher means higher precedence), or 0 if pack is null.
     */
    private int getStrength(Package pack) {
        if (pack == null) return 0;

        int levelWeight;
        Ontology.OntoLevel level = pack.getLevel();
        if (level == null) {
            levelWeight = 0;
        } else {
            levelWeight = switch (level) {
                case DOMAIN -> 1_000_000; // 100^3
                case CORE -> 10_000;      // 100^2
                case FOUNDATIONAL -> 100;  // 100^1
                default -> 0;
            };
        }
        return levelWeight + pack.order;
    }
}
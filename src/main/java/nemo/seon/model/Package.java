package nemo.seon.model;

import java.util.ArrayList;
import java.util.List;

import com.change_vision.jude.api.inf.model.IPackage;

/**
 * Represents a container (folder) in the SEON ontology model parsed from an Astah file.
 * Packages form a hierarchy (parent-child) and can contain subpackages, diagrams, and concepts.
 * The package type is determined by a Tagged Value in Astah ("Ontology", "Package", "Level", etc.).
 * Definition is not stored as a field — it is retrieved dynamically via the Astah IPackage object.
 */
public class Package implements Comparable<Package> {

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

    public Package(String name, String definition, PackType type, int order, IPackage pack) {
        this.pack = pack;
        this.subpacks = new ArrayList<>();
        this.dependencies = new ArrayList<>();
        this.diagrams = new ArrayList<>();
        this.type = type;
        this.name = name;
        this.order = order;
    }

    public String getName() {
        return this.pack.getName();
    }

    public IPackage getAstahPack() {
        return this.pack;
    }

    /**
     * Maps an Astah Tagged Value string to a PackType.
     * Returns PACKAGE as default for null or unrecognized values.
     */
    public static PackType getPackType(String givenType) {
        if (givenType == null) return PackType.PACKAGE;
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

    public void setParent(Package parent) { this.parent = parent; }
    public void addDependency(Dependency dependency) { this.dependencies.add(dependency); }
    public void addSubPack(Package pack) { this.subpacks.add(pack); }
    public void addDiagram(Diagram diagram) { this.diagrams.add(diagram); }

    public List<Package> getSubpacks() { return subpacks; }
    public PackType getPackageType() { return this.type; }
    public Package getParent() { return this.parent; }
    public List<Dependency> getDependencies() { return this.dependencies; }
    public List<Diagram> getDiagrams() { return this.diagrams; }

    public String getNetwork() {
        return this.pack.getTaggedValue("Network");
    }

    /** Definition is retrieved dynamically from Astah — not stored as a field. */
    public String getDefinition() {
        return this.pack.getDefinition();
    }

    /**
     * Traverses parent packages upward until an ONTOLOGY-typed package is found.
     * Returns null if this package is not within an ontology.
     */
    public Ontology getMainOntology() {
        Package current = this;
        while (current != null && current.type != PackType.ONTOLOGY) {
            current = current.getParent();
        }
        return current instanceof Ontology ? (Ontology) current : null;
    }

    /**
     * Returns an HTML anchor reference for this package.
     * Format: "ShortName.html#ShortName_Package+Name"
     */
    public String getReference() {
        if (name == null || name.trim().isEmpty()) return "#";
        Ontology mainOntology = getMainOntology();
        String shortName = mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() : "Unknown";
        String formattedName = name.trim().replaceAll("\\s+", "+");
        return mainOntology != null ?
                String.format("%s.html#%s_%s", shortName, shortName, formattedName) :
                formattedName;
    }

    /**
     * Returns an HTML id string for this package.
     * Format: "ShortName_Package+Name"
     */
    public String getLabel() {
        String formattedName = name != null && !name.trim().isEmpty() ?
                name.trim().replaceAll("\\s+", "+") : "Unnamed";
        Ontology mainOntology = getMainOntology();
        return mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() + "_" + formattedName : formattedName;
    }

    /** Returns the depth of this package in the package tree (root = 0). */
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
     * Returns the ontology level (FOUNDATIONAL/CORE/DOMAIN) by looking for "Foundational",
     * "Core", or "Domain" keywords in the LEVEL or NETWORK ancestor package name.
     * Returns null if no LEVEL/NETWORK ancestor is found.
     */
    public Ontology.OntoLevel getLevel() {
        Package current = this;
        while (current != null && current.getPackageType() != PackType.LEVEL && current.getPackageType() != PackType.NETWORK)
            current = current.getParent();
        if (current == null) return null;
        if (current.getName().contains("Foundational")) return Ontology.OntoLevel.FOUNDATIONAL;
        else if (current.getName().contains("Core")) return Ontology.OntoLevel.CORE;
        else if (current.getName().contains("Domain")) return Ontology.OntoLevel.DOMAIN;
        return null;
    }

    /** Returns all concepts from this package and its subpackages recursively. */
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

    /**
     * Sorts by ontology level first (DOMAIN > CORE > FOUNDATIONAL), then by order within the same level.
     * Uses exponential level weights (DOMAIN=1_000_000, CORE=10_000, FOUNDATIONAL=100) to ensure
     * level takes priority over the order value.
     */
    @Override
    public int compareTo(Package pack) {
        if (pack == null) return 1;
        return getStrength(this) - getStrength(pack);
    }

    private int getStrength(Package pack) {
        if (pack == null) return 0;
        Ontology.OntoLevel level = pack.getLevel();
        int levelWeight = level == null ? 0 : switch (level) {
            case DOMAIN -> 1_000_000;
            case CORE -> 10_000;
            case FOUNDATIONAL -> 100;
        };
        return levelWeight + pack.order;
    }
}

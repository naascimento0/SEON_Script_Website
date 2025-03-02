package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Package implements Comparable<Package> {
    public enum PackType {
        NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, IGNORE
    }

    private static final Map<IPackage, Package> packageMap	= new HashMap<>();
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
        packageMap.put(pack, this);
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

    public static PackType getPackType(String givenType) {
        if (givenType != null) {
            switch (givenType) {
                case "Level":
                    return PackType.LEVEL;
                case "Subnetwork":
                    return PackType.SUBNETWORK;
                case "Package":
                    return PackType.PACKAGE;
                case "Ontology":
                    return PackType.ONTOLOGY;
                case "Subontology":
                    return PackType.SUBONTOLOGY;
                case "Ignore":
                    return PackType.IGNORE;
            }
        }
        return PackType.PACKAGE;
    }

    public static Package getAstahPackFromMap(IPackage pack) {
        return packageMap.get(pack);
    }

    public List<Package> getSubpacks() {
        return subpacks;
    }

    public static Package getPackageByFullName(String fullName) {
        for (Package pack : packageMap.values())
            if (pack.getAstahPack().getFullName("::").equals(fullName))
                return pack;
        return null;
    }

    public static List<Package> getAllPackages() {
        return new ArrayList<>(packageMap.values());
    }

    public PackType getPackageType() {
        return this.type;
    }

    public Package getParent() {
        return this.parent;
    }

    public String getNetwork() {
        return this.pack.getTaggedValue("Network");
    }

    public List<Dependency> getDependencies() {
        return this.dependencies;
    }

    public String getDefinition() {
        return this.pack.getDefinition();
    }

    public List<Diagram> getDiagrams() {
        return this.diagrams;
    }

    /**
     * Returns the main ontology this package belongs to by traversing the hierarchy.
     * @return The main Ontology object, or null if not found or invalid.
     */
    public Ontology getMainOntology() {
        Package current = this;
        while (current != null && current.type != PackType.ONTOLOGY) {
            current = current.getParent();
        }
        return current instanceof Ontology ? (Ontology) current : null;
    }

    /**
     * Returns the string used for referencing this package in the HTML.
     * Example: "SPO.html#SPO_Standard+Process+Structure"
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
     * Returns a unique label for this package, prefixed by the main ontology short name if available.
     * Example: "SPO_Standard+Process"
     */
    public String getLabel() {
        String formattedName = name != null && !name.trim().isEmpty() ?
                name.trim().replaceAll("\\s+", "+") : "Unnamed";
        Ontology mainOntology = getMainOntology();
        return mainOntology != null && mainOntology.getShortName() != null ?
                mainOntology.getShortName() + "_" + formattedName : formattedName;
    }

    /**
     * Calculates the hierarchical level of this package by counting parent packages.
     * @return The depth level (0 for root, 1 for first child, etc.).
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

    public Ontology.OntoLevel getLevel() {
        Package pack = this;
        while (pack.getPackageType() != PackType.LEVEL && pack.getPackageType() != PackType.NETWORK)
            pack = pack.getParent();
        if (pack.getName().contains("Foundational"))
            return Ontology.OntoLevel.FOUNDATIONAL;
        else if (pack.getName().contains("Core"))
            return Ontology.OntoLevel.CORE;
        else if (pack.getName().contains("Domain"))
            return Ontology.OntoLevel.DOMAIN;
        return null;
    }

    /* Gets all concepts of the Package, including subpackages. */
    public List<Concept> getAllConcepts() {
        List<Concept> concepts = new ArrayList<Concept>();
        if (this instanceof Ontology) {
            concepts.addAll(((Ontology) this).getConcepts());
        }
        for (Package pack : this.subpacks) {
            concepts.addAll(pack.getAllConcepts());
        }
        return concepts;
    }

    public void setParent(Package parent) {
        this.parent = parent;
    }

    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public void addSubPack(Package pack) {
        this.subpacks.add(pack);
    }

    public void addDiagram(Diagram diagram) {
        this.diagrams.add(diagram);
    }

    /**
     * Compares this package to another based on strength (level precedence and order).
     * @param pack The package to compare with.
     * @return A negative integer, zero, or a positive integer if this package is less than, equal to, or greater than the specified package.
     */
    @Override
    public int compareTo(Package pack) {
        if (pack == null) return 1; // Considera este pacote maior se o outro for null
        return getStrength(this) - getStrength(pack);
    }

    /**
     * Calculates the precedence strength of a package based on its ontology level and order.
     * Levels: DOMAIN (1000000), CORE (10000), FOUNDATIONAL (100), others (0).
     * @param pack The package to evaluate.
     * @return The strength value (higher means higher precedence).
     */
    private int getStrength(Package pack) {
        if (pack == null) return 0;

        int levelWeight;
        Ontology.OntoLevel level = pack.getLevel();
        if (level == null) {
            levelWeight = 0;
        } else {
            switch (level) {
                case DOMAIN: levelWeight = 1_000_000; break; // 100^3
                case CORE: levelWeight = 10_000; break;      // 100^2
                case FOUNDATIONAL: levelWeight = 100; break;  // 100^1
                default: levelWeight = 0;
            }
        }
        return levelWeight + pack.order;
    }

}

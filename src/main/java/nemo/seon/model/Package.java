package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Package {
    public enum PackType {
        NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, IGNORE
    }

    private static final Map<IPackage, Package> packageMap	= new HashMap<>();
    private final List<Package> subpacks;
    private final List<Dependency> dependencies;
    private Package parent;
    private final IPackage pack;
    private final List<Diagram> diagrams;

    public Package(String name, String definition, PackType type, int order, IPackage pack) {
        this.pack = pack;
        this.subpacks = new ArrayList<>();
        packageMap.put(pack, this);
        this.dependencies = new ArrayList<>();
        this.diagrams = new ArrayList<>();
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
            if (pack.getAstahPack().getFullName("::").equals(fullName)) {
                System.out.println(pack.getAstahPack().getFullName("::"));
                return pack;
            }
        return null;
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
}

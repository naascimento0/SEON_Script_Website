package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.List;

public class Package {
    public enum PackType {
        NETWORK, SUBNETWORK, LEVEL, PACKAGE, ONTOLOGY, SUBONTOLOGY, IGNORE
    }

    private final List<Package> subpacks;
    private Package parent;
    private final IPackage pack;

    public Package(String name, String definition, PackType type, int order, IPackage pack) {
        this.pack = pack;
        this.subpacks = new ArrayList<>();
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

    public void setParent(Package parent) {
        this.parent = parent;
    }

    public void addSubPack(Package pack) {
        this.subpacks.add(pack);
    }

}

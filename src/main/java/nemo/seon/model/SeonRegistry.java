package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe registry of all parsed SEON model objects.
 * Replaces the former static maps in Concept and Package, making the model
 * safe to reload when a new .asta file is uploaded.
 */
public class SeonRegistry {

    private final Map<IClass, Concept> conceptMap = new ConcurrentHashMap<>();
    private final Map<IPackage, Package> packageMap = new ConcurrentHashMap<>();

    public void registerConcept(IClass key, Concept concept) { conceptMap.put(key, concept); }
    public Concept getConceptByIClass(IClass key) { return conceptMap.get(key); }
    public List<Concept> getAllConcepts() { return new ArrayList<>(conceptMap.values()); }

    /** Looks up a concept by the Astah full name format "A::B::C". */
    public Concept getConceptByFullName(String fullName) {
        for (Concept concept : conceptMap.values()) {
            if (concept.getAstahConceptObject().getFullName("::").equals(fullName)) {
                return concept;
            }
        }
        return null;
    }

    public void registerPackage(IPackage key, Package pack) { packageMap.put(key, pack); }
    public Package getPackageByIPackage(IPackage key) { return packageMap.get(key); }

    /** Looks up a package by the Astah full name format "A::B::C". */
    public Package getPackageByFullName(String fullName) {
        for (Package pack : packageMap.values()) {
            if (pack.getAstahPack().getFullName("::").equals(fullName)) {
                return pack;
            }
        }
        return null;
    }

    /** Clears all registered data. Called before reloading from a new .asta file. */
    public void clear() {
        conceptMap.clear();
        packageMap.clear();
    }
}

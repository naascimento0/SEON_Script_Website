package nemo.seon.model;

import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry that holds all parsed SEON model objects (Concepts, Packages).
 * Replaces the former static maps in Concept and Package classes,
 * making the model safe for reloading and thread-safe for concurrent access.
 */
public class SeonRegistry {

    private final Map<IClass, Concept> conceptMap = new ConcurrentHashMap<>();
    private final Map<IPackage, Package> packageMap = new ConcurrentHashMap<>();

    // ---- Concept operations ----

    public void registerConcept(IClass key, Concept concept) {
        conceptMap.put(key, concept);
    }

    public Concept getConceptByIClass(IClass key) {
        return conceptMap.get(key);
    }

    public List<Concept> getAllConcepts() {
        return new ArrayList<>(conceptMap.values());
    }

    public Concept getConceptByFullName(String fullName) {
        for (Concept concept : conceptMap.values()) {
            if (concept.getAstahConceptObject().getFullName("::").equals(fullName)) {
                return concept;
            }
        }
        return null;
    }

    // ---- Package operations ----

    public void registerPackage(IPackage key, Package pack) {
        packageMap.put(key, pack);
    }

    public Package getPackageByIPackage(IPackage key) {
        return packageMap.get(key);
    }

    public Package getPackageByFullName(String fullName) {
        for (Package pack : packageMap.values()) {
            if (pack.getAstahPack().getFullName("::").equals(fullName)) {
                return pack;
            }
        }
        return null;
    }

    // ---- Lifecycle ----

    /**
     * Clears all registered data. Called before reloading from a new .asta file.
     */
    public void clear() {
        conceptMap.clear();
        packageMap.clear();
    }
}

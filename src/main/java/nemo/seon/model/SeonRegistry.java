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

    /**
     * Registers a concept in the registry using its Astah IClass as key.
     * @param key the Astah IClass object.
     * @param concept the Concept to register.
     */
    public void registerConcept(IClass key, Concept concept) {
        conceptMap.put(key, concept);
    }

    /**
     * Retrieves a concept by its Astah IClass object.
     * @param key the Astah IClass object.
     * @return the Concept, or null if not found.
     */
    public Concept getConceptByIClass(IClass key) {
        return conceptMap.get(key);
    }

    /**
     * Returns all registered concepts.
     * @return a list containing all Concept objects in the registry.
     */
    public List<Concept> getAllConcepts() {
        return new ArrayList<>(conceptMap.values());
    }

    /**
     * Retrieves a concept by its fully qualified name.
     * @param fullName the fully qualified name (e.g., "SPO::Artifact").
     * @return the Concept, or null if not found.
     */
    public Concept getConceptByFullName(String fullName) {
        for (Concept concept : conceptMap.values()) {
            if (concept.getAstahConceptObject().getFullName("::").equals(fullName)) {
                return concept;
            }
        }
        return null;
    }

    // ---- Package operations ----

    /**
     * Registers a package in the registry using its Astah IPackage as key.
     * @param key the Astah IPackage object.
     * @param pack the Package to register.
     */
    public void registerPackage(IPackage key, Package pack) {
        packageMap.put(key, pack);
    }

    /**
     * Retrieves a package by its Astah IPackage object.
     * @param key the Astah IPackage object.
     * @return the Package, or null if not found.
     */
    public Package getPackageByIPackage(IPackage key) {
        return packageMap.get(key);
    }

    /**
     * Retrieves a package by its fully qualified name.
     * @param fullName the fully qualified name (e.g., "SEON::Process::Foundational").
     * @return the Package, or null if not found.
     */
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

package nemo.seon.model;

/**
 * Represents a dependency relationship between two packages in the SEON ontology network.
 * 
 * A Dependency signals a relationship of reliance or influence from one package to another.
 * The dependency indicates that a package (the source) depends on another package (the target)
 * for its functionality, definitions, or semantic meaning.
 * 
 * Attributes:
 * - Package target: the package on which the source depends
 * - String description: description of the dependency relationship
 * - String level: the dependency level or type (Tagged Value from Astah, e.g., "strong", "weak")
 * 
 * Important Note:
 * The source package (the one that has the dependency) is NOT stored in Dependency.
 * Instead, the dependency is added to the 'dependencies' list of the source package.
 * This design allows the source package to maintain and navigate its dependencies efficiently.
 * 
 */
public class Dependency {
    private final Package target;
    private final String description;
    private final String level;

    /**
     * Creates a dependency from one package to another.
     * 
     * Note: The source package is not stored in the Dependency object. Instead, this Dependency
     * instance is added to the source package's 'dependencies' list for navigability.
     * 
     * @param source the package that has the dependency (not stored, used for context).
     * @param target the package that is depended upon.
     * @param description a human-readable description of the dependency.
     * @param level the dependency level or type set as a Tagged Value in Astah
     *              (e.g., "strong", "weak", "temporary").
     */
    public Dependency(Package source, Package target, String description, String level) {
        this.target = target;
        this.description = description;
        this.level = level;
    }

    /**
     * Returns the target package of this dependency.
     * This is the package on which the source package depends.
     * 
     * @return the target Package object.
     */
    public Package getTarget() {
        return this.target;
    }

    /**
     * Returns the description or justification of this dependency relationship.
     * Explains why the dependency exists or what the relationship represents.
     * 
     * @return the dependency description string, or null if not defined.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the level or type of this dependency (from Astah Tagged Value).
     * Examples: "strong", "weak", "temporary", or custom values defined by the developer.
     * 
     * @return the dependency level string, or null if not defined.
     */
    public String getLevel() {
        return this.level;
    }
}

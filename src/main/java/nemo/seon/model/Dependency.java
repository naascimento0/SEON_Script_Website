package nemo.seon.model;

/**
 * Represents a dependency from one Package to another.
 * The source package is not stored here — this object is added to the source package's
 * dependencies list, so the source is navigated via the list owner.
 */
public class Dependency {
    private final Package target;
    private final String description;
    private final String level;

    /** The {@code source} parameter is accepted for context but not stored. */
    public Dependency(Package source, Package target, String description, String level) {
        this.target = target;
        this.description = description;
        this.level = level;
    }

    public Package getTarget() { return this.target; }
    public String getDescription() { return this.description; }
    public String getLevel() { return this.level; }
}

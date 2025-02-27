package nemo.seon.model;

public class Dependency {
    private final Package target;
    private final String description;
    private final String level;

    public Dependency(Package source, Package target, String description, String level) {
        this.target = target;
        this.description = description;
        this.level = level;
    }

    public Package getTarget() {
        return this.target;
    }

    public String getDescription() {
        return this.description;
    }

    public String getLevel() {
        return this.level;
    }
}

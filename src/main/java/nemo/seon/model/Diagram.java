package nemo.seon.model;

public class Diagram {

    private Package	pack;

    public Diagram(String name, String definition, DiagType type, String network) {
    }

    public enum DiagType {
        PACKAGE, CONCEPTUALMODEL, OTHER, IGNORE
    }

    public static DiagType getDiagramType(String type) {
        if (type != null) {
            switch (type) {
                case "CM":
                    return DiagType.CONCEPTUALMODEL;
                case "Package":
                    return DiagType.PACKAGE;
                case "Other":
                    return DiagType.OTHER;
                case "Ignore":
                    return DiagType.IGNORE;
            }
        }
        return DiagType.CONCEPTUALMODEL;
    }

    public void setPack(Package pack) {
        this.pack = pack;
    }
}

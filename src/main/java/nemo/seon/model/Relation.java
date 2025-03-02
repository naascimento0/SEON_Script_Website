package nemo.seon.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Relation {
    private final Concept source;
    private final Concept target;
    private static final List<Relation> relationsList	= new ArrayList<>();
    private final String name;
    private final boolean composition;
    private final String sourceMult;
    private final String targetMult;
    private final String stereotype;
    private final String definition;

    public Relation(String name, String definition, String stereotype, boolean composition, Package pack, Concept source, Concept target, String smult, String tmult) {
        this.source = source;
        this.target = target;
        if (!relationsList.contains(this))
            relationsList.add(this);
        this.name = name;
        this.composition = composition;
        this.sourceMult = smult;
        this.targetMult = tmult;
        this.stereotype = stereotype;
        this.definition = definition;
    }

    public Concept getSource() {
        return this.source;
    }

    public Concept getTarget() {
        return this.target;
    }

    /**
     * Create a relation between two concepts in string format
     * @return the relation in string format
     */
    @Override
    public String toString() {
        String rname = name;
        String smult = " ";
        String tmult = " ";
        String ster = "";
        String def = "";
        if (composition) rname = "<>--" + name;
        if (!sourceMult.isEmpty()) smult = " (" + sourceMult + ") ";
        if (!targetMult.isEmpty()) tmult = " (" + targetMult + ") ";
        if (stereotype != null) ster = "  &lt&lt" + stereotype + "&gt&gt";
        if (!definition.isEmpty()) def = " [" + def + "]";
        return source.getName() + smult + rname + tmult + target.getName() + ster + def;
    }
}

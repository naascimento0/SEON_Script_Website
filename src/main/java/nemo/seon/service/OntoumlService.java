package nemo.seon.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nemo.seon.model.Concept;
import nemo.seon.model.Ontology;
import nemo.seon.model.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Generates the OntoUML JSON of a single SEON ontology in the serialization read by the
 * Visual Paradigm OntoUML plugin (ontouml-vp-plugin 0.5.x).
 *
 * <p><b>Which serialization.</b> Two incompatible OntoUML JSON formats exist. The
 * w3id.org "OntoUML Schema v1.0.2" is a flat, id-referenced graph ({@code elements} +
 * {@code root}) used by the OntoUML/UFO catalog. The VP plugin does not read it: its
 * {@code ProjectDeserializer} only looks at {@code model} and {@code diagrams}, so a
 * v1.0.2 file imports as an empty project. This class emits the plugin's format:
 *
 * <ul>
 *   <li>{@code Project} carries a single nested {@code model} package (plus {@code diagrams});</li>
 *   <li>{@code Package.contents} holds <em>full nested objects</em>, not id strings;</li>
 *   <li>a binary relation has {@code "type": "Relation"} — {@code "BinaryRelation"} is
 *       silently dropped by the plugin's content switch;</li>
 *   <li>{@code propertyType}, {@code general} and {@code specific} are reference
 *       <em>objects</em> {@code {"id": ..., "type": ...}}, not id strings;</li>
 *   <li>a classifier's {@code properties} holds nested {@code Property} objects;</li>
 *   <li>custom data lives in {@code propertyAssignments}, not {@code customProperties};</li>
 *   <li>{@code order} is a number.</li>
 * </ul>
 *
 * <p>Every reference must resolve inside the file: the plugin's {@code ReferenceResolver}
 * throws on a dangling id and the whole import fails. {@code scripts/validate-ontouml.mjs}
 * checks exactly these rules.
 *
 * <p>This is the "walking skeleton": it emits Classes, Generalizations and binary Relations
 * (with their Properties) whose endpoints are both inside the target ontology.
 * Cross-ontology relations, generalization sets, datatype attributes and diagram layout are
 * intentionally out of scope for now.
 */
@Service
public class OntoumlService {

    private static final Logger logger = LoggerFactory.getLogger(OntoumlService.class);

    private final OntologyService ontologyService;
    private final ObjectMapper mapper = new ObjectMapper();

    public OntoumlService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    /** Serializes the OntoUML Project for the given ontology as pretty-printed JSON bytes. */
    public byte[] generateOntoumlJson(String ontologyName) throws JsonProcessingException {
        Map<String, Object> project = buildProject(ontologyName);
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(project);
    }

    /** Builds the OntoUML Project tree for the given ontology, or {@code null} if not found. */
    public Map<String, Object> buildProject(String ontologyName) {
        Ontology onto = resolveOntology(ontologyName);
        if (onto == null) return null;

        List<Concept> concepts = conceptsOf(onto);

        // Class ids are the referential backbone: a relation end or generalization end
        // pointing outside this set would make the plugin's ReferenceResolver throw.
        Map<Concept, String> classIds = new LinkedHashMap<>();
        for (Concept c : concepts) {
            String id = classId(c);
            if (id != null && !id.isBlank()) classIds.put(c, id);
        }

        List<Map<String, Object>> contents = new ArrayList<>();

        for (Map.Entry<Concept, String> entry : classIds.entrySet()) {
            contents.add(classElement(entry.getKey(), entry.getValue()));
        }

        // Generalizations (only when both ends are classes of this ontology)
        Set<String> seenGeneralizations = new LinkedHashSet<>();
        for (Concept specific : classIds.keySet()) {
            for (Concept general : specific.getGeneralizations()) {
                String generalId = classIds.get(general);
                String specificId = classIds.get(specific);
                if (generalId == null || generalId.equals(specificId)) continue;

                String id = "gen-" + specificId + "-" + generalId;
                if (!seenGeneralizations.add(id)) continue;
                contents.add(generalizationElement(id, specificId, generalId));
            }
        }

        // Binary relations (only when both endpoints are classes of this ontology)
        Set<String> seenRelations = new LinkedHashSet<>();
        for (Concept c : classIds.keySet()) {
            for (Relation r : c.getRelations()) {
                String sourceId = classIds.get(r.getSource());
                String targetId = classIds.get(r.getTarget());
                if (sourceId == null || targetId == null) continue;

                String relId = relationId(sourceId, targetId, r.getName());
                if (!seenRelations.add(relId)) continue;
                contents.add(relationElement(relId, r, sourceId, targetId));
            }
        }

        Map<String, Object> model = base("Package", "pkg-" + onto.getShortName(), onto.getShortName(), null);
        model.put("contents", contents.isEmpty() ? null : contents);

        Map<String, Object> project = new LinkedHashMap<>();
        project.put("id", "seon-" + onto.getShortName());
        project.put("name", languageString(onto.getShortName()));
        project.put("description", null);
        project.put("type", "Project");
        project.put("model", model);
        project.put("diagrams", null);

        logger.info("Built OntoUML project for {}: {} classes, {} model elements",
                onto.getShortName(), classIds.size(), contents.size());
        return project;
    }

    /**
     * Reports the stereotype coverage of an ontology: how many classes carry a known
     * OntoUML stereotype, which raw values appear, and which classes are missing/unknown.
     */
    public Map<String, Object> coverage(String ontologyName) {
        Ontology onto = resolveOntology(ontologyName);
        if (onto == null) return null;

        List<Concept> concepts = conceptsOf(onto);
        Map<String, Integer> rawCounts = new LinkedHashMap<>();
        List<String> missing = new ArrayList<>();
        List<String> unknown = new ArrayList<>();
        int mapped = 0;

        for (Concept c : concepts) {
            String raw = c.getStereotype();
            if (raw == null || raw.isBlank()) {
                missing.add(c.getName());
                continue;
            }
            rawCounts.merge(raw, 1, Integer::sum);
            if (OntoumlStereotypeMapper.isKnownClassStereotype(raw)) {
                mapped++;
            } else {
                unknown.add(c.getName() + " («" + raw + "»)");
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ontology", onto.getShortName());
        result.put("totalClasses", concepts.size());
        result.put("mappedToOntoUml", mapped);
        result.put("missingStereotype", missing);
        result.put("unknownStereotype", unknown);
        result.put("rawStereotypeCounts", rawCounts);
        return result;
    }

    // ---------------------------------------------------------------- builders

    private Map<String, Object> classElement(Concept c, String id) {
        String canonical = OntoumlStereotypeMapper.normalizeClassStereotype(c.getStereotype());
        List<String> natures = OntoumlStereotypeMapper.natureFor(canonical);

        Map<String, Object> cls = base("Class", id, c.getName(), c.getDefinition());
        cls.put("stereotype", canonical); // null when unmapped; the plugin then imports it bare
        cls.put("isAbstract", false);
        cls.put("isDerived", false);
        cls.put("properties", null); // attributes are out of scope for now
        cls.put("isExtensional", null);
        cls.put("isPowertype", false);
        cls.put("order", 1); // the plugin reads order as a number, not as "1"
        cls.put("literals", null);
        cls.put("restrictedTo", natures.isEmpty() ? null : natures);
        return cls;
    }

    private Map<String, Object> generalizationElement(String id, String specificId, String generalId) {
        Map<String, Object> gen = base("Generalization", id, null, null);
        gen.put("general", reference(generalId, "Class"));
        gen.put("specific", reference(specificId, "Class"));
        return gen;
    }

    private Map<String, Object> relationElement(String relId, Relation r, String sourceId, String targetId) {
        String canonical = OntoumlStereotypeMapper.normalizeRelationStereotype(r.getStereotype());
        String name = r.getName() == null || r.getName().isBlank() ? null : r.getName();

        // properties[0] is the source end, properties[1] the target end.
        // A composition is marked on the whole end, which is the source end here.
        Map<String, Object> sourceEnd = propertyElement(
                relId + "-e0", sourceId, r.getSourceMultiplicity(),
                r.isComposition() ? "COMPOSITE" : "NONE");
        Map<String, Object> targetEnd = propertyElement(
                relId + "-e1", targetId, r.getTargetMultiplicity(), "NONE");

        Map<String, Object> rel = base("Relation", relId, name, r.getDefinition());
        rel.put("stereotype", canonical);
        rel.put("isAbstract", false);
        rel.put("isDerived", false);
        rel.put("properties", List.of(sourceEnd, targetEnd));
        return rel;
    }

    private Map<String, Object> propertyElement(String id, String propertyTypeId,
                                                String cardinality, String aggregationKind) {
        Map<String, Object> p = base("Property", id, null, null);
        p.put("stereotype", null);
        p.put("isDerived", false);
        p.put("isReadOnly", false);
        p.put("isOrdered", false);
        p.put("cardinality", cardinality == null || cardinality.isBlank() ? null : cardinality);
        p.put("propertyType", reference(propertyTypeId, "Class"));
        p.put("subsettedProperties", null);
        p.put("redefinedProperties", null);
        p.put("aggregationKind", aggregationKind);
        return p;
    }

    /** Seeds the fields shared by every element: {@code id}, {@code name}, {@code description}, {@code type}. */
    private Map<String, Object> base(String type, String id, String name, String description) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", id);
        m.put("name", languageString(name));
        m.put("description", languageString(description));
        m.put("type", type);
        m.put("propertyAssignments", null);
        return m;
    }

    /** An {@code {"id", "type"}} reference object, the only link form the plugin resolves. */
    private Map<String, Object> reference(String id, String type) {
        Map<String, Object> ref = new LinkedHashMap<>();
        ref.put("id", id);
        ref.put("type", type);
        return ref;
    }

    private Map<String, Object> languageString(String value) {
        if (value == null || value.isBlank()) return null;
        Map<String, Object> ls = new LinkedHashMap<>();
        ls.put("en", value);
        return ls;
    }

    // ---------------------------------------------------------------- helpers

    private String classId(Concept c) {
        return c.getAstahConceptObject() == null ? null : c.getAstahConceptObject().getId();
    }

    private String relationId(String sourceId, String targetId, String name) {
        String suffix = name == null ? "" : name.replaceAll("\\s+", "+");
        return "rel-" + sourceId + "-" + targetId + "-" + suffix;
    }

    private Ontology resolveOntology(String name) {
        if (name == null) return null;
        Ontology direct = ontologyService.findByName(name);
        if (direct != null) return direct;
        for (Ontology o : ontologyService.getAllOntologies()) {
            if (name.equalsIgnoreCase(o.getShortName()) || name.equalsIgnoreCase(o.getName())) {
                return o;
            }
        }
        return null;
    }

    private List<Concept> conceptsOf(Ontology onto) {
        List<Concept> result = new ArrayList<>();
        for (Concept c : ontologyService.getRegistry().getAllConcepts()) {
            if (c.getMainOntology() == onto) result.add(c);
        }
        return result;
    }
}

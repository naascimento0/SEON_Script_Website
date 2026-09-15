package nemo.seon.service;

import java.util.List;
import java.util.Map;

/**
 * Maps SEON's Astah stereotypes to the canonical OntoUML vocabulary and infers the
 * ontological nature ({@code restrictedTo}) of a class from its class stereotype.
 *
 * <p>The Astah model already uses OntoUML-like stereotypes (e.g., {@code kind},
 * {@code relator}, {@code role}), but with inconsistent casing (e.g., {@code Kind},
 * {@code Rolemixin}). This mapper normalizes them; explicit {@code ontoUML} tagged
 * values, when present, take precedence and are looked up before calling this mapper.
 */
final class OntoumlStereotypeMapper {

    private OntoumlStereotypeMapper() {}

    /** Canonical OntoUML class stereotypes, keyed by their lower-cased form. */
    private static final Map<String, String> CLASS_STEREOTYPES = Map.ofEntries(
            Map.entry("kind", "kind"),
            Map.entry("subkind", "subkind"),
            Map.entry("phase", "phase"),
            Map.entry("role", "role"),
            Map.entry("collective", "collective"),
            Map.entry("quantity", "quantity"),
            Map.entry("relator", "relator"),
            Map.entry("mode", "mode"),
            Map.entry("quality", "quality"),
            Map.entry("category", "category"),
            Map.entry("mixin", "mixin"),
            Map.entry("rolemixin", "roleMixin"),
            Map.entry("phasemixin", "phaseMixin"),
            Map.entry("event", "event"),
            Map.entry("situation", "situation"),
            Map.entry("type", "type"),
            Map.entry("historicalrole", "historicalRole"),
            Map.entry("historicalrolemixin", "historicalRoleMixin"),
            Map.entry("abstract", "abstract"),
            Map.entry("datatype", "datatype"),
            Map.entry("enumeration", "enumeration")
    );

    /**
     * Maps each canonical class stereotype to the ontological nature(s) it implies
     * ({@code restrictedTo}). Non-sortals (category/mixin/...) and object sortals default
     * to {@code functional-complex}; this is a first-pass heuristic that can be overridden
     * later by a {@code nature} tagged value.
     */
    private static final Map<String, List<String>> NATURE_BY_STEREOTYPE = Map.ofEntries(
            Map.entry("kind", List.of("functional-complex")),
            Map.entry("subkind", List.of("functional-complex")),
            Map.entry("phase", List.of("functional-complex")),
            Map.entry("role", List.of("functional-complex")),
            Map.entry("category", List.of("functional-complex")),
            Map.entry("mixin", List.of("functional-complex")),
            Map.entry("roleMixin", List.of("functional-complex")),
            Map.entry("phaseMixin", List.of("functional-complex")),
            Map.entry("historicalRole", List.of("functional-complex")),
            Map.entry("historicalRoleMixin", List.of("functional-complex")),
            Map.entry("collective", List.of("collective")),
            Map.entry("quantity", List.of("quantity")),
            Map.entry("relator", List.of("relator")),
            Map.entry("mode", List.of("intrinsic-mode")),
            Map.entry("quality", List.of("quality")),
            Map.entry("event", List.of("event")),
            Map.entry("situation", List.of("situation")),
            Map.entry("type", List.of("type")),
            Map.entry("abstract", List.of("abstract"))
    );

    /** Canonical OntoUML relation stereotypes, keyed by their lower-cased form. */
    private static final Map<String, String> RELATION_STEREOTYPES = Map.ofEntries(
            Map.entry("material", "material"),
            Map.entry("comparative", "comparative"),
            Map.entry("mediation", "mediation"),
            Map.entry("characterization", "characterization"),
            Map.entry("externaldependence", "externalDependence"),
            Map.entry("componentof", "componentOf"),
            Map.entry("memberof", "memberOf"),
            Map.entry("subcollectionof", "subCollectionOf"),
            Map.entry("subquantityof", "subQuantityOf"),
            Map.entry("instantiation", "instantiation"),
            Map.entry("termination", "termination"),
            Map.entry("participational", "participational"),
            Map.entry("participation", "participation"),
            Map.entry("historicaldependence", "historicalDependence"),
            Map.entry("creation", "creation"),
            Map.entry("manifestation", "manifestation"),
            Map.entry("bringsabout", "bringsAbout"),
            Map.entry("triggers", "triggers"),
            // «formal» is not in the plugin's RelationStereotype enum (it was superseded by
            // «comparative»), so it is left unmapped rather than emitted and silently ignored.
            Map.entry("derivation", "derivation")
    );

    /** Returns the canonical class stereotype, or {@code null} if unknown/blank. */
    static String normalizeClassStereotype(String raw) {
        if (raw == null) return null;
        return CLASS_STEREOTYPES.get(raw.trim().toLowerCase());
    }

    /** Returns the canonical relation stereotype, or {@code null} if unknown/blank. */
    static String normalizeRelationStereotype(String raw) {
        if (raw == null) return null;
        return RELATION_STEREOTYPES.get(raw.trim().toLowerCase());
    }

    /**
     * Infers {@code restrictedTo} from a canonical class stereotype, returning an empty
     * list when the nature cannot be determined.
     */
    static List<String> natureFor(String canonicalStereotype) {
        if (canonicalStereotype == null) return List.of();
        return NATURE_BY_STEREOTYPE.getOrDefault(canonicalStereotype, List.of());
    }

    /** True when the raw stereotype maps to a known canonical OntoUML class stereotype. */
    static boolean isKnownClassStereotype(String raw) {
        return normalizeClassStereotype(raw) != null;
    }
}

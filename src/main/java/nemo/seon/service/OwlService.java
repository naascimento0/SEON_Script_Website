package nemo.seon.service;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import nemo.seon.model.Concept;
import nemo.seon.model.Ontology;
import nemo.seon.model.Relation;
import nemo.seon.model.SeonRegistry;

/**
 * Generates an OWL file representing the full SEON ontology network from the in-memory model.
 * Ported from the legacy seon2html OwlWriter so the OWL download remains available
 * in the Spring Boot version of the SEON site.
 */
@Service
public class OwlService {

    private static final Logger logger = LoggerFactory.getLogger(OwlService.class);
    private static final String NAMESPACE = "http://dev.nemo.inf.ufes.br/seon2/SEON.owl#";

    private static final boolean INCLUDE_FOUNDATIONAL = false;
    private static final boolean INCLUDE_CORE = true;
    private static final boolean INCLUDE_DOMAIN = true;

    private final OntologyService ontologyService;

    public OwlService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    /** Builds the SEON OWL ontology in memory and returns the serialized RDF/XML bytes. */
    public byte[] generateSeonOwl() {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLOntology ontology;
        try {
            ontology = manager.createOntology(IRI.create(NAMESPACE));
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Failed to create OWL ontology", e);
        }

        SeonRegistry registry = ontologyService.getRegistry();
        List<Concept> concepts = registry.getAllConcepts();

        int classes = generateClasses(manager, factory, ontology, concepts);
        int generalizations = generateGeneralizations(manager, factory, ontology, concepts);
        int relations = generateRelations(manager, factory, ontology, concepts);
        logger.info("Generated SEON.owl: {} classes, {} generalizations, {} object properties",
                classes, generalizations, relations);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            manager.saveOntology(ontology, out);
            return out.toByteArray();
        } catch (OWLOntologyStorageException | java.io.IOException e) {
            throw new RuntimeException("Failed to serialize OWL ontology", e);
        }
    }

    private int generateClasses(OWLOntologyManager manager, OWLDataFactory factory, OWLOntology ontology, List<Concept> concepts) {
        int count = 0;
        for (Concept concept : concepts) {
            if (!isIncluded(concept)) continue;

            OWLClass owlCls = getOwlClass(factory, concept);
            OWLDeclarationAxiom declarationAxiom = factory.getOWLDeclarationAxiom(owlCls);
            manager.addAxiom(ontology, declarationAxiom);
            count++;

            Ontology main = concept.getMainOntology();
            if (main != null && main.getShortName() != null) {
                OWLAnnotationValue value = factory.getOWLLiteral(main.getShortName());
                OWLAnnotation annotation = factory.getOWLAnnotation(
                        factory.getOWLAnnotationProperty(IRI.create(NAMESPACE + "ontology")), value);
                OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(owlCls.getIRI(), annotation);
                manager.applyChange(new AddAxiom(ontology, axiom));
            }

            String stereotype = concept.getStereotype();
            if (stereotype != null && !stereotype.isEmpty()) {
                OWLAnnotationValue value = factory.getOWLLiteral(stereotype);
                OWLAnnotation annotation = factory.getOWLAnnotation(
                        factory.getOWLAnnotationProperty(IRI.create(NAMESPACE + "stereotype")), value);
                OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(owlCls.getIRI(), annotation);
                manager.applyChange(new AddAxiom(ontology, axiom));
            }

            String definition = concept.getDefinition();
            if (definition != null && !definition.isEmpty()) {
                String comment = definition.replaceAll("<[^>]*>", "").replace("\"", "");
                comment = Normalizer.normalize(comment, Normalizer.Form.NFD);
                comment = comment.replaceAll("[^\\p{ASCII}]", "").replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", " ");
                OWLAnnotation commentAnno = factory.getOWLAnnotation(factory.getRDFSComment(), factory.getOWLLiteral(comment));
                OWLAxiom axiom = factory.getOWLAnnotationAssertionAxiom(owlCls.getIRI(), commentAnno);
                manager.applyChange(new AddAxiom(ontology, axiom));
            }
        }
        return count;
    }

    private int generateGeneralizations(OWLOntologyManager manager, OWLDataFactory factory, OWLOntology ontology, List<Concept> concepts) {
        int count = 0;
        for (Concept concept : concepts) {
            if (!isIncluded(concept)) continue;
            for (Concept general : concept.getGeneralizations()) {
                if (!isIncluded(general)) continue;
                OWLClass son = getOwlClass(factory, concept);
                OWLClass father = getOwlClass(factory, general);
                OWLAxiom axiom = factory.getOWLSubClassOfAxiom(son, father);
                manager.applyChange(new AddAxiom(ontology, axiom));
                count++;
            }
        }
        return count;
    }

    private int generateRelations(OWLOntologyManager manager, OWLDataFactory factory, OWLOntology ontology, List<Concept> concepts) {
        int count = 0;
        Set<Relation> seen = new HashSet<>();
        for (Concept concept : concepts) {
            for (Relation relation : concept.getRelations()) {
                if (!seen.add(relation)) continue;
                Concept src = relation.getSource();
                Concept dst = relation.getTarget();
                if (src == null || dst == null) continue;
                if (!isIncluded(src) || !isIncluded(dst)) continue;

                String name = relation.getName() == null ? "" : relation.getName();
                if (name.isEmpty() && relation.isComposition()) {
                    name = "composed of";
                }
                String localName = name + "." + src.getName() + "+" + dst.getName();
                OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(NAMESPACE + sanitizeIriFragment(localName)));

                OWLClass owlSrc = getOwlClass(factory, src);
                OWLClass owlDst = getOwlClass(factory, dst);

                manager.applyChange(new AddAxiom(ontology, factory.getOWLObjectPropertyDomainAxiom(prop, owlSrc)));
                manager.applyChange(new AddAxiom(ontology, factory.getOWLObjectPropertyRangeAxiom(prop, owlDst)));
                count++;
            }
        }
        return count;
    }

    private OWLClass getOwlClass(OWLDataFactory factory, Concept concept) {
        Ontology main = concept.getMainOntology();
        String shortName = main != null && main.getShortName() != null ? main.getShortName() : "Unknown";
        String name = shortName + "::" + concept.getName();
        return factory.getOWLClass(IRI.create(NAMESPACE + sanitizeIriFragment(name)));
    }

    /** Keeps only characters safe in an RDF/XML IRI fragment; everything else becomes '_'. */
    private static String sanitizeIriFragment(String raw) {
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            boolean safe = (c >= 'A' && c <= 'Z')
                    || (c >= 'a' && c <= 'z')
                    || (c >= '0' && c <= '9')
                    || c == '_' || c == '-' || c == '.' || c == '+' || c == ':';
            sb.append(safe ? c : '_');
        }
        return sb.toString();
    }

    private boolean isIncluded(Concept concept) {
        if (concept == null || concept.getMainOntology() == null) return false;
        Ontology.OntoLevel level = concept.getMainOntology().getLevel();
        if (level == null) return false;
        return switch (level) {
            case FOUNDATIONAL -> INCLUDE_FOUNDATIONAL;
            case CORE -> INCLUDE_CORE;
            case DOMAIN -> INCLUDE_DOMAIN;
        };
    }
}

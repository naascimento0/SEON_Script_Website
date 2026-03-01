package nemo.seon.service;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import nemo.seon.model.*;
import nemo.seon.model.Package;
import nemo.seon.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service responsible for building view DTOs for ontology page rendering.
 * Replaces the old OntologiesWriter HTML-generation approach with structured data
 * that Thymeleaf templates can render directly.
 */
@Service
public class OntologyViewService {

    private static final Logger logger = LoggerFactory.getLogger(OntologyViewService.class);

    private final OntologyService ontologyService;

    @Autowired
    public OntologyViewService(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
    }

    private SeonRegistry getRegistry() {
        return ontologyService.getRegistry();
    }

    // ======================== Ontology Level ========================

    /**
     * Returns the ontology level label with icon identifier for the template.
     * e.g., "star" / "star-half" / "star-fill" + text.
     */
    public String getOntologyLevelIcon(Ontology ontology) {
        Ontology.OntoLevel level = ontology.getLevel();
        if (level == null) return null;
        return switch (level) {
            case FOUNDATIONAL -> "star";
            case CORE -> "star-half";
            case DOMAIN -> "star-fill";
        };
    }

    public String getOntologyLevelText(Ontology ontology) {
        Ontology.OntoLevel level = ontology.getLevel();
        if (level == null) return "";
        return switch (level) {
            case FOUNDATIONAL -> "Foundational Ontology";
            case CORE -> isFromSeon(ontology) ? "Core Ontology from SEON" : "Core Ontology";
            case DOMAIN -> isFromSeon(ontology) ? "Domain Ontology from SEON" : "Domain Ontology";
        };
    }

    private boolean isFromSeon(Ontology ontology) {
        return "SEON".equals(ontology.getNetwork());
    }

    // ======================== Description ========================

    /**
     * Formats a description for display. Replaces line breaks with <br/>.
     * Returns null if the description is empty.
     */
    public String formatDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return null;
        }
        return description.replaceAll("\\R", "<br/>");
    }

    // ======================== Dependencies ========================

    public List<DependencyView> buildDependencies(Ontology ontology) {
        if (ontology == null || ontology.getDependencies() == null || ontology.getDependencies().isEmpty()) {
            return Collections.emptyList();
        }

        List<DependencyView> views = new ArrayList<>();
        for (Dependency dep : ontology.getDependencies()) {
            if (dep == null || dep.getTarget() == null) continue;
            Ontology supplier = (Ontology) dep.getTarget();
            String url = networkedOntoURL(supplier.getShortName());
            boolean isSeon = "SEON".equals(supplier.getNetwork());
            String desc = formatDescription(dep.getDescription());
            String level = dep.getLevel() != null ? dep.getLevel() : "N/A";

            views.add(new DependencyView(
                    supplier.getShortName(),
                    supplier.getFullName(),
                    url,
                    isSeon,
                    desc,
                    level
            ));
        }
        return views;
    }

    // ======================== Diagrams ========================

    /**
     * Builds a list of DiagramView DTOs for the given package's diagrams.
     * The figureCounter is an AtomicInteger shared across all diagram-building calls
     * for a single page render, so figure numbering is consistent.
     */
    public List<DiagramView> buildDiagrams(Package pack, AtomicInteger figureCounter) {
        if (pack == null || pack.getDiagrams() == null || pack.getDiagrams().isEmpty()) {
            return Collections.emptyList();
        }

        List<DiagramView> views = new ArrayList<>();
        for (Diagram diag : pack.getDiagrams()) {
            if (diag == null || diag.getType() == Diagram.DiagType.IGNORE) continue;

            String name = diag.getName() != null ? diag.getName() : "Unnamed Diagram";
            String introText;
            String labelText;
            boolean simpleImage = false;
            String imageSrc;
            int imageWidth = 0;
            String mapName = name;
            List<MapArea> mapAreas = Collections.emptyList();

            switch (diag.getType()) {
                case CONCEPTUALMODEL:
                    introText = "conceptual model of the " + name +
                            (pack.getPackageType() == Package.PackType.SUBONTOLOGY ? " subontology" : "");
                    labelText = name + " Conceptual Model";
                    imageSrc = "/images/" + buildImagePath(diag.getPack(), name) + ".png";
                    if (diag.getDiagramAstahObject() != null) {
                        imageWidth = (int) Math.round(diag.getDiagramAstahObject().getBoundRect().getWidth());
                    }
                    mapAreas = buildConceptualModelMap(diag);
                    break;
                case PACKAGE:
                    introText = "packages of the " + name;
                    labelText = name;
                    imageSrc = "/images/" + buildImagePath(diag.getPack(), name) + ".png";
                    if (diag.getDiagramAstahObject() != null) {
                        imageWidth = (int) Math.round(diag.getDiagramAstahObject().getBoundRect().getWidth());
                    }
                    mapAreas = buildPackageMap(diag);
                    break;
                case OTHER:
                    introText = name;
                    labelText = name;
                    imageSrc = "/images/" + name + ".png";
                    simpleImage = true;
                    break;
                default:
                    continue;
            }

            String description = diag.getDefinition() != null
                    ? formatDescription(diag.getDefinition()
                        .replaceAll("<ax>", "<code class=\"text-muted\">")
                        .replaceAll("</ax>", "</code>"))
                    : null;

            int figNum = figureCounter.getAndIncrement();
            views.add(new DiagramView(
                    figNum, introText, labelText, description,
                    imageSrc, imageWidth, mapName, mapAreas, simpleImage
            ));
        }
        return views;
    }

    // ======================== Image Maps ========================

    private List<MapArea> buildConceptualModelMap(Diagram diagram) {
        if (diagram == null || diagram.getDiagramAstahObject() == null) return Collections.emptyList();

        IDiagram aDiagram = diagram.getDiagramAstahObject();
        List<MapArea> areas = new ArrayList<>();

        try {
            for (IPresentation present : aDiagram.getPresentations()) {
                if (present instanceof INodePresentation node && "Class".equals(present.getType())) {
                    Concept concept = getRegistry().getConceptByFullName(((IClass) node.getModel()).getFullName("::"));
                    if (concept == null || concept.getOntology() == null) continue;

                    String coords = getMapCoords(node, aDiagram.getBoundRect());
                    String reference = concept.getReference();
                    String target = "";
                    Ontology whatOnto = concept.getOntology();
                    if ("SEON".equals(whatOnto.getNetwork())) {
                        String onURL = networkedOntoURL(whatOnto.getMainOntology().getShortName());
                        reference = onURL + "#" + whatOnto.getMainOntology().getShortName() + "_"
                                + concept.getName().replace(' ', '+');
                        target = "_blank";
                    }

                    areas.add(new MapArea(coords, reference, target,
                            concept.getDefinition() != null ? concept.getDefinition() : ""));
                }
            }
        } catch (InvalidUsingException e) {
            logger.error("Error building conceptual model map for diagram: {}", diagram.getName(), e);
        }
        return areas;
    }

    private List<MapArea> buildPackageMap(Diagram diagram) {
        if (diagram == null || diagram.getDiagramAstahObject() == null) return Collections.emptyList();

        IDiagram aDiagram = diagram.getDiagramAstahObject();
        List<MapArea> areas = new ArrayList<>();

        try {
            List<IPresentation> presentations = Arrays.stream(aDiagram.getPresentations())
                    .filter(p -> "Package".equals(p.getType()))
                    .sorted((p1, p2) -> {
                        String fname1 = ((IPackage) p1.getModel()).getFullName(":");
                        String fnamed1 = ((IPackage) p1.getModel()).getFullName("::");
                        int packs1 = fnamed1.length() - fname1.length();
                        String fname2 = ((IPackage) p2.getModel()).getFullName(":");
                        String fnamed2 = ((IPackage) p2.getModel()).getFullName("::");
                        int packs2 = fnamed2.length() - fname2.length();
                        return packs2 - packs1;
                    })
                    .toList();

            for (IPresentation present : presentations) {
                INodePresentation node = (INodePresentation) present;
                Package pack = getRegistry().getPackageByFullName(((IPackage) node.getModel()).getFullName("::"));
                if (pack == null) continue;

                String coords = getMapCoords(node, aDiagram.getBoundRect());
                String reference = pack.getReference() + "_section";
                String target = "";
                String network = pack.getNetwork();
                if (pack.getName().contains("Layer") || pack.getPackageType() == Package.PackType.NETWORK) {
                    reference = networkedOntoURL(network);
                    target = "SEON".equals(network) ? "_blank" : "";
                } else {
                    Ontology mainOnto = pack.getMainOntology();
                    if (mainOnto != null && "SEON".equals(mainOnto.getNetwork())) {
                        String mainOntoName = mainOnto.getShortName();
                        reference = networkedOntoURL(mainOntoName) + "#" + mainOntoName + "_"
                                + pack.getName().replace(' ', '+') + "_section";
                        target = "_blank";
                    }
                }

                areas.add(new MapArea(coords, reference, target, ""));
            }
        } catch (InvalidUsingException e) {
            logger.error("Error building package map for diagram: {}", diagram.getName(), e);
        }
        return areas;
    }

    // ======================== Sections (Subpackages) ========================

    public List<SectionView> buildSections(Package seonNetwork, String sectionNumber, AtomicInteger figureCounter) {
        if (seonNetwork == null || seonNetwork.getSubpacks() == null || seonNetwork.getSubpacks().isEmpty()) {
            return Collections.emptyList();
        }

        List<Package> subpacks = new ArrayList<>(seonNetwork.getSubpacks());
        Collections.sort(subpacks);
        List<SectionView> sections = new ArrayList<>();

        int subNum = 1;
        for (Package pack : subpacks) {
            if (pack == null) continue;

            int levelDiff = pack.getPackLevel() - (pack.getMainOntology() != null ? pack.getMainOntology().getPackLevel() : 0);
            int headingLevel = Math.min(3 + levelDiff, 6);

            String num = sectionNumber + subNum + ".";
            String sectionRef = pack.getLabel() + "_section";
            String sectionName = pack.getName() != null ? pack.getName() : "Unnamed Section";
            String description = formatDescription(pack.getDefinition());

            List<DiagramView> diagrams = buildDiagrams(pack, figureCounter);
            List<SectionView> subsections = buildSections(pack, num, figureCounter);

            sections.add(new SectionView(num, headingLevel, sectionRef, sectionName,
                    description, diagrams, subsections));
            subNum++;
        }
        return sections;
    }

    // ======================== Concepts Table ========================

    public List<ConceptRow> buildConceptRows(Ontology ontology) {
        if (ontology == null || ontology.getAllConcepts() == null || ontology.getAllConcepts().isEmpty()) {
            return Collections.emptyList();
        }

        List<Concept> concepts = new ArrayList<>(ontology.getAllConcepts());
        Collections.sort(concepts);
        List<ConceptRow> rows = new ArrayList<>();

        for (Concept concept : concepts) {
            if (concept == null) continue;

            String name = concept.getName() != null ? concept.getName() : "Unnamed";
            String styleClass = switch (ontology.getLevel() != null ? ontology.getLevel() : Ontology.OntoLevel.DOMAIN) {
                case FOUNDATIONAL -> "fst-italic";
                case CORE -> "fst-italic fw-bold";
                case DOMAIN -> "fw-bold";
            };

            String label = concept.getLabel() != null ? concept.getLabel() : "unknown";
            String detailLabel = label + "_detail";
            String definition = concept.getDefinition() != null
                    ? concept.getDefinition().replaceAll("\\R", "") : "No definition provided";
            String example = concept.getExample() != null
                    ? concept.getExample().replaceAll("\\R", "") : null;
            String source = concept.getSourceDefinition() != null
                    ? concept.getSourceDefinition().replaceAll("\\R", "") : null;

            rows.add(new ConceptRow(name, styleClass, label, detailLabel, definition, example, source));
        }
        return rows;
    }

    // ======================== Detailed Concepts ========================

    public List<ConceptDetail> buildConceptDetails(Ontology ontology) {
        if (ontology == null || ontology.getAllConcepts() == null || ontology.getAllConcepts().isEmpty()) {
            return Collections.emptyList();
        }

        List<Concept> concepts = new ArrayList<>(ontology.getAllConcepts());
        Collections.sort(concepts);
        List<ConceptDetail> details = new ArrayList<>();

        for (Concept concept : concepts) {
            if (concept == null) continue;

            String stereotype = concept.getStereotype() != null && !concept.getStereotype().isEmpty()
                    ? concept.getStereotype() : null;
            String definition = concept.getDefinition() != null
                    ? concept.getDefinition().replaceAll("\\R", "") : "No definition provided";
            String example = concept.getExample() != null
                    ? concept.getExample().replaceAll("\\R", "") : null;
            String source = concept.getSourceDefinition() != null
                    ? concept.getSourceDefinition().replaceAll("\\R", "") : null;

            // Unique generalizations
            Set<String> uniqueGenerals = new LinkedHashSet<>();
            if (concept.getGeneralizations() != null) {
                for (Concept general : concept.getGeneralizations()) {
                    if (general != null && general.getFullName() != null) {
                        uniqueGenerals.add(general.getFullName());
                    }
                }
            }

            // Unique relations
            Set<String> uniqueRelations = new LinkedHashSet<>();
            if (concept.getRelations() != null) {
                for (Relation relation : concept.getRelations()) {
                    if (relation != null) {
                        String text = relation.toString();
                        if (text != null && !text.trim().isEmpty()) {
                            uniqueRelations.add(text);
                        }
                    }
                }
            }

            String label = concept.getLabel() != null ? concept.getLabel() : "unknown";
            String fullName = concept.getFullName() != null ? concept.getFullName() : "Unnamed";
            String name = concept.getName() != null ? concept.getName() : "Unnamed";

            details.add(new ConceptDetail(
                    fullName, name, stereotype,
                    label + "_detail", definition, example, source,
                    new ArrayList<>(uniqueGenerals),
                    new ArrayList<>(uniqueRelations)
            ));
        }
        return details;
    }

    // ======================== Utility ========================

    private String buildImagePath(Package pack, String diagramName) {
        StringBuilder path = new StringBuilder("astah_seon/");
        List<String> hierarchy = new ArrayList<>();
        Package current = pack;
        while (current != null) {
            String packName = current.getName() != null ? current.getName() : "Unnamed";
            hierarchy.addFirst(packName);
            current = current.getParent();
        }
        if (!hierarchy.isEmpty() && "astah_seon".equals(hierarchy.getFirst())) {
            hierarchy.removeFirst();
        }
        for (String folder : hierarchy) {
            path.append(folder).append("/");
        }
        path.append(diagramName);
        return path.toString();
    }

    private String getMapCoords(INodePresentation node, Rectangle2D adjust) {
        if (node == null || adjust == null) return "0,0,0,0";
        int x = (int) Math.round(node.getLocation().getX() - adjust.getX());
        int y = (int) Math.round(node.getLocation().getY() - adjust.getY());
        int w = (int) Math.round(node.getWidth());
        int h = (int) Math.round(node.getHeight());
        return String.format("%d,%d,%d,%d", x, y, x + w, y + h);
    }

    private static String networkedOntoURL(String network) {
        if (network == null || network.trim().isEmpty()) return "#";

        Map<String, String> urlMap = Map.ofEntries(
                Map.entry("UFO", "UFO.html"),
                Map.entry("SEON", "https://dev.nemo.inf.ufes.br/seon/"),
                Map.entry("COM", "COM.html"),
                Map.entry("EO", "EO.html"),
                Map.entry("SysSwO", "SysSwO.html"),
                Map.entry("RSRO", "RSRO.html"),
                Map.entry("RRO", "RRO.html"),
                Map.entry("GORO", "GORO.html"),
                Map.entry("RDPO", "RDPO.html"),
                Map.entry("DPO", "DPO.html"),
                Map.entry("CPO", "CPO.html"),
                Map.entry("ROoST", "ROoST.html"),
                Map.entry("QAPO", "QAPO.html"),
                Map.entry("SPMO", "SPMO.html"),
                Map.entry("CMPO", "CMPO.html"),
                Map.entry("RSMO", "RSMO.html"),
                Map.entry("SDRO", "SDRO.html")
        );
        return urlMap.getOrDefault(network, "#");
    }
}

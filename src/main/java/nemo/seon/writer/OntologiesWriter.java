package nemo.seon.writer;

import com.change_vision.jude.api.inf.exception.InvalidUsingException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDiagram;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.presentation.IPresentation;
import nemo.seon.model.*;
import nemo.seon.model.Package;
import org.apache.commons.io.FileUtils;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class OntologiesWriter {
    private int figCount;
    public void writeOntologies(Package seonNetwork) {
        for(Package pack : seonNetwork.getSubpacks()) {
            if(pack.getPackageType() == Package.PackType.ONTOLOGY) {
                System.out.println("Writing Ontology: " + pack.getName());
                writeOntologyPage((Ontology) pack);
            } else {
                writeOntologies(pack);
            }
        }
    }

    private void writeOntologyPage(Ontology ontology) {

        // Lendo o template HTML
        String html = fileToString("./static/TemplateOntologyPage.html");
        if (html == null) {
            System.out.println("Failed to load template for ontology: " + ontology.getName());
            return;
        }

        String onLevel = "";
        String ontoLevel = "";

        // Constantes e ícones do Bootstrap 5
        final String STAR_EMPTY = "<i class=\"bi bi-star\"></i>";
        final String STAR_HALF = "<i class=\"bi bi-star-half\"></i>";
        final String STAR_FULL = "<i class=\"bi bi-star-fill\"></i>";

        figCount = 1;

        Ontology.OntoLevel level = ontology.getLevel();
        if (level != null) {
            switch (level) {
                case FOUNDATIONAL:
                    ontoLevel = STAR_EMPTY + " Foundational Ontology";
                    onLevel = "Foundational Ontology";
                    break;
                case CORE:
                    if (ontology.getNetwork() != null && ontology.getNetwork().equals("SEON")) {
                        ontoLevel = STAR_HALF + " Core Ontology from SEON";
                        onLevel = "Core Ontology from SEON";
                    } else {
                        System.out.println("Network not recognized: " + ontology.getNetwork());
                        ontoLevel = STAR_HALF + " Core Ontology";
                        onLevel = "Core Ontology";
                    }
                    break;
                case DOMAIN:
                    if (ontology.getNetwork() != null && ontology.getNetwork().equals("SEON")) {
                        ontoLevel = STAR_FULL + " Domain Ontology from SEON";
                        onLevel = "Domain Ontology from SEON";
                    } else {
                        System.out.println("Network not recognized: " + ontology.getNetwork());
                        ontoLevel = STAR_FULL + " Domain Ontology";
                        onLevel = "Domain Ontology";
                    }
                    break;
                default:
                    System.out.println("Unknown ontology level: " + level);

            }
        }

        // Informações adicionais (status e versão)
        String additionalInfo;
        String addInfo;
        String status = /*onto.getStatus() != null ? onto.getStatus() :*/ "Unknown";
        String version = /*onto.getVersion() != null ? onto.getVersion() :*/ "N/A";
        additionalInfo = "<div class=\"container-fluid d-flex justify-content-end\"><span class=\"badge bg-danger text-lowercase\">" + status + "</span></div>";
        addInfo = status;

        // Substituindo os placeholders no template
        html = html.replace("@additionalinfo", additionalInfo);
        html = html.replace("@title", ontology.getFullName() + " (" + ontology.getShortName() + ")");
        html = html.replace("@onto_level", ontoLevel);
        html = html.replace("@description", formatDescription(ontology.getDefinition()));
        html = html.replace("@myontologyDependencies", generateDependenciesTable(ontology));

        String ontoDiags = generateDiagramStructures(ontology);
        String ontoPacks = generateSectionStructures(ontology, "3.");
        html = html.replace("@sectionContent", ontoDiags + ontoPacks);

        html = html.replace("@conceptDefinitions", generateConceptsTable(ontology));
        html = html.replace("@detailedConcepts", generateDetailedConcepts(ontology));

        // Footer
        html = html.replace("@onto", ontology.getShortName());
        html = html.replace("@onlyname", ontology.getFullName());
        html = html.replace("@onlevel", onLevel);
        html = html.replace("@addinfo", addInfo);
        html = html.replace("@currentYear", String.valueOf(java.time.Year.now().getValue()));
        html = html.replace("@date", new Date().toString());

        // Gravando o arquivo HTML gerado
        stringToFile("./page/" + ontology.getShortName() + ".html", html);
    }

    private String formatDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            return "<span class=\"text-danger fw-bold\">No definition available</span>";
        }
        return description.replaceAll("\\R", "<br/>");
    }

    private String generateDependenciesTable(Ontology ontology) {
        if (ontology == null || ontology.getDependencies() == null || ontology.getDependencies().isEmpty()) {
            return "<tr><td colspan=\"3\" class=\"text-center\">No dependencies available</td></tr>";
        }

        StringBuilder table = new StringBuilder();
        for (Dependency depend : ontology.getDependencies()) {
            if (depend == null || depend.getTarget() == null) continue;

            Ontology supplier = (Ontology) depend.getTarget();
            String url = networkedOntoURL(supplier.getShortName());
            boolean isSeon = "SEON".equals(supplier.getNetwork());
            String targetAttr = isSeon ? " target=\"_blank\"" : "";

            table.append("<tr>")
                    .append("<td><a class=\"text-dark\" href=\"").append(url).append("\"").append(targetAttr).append(">")
                    .append(supplier.getShortName()).append(" - ").append(supplier.getFullName())
                    .append("</a></td>")
                    .append("<td>").append(formatDescription(depend.getDescription())).append("</td>")
                    .append("<td class=\"text-center\">").append(depend.getLevel() != null ? depend.getLevel() : "N/A").append("</td>")
                    .append("</tr>\n");
        }

        return table.toString();
    }

    private static String networkedOntoURL(String network) {
        if (network == null || network.trim().isEmpty()) {
            return "#"; // Link vazio padrão
        }

        Map<String, String> urlMap = new HashMap<>();
        urlMap.put("UFO", "UFO.html");
        urlMap.put("SEON", "https://dev.nemo.inf.ufes.br/seon/");
        urlMap.put("COM", "COM.html");
        urlMap.put("EO", "EO.html");
        urlMap.put("SysSwO", "SysSwO.html");
        urlMap.put("RSRO", "RSRO.html");
        urlMap.put("RRO", "RRO.html");
        urlMap.put("GORO", "GORO.html");
        urlMap.put("RDPO", "RDPO.html");
        urlMap.put("DPO", "DPO.html");
        urlMap.put("CPO", "CPO.html");
        urlMap.put("ROoST", "ROoST.html");
        urlMap.put("QAPO", "QAPO.html");
        urlMap.put("SPMO", "SPMO.html");
        urlMap.put("CMPO", "CMPO.html");
        urlMap.put("RSMO", "RSMO.html");
        urlMap.put("SDRO", "SDRO.html");

        return urlMap.getOrDefault(network, "#"); // Retorna "#" se não encontrar
    }

    private String generateDiagramStructures(Package pack) {
        if (pack == null || pack.getDiagrams() == null || pack.getDiagrams().isEmpty()) {
            return "<p class=\"lead text-center\">No diagrams available for this package.</p>";
        }

        StringBuilder diagramStructs = new StringBuilder();
        String diagramTemplate = "<p class=\"lead\">@intro</p>\n<div class=\"container-fluid my-5 text-center\">@image\n<p class=\"fw-bold mt-4\">@flabel</p></div>\n<p class=\"lead text-justify\">@description</p>\n";

        for (Diagram diag : pack.getDiagrams()) {
            if (diag == null || diag.getType() == Diagram.DiagType.IGNORE) continue;

            String name = diag.getName() != null ? diag.getName() : "Unnamed Diagram";
            String introText = "";
            String labelText = "";
            String image = "";

            switch (diag.getType()) {
                case CONCEPTUALMODEL:
                    introText = "conceptual model of the " + name + (pack.getPackageType() == Package.PackType.SUBONTOLOGY ? " subontology" : "");
                    labelText = name + " Conceptual Model";
                    image = parseImage(diag);
                    break;
                case PACKAGE:
                    introText = "packages of the " + name;
                    labelText = name;
                    image = parseImage(diag);
                    break;
                case OTHER:
                    introText = name;
                    labelText = name;
                    image = "<img class=\"map img-fluid\" src=\"images/" + name + ".png\" alt=\"" + name + "\">";
                    break;
            }

            String struct = diagramTemplate
                    .replace("@intro", "Figure " + figCount + " presents the " + introText + ".")
                    .replace("@flabel", "Figure " + figCount + ". " + labelText + ".")
                    .replace("@image", image)
                    .replace("@description", formatDescription(diag.getDefinition() != null ?
                            diag.getDefinition().replaceAll("<ax>", "<code class=\"text-muted\">").replaceAll("</ax>", "</code>") : ""));

            figCount++;
            diagramStructs.append(struct);
        }
        return diagramStructs.toString();
    }

    private String parseImage(Diagram diagram) {
        if (diagram == null || diagram.getName() == null || diagram.getDiagramAstahObject() == null) {
            return "<p class=\"text-danger\">Invalid diagram data</p>";
        }

        IDiagram aDiagram = diagram.getDiagramAstahObject();
        String name = diagram.getName();
        int width = (int) Math.round(aDiagram.getBoundRect().getWidth());

        String imagePath = buildImagePath(diagram.getPack(), name);

        String image = String.format("<img src=\"images/%s.png\" width=\"%d\" class=\"map img-fluid\" usemap=\"#%s\" alt=\"Diagram of %s\">",
                imagePath, width, name, name);
        return image + parseMap(diagram);
    }

    private String buildImagePath(Package pack, String diagramName) {
        StringBuilder path = new StringBuilder("astah_seon/"); // Raiz fixa baseada na exportação

        // Percorre a hierarquia de pacotes até a raiz
        List<String> hierarchy = new ArrayList<>();
        Package current = pack;
        while (current != null) {
            String packName = current.getName() != null ? current.getName() : "Unnamed";
            hierarchy.addFirst(packName); // Mantém espaços como no Astah
            current = current.getParent();
        }

        // Remove o "astah_seon" da hierarquia, pois já está no prefixo
        if (!hierarchy.isEmpty() && "astah_seon".equals(hierarchy.getFirst())) {
            hierarchy.removeFirst();
        }

        // Constrói o caminho completo
        for (String folder : hierarchy) {
            path.append(folder).append("/");
        }
        path.append(diagramName);
        return path.toString();
    }

    private String parseMap(Diagram diagram) {
        if (diagram == null || diagram.getDiagramAstahObject() == null) {
            return "";
        }

        IDiagram aDiagram = diagram.getDiagramAstahObject();
        StringBuilder mapCode = new StringBuilder("<map name=\"" + (diagram.getName() != null ? diagram.getName() : "unnamed") + "\">");
        String areaTemplate = "\n<area shape=\"rect\" coords=\"@coords\" href=\"@reference\" target=\"@target\" alt=\"@definition\">";

        try {
            if (diagram.getType() == Diagram.DiagType.CONCEPTUALMODEL) {
                for (IPresentation present : aDiagram.getPresentations()) {
                    if (present instanceof INodePresentation node && "Class".equals(present.getType())) {
                        Concept concept = Concept.getConceptByFullName(((IClass) node.getModel()).getFullName("::"));
                        if (concept == null || concept.getOntology() == null) continue;

                        String coords = getMapCoords(node, aDiagram.getBoundRect());
                        String reference = concept.getReference();
                        String target = "";
                        Ontology whatOnto = concept.getOntology();
                        if ("SEON".equals(whatOnto.getNetwork())) {
                            String onURL = networkedOntoURL(whatOnto.getMainOntology().getShortName());
                            reference = onURL + "#" + whatOnto.getMainOntology().getShortName() + "_" + concept.getName().replace(' ', '+');
                            target = "_blank";
                        }

                        mapCode.append(areaTemplate
                                .replace("@coords", coords)
                                .replace("@reference", reference)
                                .replace("@target", target)
                                .replace("@definition", concept.getDefinition() != null ? concept.getDefinition() : ""));
                    }
                }
            } else if (diagram.getType() == Diagram.DiagType.PACKAGE) {
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
                    Package pack = Package.getPackageByFullName(((IPackage) node.getModel()).getFullName("::"));
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
                            reference = networkedOntoURL(mainOntoName) + "#" + mainOntoName + "_" + pack.getName().replace(' ', '+') + "_section";
                            target = "_blank";
                        }
                    }

                    mapCode.append(areaTemplate
                            .replace("@coords", coords)
                            .replace("@reference", reference)
                            .replace("@target", target)
                            .replace("@definition", ""));
                }
            }
        } catch (InvalidUsingException e) {
            e.printStackTrace();
        }
        return mapCode.append("</map>").toString();
    }

    private String getMapCoords(INodePresentation node, Rectangle2D adjust) {
        if (node == null || adjust == null) {
            return "0,0,0,0"; // Coordenadas padrão para erro
        }
        int x = (int) Math.round(node.getLocation().getX() - adjust.getX());
        int y = (int) Math.round(node.getLocation().getY() - adjust.getY());
        int w = (int) Math.round(node.getWidth());
        int h = (int) Math.round(node.getHeight());
        return String.format("%d,%d,%d,%d", x, y, x + w, y + h);
    }

    private String generateSectionStructures(Package seonNetwork, String sectionNumber) {
        if (seonNetwork == null || seonNetwork.getSubpacks() == null || seonNetwork.getSubpacks().isEmpty()) {
            return "";
        }

        String sectionTemplate = "\n<hr class=\"my-5\"><div class=\"py-5\"><h3 class=\"display-6\" id=\"@sectionref\">@snum @section</h3>\n<p class=\"lead text-justify\">@intro</p>\n@packdiagrams\n</div>";
        StringBuilder sectionStructures = new StringBuilder();
        List<Package> subpacks = new ArrayList<>(seonNetwork.getSubpacks());
        Collections.sort(subpacks);

        int subNum = 1;
        for (Package pack : subpacks) {
            if (pack == null) continue;

            int levelDiff = pack.getPackLevel() - (pack.getMainOntology() != null ? pack.getMainOntology().getPackLevel() : 0);
            int headingLevel = Math.min(3 + levelDiff, 6); // Limita a h6 (máximo em HTML)

            String struct = sectionTemplate
                    .replace("h3", "h" + headingLevel)
                    .replace("@snum", sectionNumber + subNum + ". ")
                    .replace("@sectionref", pack.getLabel() + "_section")
                    .replace("@section", pack.getName() != null ? pack.getName() : "Unnamed Section")
                    .replace("@intro", formatDescription(pack.getDefinition()))
                    .replace("@packdiagrams", generateDiagramStructures(pack));

            sectionStructures.append(struct)
                    .append(generateSectionStructures(pack, sectionNumber + subNum + "."));
            subNum++;
        }
        return sectionStructures.toString();
    }

    public String generateConceptsTable(Ontology ontology) {
        if (ontology == null || ontology.getAllConcepts() == null || ontology.getAllConcepts().isEmpty()) {
            return "<tr><td colspan=\"2\" class=\"text-center\">No concepts available</td></tr>";
        }

        String conceptLineTemplate = "<tr><td><p id=\"@reference\">@concept<a class=\"text-muted\" href=\"#@reference_detail\"><span class=\"m-1\"><i class=\"bi bi-plus-circle\"></i></span></a></p></td><td><p>@definition@example@source</p></td></tr>";
        List<Concept> concepts = new ArrayList<>(ontology.getAllConcepts());
        concepts.sort(Comparator.naturalOrder()); // Mais explícito que Collections.sort
        StringBuilder conceptsTable = new StringBuilder();

        for (Concept concept : concepts) {
            if (concept == null) continue;

            String name = concept.getName() != null ? concept.getName() : "Unnamed";
            String styleClass = "";
            switch (ontology.getLevel() != null ? ontology.getLevel() : Ontology.OntoLevel.DOMAIN) {
                case FOUNDATIONAL: styleClass = "fst-italic"; break;
                case CORE: styleClass = "fst-italic fw-bold"; break;
                case DOMAIN: styleClass = "fw-bold"; break;
            }
            String styledName = String.format("<span class=\"%s\">%s</span>", styleClass, name);

            String definition = concept.getDefinition() != null ? concept.getDefinition().replaceAll("\\R", "") : "No definition provided";
            String example = concept.getExample() != null ?
                    "<br><span class=\"fw-light\">E.g.: </span><span class=\"fst-italic\">" + concept.getExample().replaceAll("\\R", "") + "</span>" : "";
            String source = concept.getSourceDefinition() != null ?
                    "<br><span class=\"fw-light\">Src.: </span>" + concept.getSourceDefinition().replaceAll("\\R", "") : "";

            conceptsTable.append(conceptLineTemplate
                    .replace("@concept", styledName)
                    .replace("@reference", concept.getLabel() != null ? concept.getLabel() : "unknown")
                    .replace("@reference_detail", concept.getLabel() != null ? concept.getLabel() + "_detail" : "unknown_detail")
                    .replace("@definition", definition)
                    .replace("@example", example)
                    .replace("@source", source)
            ).append("\n");
        }
        return conceptsTable.toString();
    }

    private String generateDetailedConcepts(Ontology ontology) {
        if (ontology == null || ontology.getAllConcepts() == null || ontology.getAllConcepts().isEmpty()) {
            return "<p class=\"text-center\">No detailed concepts available</p>";
        }

        String detailedIcon = "<i class=\"bi bi-diagram-3\"></i>";
        String specializeIcon = "<i class=\"bi bi-diagram-2\"></i>";
        String detailItemTemplate = "<hr><div class=\"container-fluid\" id=\"@reference_detail\"><h4>@fullName</h4><div class=\"row d-flex\"><div class=\"p-3 m-3 col\"><div class=\"border border-dark mb-3\"><p class=\"text-center\">@stereotype<br><span class=\"fw-bold\">@concept</span></p></div><br><h5>Specializes:</h5>@generals</div><div class=\"p-3 m-3 col\"><h5>Definition:</h5><p>@definition@example@source</p></div><div class=\"p-3 m-3 col\"><h5>Relations:</h5><p>@relations</p></div></div></div>";

        List<Concept> concepts = new ArrayList<>(ontology.getAllConcepts());
        concepts.sort(Comparator.naturalOrder());
        StringBuilder detailedConcepts = new StringBuilder();

        for (Concept concept : concepts) {
            if (concept == null) continue;

            String stereotype = concept.getStereotype() != null && !concept.getStereotype().isEmpty() ?
                    "<br><code class=\"text-muted\">&lt;&lt;" + concept.getStereotype() + "&gt;&gt;</code>" : "";
//            if (stereotype.isEmpty() && !"UFO".equals(ontology.getShortName())) {
//                System.out.println("*" + concept + " <none>");
//            }

            String definition = concept.getDefinition() != null ? concept.getDefinition().replaceAll("\\R", "") : "No definition provided";
            String example = concept.getExample() != null ?
                    "<br><span class=\"fw-bold\">Example: </span><span class=\"fst-italic\">" + concept.getExample().replaceAll("\\R", "") + "</span>" : "";
            String source = concept.getSourceDefinition() != null ?
                    "<br><span class=\"fw-bold\">Source: </span>" + concept.getSourceDefinition().replaceAll("\\R", "") : "";

            StringBuilder generals = new StringBuilder();
            List<Concept> generalizations = concept.getGeneralizations() != null ? concept.getGeneralizations() : Collections.emptyList();
            for (Concept general : generalizations) {
                generals.append("<p>").append(specializeIcon).append(" ").append(general.getFullName()).append("</p>");
            }

            StringBuilder relations = new StringBuilder();
            List<Relation> conceptRelations = concept.getRelations();
            if (conceptRelations != null && !conceptRelations.isEmpty()) {
                relations.append("<code class=\"text-muted\">");
                for (Relation relation : conceptRelations) {
                    Ontology ontoSource = relation.getSource() != null ? relation.getSource().getMainOntology() : null;
                    Ontology ontoTarget = relation.getTarget() != null ? relation.getTarget().getMainOntology() : null;
                    String relationText = relation.toString();
                    if (true) { /* SeonParser.STABLE || ontoSource.getLevel().getValue() >= ontoTarget.getLevel().getValue() */
                        relations.append(relationText).append("<br>");
                    } else {
                        relations.append("<span class=\"text-danger\" title=\"Relation to a lower level (")
                                .append(ontoSource != null ? ontoSource.getName() : "Unknown").append("-->")
                                .append(ontoTarget != null ? ontoTarget.getName() : "Unknown").append(")\">")
                                .append(relationText).append("</span><br>");
                    }
                }
                relations.append("</code>");
            }

            detailedConcepts.append(detailItemTemplate
                    .replace("@reference_detail", concept.getLabel() != null ? concept.getLabel() + "_detail" : "unknown_detail")
                    .replace("@stereotype", stereotype)
                    .replace("@concept", concept.getName() != null ? concept.getName() : "Unnamed")
                    .replace("@fullName", detailedIcon + " " + (concept.getFullName() != null ? concept.getFullName() : "Unnamed"))
                    .replace("@definition", definition)
                    .replace("@example", example)
                    .replace("@source", source)
                    .replace("@generals", generals.toString())
                    .replace("@relations", relations.toString())
            ).append("\n\n");
        }
        return detailedConcepts.toString();
    }



    public static String fileToString(String filename) {
        String text = null;
        try {
            text = FileUtils.readFileToString(new File(filename), "UTF-8");
            //text = FileUtils.readFileToString(new File(Utils.class.getResource(filename).toURI()), "UTF-8");
        } catch (IOException e) {
            System.out.println("Error while reading file: " + filename + "at OntologiesWriter.fileToString()");
            e.printStackTrace();
        }
        return text;
    }

    public static void stringToFile(String filename, String text) {
        try {
            FileUtils.writeStringToFile(new File(filename), text, "UTF-8");
        } catch (IOException e) {
            System.out.println("Error while writing file: " + filename + " at OntologiesWriter.stringToFile()");
            e.printStackTrace();
        }
    }
}

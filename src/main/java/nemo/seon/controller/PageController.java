package nemo.seon.controller;

import nemo.seon.model.Ontology;
import nemo.seon.service.OntologyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import nemo.seon.writer.OntologiesWriter;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Year;

@Controller
public class PageController {
    private final OntologiesWriter ontologiesWriter;
    private final OntologyService ontologyService;

    @Autowired
    public PageController(OntologyService ontologyService) {
        this.ontologyService = ontologyService;
        this.ontologiesWriter = new OntologiesWriter();
    }

    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("date", java.time.LocalDate.now());
        return "TemplateHomePage";
    }

    @GetMapping("/publications")
    public String publicationsPage(Model model) {
        model.addAttribute("date", java.time.LocalDate.now());
        return "TemplatePublications";
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        model.addAttribute("date", java.time.LocalDate.now());
        return "UploadPage";
    }

    @GetMapping("/ontology/{ontologyName}")
    public String ontologyPage(@PathVariable String ontologyName, Model model) {
        try {
            // Buscar a ontologia pelo nome usando o serviço
            Ontology ontology = ontologyService.findByName(ontologyName);
            if (ontology == null) {
                System.out.println("Ontology not found: " + ontologyName);
                return "ErrorPage";
            }

            String status = "Unknown";
            String additionalInfo = "<div class=\"container-fluid d-flex justify-content-end\"><span class=\"badge bg-danger text-lowercase\">" + status + "</span></div>";
            model.addAttribute("additionalinfo", additionalInfo);

            model.addAttribute("title", ontology.getFullName() + " (" + ontology.getShortName() + ")");

            String ontoLevel = ontologiesWriter.formatOntologyLevelHtml(ontology);
            String onLevel = ontologiesWriter.formatOntologyLevelText(ontology);
            model.addAttribute("onto_level", ontoLevel);

            model.addAttribute("description", ontologiesWriter.formatDescription(ontology.getDefinition()));
            model.addAttribute("myontologyDependencies", ontologiesWriter.generateDependenciesTable(ontology));

            String ontoDiags = ontologiesWriter.generateDiagramStructures(ontology);
            String ontoPacks = ontologiesWriter.generateSectionStructures(ontology, "3.");
            model.addAttribute("sectionContent",ontoDiags + ontoPacks);

            model.addAttribute("conceptDefinitions", ontologiesWriter.generateConceptsTable(ontology));
            model.addAttribute("detailedConcepts", ontologiesWriter.generateDetailedConcepts(ontology));

            // Footer
            model.addAttribute("onto", ontology.getShortName());
            model.addAttribute("onlyname", ontology.getFullName());
            model.addAttribute("onlevel", onLevel);
            String addInfo = status;
            model.addAttribute("addinfo", addInfo);
            model.addAttribute("currentYear", String.valueOf(Year.now().getValue()));
            model.addAttribute("date", java.time.LocalDate.now());

            return "TemplateOntologyPage";

        } catch (Exception e) {
            // Log do erro
            System.err.println("Error loading ontology page for: " + ontologyName);
            e.printStackTrace();

            // Redirecionar para página de erro
            model.addAttribute("error", "Error loading ontology: " + e.getMessage());
            return "ErrorPage";
        }
    }
}
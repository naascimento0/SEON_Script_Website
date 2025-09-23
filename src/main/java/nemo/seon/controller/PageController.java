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

        // Add variables to sidebar
        model.addAttribute("definition", "definition");
        model.addAttribute("architecture", "architecture"); 
        model.addAttribute("foundational", "foundational");
        model.addAttribute("core", "core");
        model.addAttribute("domain", "domain");
        model.addAttribute("references", "references");
        model.addAttribute("currentYear", String.valueOf(Year.now().getValue()));
        model.addAttribute("date", java.time.LocalDate.now().toString());
        
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
            // Search for the ontology by name using the service
            Ontology ontology = ontologyService.findByName(ontologyName);
            if (ontology == null) {
                System.out.println("Ontology not found: " + ontologyName);
                return "ErrorPage";
            }

            // Section 0
            String status = "Unknown";
            String additionalInfo = "<div class=\"container-fluid d-flex justify-content-end\"><span class=\"badge bg-danger text-lowercase\">" + status + "</span></div>";
            model.addAttribute("additionalinfo", additionalInfo);

            model.addAttribute("title", ontology.getFullName() + " (" + ontology.getShortName() + ")");

            String ontoLevel = ontologiesWriter.formatOntologyLevelHtml(ontology);
            String onLevel = ontologiesWriter.formatOntologyLevelText(ontology);
            model.addAttribute("onto_level", ontoLevel);

            // Published paper information
            model.addAttribute("ontoPublishedPaper", ontology.getShortName() + " published paper");

            // Section 1
            model.addAttribute("description", ontologiesWriter.formatDescription(ontology.getDefinition()));

            // Section 2
            model.addAttribute("myontologyDependencies", ontologiesWriter.generateDependenciesTable(ontology));

            // Section 3
            ontologiesWriter.resetFigureCounter(); // Reset figCOunt for each ontology
            String ontoDiags = ontologiesWriter.generateDiagramStructures(ontology);
            String ontoPacks = ontologiesWriter.generateSectionStructures(ontology, "3.");
            model.addAttribute("sectionContent",ontoDiags + ontoPacks);

            // Section 4
            model.addAttribute("conceptDefinitions", ontologiesWriter.generateConceptsTable(ontology));

            // Section 5
            model.addAttribute("detailedConcepts", ontologiesWriter.generateDetailedConcepts(ontology));

            // Footer
            model.addAttribute("onto", ontology.getShortName());
            model.addAttribute("onlyname", ontology.getFullName());
            model.addAttribute("onlevel", onLevel);
            String addInfo = status;
            model.addAttribute("addinfo", addInfo);
            
            // Footer variables 
            model.addAttribute("ontoName", ontology.getShortName());
            model.addAttribute("onlyName", ontology.getFullName());
            model.addAttribute("onLevel", onLevel);
            model.addAttribute("addInfo", addInfo);
            
            // Links for footer (it is possible to customize these URLs as needed)
            model.addAttribute("onlyNameLink", "#");
            model.addAttribute("onLevelLink", "#");
            model.addAttribute("addInfoLink", "#");
            
            model.addAttribute("currentYear", String.valueOf(Year.now().getValue()));
            model.addAttribute("date", java.time.LocalDate.now());

            // Variables for the ontology page sidebar
            model.addAttribute("ontologydescription", "ontologydescription");
            model.addAttribute("relatedontologies", "relatedontologies");
            model.addAttribute("ontologymodels", "ontologymodels");
            model.addAttribute("conceptsdefinition", "conceptsdefinition");
            model.addAttribute("detailedconcepts", "detailedconcepts");

            return "TemplateOntologyPage";

        } catch (Exception e) {
            // Log the error
            System.err.println("Error loading ontology page for: " + ontologyName);
            e.printStackTrace();

            // Redirection to error page
            model.addAttribute("error", "Error loading ontology: " + e.getMessage());
            return "ErrorPage";
        }
    }
}
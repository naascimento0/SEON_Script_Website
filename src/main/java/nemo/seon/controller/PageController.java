package nemo.seon.controller;

import nemo.seon.model.Ontology;
import nemo.seon.model.dto.*;
import nemo.seon.service.OntologyService;
import nemo.seon.service.OntologyViewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Year;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
public class PageController {
    private static final Logger logger = LoggerFactory.getLogger(PageController.class);
    private final OntologyViewService viewService;
    private final OntologyService ontologyService;

    @Autowired
    public PageController(OntologyService ontologyService, OntologyViewService viewService) {
        this.ontologyService = ontologyService;
        this.viewService = viewService;
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
                logger.warn("Ontology not found: {}", ontologyName);
                return "ErrorPage";
            }

            // Title and level
            model.addAttribute("title", ontology.getFullName() + " (" + ontology.getShortName() + ")");
            model.addAttribute("ontoLevelIcon", viewService.getOntologyLevelIcon(ontology));
            model.addAttribute("ontoLevelText", viewService.getOntologyLevelText(ontology));
            model.addAttribute("onto", ontology.getShortName());

            // Status badge
            String status = "Unknown";
            model.addAttribute("status", status);

            // Published paper info
            model.addAttribute("ontoPublishedPaper", ontology.getShortName() + " published paper");

            // Section 1 - Description
            model.addAttribute("description", viewService.formatDescription(ontology.getDefinition()));

            // Section 2 - Dependencies (as DTOs)
            List<DependencyView> dependencies = viewService.buildDependencies(ontology);
            model.addAttribute("dependencies", dependencies);

            // Section 3 - Diagrams and Sections (as DTOs)
            AtomicInteger figureCounter = new AtomicInteger(1);
            List<DiagramView> ontoDiagrams = viewService.buildDiagrams(ontology, figureCounter);
            List<SectionView> ontoSections = viewService.buildSections(ontology, "3.", figureCounter);
            model.addAttribute("ontoDiagrams", ontoDiagrams);
            model.addAttribute("ontoSections", ontoSections);

            // Section 4 - Concepts table (as DTOs)
            List<ConceptRow> conceptRows = viewService.buildConceptRows(ontology);
            model.addAttribute("conceptRows", conceptRows);

            // Section 5 - Detailed concepts (as DTOs)
            List<ConceptDetail> conceptDetails = viewService.buildConceptDetails(ontology);
            model.addAttribute("conceptDetails", conceptDetails);

            // Footer variables
            model.addAttribute("ontoName", ontology.getShortName());
            model.addAttribute("onlyName", ontology.getFullName());
            model.addAttribute("onLevel", viewService.getOntologyLevelText(ontology));
            model.addAttribute("addInfo", status);
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
            logger.error("Error loading ontology page for: {}", ontologyName, e);
            model.addAttribute("error", "Error loading ontology: " + e.getMessage());
            return "ErrorPage";
        }
    }
}
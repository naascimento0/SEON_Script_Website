package nemo.seon.controller;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nemo.seon.model.Ontology;
import nemo.seon.model.dto.ConceptDetail;
import nemo.seon.model.dto.ConceptRow;
import nemo.seon.model.dto.DependencyView;
import nemo.seon.model.dto.DiagramView;
import nemo.seon.model.dto.OntologyListItem;
import nemo.seon.model.dto.OntologyPageResponse;
import nemo.seon.model.dto.SectionView;
import nemo.seon.service.OntologyService;
import nemo.seon.service.OntologyViewService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final OntologyService ontologyService;
    private final OntologyViewService viewService;

    public ApiController(OntologyService ontologyService, OntologyViewService viewService) {
        this.ontologyService = ontologyService;
        this.viewService = viewService;
    }

    @GetMapping("/ontologies")
    public List<OntologyListItem> listOntologies() {
        List<OntologyListItem> items = new ArrayList<>();
        for (Ontology o : ontologyService.getAllOntologies()) {
            items.add(new OntologyListItem(
                    o.getShortName(),
                    o.getFullName(),
                    o.getLevel() != null ? o.getLevel().name() : null,
                    o.getNetwork()
            ));
        }
        Collections.sort(items, (a, b) -> a.shortName().compareToIgnoreCase(b.shortName()));
        return items;
    }

    @GetMapping("/ontologies/{ontologyName}")
    public ResponseEntity<OntologyPageResponse> getOntology(@PathVariable String ontologyName) {
        Ontology ontology = ontologyService.findByName(ontologyName);
        if (ontology == null) {
            logger.warn("Ontology not found: {}", ontologyName);
            return ResponseEntity.notFound().build();
        }

        AtomicInteger figureCounter = new AtomicInteger(1);
        List<DependencyView> dependencies = viewService.buildDependencies(ontology);
        List<DiagramView> diagrams = viewService.buildDiagrams(ontology, figureCounter);
        List<SectionView> sections = viewService.buildSections(ontology, "3.", figureCounter);
        List<ConceptRow> conceptRows = viewService.buildConceptRows(ontology);
        List<ConceptDetail> conceptDetails = viewService.buildConceptDetails(ontology);

        OntologyPageResponse response = new OntologyPageResponse(
                ontology.getFullName() + " (" + ontology.getShortName() + ")",
                ontology.getShortName(),
                ontology.getFullName(),
                viewService.getOntologyLevelIcon(ontology),
                viewService.getOntologyLevelText(ontology),
                viewService.formatDescription(ontology.getDefinition()),
                dependencies,
                diagrams,
                sections,
                conceptRows,
                conceptDetails
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/meta")
    public Map<String, Object> meta() {
        return Map.of("currentYear", String.valueOf(Year.now().getValue()));
    }

    @GetMapping("/auth/me")
    public ResponseEntity<Map<String, Object>> currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        List<String> roles = new ArrayList<>();
        for (GrantedAuthority ga : authentication.getAuthorities()) {
            roles.add(ga.getAuthority());
        }
        return ResponseEntity.ok(Map.of(
                "username", authentication.getName(),
                "roles", roles
        ));
    }
}

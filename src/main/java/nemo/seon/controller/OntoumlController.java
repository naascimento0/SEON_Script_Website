package nemo.seon.controller;

import nemo.seon.service.OntoumlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes the OntoUML Schema JSON export for a single ontology, plus a stereotype
 * coverage diagnostic to guide the (mostly automatic) mapping effort.
 */
@RestController
@RequestMapping("/api/ontouml")
public class OntoumlController {

    private static final Logger logger = LoggerFactory.getLogger(OntoumlController.class);
    private final OntoumlService ontoumlService;

    public OntoumlController(OntoumlService ontoumlService) {
        this.ontoumlService = ontoumlService;
    }

    /** Downloads the OntoUML Schema JSON for {@code name} (e.g., {@code sysswo}). */
    @GetMapping("/{name}")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String name) {
        try {
            if (ontoumlService.buildProject(name) == null) {
                return ResponseEntity.notFound().build();
            }
            byte[] json = ontoumlService.generateOntoumlJson(name);
            ByteArrayResource resource = new ByteArrayResource(json);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + name + ".ontouml.json\"")
                    .contentLength(json.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to generate OntoUML JSON for {}", name, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /** Returns the stereotype coverage report for {@code name}. */
    @GetMapping("/{name}/coverage")
    public ResponseEntity<Map<String, Object>> coverage(@PathVariable String name) {
        Map<String, Object> report = ontoumlService.coverage(name);
        if (report == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(report);
    }
}

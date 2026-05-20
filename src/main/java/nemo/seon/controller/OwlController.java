package nemo.seon.controller;

import nemo.seon.service.OwlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OwlController {

    private static final Logger logger = LoggerFactory.getLogger(OwlController.class);
    private final OwlService owlService;

    public OwlController(OwlService owlService) {
        this.owlService = owlService;
    }

    @GetMapping("/seon.owl")
    public ResponseEntity<ByteArrayResource> downloadSeonOwl() {
        try {
            byte[] owl = owlService.generateSeonOwl();
            ByteArrayResource resource = new ByteArrayResource(owl);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/rdf+xml"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"SEON.owl\"")
                    .contentLength(owl.length)
                    .body(resource);
        } catch (Exception e) {
            logger.error("Failed to generate SEON.owl", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

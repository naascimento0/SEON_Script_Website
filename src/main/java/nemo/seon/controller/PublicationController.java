package nemo.seon.controller;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import nemo.seon.model.Publication;
import nemo.seon.model.dto.PublicationRequest;
import nemo.seon.service.PublicationService;

/**
 * Reading the publication list is public; adding, editing and removing entries require
 * ROLE_ADMIN (enforced in {@code SecurityConfig}).
 */
@RestController
@RequestMapping(value = "/api/publications", produces = MediaType.APPLICATION_JSON_VALUE)
public class PublicationController {

    private static final Logger logger = LoggerFactory.getLogger(PublicationController.class);

    private final PublicationService publicationService;

    public PublicationController(PublicationService publicationService) {
        this.publicationService = publicationService;
    }

    @GetMapping
    public List<Publication> listPublications() {
        return publicationService.findAll();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Publication> addPublication(@RequestBody PublicationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(publicationService.add(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Publication updatePublication(@PathVariable String id, @RequestBody PublicationRequest request) {
        return publicationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublication(@PathVariable String id) {
        publicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> onUnknownPublication(NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "This publication no longer exists. Reload the page."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> onInvalidRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> onStorageFailure(IllegalStateException e) {
        logger.error("Publication storage failure: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Could not save the publication. Please try again."));
    }
}

package nemo.seon.config;

import nemo.seon.service.DiagramsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/** Exports diagrams from the Astah file at startup so they are available on first page load. */
@Component
public class StartupDiagramGenerator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(StartupDiagramGenerator.class);
    private final DiagramsService diagramsService;

    public StartupDiagramGenerator(DiagramsService diagramsService) {
        this.diagramsService = diagramsService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("=== Generating diagrams at startup ===");
        try {
            diagramsService.exportAstahDiagrams();
            logger.info("=== Diagram generation completed ===");
        } catch (Exception e) {
            logger.error("Failed to generate diagrams at startup: {}", e.getMessage(), e);
        }
    }
}

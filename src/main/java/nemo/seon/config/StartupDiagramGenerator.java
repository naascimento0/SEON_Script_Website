package nemo.seon.config;

import nemo.seon.service.DiagramsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Executes diagram generation at application startup.
 * This ensures that all diagrams are available when the web pages are accessed.
 */
@Component
public class StartupDiagramGenerator implements ApplicationRunner {

    private final DiagramsService diagramsService;

    @Autowired
    public StartupDiagramGenerator(DiagramsService diagramsService) {
        this.diagramsService = diagramsService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("=== Generating diagrams at startup ===");
        
        try {
            diagramsService.exportAstahDiagrams();
            System.out.println("=== Diagram generation completed ===");
        } catch (Exception e) {
            System.err.println("Failed to generate diagrams at startup: " + e.getMessage());
            // Don't fail the application startup
        }
    }
}

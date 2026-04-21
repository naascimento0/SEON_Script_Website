package nemo.seon.service;

import jakarta.annotation.PostConstruct;
import nemo.seon.model.Ontology;
import nemo.seon.model.Package;
import nemo.seon.model.SeonRegistry;
import nemo.seon.parser.ModelReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OntologyService {
    private static final Logger logger = LoggerFactory.getLogger(OntologyService.class);
    private Package seonNetwork;
    private final SeonRegistry registry = new SeonRegistry();
    private final Map<String, Ontology> ontologyNames = new ConcurrentHashMap<>();

    @Value("${seon.astah.filepath}")
    private String astahFileName;

    @PostConstruct
    public void initialize() {
        loadOntologies();
        buildCache(seonNetwork);
        printOntologyNames();
    }

    private void loadOntologies() {
        try {
            registry.clear();
            ModelReader modelReader = new ModelReader(registry);
            String astahFilePath = Paths.get(System.getProperty("user.dir"), astahFileName).toString();
            this.seonNetwork = modelReader.parseAstah2Seon(astahFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SEON ontologies from Astah file", e);
        }
    }

    public SeonRegistry getRegistry() {
        return registry;
    }

    private void buildCache(Package seonNetwork) {
        for (Package pack : seonNetwork.getSubpacks()) {
            if (pack.getPackageType() == Package.PackType.ONTOLOGY) {
                logger.debug("Caching ontology: {}", pack.getName().toLowerCase());
                ontologyNames.put(pack.getName().toLowerCase(), (Ontology) pack);
            } else {
                buildCache(pack);
            }
        }
    }

    private void printOntologyNames() {
        ontologyNames.forEach((name, ontology) ->
            logger.info("Ontology loaded: {} ({})", name, ontology.getShortName()));
        if (ontologyNames.isEmpty()) {
            logger.warn("No ontologies found");
        }
    }

    public Ontology findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        return ontologyNames.get(name.toLowerCase().trim());
    }

    /** Reloads ontologies from the Astah file when the .asta file has been updated. */
    public void reloadOntologies() {
        logger.info("Reloading ontologies from Astah file...");
        ontologyNames.clear();
        loadOntologies();
        buildCache(seonNetwork);
        logger.info("Ontologies reloaded successfully.");
        printOntologyNames();
    }
}

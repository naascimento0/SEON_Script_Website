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
        loadOntologies(getActiveAstahFilePath());
        buildCache(seonNetwork);
        printOntologyNames();
    }

    /** Absolute path of the {@code .asta} file the site currently serves. */
    public String getActiveAstahFilePath() {
        return Paths.get(System.getProperty("user.dir")).resolve(astahFileName).toString();
    }

    private void loadOntologies(String astahFilePath) {
        try {
            registry.clear();
            ModelReader modelReader = new ModelReader(registry);
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

    public java.util.Collection<Ontology> getAllOntologies() {
        return ontologyNames.values();
    }

    /** Reloads ontologies from the active Astah file when the .asta file has been updated. */
    public synchronized void reloadOntologies() {
        reloadFrom(getActiveAstahFilePath());
    }

    /**
     * Reloads ontologies from an arbitrary {@code .asta} file — used to parse a candidate upload
     * before it is promoted to the active file.
     *
     * <p>The Astah API keeps a single open project per JVM, so a successful call leaves the given
     * file as the live model and a failed one leaves no usable model at all: the caller must then
     * put the previous file back and call {@link #reloadOntologies()} to recover.
     *
     * @throws RuntimeException if the file cannot be parsed
     */
    public synchronized void reloadFrom(String astahFilePath) {
        logger.info("Reloading ontologies from Astah file: {}", astahFilePath);
        ontologyNames.clear();
        loadOntologies(astahFilePath);
        buildCache(seonNetwork);
        logger.info("Ontologies reloaded successfully.");
        printOntologyNames();
    }

    /** Number of ontologies in the model currently loaded. */
    public int getOntologyCount() {
        return ontologyNames.size();
    }

    /** Number of concepts in the model currently loaded. */
    public int getConceptCount() {
        return registry.getAllConcepts().size();
    }
}

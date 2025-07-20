package nemo.seon.service;

import jakarta.annotation.PostConstruct;
import nemo.seon.model.Ontology;
import nemo.seon.model.Package;
import nemo.seon.parser.ModelReader;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static nemo.seon.parser.Parser.astahFilePath;

@Service
public class OntologyService {
    private Package seonNetwork;
    private final Map<String, Ontology> ontologyNames = new ConcurrentHashMap<>();

    private volatile boolean initialized = false;
    @PostConstruct
    public void initialize() {
        if (!initialized) {
            loadOntologies();
            buildCache(seonNetwork);
            printOntologyNames();
            initialized = true;
        }
    }

    private void loadOntologies() {
        try {
            ModelReader modelReader = new ModelReader();
            this.seonNetwork = modelReader.parseAstah2Seon(astahFilePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load SEON ontologies from Astah file", e);
        }
    }

    /**
     * Constrói o cache de ontologias para busca rápida
     */
    private void buildCache(Package seonNetwork) {
        for (Package pack : seonNetwork.getSubpacks()) {
            if (pack.getPackageType() == Package.PackType.ONTOLOGY) {
                System.out.println("Writing Ontology: " + pack.getName().toLowerCase());
                ontologyNames.put(pack.getName().toLowerCase(), (Ontology) pack);
            } else {
                buildCache(pack);
            }
        }
    }

    public void printOntologyNames() {
        ontologyNames.forEach((name, ontology) -> {
            System.out.println(name + ": " + ontology.getShortName());
        });
        if(ontologyNames.isEmpty()) {
            System.out.println("No ontologies found");
        }
    }

    public Ontology findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        if (!initialized) {
            initialize();
        }
        return ontologyNames.get(name.toLowerCase().trim());
    }
}

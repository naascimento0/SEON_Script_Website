package nemo.seon.writer;

import nemo.seon.model.Package;

public class PageWriter {
    public void generateSeonPages(Package seonNetwork) {
        OntologiesWriter ontologiesWriter = new OntologiesWriter();
        ontologiesWriter.writeOntologies(seonNetwork);
        System.out.println("Ontologies pages were generated successfully.");
    }
}

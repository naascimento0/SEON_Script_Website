package nemo.seon.writer;

import nemo.seon.model.Ontology;
import nemo.seon.model.Package;

public class OntologiesWriter {
    public void writeOntologies(Package seonNetwork) {
        for(Package pack : seonNetwork.getSubpacks()) {
            if(pack.getPackageType() == Package.PackType.ONTOLOGY) {
                System.out.println("Writing Ontology: " + pack.getName());
                writeOntologyPage((Ontology) pack);
            } else {
                writeOntologies(pack);
            }
        }
    }

    private void writeOntologyPage(Ontology pack) {

    }
}

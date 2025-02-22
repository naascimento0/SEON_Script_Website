package nemo.seon.parser;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.LicenseNotFoundException;
import com.change_vision.jude.api.inf.exception.NonCompatibleException;
import com.change_vision.jude.api.inf.exception.ProjectLockedException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import nemo.seon.model.Concept;
import nemo.seon.model.Ontology;
import nemo.seon.model.Package;
import nemo.seon.model.Package.PackType;

import java.io.IOException;

public class ModelReader {

    /**
     * Reads the Astah file and builds the Seon Model.
     * @param astahFilePath the path to the astah file
     * @return the Seon model
     */
    public Package parseAstah2Seon(String astahFilePath) {
        try {
            ProjectAccessor acessor = AstahAPI.getAstahAPI().getProjectAccessor();
            // Opening a astah project (filepath, true not to check model version, false not to lock a project file, true to open a project file with the read only mode if the file is locked.)
            acessor.open(astahFilePath, true, false, true);
            IModel model = acessor.getProject();

            Package seonNetwork = new Package(model.getName(), model.getDefinition(), PackType.NETWORK, 0, model);

            parsePackages(seonNetwork);
            System.out.println("Parsed Network Packages and Concepts");

            parseDependencies(seonNetwork);

        } catch (ClassNotFoundException | ProjectNotFoundException | ProjectLockedException | LicenseNotFoundException |
                 IOException | NonCompatibleException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Reads the packages recursively and creates the Package objects.
     * @param mainPackage the main package
     */
    private void parsePackages(Package mainPackage) {
        for (INamedElement object : mainPackage.getAstahPack().getOwnedElements()) {
            if (object instanceof IPackage) {
                String givenType = object.getTaggedValue("Type");
                if (givenType == null)
                    System.out.println("No type defined for package: " + object.getName());

                PackType type = Package.getPackType(givenType);
                if(type != PackType.IGNORE) {
                    String sorder = object.getTaggedValue("Order");
                    int order = 100;
                    if (!sorder.isEmpty())
                        order = Integer.parseInt(sorder);

                    Package pack = null;
                    if (type == PackType.LEVEL || type == PackType.PACKAGE || type == PackType.SUBNETWORK) {
                        pack = new Package(object.getName(), object.getDefinition(), type, 0, (IPackage)object);
                    } else if (type == PackType.ONTOLOGY || type == PackType.SUBONTOLOGY) {
                        pack = new Ontology(object.getName(), object.getTaggedValue("FullName"), object.getTaggedValue("ShortName"), object.getDefinition(), type, order, (IPackage) object);
                    }

                    if(pack == null) 
                        throw new RuntimeException("Package not created");
                    pack.setParent(mainPackage);
                    mainPackage.addSubPack(pack);
                    parsePackages(pack);
                }

            } else if (object instanceof IClass) {
                String stereotype = "";
                if ((object.getStereotypes()).length > 0) {
                    stereotype = object.getStereotypes()[0]; // only the first for while
                }
                Concept concept = new Concept(object.getName(), object.getDefinition(), stereotype, (IClass) object);
                concept.setOntology((Ontology) mainPackage);
                ((Ontology) mainPackage).addConcept(concept);
            }
        }
    }

    private void parseDependencies(Package seonNetwork) {

    }

}

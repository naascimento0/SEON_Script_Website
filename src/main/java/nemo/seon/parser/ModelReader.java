package nemo.seon.parser;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.exception.LicenseNotFoundException;
import com.change_vision.jude.api.inf.exception.NonCompatibleException;
import com.change_vision.jude.api.inf.exception.ProjectLockedException;
import com.change_vision.jude.api.inf.exception.ProjectNotFoundException;
import com.change_vision.jude.api.inf.model.*;
import com.change_vision.jude.api.inf.project.ProjectAccessor;
import nemo.seon.model.*;
import nemo.seon.model.Package;
import nemo.seon.model.Package.PackType;

import java.io.IOException;
import java.util.List;


public class ModelReader {

    /**
     * Reads the Astah file and builds the Seon Model.
     * @param astahFilePath the path to the astah file
     * @return the Seon model
     */
    public Package parseAstah2Seon(String astahFilePath) {
        Package seonNetwork;
        try {
            ProjectAccessor acessor = AstahAPI.getAstahAPI().getProjectAccessor();
            // Opening a astah project (filepath, true not to check model version, false not to lock a project file, true to open a project file with the read only mode if the file is locked.)
            acessor.open(astahFilePath, true, false, true);

            IModel model = acessor.getProject();

            seonNetwork = new Package(model.getName(), model.getDefinition(), PackType.NETWORK, 0, model);

            parsePackages(seonNetwork);
            System.out.println("Parsed Network Packages and Concepts");

            parseDependencies(seonNetwork);
            System.out.println("Parsed Network Dependencies");

            parseGeneralizations(Concept.getAllConcepts());
            System.out.println("Parsed Generalizations");

            parseRelations(Concept.getAllConcepts());
            System.out.println("Parsed Relations");

            parseDiagrams(seonNetwork);
            System.out.println("Parsed Diagrams");

        } catch (ClassNotFoundException | ProjectNotFoundException | ProjectLockedException | LicenseNotFoundException |
                 IOException | NonCompatibleException e) {
            System.out.println("Error while parsing the Astah file.");
            throw new RuntimeException(e);
        }
        return seonNetwork;
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
                if (type != PackType.IGNORE) {
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

                    if (pack == null)
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

    /**
     * Reads the Packages recursively and creates their Dependencies.
     * @param seonNetwork the main package
     */
    private void parseDependencies(Package seonNetwork) {
        for (IDependency object : seonNetwork.getAstahPack().getClientDependencies()) {
            Package client = Package.getAstahPackFromMap((IPackage) object.getClient());
            Package supplier = Package.getAstahPackFromMap((IPackage) object.getSupplier());

            Dependency dependency = new Dependency(client, supplier, object.getDefinition(), object.getTaggedValue("Level"));
            client.addDependency(dependency);
        }

        for (Package pack : seonNetwork.getSubpacks()) {
            parseDependencies(pack);
        }
    }

    /**
     * Reads the generalizations between the concepts. Children are added to the parents.
     * <a href="https://www.ibm.com/docs/en/dma?topic=diagrams-generalization-relationships">...</a>
     * @param concepts the list of concepts
     */
    private void parseGeneralizations(List<Concept> concepts) {
        for (Concept child : concepts) {
            for (IGeneralization generalization : child.getAstahConceptObject().getGeneralizations()) {
                Concept parent = Concept.getConceptObjectByItsIClass(generalization.getSuperType());
                child.addGeneralization(parent);
            }
        }
    }


    /** NÃO ESTÁ SENDO USADO ????????
     * Reads the relations between the concepts.
     * @param concepts the list of concepts
     */
    private void parseRelations(List<Concept> concepts) {
        for(Concept source : concepts) {
            for (IAttribute attribute : source.getAstahConceptObject().getAttributes()) {
                IAssociation association = attribute.getAssociation();
                if (association != null) { // it is an Association, not an Attribute
                    IAttribute firstEnd = association.getMemberEnds()[0];
                    IAttribute secondEnd = association.getMemberEnds()[1];
                    if (firstEnd.getType().equals(source.getAstahConceptObject())) {
                        String stereotype = null;
                        if (association.getStereotypes().length > 0)
                            stereotype = association.getStereotypes()[0];
                        boolean composition = (firstEnd.isComposite() || secondEnd.isAggregate());
                        String smult = "";
                        String tmult = "";
                        if (firstEnd.getMultiplicity().length > 0)
                            smult = multiplicityToString(firstEnd.getMultiplicity()[0]);
                        if (secondEnd.getMultiplicity().length > 0)
                            tmult = multiplicityToString(secondEnd.getMultiplicity()[0]);

                        Concept target = Concept.getConceptObjectByItsIClass(attribute.getType());
                        Package pack = Package.getPackageByFullName(association.getFullName("::"));

                        Relation relation = new Relation(association.getName(), association.getDefinition(), stereotype, composition, pack, source, target, smult, tmult);
                    }
                }
            }
        }
    }

    /**
     * Returns the multiplicity of an end in text format (n..m).
     * @param imult the multiplicity range
     * @return the multiplicity in text format
     */
    private String multiplicityToString(IMultiplicityRange imult) {
        int lower = imult.getLower();
        int upper = imult.getUpper();
        if (lower == IMultiplicityRange.UNDEFINED) return "";
        if (lower == IMultiplicityRange.UNLIMITED) return "*";
        if (upper == IMultiplicityRange.UNDEFINED) return lower + "";
        if (upper == IMultiplicityRange.UNLIMITED) return lower + "..*";
        return lower + ".." + upper;
    }


    /**
     * Reads the diagrams and creates the Diagram objects.
     * @param seonNetwork the main package
     */
    private void parseDiagrams(Package seonNetwork) {
        for (IDiagram object : seonNetwork.getAstahPack().getDiagrams()) {
            Diagram diagram = new Diagram(object.getName(), object.getDefinition(), Diagram.getDiagramType(object.getTaggedValue("Type")), object.getTaggedValue("Network"));
            diagram.setPack(seonNetwork);
            seonNetwork.addDiagram(diagram);
        }

        for(Package pack : seonNetwork.getSubpacks())
            parseDiagrams(pack);
    }
}

package nemo.seon.writer;

import nemo.seon.model.Package;

import java.util.Date;

import static nemo.seon.writer.Utils.fileToString;
import static nemo.seon.writer.Utils.stringToFile;

public class PageWriter {
    public void generateSeonPages(Package seonNetwork) {
        OntologiesWriter ontologiesWriter = new OntologiesWriter();

        ontologiesWriter.writeOntologies(seonNetwork);
        System.out.println("Ontologies pages were generated successfully.");

        writeHomePage();
        System.out.println("Home page was generated successfully.");

        writePublicationsPage();
        System.out.println("Publications page was generated successfully.");
    }

    /**
     * Writes the home page.
     */
    private void writeHomePage() {
        // Lendo o template HTML
        String html = fileToString("./static/TemplateHomePage.html");
        if (html == null) {
            System.out.println("Failed to load template for home page: ./static/TemplateHomePage.html");
            return;
        }

        html = html.replace("@date", new Date().toString());
        // Gravando o arquivo HTML gerado
        stringToFile("./page/" + "HomePage.html", html);
    }

    /**
     * Writes the publications page.
     */
    private void writePublicationsPage() {
        // Lendo o template HTML
        String html = fileToString("./static/TemplatePublications.html");
        if (html == null) {
            System.out.println("Failed to load template for home page: ./static/TemplatePublications.html");
            return;
        }

        html = html.replace("@date", new Date().toString());
        // Gravando o arquivo HTML gerado
        stringToFile("./page/" + "Publications.html", html);
    }
}

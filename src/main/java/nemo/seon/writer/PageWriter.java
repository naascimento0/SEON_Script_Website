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

        writeUploadPage();
        System.out.println("Upload page was generated successfully.");

        writeLoginPage();
        System.out.println("Login page was generated successfully.");
    }

    /**
     * Writes the home page.
     */
    private void writeHomePage() {
        // Reading the HTML template
        String html = fileToString("./src/main/resources/templates/TemplateHomePage.html");
        if (html == null) {
            System.out.println("Failed to load template for home page: ./src/main/resources/templates/TemplateHomePage.html");
            return;
        }

        html = html.replace("@date", new Date().toString());
        // Writing the generated HTML file
        stringToFile("./page/" + "HomePage.html", html);
    }

    /**
     * Writes the publications page.
     */
    private void writePublicationsPage() {
        // Reading the HTML template
        String html = fileToString("./src/main/resources/templates/TemplatePublications.html");
        if (html == null) {
            System.out.println("Failed to load template for home page: ./src/main/resources/templates/TemplatePublications.html");
            return;
        }

        html = html.replace("@date", new Date().toString());
        // Writing the generated HTML file
        stringToFile("./page/" + "Publications.html", html);
    }

    private void writeUploadPage() {
        // Reading the HTML template
        String html = fileToString("./src/main/resources/templates/UploadPage.html");
        if (html == null) {
            System.out.println("Failed to load template for upload page: ./src/main/resources/templates/UploadPage.html");
            return;
        }

        html = html.replace("@date", new Date().toString());
        // Writing the generated HTML file
        stringToFile("./page/" + "UploadPage.html", html);
    }

    private void writeLoginPage() {
        // Reading the HTML template
        String html = fileToString("./src/main/resources/templates/LoginPage.html");
        if (html == null) {
            System.out.println("Failed to load template for login page: ./src/main/resources/templates/LoginPage.html");
            return;
        }

//        html = html.replace("@date", new Date().toString());
        // Writing the generated HTML file
        stringToFile("./page/" + "LoginPage.html", html);
    }
}

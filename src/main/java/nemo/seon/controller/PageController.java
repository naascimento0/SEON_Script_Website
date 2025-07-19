package nemo.seon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/upload")
    public String uploadPage() {
        return "UploadPage";
    }

     @GetMapping("/")
     public String homePage() {
         return "TemplateHomePage";
     }

    @GetMapping("/ontology")
    public String ontologyPage() {
        return "TemplateOntologyPage";
    }
}
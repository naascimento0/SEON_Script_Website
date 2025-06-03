package nemo.seon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String uploadPage() {
        return "UploadPage";  // This refers to UploadPage.html in templates folder
    }
}
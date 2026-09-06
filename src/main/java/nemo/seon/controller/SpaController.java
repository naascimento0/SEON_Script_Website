package nemo.seon.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards SPA routes to the React app's index.html so that React Router can
 * take over client-side. Any path that does NOT start with /api, /upload-asta,
 * /logout, /login, /seon.owl, /images, /assets, /css, /js, /favicon... is a
 * candidate. We list the known SPA routes explicitly to avoid swallowing
 * legitimate 404s.
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/publications",
            "/ontologies",
            "/upload",
            "/login",
            "/ontology/{name}"
    })
    public String spa() {
        return "forward:/index.html";
    }
}

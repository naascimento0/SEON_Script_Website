package nemo.seon;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
// import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    /**
     * Configures Cross-Origin Resource Sharing (CORS) settings for the application.
     * Allows requests from http://localhost:8080 to access all API endpoints
     * using GET and POST methods with any headers.
     *
     * @param registry the CORS registry to configure
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:8080")
                .allowedMethods("GET", "POST")
                .allowedHeaders("*");
    }
}

package nemo.seon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeonBackendApplication {
    /**
     * Main entry point for the SEON Backend Spring Boot application.
     * Initializes and starts the embedded Tomcat server with all configured components.
     *
     * @param args command-line arguments passed to the application
     */
    public static void main(String[] args) {
        SpringApplication.run(SeonBackendApplication.class, args);
    }
}
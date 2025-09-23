package nemo.seon;

import nemo.seon.config.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SeonBackendApplication {
    public static void main(String[] args) {
        // Load environment variables from .env file
        EnvLoader.loadEnvFile();
        
        SpringApplication.run(SeonBackendApplication.class, args);
    }
}
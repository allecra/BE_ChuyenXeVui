package com.example.ckdatveexe;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Main entry point for CK DatVeXe Backend application
 */
@SpringBootApplication
@EnableScheduling
@RestController
public class CkDatVeXeApplication {

    @GetMapping("/health")
    public String health() {
        return "Server is working!";
    }

    @GetMapping("/api/test")
    public String apiTest() {
        return "API is working!";
    }

    public static void main(String[] args) {
        // Load .env file before Spring Boot starts
        Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();

        // Set system properties from .env
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

        SpringApplication.run(CkDatVeXeApplication.class, args);
    }
}

package com.example.ckdatveexe.module.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/")
@Tag(name = "Welcome", description = "Welcome and API information endpoints")
public class WelcomeController {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Operation(summary = "Welcome message with API links", description = "Get welcome message and important API links")
    @GetMapping
    public ResponseEntity<Map<String, Object>> welcome(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "🎉 Welcome to CK DatVeXe API!");
        response.put("version", "1.0.0");
        response.put("status", "✅ Running");

        Map<String, String> links = new HashMap<>();
        links.put("🏠 API Base URL", baseUrl);
        links.put("📚 Swagger UI", baseUrl + "/swagger-ui.html");
        links.put("📋 API Docs (JSON)", baseUrl + "/v3/api-docs");
        links.put("❤️ Health Check", baseUrl + "/health");
        links.put("🗄️ Database Health", baseUrl + "/health/db");

        response.put("links", links);

        Map<String, String> instructions = new HashMap<>();
        instructions.put("Swagger UI", "Click the Swagger UI link to explore and test all API endpoints");
        instructions.put("Health Check", "Use health endpoints to monitor application status");
        instructions.put("API Documentation", "Access complete API documentation via Swagger");

        response.put("instructions", instructions);
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get API links", description = "Get all important API links")
    @GetMapping("/links")
    public ResponseEntity<Map<String, String>> getLinks(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);

        Map<String, String> links = new HashMap<>();
        links.put("swagger_ui", baseUrl + "/swagger-ui.html");
        links.put("api_docs", baseUrl + "/v3/api-docs");
        links.put("health", baseUrl + "/health");
        links.put("database_health", baseUrl + "/health/db");

        return ResponseEntity.ok(links);
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        StringBuilder url = new StringBuilder();
        url.append(scheme).append("://").append(serverName);

        if ((scheme.equals("http") && serverPort != 80) ||
                (scheme.equals("https") && serverPort != 443)) {
            url.append(":").append(serverPort);
        }

        url.append(contextPath);
        return url.toString();
    }
}
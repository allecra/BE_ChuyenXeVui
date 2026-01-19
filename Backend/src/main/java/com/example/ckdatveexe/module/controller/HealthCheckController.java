package com.example.ckdatveexe.module.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for API
 */
@RestController
@RequestMapping("/health")
@Tag(name = "Health Check", description = "Health check endpoints")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Operation(summary = "Health check", description = "Check if the application is running")
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck(HttpServletRequest request) {
        String baseUrl = getBaseUrl(request);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("message", "CK DatVeXe Backend is running");
        response.put("timestamp", System.currentTimeMillis());

        // Add useful links
        Map<String, String> links = new HashMap<>();
        links.put("swagger_ui", baseUrl + "/swagger-ui.html");
        links.put("api_docs", baseUrl + "/v3/api-docs");
        links.put("welcome", baseUrl + "/");
        response.put("links", links);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Database health check", description = "Check database connection")
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> databaseHealthCheck() {
        Map<String, Object> response = new HashMap<>();

        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(5)) {
                response.put("status", "UP");
                response.put("database", "Connected");
                response.put("message", "Database connection is healthy");
            } else {
                response.put("status", "DOWN");
                response.put("database", "Invalid connection");
                response.put("message", "Database connection is invalid");
            }
        } catch (Exception e) {
            response.put("status", "DOWN");
            response.put("database", "Connection failed");
            response.put("message", "Failed to connect to database: " + e.getMessage());
            response.put("error", e.getClass().getSimpleName());
        }

        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
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

package com.example.ckdatveexe.module.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/test")
@Tag(name = "Test", description = "Test endpoints for debugging")
public class TestController {

    @Operation(summary = "Simple test endpoint", description = "Test if routing works")
    @GetMapping
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Test endpoint works!");
        response.put("status", "success");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Test routing", description = "Test different routing patterns")
    @GetMapping("/routing")
    public ResponseEntity<Map<String, Object>> testRouting() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "✅ Routing test successful!");
        response.put("endpoint", "/api/test/routing");
        response.put("method", "GET");
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }
}
package com.example.ckdatveexe.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import jakarta.annotation.PostConstruct;

/**
 * Database configuration management
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.jpa.hibernate.ddl-auto:update}")
    private String ddlAuto;

    @Value("${spring.jpa.show-sql:true}")
    private boolean showSql;

    @PostConstruct
    public void logDatabaseConfig() {
        System.out.println("=== DATABASE CONFIGURATION ===");
        System.out.println("DDL Auto: " + ddlAuto);
        System.out.println("Show SQL: " + showSql);
        System.out.println("===============================");

        if ("create".equals(ddlAuto) || "create-drop".equals(ddlAuto)) {
            System.out.println("⚠️  WARNING: DDL mode '" + ddlAuto + "' will DROP existing data!");
        } else if ("update".equals(ddlAuto)) {
            System.out.println("✅ DDL mode 'update' - Safe for production");
        } else if ("validate".equals(ddlAuto)) {
            System.out.println("🔍 DDL mode 'validate' - Read-only schema validation");
        } else if ("none".equals(ddlAuto)) {
            System.out.println("🚫 DDL mode 'none' - No schema management");
        }
    }
}
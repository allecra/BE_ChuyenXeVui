package com.example.ckdatveexe;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnectionTest {
    public static void main(String[] args) {
        String host = "gateway01.ap-southeast-1.prod.aws.tidbcloud.com";
        String port = "4000";
        String database = "ChuyenXeVuiV2";
        String username = "3MSgw5zWgbvqfjL.root";
        String password = "3MCokiSKX9HBnmtS";

        String url = "jdbc:mysql://" + host + ":" + port + "/" + database +
                "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

        try {
            System.out.println("Attempting to connect to database...");
            System.out.println("URL: " + url);
            System.out.println("Username: " + username);

            Connection connection = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Database connection successful!");

            // Test a simple query
            var statement = connection.createStatement();
            var resultSet = statement.executeQuery("SELECT 1 as test");
            if (resultSet.next()) {
                System.out.println("✅ Query test successful: " + resultSet.getInt("test"));
            }

            connection.close();
            System.out.println("✅ Connection closed successfully");

        } catch (Exception e) {
            System.out.println("❌ Database connection failed:");
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
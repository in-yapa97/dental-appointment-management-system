package com.dental.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main application entry point for Dental Appointment and Patient Management System.
 */
@SpringBootApplication
public class DentalManagementApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(DentalManagementApplication.class, args);
    }

    /**
     * Discovers and loads variables from .env file into System properties
     * to support running directly with mvnw spring-boot:run or IDE without manual export.
     */
    private static void loadDotEnv() {
        Path[] searchPaths = new Path[]{
                Paths.get(".env"),
                Paths.get("backend/.env"),
                Paths.get("../backend/.env")
        };

        for (Path envPath : searchPaths) {
            if (Files.exists(envPath)) {
                try (BufferedReader reader = Files.newBufferedReader(envPath)) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                            int eqIndex = line.indexOf('=');
                            String key = line.substring(0, eqIndex).trim();
                            String value = line.substring(eqIndex + 1).trim();
                            if (!key.isEmpty() && System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
                break;
            }
        }
    }
}


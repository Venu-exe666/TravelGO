package com.travelgo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the TravelGo application.
 * 
 * [HOW IT WORKS]:
 * - @SpringBootApplication: Tells Spring Boot to enable auto-configuration, component scanning,
 *   and configuration property support across the 'com.travelgo' package.
 */
@SpringBootApplication
public class TravelGoApplication {

    /**
     * The main execution method that starts the embedded web server (Tomcat) and boots up the Spring application.
     * 
     * [STEP-BY-STEP BREAKDOWN]:
     * 1. Launches the Spring Application context.
     * 2. Scans all packages for Spring components (@Controller, @Service, @Repository).
     * 3. Sets up database connections (MS SQL Server via JPA).
     * 4. Starts the embedded web server on port 8080.
     */
	public static void main(String[] args) {
		SpringApplication.run(TravelGoApplication.class, args);
	}

}


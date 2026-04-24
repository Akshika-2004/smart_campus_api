package com.smartcampus.application;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Main entry point. Starts an embedded Grizzly HTTP server on port 8080.
 * No external Tomcat or application server needed.
 */
public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    public static final String BASE_URI = "http://localhost:8080/api/v1/";

    public static void main(String[] args) throws Exception {

        // Create application config from SmartCampusApplication
        // (includes @ApplicationPath("/api/v1") and all resource registrations)
        ResourceConfig config = new SmartCampusApplication();

        // Start the embedded HTTP server
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(
                URI.create(BASE_URI), config);

        LOGGER.info("==============================================");
        LOGGER.info(" Smart Campus API is running!");
        LOGGER.info(" Base URL : http://localhost:8080/");
        LOGGER.info(" API Root : " + BASE_URI);
        LOGGER.info(" Press CTRL+C to stop.");
        LOGGER.info("==============================================");

        // Keep running until CTRL+C
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("Shutting down Smart Campus API...");
            server.shutdownNow();
        }));

        Thread.currentThread().join();
    }
}
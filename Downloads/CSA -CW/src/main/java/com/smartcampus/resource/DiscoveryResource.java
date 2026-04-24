package com.smartcampus.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Part 1.2 - Discovery Endpoint
 * GET /api/v1 returns API metadata including version, contact and resource links.
 * This is the HATEOAS "entry point" - clients can discover all resources from here.
 */
@Path("")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new HashMap<>();

        // API metadata
        response.put("name", "Smart Campus API");
        response.put("version", "v1");
        response.put("description", "RESTful API for managing campus rooms and sensors");
        response.put("contact", "admin@smartcampus.ac.uk");

        // HATEOAS - links to all primary resource collections
        Map<String, String> links = new HashMap<>();
        links.put("self",    "/api/v1");
        links.put("rooms",   "/api/v1/rooms");
        links.put("sensors", "/api/v1/sensors");
        response.put("links", links);

        return Response.ok(response).build();
    }
}

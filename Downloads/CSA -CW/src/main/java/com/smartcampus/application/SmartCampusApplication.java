package com.smartcampus.application;

import javax.ws.rs.ApplicationPath;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.jackson.JacksonFeature;

import com.smartcampus.resource.DiscoveryResource;
import com.smartcampus.resource.RoomResource;
import com.smartcampus.resource.SensorResource;
import com.smartcampus.exception.*;
import com.smartcampus.filter.LoggingFilter;

/**
 * JAX-RS Application configuration.
 * @ApplicationPath sets the base URL for all API endpoints to /api/v1
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {
    
    public SmartCampusApplication() {
        // Register all resources
        register(DiscoveryResource.class);
        register(RoomResource.class);
        register(SensorResource.class);
        
        // Register all exception mappers
        register(LinkedResourceNotFoundExceptionMapper.class);
        register(RoomNotEmptyExceptionMapper.class);
        register(SensorUnavailableExceptionMapper.class);
        register(GlobalExceptionMapper.class);
        
        // Register filters
        register(LoggingFilter.class);
        
        // Register Jackson for JSON support
        register(JacksonFeature.class);
    }
}
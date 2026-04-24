package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Part 5.5 - API Request & Response Logging Filter
 * Implements both ContainerRequestFilter and ContainerResponseFilter
 * to log every incoming request and outgoing response.
 *
 * This is a "cross-cutting concern" — by handling it here in one place,
 * we avoid duplicating Logger.info() calls in every resource method.
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(LoggingFilter.class.getName());

    /**
     * Runs BEFORE the request reaches the resource method.
     * Logs the HTTP method and full URI.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
            "[REQUEST]  Method: %s | URI: %s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString()
        ));
    }

    /**
     * Runs AFTER the resource method has returned a response.
     * Logs the final HTTP status code.
     */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
            "[RESPONSE] Method: %s | URI: %s | Status: %d",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri().toString(),
            responseContext.getStatus()
        ));
    }
}

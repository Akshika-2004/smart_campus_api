package com.smartcampus.exception;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Part 5.4 - Global Safety Net
 * Catches ALL uncaught Throwables (NullPointerException, etc.)
 * and returns a clean 500 JSON response.
 * NEVER exposes stack traces to API consumers (security risk).
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Preserve the correct HTTP status for expected JAX-RS exceptions (404/405/415/etc.)
        // so this catch-all mapper doesn't incorrectly turn them into 500s.
        if (ex instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) ex;
            Response original = webEx.getResponse();
            int status = original != null ? original.getStatus() : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

            String error = Response.Status.fromStatusCode(status) != null
                ? Response.Status.fromStatusCode(status).getReasonPhrase()
                : "Error";

            Map<String, Object> body = new HashMap<>();
            body.put("status", status);
            body.put("error", error);
            body.put("message", error);

            return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
        }

        // Log the full stack trace server-side for debugging
        LOGGER.log(Level.SEVERE, "Unhandled exception", ex);

        // Return a clean, generic error to the client — no stack trace!
        Map<String, Object> body = new HashMap<>();
        body.put("status", 500);
        body.put("error", "Internal Server Error");
        body.put("message", "An unexpected error occurred. Please contact the administrator.");

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}

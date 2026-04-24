package com.smartcampus.exception;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

/**
 * Part 5.2 - Maps LinkedResourceNotFoundException → HTTP 422 Unprocessable Entity
 * Used when a sensor references a roomId that does not exist.
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("status", 422);
        body.put("error", "Unprocessable Entity");
        body.put("message", ex.getMessage());
        return Response.status(422)
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}

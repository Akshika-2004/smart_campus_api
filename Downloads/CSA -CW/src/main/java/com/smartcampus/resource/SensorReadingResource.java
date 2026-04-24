package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Part 4.2 - Sensor Reading Sub-Resource
 * Handles GET and POST for /api/v1/sensors/{sensorId}/readings
 * Instantiated by SensorResource via the Sub-Resource Locator pattern.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full reading history for this sensor.
     */
    @GET
    public Response getReadings() {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error("Sensor not found: " + sensorId)).build();
        }

        List<SensorReading> history = DataStore.getReadings().get(sensorId);
        if (history == null) {
            return Response.ok(Collections.emptyList()).build();
        }

        List<SensorReading> snapshot;
        synchronized (history) {
            snapshot = new ArrayList<>(history);
        }
        return Response.ok(snapshot).build();
    }

    /**
     * POST /api/v1/sensors/{sensorId}/readings
     * Appends a new reading for this sensor.
     *
     * Business rules:
     * - Sensor must exist.
     * - Sensor status must NOT be "MAINTENANCE" → 403 if it is.
     * - Side effect: updates the parent sensor's currentValue.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error("Sensor not found: " + sensorId)).build();
        }

        // State constraint: MAINTENANCE sensors cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept readings."
            );
        }

        // Auto-fill id and timestamp if not provided
        if (reading.getId() == null) {
            reading.setId(java.util.UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Store the reading
        DataStore.getReadings()
            .computeIfAbsent(sensorId, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(reading);

        // Side effect: update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        URI location = URI.create("/api/v1/sensors/" + sensorId + "/readings/" + reading.getId());
        return Response.created(location).entity(reading).build();
    }

    private Map<String, String> error(String message) {
        return Collections.singletonMap("error", message);
    }
}

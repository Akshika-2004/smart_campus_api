package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Part 3 - Sensor Operations
 * Handles all /api/v1/sensors endpoints.
 * Also acts as the sub-resource locator for /api/v1/sensors/{sensorId}/readings.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * GET /api/v1/sensors?type=CO2
     * Returns all sensors, optionally filtered by type query parameter.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> all = DataStore.getSensors().values();

        if (type != null && !type.isBlank()) {
            // Filter by type (case-insensitive for robustness)
            List<Sensor> filtered = all.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
            return Response.ok(filtered).build();
        }

        return Response.ok(all).build();
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor. Validates that the referenced roomId actually exists.
     * If roomId is invalid → throws LinkedResourceNotFoundException → 422 response.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error("Sensor 'id' is required.")).build();
        }

        // Validate the roomId foreign key
        if (sensor.getRoomId() == null || !DataStore.getRooms().containsKey(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: room with id '" + sensor.getRoomId() + "' does not exist."
            );
        }

        Sensor existing = DataStore.getSensors().putIfAbsent(sensor.getId(), sensor);
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(error("Sensor with id '" + sensor.getId() + "' already exists.")).build();
        }

        DataStore.getReadings().putIfAbsent(sensor.getId(), Collections.synchronizedList(new ArrayList<>()));

        // Link this sensor to its room
        DataStore.getRooms().get(sensor.getRoomId()).getSensorIds().add(sensor.getId());

        URI location = URI.create("/api/v1/sensors/" + sensor.getId());
        return Response.created(location).entity(sensor).build();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a single sensor by ID.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensors().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error("Sensor not found: " + sensorId)).build();
        }
        return Response.ok(sensor).build();
    }

    /**
     * Part 4.1 - Sub-Resource Locator
     * Any request to /api/v1/sensors/{sensorId}/readings is delegated
     * to SensorReadingResource. This is the Sub-Resource Locator pattern.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    private Map<String, String> error(String message) {
        return Collections.singletonMap("error", message);
    }
}

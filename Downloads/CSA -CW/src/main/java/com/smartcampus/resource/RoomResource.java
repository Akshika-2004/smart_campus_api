package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * Part 2 - Room Management
 * Handles all /api/v1/rooms endpoints.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns full list of all rooms.
     */
    @GET
    public Response getAllRooms() {
        Collection<Room> rooms = DataStore.getRooms().values();
        return Response.ok(rooms).build();
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with a Location header.
     */
    @POST
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room 'id' is required."))
                    .build();
        }

        Room existing = DataStore.getRooms().putIfAbsent(room.getId(), room);
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A room with id '" + room.getId() + "' already exists."))
                    .build();
        }

        // 201 Created + Location header pointing to the new resource
        URI location = URI.create("/api/v1/rooms/" + room.getId());
        return Response.created(location).entity(room).build();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns a single room by ID.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room not found: " + roomId))
                    .build();
        }
        return Response.ok(room).build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Blocked (409) if the room still has sensors assigned.
     * This operation is IDEMPOTENT - deleting a non-existent room returns 404,
     * but deleting the same room twice has no additional side effects.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRooms().get(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room not found: " + roomId))
                    .build();
        }

        // Business rule: cannot delete a room that still contains sensors
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "'. It still has " +
                room.getSensorIds().size() + " sensor(s) assigned. " +
                "Please remove all sensors first."
            );
        }

        DataStore.getRooms().remove(roomId);
        return Response.noContent().build(); // 204 No Content
    }

    private Map<String, String> errorBody(String message) {
        return java.util.Collections.singletonMap("error", message);
    }
}
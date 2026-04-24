package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central in-memory data store for the Smart Campus API.
 * Uses ConcurrentHashMap for thread safety (important because JAX-RS
 * creates a new resource instance per request, so multiple threads
 * can access this shared static data simultaneously).
 */
public class DataStore {

    // Static maps shared across all requests
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();
    private static final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // Pre-load some sample data so the API isn't empty on first run
    static {
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Science Lab", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);

        // Link sensors to their rooms
        r1.getSensorIds().add(s1.getId());
        r2.getSensorIds().add(s2.getId());

        // Sample readings
        List<SensorReading> r1Readings = Collections.synchronizedList(new ArrayList<>());
        r1Readings.add(new SensorReading(22.5));
        readings.put(s1.getId(), r1Readings);

        List<SensorReading> r2Readings = Collections.synchronizedList(new ArrayList<>());
        r2Readings.add(new SensorReading(400.0));
        readings.put(s2.getId(), r2Readings);
    }

    public static Map<String, Room> getRooms() { return rooms; }
    public static Map<String, Sensor> getSensors() { return sensors; }
    public static Map<String, List<SensorReading>> getReadings() { return readings; }
}
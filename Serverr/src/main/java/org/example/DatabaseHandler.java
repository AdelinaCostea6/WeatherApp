package org.example;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Optional;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseHandler {
    private static final String URL = "jdbc:postgresql://localhost:5432/weather_app";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1q2w3e";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }


    private static final double MAX_DISTANCE = 100;

    public Optional<String> getClosestLocation(int userLat, int userLon) throws SQLException {
        String query = "SELECT name, latitude, longitude FROM locations";
        List<Map.Entry<String, int[]>> locations = new ArrayList<>();

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("name");
                int lat = rs.getInt("latitude");
                int lon = rs.getInt("longitude");
                locations.add(new AbstractMap.SimpleEntry<>(name, new int[]{lat, lon}));
            }
        }


        return locations.stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), calculateDistance(userLat, userLon, entry.getValue()[0], entry.getValue()[1])))
                .filter(entry -> entry.getValue() <= MAX_DISTANCE)
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey);
    }

    private double calculateDistance(int lat1, int lon1, int lat2, int lon2) {
        return Math.sqrt(Math.pow(lat1 - lat2, 2) + Math.pow(lon1 - lon2, 2));
    }

    public JSONArray getWeatherForecast(String location) {
        try {
            String jsonData = new String(Files.readAllBytes(Paths.get("received_weather.json")));
            JSONArray jsonArray = new JSONArray(jsonData);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                if (obj.getString("location").equalsIgnoreCase(location)) {
                    return obj.getJSONObject("weather").getJSONArray("forecast");
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading weather forecast: " + e.getMessage());
        }
        return null;
    }


    public String getWeatherData(String location) throws SQLException {
        String query = "SELECT temperature, description FROM locations WHERE name = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, location);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return "Temperature: " + rs.getInt("temperature") + "°C, Description: " + rs.getString("description");
            }
        }
        return "No data available.";
    }

    public boolean locationExists(String locationName) throws SQLException {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM locations WHERE name = ?")) {
            stmt.setString(1, locationName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void insertLocation(String name, int latitude, int longitude, int temperature, String description) throws SQLException {
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement("INSERT INTO locations (name, latitude, longitude, temperature, description) VALUES (?, ?, ?, ?, ?)")) {
            stmt.setString(1, name);
            stmt.setInt(2, latitude);
            stmt.setInt(3, longitude);
            stmt.setInt(4, temperature);
            stmt.setString(5, description);
            stmt.executeUpdate();
        }
    }
    public void updateUserCoordinates(String username, int latitude, int longitude) throws SQLException {
        String query = "UPDATE users SET latitude = ?, longitude = ? WHERE username = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, latitude);
            stmt.setInt(2, longitude);
            stmt.setString(3, username);
            stmt.executeUpdate();
        }
    }

}

package org.example;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Optional;
import org.json.*;

public class ClientHandler extends Thread {
    private final Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private final AuthService authService;
    private final DatabaseHandler dbHandler;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.authService = new AuthService();
        this.dbHandler = new DatabaseHandler();
    }

    @Override
    public void run() {
        try {
            System.out.println("Client connected: " + socket.getRemoteSocketAddress());

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            boolean sessionActive = true;

            while (sessionActive) {
                out.println("Welcome!");
                String command = in.readLine();

                if (command == null || command.equalsIgnoreCase("exit")) {
                    System.out.println("Client disconnected.");
                    sessionActive = false;
                    break;
                }

                if ("register".equalsIgnoreCase(command)) {
                    User user = null;
                    while (user == null) {
                        user = registerUser();
                        if (user == null) {
                            out.println("Registration failed! Username might already exist. Type 'exit' to cancel or try again.");
                        } else {
                            out.println("Registration successful!");
                            out.println(user.getRole());
                            handleUserSession(user);
                            break;
                        }
                    }
                }

                if ("login".equalsIgnoreCase(command)) {
                    User user = null;
                    while (user == null) {
                        user = loginUser();
                        if (user == null) {
                            out.println("Invalid credentials. Type 'exit' to cancel or try again.");
                        } else {
                            out.println(user.getRole().equals("admin") ? "Admin access granted." : "Welcome, regular user!");
                            handleUserSession(user);
                            break;
                        }
                    }
                }
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error during client interaction: " + e.getMessage());
        }
    }

    private User registerUser() throws IOException, SQLException {
        // Citire username
        String username = in.readLine();
        System.out.println("Received username: " + username);

        if (username.equalsIgnoreCase("exit")|| username == null || username.trim().isEmpty()) {
            return null;
        }
        if (username == null || username.trim().isEmpty()) {
            System.out.println("Username not provided. Closing connection.");
        }


        String password = in.readLine();
        System.out.println("Received password: " + password);


        String role = in.readLine();
        System.out.println("Received role: " + role);


        int latitude = Integer.parseInt(in.readLine().trim());
        System.out.println("Received latitude: " + latitude);

        int longitude = Integer.parseInt(in.readLine().trim());
        System.out.println("Received longitude: " + longitude);

        Optional<User> userOptional = authService.register(username, password, role, latitude, longitude);
        return userOptional.orElse(null);

    }

    private User loginUser() throws IOException, SQLException {
        String username = in.readLine().trim();
        if (username.equalsIgnoreCase("exit")|| username == null || username.trim().isEmpty()) {
            out.println("Exiting...");
            return null;
        }
        String password = in.readLine().trim();
        Optional<User> userOptional = authService.login(username, password);
        return userOptional.orElse(null);
    }


    private void handleUserSession(User user) throws IOException, SQLException {
        if ("admin".equalsIgnoreCase(user.getRole())) {
            System.out.println("Admin session started for: " + user.getUsername());

            while (true) {

                String adminCommand = in.readLine().trim();

                if ("import".equalsIgnoreCase(adminCommand)) {
                    importWeatherData();
                } else if ("weather".equalsIgnoreCase(adminCommand)) {
                    sendWeatherInfo(user);
                } else if ("update_coordinates".equalsIgnoreCase(adminCommand)) {
                    updateUserCoordinates(user);
                } else if ("exit".equalsIgnoreCase(adminCommand)) {
                    out.println("Exiting session...");
                    break;
                } else {
                    out.println("Unknown command. Please use 'import', 'weather', 'update_coordinates', or 'exit'.");
                }
            }
        } else {

            System.out.println("User session started for: " + user.getUsername());
                String userCommand = in.readLine().trim();
                if ("weather".equalsIgnoreCase(userCommand)) {
                    sendWeatherInfo(user);
                } else if ("exit".equalsIgnoreCase(userCommand)) {
                    out.println("Exiting session...");

                } else {
                    out.println("Unknown command. Please use 'weather' or 'exit'.");
            }
        }
    }

    private void sendWeatherInfo(User user) throws SQLException {
        Optional<String> closestLocation = dbHandler.getClosestLocation(user.getLatitude(), user.getLongitude());
        if (closestLocation.isPresent()) {
            String location = closestLocation.get();
            out.println("Closest location: " + location);
            out.println("Current weather:");
            String currentWeather = dbHandler.getWeatherData(location);
            out.println(currentWeather);

            JSONArray forecastArray = dbHandler.getWeatherForecast(location);
            if (forecastArray != null) {
                out.println("Weather forecast for the next days:");
                for (int i = 0; i < forecastArray.length(); i++) {
                    JSONObject forecast = forecastArray.getJSONObject(i);
                    out.println(forecast.getString("day") + ": " + forecast.getInt("temperature") + "°C, " + forecast.getString("description"));
                }
            } else {
                out.println("No forecast data available.");
            }
        } else {
            out.println("No location found within the specified range.");
        }
        out.println("END");
    }


    private void updateUserCoordinates(User user) throws IOException, SQLException {
        String[] coordinates = in.readLine().split(" ");
        if (coordinates.length != 2) {
            out.println("Invalid coordinates format.");
            out.println("END");
            return;
        }
        int newLat = Integer.parseInt(coordinates[0]);
        int newLon = Integer.parseInt(coordinates[1]);


        dbHandler.updateUserCoordinates(user.getUsername(), newLat, newLon);
        user.setLatitude(newLat);
        user.setLongitude(newLon);
        out.println("Coordinates updated successfully!");
        out.println("END");
        System.out.println("User coordinates updated in memory: " + newLat + ", " + newLon);
    }



    private void importWeatherData() throws IOException {
        out.println("Waiting for JSON file...");
        InputStream inputStream = socket.getInputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;

        FileOutputStream fos = new FileOutputStream("received_weather.json");
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
            if (bytesRead < buffer.length) {
                break;
            }
        }
        fos.close();

        try {
            JSONArray jsonArray = new JSONArray(new String(Files.readAllBytes(Paths.get("received_weather.json"))));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                String locationName = obj.getString("location");
                int latitude = obj.getJSONObject("coordinates").getInt("latitude");
                int longitude = obj.getJSONObject("coordinates").getInt("longitude");
                int temperature = obj.getJSONObject("weather").getInt("temperature");
                String description = obj.getJSONObject("weather").getString("description");

                if (!dbHandler.locationExists(locationName)) {
                    dbHandler.insertLocation(locationName, latitude, longitude, temperature, description);
                    out.println("Inserted location: " + locationName);
                } else {
                    out.println("Location already exists: " + locationName);
                }
            }
            out.println("Weather data imported successfully!");
        } catch (SQLException e) {
            out.println("Error importing weather data: " + e.getMessage());
            e.printStackTrace();
        }

        out.println("END");
    }

}

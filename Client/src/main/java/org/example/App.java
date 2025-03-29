package org.example;
import java.net.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class App {
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, PORT);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println(in.readLine());

            boolean loggedInOrRegistered = false;

            while (!loggedInOrRegistered) {
                System.out.println("Type 'login' to log in, 'register' to create a new account, or 'exit' to quit:");
                String command = stdIn.readLine().trim();
                out.println(command);

                if (command.equalsIgnoreCase("exit")) {
                    System.out.println("Exiting application...");
                    break;
                }

                if ("register".equalsIgnoreCase(command)) {
                    boolean registered = false;
                    while (!registered) {
                        System.out.println("Enter username:");
                        String username = stdIn.readLine();
                        if (username.equalsIgnoreCase("exit")) {
                            System.out.println("Exiting registration...");
                            out.println("exit");
                            break;
                        }
                        out.println(username);

                        System.out.println("Enter password:");
                        out.println(stdIn.readLine());

                        System.out.println("Enter role (user/admin):");
                        out.println(stdIn.readLine().trim());

                        System.out.println("Enter your latitude:");
                        out.println(stdIn.readLine());

                        System.out.println("Enter your longitude:");
                        out.println(stdIn.readLine());

                        String serverResponse = in.readLine();
                        System.out.println("Server response: " + serverResponse);

                        if (serverResponse.contains("Registration successful!")) {
                            registered = true;
                            loggedInOrRegistered = true;
                            String role = in.readLine();
                            System.out.println("Role: " + role);
                            handleSession(role, in, out, stdIn);
                        }
                    }
                }

                if ("login".equalsIgnoreCase(command)) {
                    boolean loggedIn = false;
                    while (!loggedIn) {
                        System.out.println("Enter username:");
                        String username = stdIn.readLine();
                        if (username.equalsIgnoreCase("exit")) {
                            System.out.println("Exiting log...");
                            out.println("exit");
                            break;
                        }
                        out.println(username);

                        System.out.println("Enter password:");
                        out.println(stdIn.readLine());

                        String serverResponse = in.readLine();
                        System.out.println(serverResponse);

                        if (serverResponse.contains("Admin access granted") || serverResponse.contains("Welcome, regular user")) {
                            loggedIn = true;
                            loggedInOrRegistered = true;
                            String role = serverResponse.contains("Admin") ? "admin" : "user";
                            handleSession(role, in, out, stdIn);
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
    }

    private static void handleSession(String role, BufferedReader in, PrintWriter out, BufferedReader stdIn) throws IOException {
        if ("admin".equalsIgnoreCase(role)) {
            while (true) {
                System.out.println("Type 'import' to load JSON data, 'weather' to get weather info, 'update_coordinates' to update your coordinates, or 'exit' to quit:");
                String adminCommand = stdIn.readLine().trim();
                out.println(adminCommand);

                if ("import".equalsIgnoreCase(adminCommand)) {
                    sendJsonFile(out);
                    String line;
                    while ((line = in.readLine()) != null && !line.equals("END")) {
                        System.out.println(line);
                    }
                } else if ("weather".equalsIgnoreCase(adminCommand)) {
                    readWeatherInfo(in);
                } else if ("update_coordinates".equalsIgnoreCase(adminCommand)) {
                        System.out.println("Enter new latitude and longitude:");
                        out.println(stdIn.readLine());

                        String serverResponse;
                        while ((serverResponse = in.readLine()) != null) {
                            if ("END".equals(serverResponse)) {
                                break;
                            }
                            System.out.println("Server response: " + serverResponse);
                        }

                } else if ("exit".equalsIgnoreCase(adminCommand)) {
                    System.out.println("Exiting session...");
                    break;
                }
            }
        } else if ("user".equalsIgnoreCase(role)) {
            System.out.println("Type 'weather' to get weather info or 'exit' to quit:");
                String userCommand = stdIn.readLine().trim();
                out.println(userCommand);

                if ("weather".equalsIgnoreCase(userCommand)) {
                    readWeatherInfo(in);
                } else if ("exit".equalsIgnoreCase(userCommand)) {
                    System.out.println("Exiting session...");

            }
        }
    }

    private static void sendJsonFile(PrintWriter out) {
        try {
            String filePath = "C:/Users/Dell/Desktop/BD/Client/src/main/java/org/example/weather.json";  // Fișier JSON
            byte[] fileData = Files.readAllBytes(Paths.get(filePath));
            out.println(new String(fileData));
            System.out.println("JSON file sent to the server!");
        } catch (IOException e) {
            System.err.println("Error sending JSON file: " + e.getMessage());
        }
    }

    private static void readWeatherInfo(BufferedReader in) throws IOException {
        System.out.println("Weather Info from Server:");
        String line;
        while ((line = in.readLine()) != null && !line.equals("END")) {
            System.out.println(line);
        }
    }
}

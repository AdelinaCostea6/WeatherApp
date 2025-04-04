package org.example;
import java.sql.*;
import java.util.Optional;

public class AuthService {
    private final DatabaseHandler dbHandler = new DatabaseHandler();


    public Optional<User> login(String username, String password) throws SQLException {
        try (Connection conn = dbHandler.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT role, latitude, longitude FROM users WHERE username = ? AND password = ?")) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String role = rs.getString("role");
                int latitude = rs.getInt("latitude");
                int longitude = rs.getInt("longitude");
                return Optional.of(new User(username, password, role, latitude, longitude));
            }
        }
        return Optional.empty();
    }

    public Optional<User> register(String username, String password, String role, int latitude, int longitude) throws SQLException {
        try (Connection conn = dbHandler.connect()) {
            String checkUserQuery = "SELECT COUNT(*) FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Username already exists. Registration failed.");
                    return Optional.empty();
                }
            }

            String insertUserQuery = "INSERT INTO users (username, password, role, latitude, longitude) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserQuery)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, role);
                insertStmt.setInt(4, latitude);
                insertStmt.setInt(5, longitude);

                int rowsAffected = insertStmt.executeUpdate();
                if (rowsAffected > 0) {
                    System.out.println("User successfully registered: " + username);
                    return Optional.of(new User(username, password, role, latitude, longitude));
                } else {
                    return Optional.empty();
                }
            }
        } catch (SQLException e) {
            System.out.println("Error during registration: " + e.getMessage());
            return Optional.empty();
        }
    }

}

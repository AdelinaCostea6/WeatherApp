# WeatherApp

This Java-based application is designed to manage user accounts and provide weather information based on geographical coordinates. The system consists of the following key components:

## Features

### 1. **User Registration and Login**
- Users can register by providing a username, password, role (admin/user), and location coordinates (latitude and longitude).
- Upon registration or login, the application authenticates users and provides them access based on their role.

### 2. **Weather Data Management**
- Admin users can upload weather data in JSON format and update the database with new weather information.
- Regular users can view the current weather and forecasts for locations close to their current geographical coordinates.

### 3. **Database Integration**
- The application uses PostgreSQL to store and retrieve user and weather data, including locations and weather details (temperature, description, forecast).

### 4. **Client-Server Communication**
- The server (running on port 1234) listens for client connections, handling registration, login, and weather data requests using a socket-based communication model.
- The client interacts with the server by sending commands such as:
  - `login`
  - `register`
  - `weather`
  - `import`
  - `update_coordinates`

### 5. **JSON Data Import**
- Weather data is stored in JSON format, including locations, current weather, and forecasts. Admin users can import weather data to update the system.

## How It Works

1. **Server**:
   - Listens for incoming client connections on port `1234`.
   - Handles registration, login, and weather requests.
   - Admins can import weather data or update user coordinates.
   - Regular users can check weather info based on their coordinates.

2. **Client**:
   - Connects to the server using a socket.
   - Supports commands for logging in, registering, viewing weather, importing data, and updating coordinates.

## Requirements
- **Java 11+**
- **PostgreSQL Database** for storing user and weather data
- **JSON File** for importing weather data




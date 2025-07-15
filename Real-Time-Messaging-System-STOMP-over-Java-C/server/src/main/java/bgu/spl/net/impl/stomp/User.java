package bgu.spl.net.impl.stomp;

public class User {
    private final String username;
    private final String password;
    private boolean connected; // Indicates whether the user is currently connected
    private int connectionId;  // Stores the connection ID if the user is connected

    // Constructor to initialize a new user with a username and password
    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.connected = false; // Default
        this.connectionId = -1; // Default to no connection ID
    }

    // Getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public boolean isConnected() {
        return connected;
    }

    public int getConnectionId() {
        return connectionId;
    }

    // Setters
    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void setConnectionId(int connectionId) {
        this.connectionId = connectionId;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", connected=" + connected +
                ", connectionId=" + connectionId +
                '}';
    }
}



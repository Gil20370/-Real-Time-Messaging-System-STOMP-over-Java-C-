package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;

public class ServerStorage {
    private final ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
    private static ServerStorage instance;

    private ServerStorage() {}

    // Get the singleton instance
    public static synchronized ServerStorage getInstance() {
        if (instance == null) {
            instance = new ServerStorage();
        }
        return instance;
    }

    public boolean addUser(String username, String password) {
        return users.putIfAbsent(username, new User(username, password)) == null;
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    public synchronized void connectUser(String username, int connectionId) {
        User user = users.get(username);
        if (user != null) {
            user.setConnected(true);
            user.setConnectionId(connectionId);
        }
    }

    public synchronized void disconnectUser(int connectionId) {
        users.values().stream()
             .filter(user -> user.getConnectionId() == connectionId)
             .findFirst()  // We can assume only one user matches the connectionId
             .ifPresent(user -> user.setConnected(false));
    }
}
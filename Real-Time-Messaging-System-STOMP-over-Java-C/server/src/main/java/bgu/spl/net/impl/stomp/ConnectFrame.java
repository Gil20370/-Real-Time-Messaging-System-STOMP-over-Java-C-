package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

public class ConnectFrame extends Frame {

    public ConnectFrame(){
        super("CONNECT");
    }

    public ConnectFrame(String login, String passcode){
        super("CONNECT");
        addHeader("version", "1.2");
        addHeader("host", "stomp.bgu.ac.il");
        addHeader("login", login);
        addHeader("passcode", passcode);
    } 

   
    public void process(int connectionId, Connections<Frame> connections) {
        // Validate headers
        if (!headers.containsKey("login") || !headers.containsKey("passcode")) {
            sendErrorAndDisconnect(connectionId, "Missing login or passcode", connections);
            return;
        }
    
        String login = headers.get("login");
        String passcode = headers.get("passcode");
    
        // Check if the user exists in the server's storage
        User user = ServerStorage.getInstance().getUser(login); 
        
        if (user == null) {
            // New user: create and store the user
            user = new User(login, passcode);
            ServerStorage.getInstance().addUser(login, passcode);
            ServerStorage.getInstance().connectUser(login,connectionId);
            connections.send(connectionId, new ConnectedFrame()); // Success
        } 
        else {


            // Ensure the user is not already connected
            if (user.isConnected()) {
                sendErrorAndDisconnect(connectionId, "User already logged in", connections);
                return;
            }
            // Existing user: validate credentials
            if (!user.getPassword().equals(passcode)) {
                sendErrorAndDisconnect(connectionId, "Wrong password", connections);
                return;
            }
    
            
    
            // Mark the user as connected and store the connectionId
            ServerStorage.getInstance().connectUser(login,connectionId);
            user.setConnectionId(connectionId); // Optionally, store the connectionId in the User object
            connections.send(connectionId, new ConnectedFrame()); // Success
        }
    }
    
    // Helper method for error handling and disconnecting
    private void sendErrorAndDisconnect(int connectionId, String errorMessage, Connections<Frame> connections) {
        String receiptId=getHeader("receipt");
        connections.send(connectionId, new ErrorFrame(errorMessage,printFormattedMessage(toString()),receiptId));
        connections.disconnect(connectionId); // Disconnect the client
    }

    public String printFormattedMessage(String content) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append("the message:\n");
        formattedMessage.append("-----\n");
        formattedMessage.append(content).append("\n");
        formattedMessage.append("-----");
        return formattedMessage.toString();
    }

}

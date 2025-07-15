package bgu.spl.net.impl.stomp;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import bgu.spl.net.srv.Connections;

public abstract class Frame {
    protected String command; // The STOMP command (e.g., CONNECT, SEND)
    protected ConcurrentHashMap<String, String> headers; // Key-value pairs for frame headers
    protected String body; // The body of the frame, can be empty

    public Frame(String command) {
        this.command = command;
        this.headers = new ConcurrentHashMap<>();
        this.body = "";
    }

    

    // method for processing frames- shall be overrided only by client frames
    public void process(int connectionId, Connections<Frame> connections){}

    // Add a header to the frame
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    // Get a header value by key
    public String getHeader(String key) {
        return headers.get(key);
    }

    // Set the body of the frame
    public void setBody(String body) {
        this.body = body;
    }

    // Get the body of the frame
    public String getBody() {
        return body;
    }

    


    // Serialize the frame to a string
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        // Add the command
        builder.append(command).append("\n"); // Example: CONNECTED
        
        // Add headers
        headers.forEach((key, value) -> builder.append(key).append(":").append(value).append("\n")); 
        
        // Add an empty line between headers and the body
        builder.append("\n");
        
        // Add the body (if it exists)
        if (body != null && !body.isEmpty()) {
            builder.append(body);
        }
        
        // Add the null terminator
        builder.append("\u0000");
        
        return builder.toString();
    }

 
}
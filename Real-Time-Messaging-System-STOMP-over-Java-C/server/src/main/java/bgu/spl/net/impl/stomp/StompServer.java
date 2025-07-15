package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Server;

public class StompServer {

    public static void main(String[] args) {
        System.err.println("hey");

// Validate command-line arguments
if (args.length != 2) {
    System.err.println("Usage: StompServer <port> <server-type>");
    System.err.println("<server-type>: 'tpc' for Thread-per-Client or 'reactor'");
    System.exit(1);
}

int port = Integer.parseInt(args[0]);
String serverType = args[1];

// Initialize the server based on the server type
Server<Frame> server;
switch (serverType.toLowerCase()) {
    case "tpc":
        // Thread-per-Client server
        server = Server.threadPerClient(
            port,
            StompMessagesProtocol::new,  // Protocol factory
            StompEncodeDecoder::new           // Encoder/decoder factory
        );
        break;

    case "reactor":
        // Reactor server
        server = Server.reactor(
            Runtime.getRuntime().availableProcessors(), // Number of threads
            port,
            StompMessagesProtocol::new,  // Protocol factory
            StompEncodeDecoder::new          // Encoder/decoder factory
        );
        break;

    default:
        System.err.println("Invalid server type. Use 'tpc' or 'reactor'.");
        System.exit(1);
        return; // Exit early if invalid type
}

// Start the server
System.out.println("Starting " + serverType.toUpperCase() + " server on port " + port);
server.serve(); // Blocks and starts the server
}
}






 

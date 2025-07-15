package bgu.spl.net.impl.stomp;

import java.nio.charset.StandardCharsets;

import bgu.spl.net.api.MessageEncoderDecoder;

public class StompEncodeDecoder implements MessageEncoderDecoder<Frame> {
        private StringBuilder buffer = new StringBuilder();
    
        @Override
        public Frame decodeNextByte(byte nextByte) {
            if (nextByte == '\u0000') { // End of frame
                String rawFrame = buffer.toString();
                buffer.setLength(0);
    
                // Convert the raw string to a Frame object
                return parse(rawFrame);
            } else {
                buffer.append((char) nextByte);
                return null; // Wait for more data
            }
        }
    
        @Override
        public byte[] encode(Frame frame) {
            // Serialize the Frame object and append the null terminator
            return (frame.toString()).getBytes(StandardCharsets.UTF_8);
        }

        public static Frame parse(String rawFrame) {
            String[] lines = rawFrame.split("\n");
            if (lines.length == 0) {
                throw new IllegalArgumentException("Empty frame");
            }
    
            // Extract command (first line)
            String command = lines[0].trim();
    
            // Create the appropriate Frame subclass
            Frame frame = createFrame(command);
    
            // Parse headers
            int i = 1;
            while (i < lines.length && !lines[i].isEmpty()) {
                String[] headerParts = lines[i].split(":", 2);
                if (headerParts.length == 2) {
                    frame.addHeader(headerParts[0].trim(), headerParts[1].trim());
                } else {
                    throw new IllegalArgumentException("Malformed header: " + lines[i]);
                }
                i++;
            }
    
            // Parse body
            if (i < lines.length - 1) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int j = i + 1; j < lines.length; j++) {
                    bodyBuilder.append(lines[j]).append("\n");
                }
                frame.setBody(bodyBuilder.toString().trim());
            }
    
            return frame;
        }
    
        private static Frame createFrame(String command) {
            switch (command) {
                case "CONNECT":
                    return new ConnectFrame();
                case "SEND":
                    return new SendFrame();
                case "SUBSCRIBE":
                    return new SubscribeFrame();
                case "UNSUBSCRIBE":
                    return new UnsubscribeFrame();
                case "DISCONNECT":
                    return new DisconnectFrame();
                default:
                    throw new IllegalArgumentException("Unknown command: " + command);
            }
        }
       
}




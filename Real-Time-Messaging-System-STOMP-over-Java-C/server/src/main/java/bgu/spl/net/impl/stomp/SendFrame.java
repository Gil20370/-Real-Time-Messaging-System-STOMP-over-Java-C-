package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

public class SendFrame extends Frame {

    public SendFrame(){
        super("SEND");

    }

    public SendFrame(String topic, String messageContent){
        super("SEND");
        if (topic.startsWith("/")) 
            topic = topic.substring(1);
        addHeader("destination",topic);
        setBody(messageContent);
    }

    
    public void process(int connectionId, Connections<Frame> connections){    
        String topic = headers.get("destination");
        if (topic == null || body.isEmpty()) {
            String receiptId=getHeader("receipt");
            connections.send(connectionId,new ErrorFrame("Invalid SEND frame",printFormattedMessage(toString()),receiptId));
            connections.disconnect(connectionId); // Remove the client after sending the error frame

        }
        else
        {
            if (topic.startsWith("/")) 
            topic = topic.substring(1);
            connections.send(topic, new MessageFrame(topic, body)); // Broadcast to subscribers
        }
    }

    public String printFormattedMessage(String content) {
        StringBuilder formattedMessage = new StringBuilder();
        formattedMessage.append("the message\n");
        formattedMessage.append("-----\n");
        formattedMessage.append(content).append("\n");
        formattedMessage.append("-----");
        return formattedMessage.toString();
    }

}

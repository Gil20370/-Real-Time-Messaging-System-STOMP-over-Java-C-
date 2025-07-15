package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

public class SubscribeFrame extends Frame {

    public SubscribeFrame(){
        super("SUBSCRIBE");
    }
    public SubscribeFrame(String topic,int subscribtionId){
        super("SUBSCRIBE");
        if (topic.startsWith("/")) 
            topic = topic.substring(1);
        addHeader("destination",topic);
        addHeader("id",String.valueOf(subscribtionId));

    }

    
    public void process(int connectionId, Connections<Frame> connections) {
        String topic = headers.get("destination");
        String subscriptionId = headers.get("id");
        String receiptId = getHeader("receipt");
    
        if (topic == null || topic.isEmpty()) {
            // Missing or invalid topic
            connections.send(connectionId, new ErrorFrame(
                "Missing or invalid topic to subscribe to", 
                printFormattedMessage(toString()), 
                receiptId
            ));
            connections.disconnect(connectionId);
            return;
        }
    
        // Check whether the user already subscribed to this channel
        if (connections.existsSubscription(topic, connectionId)) {
            connections.send(connectionId, new ErrorFrame(
                "the user already subscribed to this channel", 
                printFormattedMessage(toString()), 
                receiptId
            ));
            connections.disconnect(connectionId);
            return;
        }
    
        // Valid subscription
        connections.subscribe(topic, subscriptionId, connectionId);
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

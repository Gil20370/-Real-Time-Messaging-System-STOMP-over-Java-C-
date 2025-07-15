package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

public class UnsubscribeFrame extends Frame{

    public UnsubscribeFrame(){
        super("UNSUBSCRIBE");
    }

    public UnsubscribeFrame(String subscriptionId){
        super("UNSUBSCRIBE");
        addHeader("id",subscriptionId);

    }

    
    public void process(int connectionId, Connections<Frame> connections){
        String id = headers.get("id");
        if (id == null) {
            String receiptId=getHeader("receipt");
            connections.send(connectionId,new ErrorFrame("Missing id",printFormattedMessage(toString()),receiptId));
            connections.disconnect(connectionId); // Remove the client after sending the error frame

        }
        else
            connections.unsubscribe(connectionId,id); // unbubscribe the client 

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

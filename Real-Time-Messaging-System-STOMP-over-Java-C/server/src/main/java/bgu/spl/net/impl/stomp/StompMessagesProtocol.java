package bgu.spl.net.impl.stomp;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.srv.Connections;


public class StompMessagesProtocol<T extends Frame> implements StompMessagingProtocol<T> {

    private int connectionId;
    private Connections<Frame> connections;
    private boolean shouldTerminate;

    public StompMessagesProtocol() {
        this.shouldTerminate = false;
    }

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections =  (Connections<Frame>)connections; 
    }

    @Override
    public void process(Frame message) {
       
        
        message.process(connectionId, connections); // Now the compiler knows `process` exists
        String receiptId= message.getHeader("receipt");
        if (message instanceof DisconnectFrame || message instanceof ErrorFrame )
            shouldTerminate=true;
            else  
            {
            if (receiptId!=null)  //there is a receipt id which was not already sent in the proccesing
                connections.send(connectionId,new ReceiptFrame(receiptId));
            }
        
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
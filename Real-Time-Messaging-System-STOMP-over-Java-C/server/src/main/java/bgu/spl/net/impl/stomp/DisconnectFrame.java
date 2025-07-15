package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

public class DisconnectFrame extends Frame {

    public DisconnectFrame(){
        super("DISCONNECT");
    }
    public DisconnectFrame(String receiptId){
        super("DISCONNECT");
        addHeader("receipt-id",receiptId);
    } 

    
    public void process(int connectionId, Connections<Frame> connections){
        String receiptId= getHeader("receipt");
        if (receiptId!=null)
        connections.send(connectionId,new ReceiptFrame(receiptId));
        connections.disconnect(connectionId);
    }
    

}

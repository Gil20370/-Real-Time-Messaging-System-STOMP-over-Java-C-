package bgu.spl.net.impl.stomp;



public class ErrorFrame extends Frame {

    public ErrorFrame(String errorDescription,String receiptId){
        super("ERROR");
        addHeader("message", errorDescription);
        if (receiptId!=null)
            addHeader("receipt-id","message-"+ receiptId);



    }

    public ErrorFrame(String errorDescription,String detailedInfo,String receiptId ){
        super("ERROR");
        addHeader("message", errorDescription);
        setBody(detailedInfo);
        if (receiptId!=null)
        addHeader("receipt-id","message-"+ receiptId);


    } 

    

    
 


}

package bgu.spl.net.impl.stomp;



public class ReceiptFrame extends Frame {

    public ReceiptFrame(String receiptId){
        super("RECEIPT");
        addHeader("receipt-id",receiptId);

    } 

    



}

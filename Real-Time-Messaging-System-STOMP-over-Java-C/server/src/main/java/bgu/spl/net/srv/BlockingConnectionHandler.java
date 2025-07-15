package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.stomp.Frame;
import bgu.spl.net.impl.stomp.ReceiptFrame;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;

public class BlockingConnectionHandler<T> implements Runnable, ConnectionHandler<T> {

    private final MessagingProtocol<T> protocol;
    private final MessageEncoderDecoder<T> encdec;
    private final Socket sock;
    private BufferedInputStream in;
    private BufferedOutputStream out;
    private volatile boolean connected = true;
   
    

    public BlockingConnectionHandler(Socket sock, MessageEncoderDecoder<T> reader, MessagingProtocol<T> protocol,Connections<T> connections,int connectionId) {
        this.sock = sock;
        this.encdec = reader;
        this.protocol = protocol;
    

        // Call start to initialize the protocol
        this.protocol.start(connectionId, connections);
    }

    @Override
    public void run() {
        try (Socket sock = this.sock) { //just for automatic closing
            int read;

            in = new BufferedInputStream(sock.getInputStream());
            out = new BufferedOutputStream(sock.getOutputStream());

            while (!protocol.shouldTerminate() && connected && (read = in.read()) >= 0) {
                T nextMessage = encdec.decodeNextByte((byte) read);
                if (nextMessage != null) 
                    protocol.process(nextMessage);   
                
            }
        } catch (IOException ex) {
            System.err.println("Error in connection handler: " + ex.getMessage());
            ex.printStackTrace();
        }
      try
       {
        close();
    }
      catch (IOException ex) {
        System.err.println("Error in closing connection: " + ex.getMessage());
        ex.printStackTrace(); }

    }

    @Override
    public void close() throws IOException {
        connected = false;
        sock.close();
    }

    @Override
    public void send(T msg) {
        try {
            this.sock.getOutputStream().write(encdec.encode(msg));
            out.flush();
        } catch (Exception e) {
            System.err.println("Error in connection handler: " + e.getMessage());

            // TODO: handle exception
        }
        
    }
}

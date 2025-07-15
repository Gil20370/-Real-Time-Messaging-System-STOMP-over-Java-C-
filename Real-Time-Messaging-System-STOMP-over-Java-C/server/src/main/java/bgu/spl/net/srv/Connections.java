package bgu.spl.net.srv;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);

    public void subscribe(String channel, String subscriptionId, int connectionId);

    public void unsubscribe(int connectionId, String subscriptionId);

    public void addConnection(int connectionId, ConnectionHandler<T> handler);

    public boolean existsSubscription(String channel, int connectionId);


}

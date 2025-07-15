package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.ConnectionHandler;
import bgu.spl.net.srv.Connections;


import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectionsImpl <T> implements Connections <T>{

    private final ConcurrentHashMap<Integer, ConnectionHandler<T>> clientHandlers; // Maps connection IDs to handlers
    private final ConcurrentHashMap<String, Set<Integer>> topicSubscribers;       // Maps topics to subscribed client IDs
    private final ConcurrentHashMap<String, Map<Integer, String>> channelSubscriptions; // Maps channel to connectionIds -> subscriptionIds

    private final AtomicInteger messageIdCounter = new AtomicInteger(0);

    public ConnectionsImpl() {
        this.clientHandlers = new ConcurrentHashMap<>();
        this.topicSubscribers = new ConcurrentHashMap<>();
        this.channelSubscriptions=new ConcurrentHashMap<>();
    }

    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> handler = clientHandlers.get(connectionId);
        if (handler != null) {
            handler.send(msg); // Send message to the specific client
            return true;
        }
        return false; // Connection ID not found
    }

    @Override
    public void send(String channel, T msg) {
        Set<Integer> subscribers = topicSubscribers.get(channel);
        if (subscribers != null) {
            synchronized (subscribers) { // Synchronize access to the set
                for (Integer connectionId : subscribers) {
                    // Retrieve the set of subscription IDs for this user on the given channel
                    Map<Integer, String> subscriptions = channelSubscriptions.get(channel);
                    MessageFrame currMsgCopy = ((MessageFrame) msg).copy();                    for (String subscriptionId : subscriptions.values()) {
                        // Copy the message to avoid modifying the original
                        currMsgCopy.addHeader("subscription", subscriptionId);  // Add subscriptionId to the header
                        currMsgCopy.addHeader("message-id", String.valueOf(generateMessageId()));

                      }  // Send the copied message to the specific subscriber
                        send(connectionId, (T) currMsgCopy);
                    
                }
            }
        }
    }


    @Override
    public synchronized void disconnect(int connectionId) {
        for (Map.Entry<String, Map<Integer, String>> channelEntry : channelSubscriptions.entrySet()) {
            String channel = channelEntry.getKey();
            Map<Integer, String> subscriptions = channelEntry.getValue();

            // Remove all subscriptions of this connectionId
            subscriptions.entrySet().removeIf(entry -> entry.getKey().equals(connectionId));

            // If there are no more subscriptions for this channel, clean up
            if (subscriptions.isEmpty()) {
                channelSubscriptions.remove(channel);
                topicSubscribers.remove(channel); // No subscribers for this topic
            }
        }

        // Remove the connection handler for this connectionId
        clientHandlers.remove(connectionId);
        ServerStorage.getInstance().disconnectUser(connectionId);
    }


    // Utility methods for managing subscriptions
     // Utility method to subscribe a user with a new subscription ID
     public void subscribe(String channel, String subscriptionId, int connectionId) {
        // Ensure the channel's subscriber set exists
        topicSubscribers.putIfAbsent(channel, ConcurrentHashMap.newKeySet());
        channelSubscriptions.putIfAbsent(channel, new ConcurrentHashMap<>());

        synchronized (topicSubscribers.get(channel)) {
            Set<Integer> subscribers = topicSubscribers.get(channel);

            // Check if the user is already subscribed (if connectionId is present)
            if (subscribers.contains(connectionId)) {
                System.out.println("User with connectionId " + connectionId + " is already subscribed to: " + channel);
            } else {
                // If not already subscribed, add them to the topic's subscribers
                subscribers.add(connectionId);

                // Generate a new unique subscriptionId for this user on this channel

                // Map the new subscriptionId to the channel for this connectionId
                channelSubscriptions.get(channel).put(connectionId, subscriptionId);
            }
        }
    }
    

    public void unsubscribe(int connectionId, String subscriptionId) {
        for (Map.Entry<String, Map<Integer, String>> entry : channelSubscriptions.entrySet()) {
            String channel = entry.getKey();
            Map<Integer, String> subscriptions = entry.getValue();

            // Find and remove the subscriptionId for this connectionId
            if (subscriptions.containsValue(subscriptionId)) {
                subscriptions.entrySet().removeIf(entry1 -> entry1.getValue().equals(subscriptionId));
                topicSubscribers.get(channel).remove(connectionId);
            }
        }
    }


    // Register a new connection
    public void addConnection(int connectionId, ConnectionHandler<T> handler) {
        clientHandlers.put(connectionId, handler);
    }

    private int generateMessageId() {
        return messageIdCounter.getAndIncrement();
    }

    public boolean existsSubscription(String channel, int connectionId) {
        Set<Integer> subscribers = topicSubscribers.get(channel);
        if (subscribers != null) {
            synchronized (subscribers) {
                return subscribers.contains(connectionId);
            }
        }
        return false; // No duplicate subscription found
    }

} 




package bgu.spl.net.impl.stomp;


public class MessageFrame extends Frame {


    public MessageFrame(String topic, String messageContent){
        super("MESSAGE");
        topic = "/" + topic;
        addHeader("destination",topic);
        setBody(messageContent);
    }
    
    public MessageFrame copy(){
        MessageFrame result=new MessageFrame(getTopic(),body);
        return result;
            

    }

    public String getTopic(){
        String topic=getHeader("destination");
        if (topic.startsWith("/")) 
            topic = topic.substring(1);
        return topic;
    }

    


    


}

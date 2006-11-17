package org.mortbay.cometd.jms;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

public class CometdProducer implements javax.jms.TopicPublisher
{
    public Topic getTopic() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void publish(Message message) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void publish(Topic topic, Message message) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void publish(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void publish(Topic topic, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void close() throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public int getDeliveryMode() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public Destination getDestination() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean getDisableMessageID() throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean getDisableMessageTimestamp() throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public int getPriority() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public long getTimeToLive() throws JMSException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public void send(Message message) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void send(Destination destination, Message message) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void send(Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void send(Destination destination, Message message, int deliveryMode, int priority, long timeToLive) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void setDeliveryMode(int deliveryMode) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void setDisableMessageID(boolean value) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void setDisableMessageTimestamp(boolean value) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void setPriority(int defaultPriority) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public void setTimeToLive(long timeToLive) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

}

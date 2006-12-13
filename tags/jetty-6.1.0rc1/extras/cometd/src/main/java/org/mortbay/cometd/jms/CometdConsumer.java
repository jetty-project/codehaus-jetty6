package org.mortbay.cometd.jms;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class CometdConsumer implements javax.jms.TopicSubscriber
{

    public boolean getNoLocal() throws JMSException
    {
        // TODO Auto-generated method stub
        return false;
    }

    public Topic getTopic() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void close() throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

    public MessageListener getMessageListener() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMessageSelector() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Message receive() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Message receive(long arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Message receiveNoWait() throws JMSException
    {
        // TODO Auto-generated method stub
        return null;
    }

    public void setMessageListener(MessageListener arg0) throws JMSException
    {
        // TODO Auto-generated method stub
        
    }

}

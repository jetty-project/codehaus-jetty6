package org.mortbay.cometd.jms;

import java.io.Serializable;

import org.mortbay.cometd.Client;
import org.mortbay.cometd.DataFilter;
import org.mortbay.log.Log;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.TextMessage;
import javax.jms.Session;


public class JmsFilter implements DataFilter
{
    Session _session;
    MessageProducer _producer;
    MessageConsumer _consumer;
    
    public Object filter(Object data, Client from) throws IllegalStateException
    {
        try
        {
            if (data instanceof TextMessage)
            {
                // convert to string
                return ((TextMessage)data).getText();
            }
            else if (data instanceof ObjectMessage)
            {
                // convert to object
                return ((ObjectMessage)data).getObject();
            }
            else 
            {
                Message message=null;
                
                if (data instanceof String)
                    message = _session.createTextMessage((String)data);
                else if (data instanceof Serializable)
                    message = _session.createObjectMessage((Serializable)data);
                else
                    message = _session.createTextMessage(data.toString());
                
                _producer.send(message);
                
            }
        }
        catch (JMSException e)
        {
            Log.warn(e);
        }
        return null;
    }

    public void init(Object init)
    {
        // TODO Auto-generated method stub
        
    }

}

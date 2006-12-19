package org.mortbay.cometd.demo;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.mortbay.cometd.Bayeux;
import org.mortbay.cometd.Client;
import org.mortbay.cometd.CometdServlet;
import org.mortbay.cometd.DataFilter;

public class BayeuxStartupListener implements ServletContextAttributeListener
{

    public void attributeAdded(ServletContextAttributeEvent scab)
    {
        if (scab.getName().equals(CometdServlet.ORG_MORTBAY_BAYEUX))
        {
            Bayeux bayeux=(Bayeux)scab.getValue();

            Client client=bayeux.newClient();
            client.addDataFilter(new Reporter());  // Report all messages
            client.addDataFilter(new BitBucket()); // prevent messages queueing forever 
            bayeux.getChannel(Bayeux.MONITOR_CHANNEL_EVENT).addSubscriber(client);
            bayeux.getChannel(Bayeux.MONITOR_CLIENT_EVENT).addSubscriber(client);
  
        }
    }

    public void attributeRemoved(ServletContextAttributeEvent scab)
    {

    }

    public void attributeReplaced(ServletContextAttributeEvent scab)
    {

    }

    
    private static class Reporter implements DataFilter
    {
        public Object filter(Object data, Client from) throws IllegalStateException
        {
            System.out.println("DATA: "+from+"-->"+data);
            return data;
        }

        public void init(Object init)
        {
        }
        
    }
    
    private static class BitBucket implements DataFilter
    {
        public Object filter(Object data, Client from) throws IllegalStateException
        {
            return null;
        }

        public void init(Object init)
        {
        }
        
    }
}

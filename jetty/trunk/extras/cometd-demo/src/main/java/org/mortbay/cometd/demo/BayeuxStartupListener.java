package org.mortbay.cometd.demo;

import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;

import org.mortbay.cometd.Bayeux;
import org.mortbay.cometd.Channel;
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

            Client monitor_client=bayeux.newClient(null);
            monitor_client.addDataFilter(new Reporter());  // Report all messages
            bayeux.getChannel(Bayeux.MONITOR_CHANNEL_EVENT).addSubscriber(monitor_client);
            bayeux.getChannel(Bayeux.MONITOR_CLIENT_EVENT).addSubscriber(monitor_client);
  
            new EchoRPC(bayeux,"/rpc/echo");
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
    
    private static class EchoRPC implements DataFilter
    {
        Client _client;
        Channel _channel;
        
        public EchoRPC(Bayeux bayeux,String channel)
        {
            _client = bayeux.newClient("echo");
            _channel = bayeux.newChannel(channel);
            _channel.addSubscriber(_client);
            _client.addDataFilter(this);
        }
        
        public Object filter(Object data, Client from) throws IllegalStateException
        {
            from.getConnection().publish(data,_client);
            return null;
        }

        public void init(Object init)
        {
        }
        
    }
    
}

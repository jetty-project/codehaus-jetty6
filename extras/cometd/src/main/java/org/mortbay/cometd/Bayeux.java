// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================

package org.mortbay.cometd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletContext;

import org.mortbay.util.DateCache;

/* ------------------------------------------------------------ */
/**
 * @author gregw
 * 
 */
public class Bayeux
{
    public static final String META_CONNECT="/meta/connect";
    public static final String META_DISCONNECT="/meta/disconnect";
    public static final String META_HANDSHAKE="/meta/handshake";
    public static final String META_PING="/meta/ping";
    public static final String META_RECONNECT="/meta/reconnect";
    public static final String META_STATUS="/meta/status";
    public static final String META_SUBSCRIBE="/meta/subscribe";
    public static final String META_UNSUBSCRIBE="/meta/unsubscribe";

    public static final String CLIENT_ATTR="clientId";
    public static final String DATA_ATTR="data";
    public static final String CHANNEL_ATTR="channel";
    public static final String TIMESTAMP_ATTR="timestamp";
    public static final String TRANSPORT_ATTR="transport";

    HashMap _channels=new HashMap();
    HashMap _clients=new HashMap();
    ServletContext _context;
    DateCache _dateCache=new DateCache();
    Random _random=new Random(System.currentTimeMillis());
    HashMap _handlers=new HashMap();
    HashMap _transports=new HashMap();
    HashMap _filters=new java.util.HashMap();
    ArrayList _filterOrder= new ArrayList();
    SecurityPolicy _securityPolicy=new DefaultPolicy();

    {
        _handlers.put("*",new PublishHandler());
        _handlers.put(META_HANDSHAKE,new HandshakeHandler());
        _handlers.put(META_CONNECT,new ConnectHandler());
        _handlers.put(META_RECONNECT,new ReconnectHandler());
        _handlers.put(META_DISCONNECT,new DisconnectHandler());
        _handlers.put(META_SUBSCRIBE,new SubscribeHandler());
        _handlers.put(META_UNSUBSCRIBE,new UnsubscribeHandler());
        _handlers.put(META_STATUS,new StatusHandler());
        _handlers.put(META_PING,new PingHandler());

        _transports.put("iframe",IFrameTransport.class);
        _transports.put("http-polling",PlainTextJSONTransport.class);
        _transports.put("long-polling",PlainTextJSONTransport.class);
    }

    Bayeux(ServletContext context)
    {
        _context=context;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @return
     */
    public Channel getChannel(String id)
    {
        return (Channel)_channels.get(id);
    }


    /* ------------------------------------------------------------ */
    /**
     * @param channels A {@link ChannelPattern}
     * @param filter The filter instance to apply to new channels matching the pattern
     */
    public void addFilter(String channels, DataFilter filter)
    {
        synchronized (_filters)
        {
            ChannelPattern pattern=new ChannelPattern(channels);
            _filters.put(pattern,filter);
            _filterOrder.remove(pattern);
            _filterOrder.add(pattern);
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @return
     */
    public Channel newChannel(String id)
    {
        Channel channel=(Channel)_channels.get(id);
        if (channel==null)
        {
            channel=new Channel(id,this);
            
            Iterator p = _filterOrder.iterator();
            while(p.hasNext())
            {
                ChannelPattern pattern = (ChannelPattern)p.next();
                if (pattern.matches(id))
                    channel.addDataFilter((DataFilter)_filters.get(pattern));
            }
            
            _channels.put(id,channel);
        }
        return channel;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public Set getChannelIDs()
    {
        return _channels.keySet();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param client_id
     * @return
     */
    public Client getClient(String client_id)
    {
        return (Client)_clients.get(client_id);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public Set getClientIDs()
    {
        return _clients.keySet();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    String getTimeOnServer()
    {
        return _dateCache.format(System.currentTimeMillis());
    }

    /* ------------------------------------------------------------ */
    /**
     * @param client
     * @param message
     * @return
     */
    Transport newTransport(Client client, Map message)
    {
        try
        {
            String type=client==null?null:client.getConnectionType();
            if (type==null)
                type=(String)message.get("connectionType");

            if (type!=null)
            {
                Class trans_class=(Class)_transports.get(type);
                if (trans_class!=null)
                    return (Transport)(trans_class.newInstance());
            }
            return new PlainTextJSONTransport();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @param client
     * @param transport
     * @param message
     * @return
     */
    void handle(Client client, Transport transport, Map message)
        throws IOException
    {
        String channel_id=(String)message.get(CHANNEL_ATTR);
        Map reply=new HashMap();
        reply.put(CHANNEL_ATTR,channel_id);
        
        Handler handler=(Handler)_handlers.get(channel_id);
        if (handler==null)
            handler=(Handler)_handlers.get("*");

        handler.handle(client,transport,message);
    }

    /* ------------------------------------------------------------ */
    long getRandom()
    {
        return _random.nextLong();
    }

    /* ------------------------------------------------------------ */
    public SecurityPolicy getSecurityPolicy()
    {
        return _securityPolicy;
    }

    /* ------------------------------------------------------------ */
    public void setSecurityPolicy(SecurityPolicy securityPolicy)
    {
        _securityPolicy=securityPolicy;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private interface Handler
    {
        void handle(Client client, Transport transport, Map message)
            throws IOException;
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class ConnectHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
           throws IOException
        {
            Map reply=new HashMap();
            
            if (client==null)
                throw new IllegalStateException("No client");
            String type=(String)message.get("connectionType");
            client.setConnectionType(type);
            String channel_id="/meta/connections/"+client.getId();
            Channel channel=getChannel(channel_id);
            if (channel!=null)
            {
                channel.addSubscriber(client);
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
            }
            else
            {
                reply.put("successful",Boolean.FALSE);
                reply.put("error","unknown client ID");
            }
            reply.put("connectionId",channel_id);
            reply.put("timestamp",_dateCache.format(System.currentTimeMillis()));
            transport.send(reply);
            transport.setPolling(true);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class PublishHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
            throws IOException
        {
            String channel_id=(String)message.get("channel");

            Channel channel=getChannel(channel_id);
            Object data=message.get("data");

            Map reply=new HashMap();
            reply.put(CHANNEL_ATTR,channel_id);
            if (channel!=null&&data!=null&&_securityPolicy.canSend(client,channel,message))
            {
                channel.publish(data,client);
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
            }
            else
            {
                reply.put("successful",Boolean.FALSE);
                reply.put("error","unknown channel");
            }
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class DisconnectHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
        {
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class HandshakeHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
            throws IOException
        {
            if (client!=null)
                throw new IllegalStateException();

            client=new Client();
            _clients.put(client.getId(),client);

            String channel_id="/meta/connections/"+client.getId();
            Channel channel=new Channel(channel_id,Bayeux.this);
            _channels.put(channel_id,channel);

            // TODO actually do authentication
            Map reply=new HashMap();
            reply.put(CHANNEL_ATTR,META_HANDSHAKE);
            
            reply.put("supportedConnectionTypes",new String[] { "long-polling", "iframe" });
            reply.put("authSuccessful",Boolean.TRUE);
            reply.put(CLIENT_ATTR,client.getId());
            reply.put("version",new Double(0.1));
            reply.put("minimumVersion",new Double(0.1));

            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class PingHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
        throws IOException
        {
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class ReconnectHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
        throws IOException
        {
            // TODO check other parameters.

            String channel_id="/meta/connections/"+message.get(CLIENT_ATTR);
            Map reply=new HashMap();
            reply.put(CHANNEL_ATTR,channel_id);
            reply.put("connectionId",channel_id);
            reply.put("timestamp",_dateCache.format(System.currentTimeMillis()));
            
            if (client==null)
            {
                reply.put("successful",Boolean.FALSE);
                reply.put("error","unknown clientID");
                transport.setPolling(false);
                transport.send(reply);
                // ((Handler)_handlers.get(META_HANDSHAKE)).handle(null,transport,null);
            }
            else
            {
                String type=(String)message.get("connectionType");
                if (type!=null)
                    client.setConnectionType(type); 
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
                transport.setPolling(true);
                transport.send(reply);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class StatusHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message)
        throws IOException
        {
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class SubscribeHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message) 
            throws IOException
        {
            if (client==null)
                throw new IllegalStateException("No client");

            String channel_id=(String)message.get("subscription");

            // select a random channel ID if none specifified
            if (channel_id==null)
            {
                channel_id=Long.toString(getRandom(),36);
                while (getChannel(channel_id)!=null)
                    channel_id=Long.toString(getRandom(),36);
            }

            // get the channel (or create if permitted)
            Channel channel=getChannel(channel_id);
            if (channel==null&&_securityPolicy.canCreate(client,channel,message))
                channel=newChannel(channel_id);

            Map reply=new HashMap();
            reply.put(CHANNEL_ATTR,channel_id);
            reply.put("subscription",channel.getId());
            
            if (channel!=null&&_securityPolicy.canSubscribe(client,channel,message))
            {
                channel.addSubscriber(client);
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
            }
            else
            {
                reply.put("successful",Boolean.FALSE);
                reply.put("error","cannot subscribe");
            }
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class UnsubscribeHandler implements Handler
    {
        public void handle(Client client, Transport transport, Map message) 
            throws IOException
        {
            if (client==null)
                return;

            String channel_id=(String)message.get("subscription");
            Channel channel=getChannel(channel_id);
            if (channel!=null)
                channel.removeSubscriber(client);

            Map reply=new HashMap();
            reply.put(CHANNEL_ATTR,channel_id);
            reply.put("subscription",channel.getId());
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class DefaultPolicy implements SecurityPolicy
    {

        public boolean canCreate(Client client, Channel channel, Map message)
        {
            return true;
            // TODO return !channel.getId().startsWith("/meta/");
        }

        public boolean canSubscribe(Client client, Channel channel, Map message)
        {
            return true;
            // TODO return !channel.getId().startsWith("/meta/");
        }

        public boolean canSend(Client client, Channel channel, Map message)
        {
            return true;
            //TODO return !channel.getId().startsWith("/meta/");
        }

    }
}

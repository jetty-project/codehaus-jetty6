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

import java.util.HashMap;
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
    static final String DATA_ATTR="data";
    static final String CHANNEL_ATTR="channel";
    static final String TIMESTAMP_ATTR="timestamp";

    HashMap _channels=new HashMap();
    HashMap _clients=new HashMap();
    ServletContext _context;
    DateCache _dateCache=new DateCache();
    Random _random=new Random(System.currentTimeMillis());
    HashMap _handlers=new HashMap();
    HashMap _transports=new HashMap();
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
     * @param id
     * @return
     */
    public Channel newChannel(String id)
    {
        Channel channel=(Channel)_channels.get(id);
        if (channel==null)
        {
            channel=new Channel(id,this);
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
     * @param message
     * @return
     */
    Client getClient(Map message)
    {
        return getClient((String)message.get("clientId"));
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
    Map handle(Client client, Transport transport, Map message)
    {
        String channel_id=(String)message.get("channel");

        Map response=new HashMap();
        response.put("channel",channel_id);
        Handler handler=(Handler)_handlers.get(channel_id);
        if (handler==null)
            handler=(Handler)_handlers.get("*");

        if (!handler.handle(client,transport,message,response))
            return null;

        return response;
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
        boolean handle(Client client, Transport transport, Map message, Map reply);
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class ConnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
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
            transport.setPolling(true);
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class PublishHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            String channel_id=(String)message.get("channel");

            Channel channel=getChannel(channel_id);
            Object data=message.get("data");

            if (channel!=null&&data!=null&&_securityPolicy.canSend(client,channel,message))
            {
                channel.publish(data);
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
            }
            else
            {
                reply.put("successful",Boolean.FALSE);
                reply.put("error","unknown channel");
            }

            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class DisconnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class HandshakeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client!=null)
                throw new IllegalStateException();

            client=new Client();
            _clients.put(client.getId(),client);

            String channel_id="/meta/connections/"+client.getId();
            Channel channel=new Channel(channel_id,Bayeux.this);
            _channels.put(channel_id,channel);

            // TODO actually do authentication

            reply.put("supportedConnectionTypes",new String[]
            { "iframe",/* "long-polling"*/ });
            reply.put("authSuccessful",Boolean.TRUE);
            reply.put("clientId",client.getId());
            reply.put("version",new Double(0.1));
            reply.put("minimumVersion",new Double(0.1));
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class PingHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class ReconnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            // TODO check other parameters.

            if (client==null)
                throw new IllegalStateException("No client");
            String type=(String)message.get("connectionType");
            if (type!=null)
                client.setConnectionType(type); // kmh only set connection type
                                                // if it's in message
            String channel_id="/meta/connections/"+client.getId();
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            reply.put("connectionId",channel_id);
            reply.put("timestamp",_dateCache.format(System.currentTimeMillis()));
            transport.setPolling(true);
            return true; // TODO - this should be true... once JSON arrays
            // can be handled by javascript.
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class StatusHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class SubscribeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
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

            if (channel!=null&&_securityPolicy.canSubscribe(client,channel,message))
            {
                channel.addSubscriber(client);
                reply.put("subscription",channel.getId());
                reply.put("successful",Boolean.TRUE);
                reply.put("error","");
            }
            else
            {
                reply.put("subscription",channel.getId());
                reply.put("successful",Boolean.FALSE);
                reply.put("error","cannot subscribe");
            }
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class UnsubscribeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client==null)
                return false;

            String channel_id=(String)message.get("subscription");
            Channel channel=getChannel(channel_id);
            if (channel!=null)
                channel.removeSubscriber(client);

            reply.put("subscription",channel.getId());
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            return true;
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class DefaultPolicy implements SecurityPolicy
    {

        public boolean canCreate(Client client, Channel channel, Map message)
        {
            return true;
        }

        public boolean canSubscribe(Client client, Channel channel, Map message)
        {
            return true;
        }

        public boolean canSend(Client client, Channel channel, Map message)
        {
            return true;
        }

    }
}
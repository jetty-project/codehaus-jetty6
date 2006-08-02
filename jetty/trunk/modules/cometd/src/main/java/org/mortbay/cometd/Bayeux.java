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

import javax.servlet.ServletContext;

import org.mortbay.util.DateCache;

public class Bayeux
{
    public static final String META_HANDSHAKE="/meta/handshake";
    public static final String META_CONNECT="/meta/connect";
    public static final String META_RECONNECT="/meta/reconnect";
    public static final String META_DISCONNECT="/meta/disconnect";
    public static final String META_SUBSCRIBE="/meta/subscribe";
    public static final String META_UNSUBSCRIBE="/meta/unsubscribe";
    public static final String META_STATUS="/meta/status";
    public static final String META_PING="/meta/ping";
    
    ServletContext _context;
    HashMap _channels = new HashMap();
    HashMap _clients = new HashMap();
    DateCache _dateCache = new DateCache();

    HashMap _handlers = new HashMap();
    {
        _handlers.put("*",new DefaultHandler());
        _handlers.put(META_HANDSHAKE,new HandshakeHandler());
        _handlers.put(META_CONNECT,new ConnectHandler());
        _handlers.put(META_RECONNECT,new ReconnectHandler());
        _handlers.put(META_DISCONNECT,new DisconnectHandler());
        _handlers.put(META_SUBSCRIBE,new SubscribeHandler());
        _handlers.put(META_UNSUBSCRIBE,new UnsubscribeHandler());
        _handlers.put(META_STATUS,new StatusHandler());
        _handlers.put(META_PING,new PingHandler());
    }
    
    HashMap _transports = new HashMap();
    {
        _transports.put("iframe",IFrameTransport.class);
        _transports.put("http-polling",PlainTextJSONTransport.class);
        _transports.put("long-polling",PlainTextJSONTransport.class);
    }

    Bayeux(ServletContext context)
    {
        _context=context;
    }
    
    public String getTimeOnServer()
    {
        return _dateCache.format(System.currentTimeMillis());
    }
    
    public Client getClient(Map message)
    {
        String client_id = (String)message.get("clientId");
        return (Client)_clients.get(client_id);
    }
    
    public Transport newTransport(Client client,Map message)
    {
        try
        {
            String type = client==null?null:client.getConnectionType();
            if (type==null)
                type=(String)message.get("connectionType");
                
            if (type!=null)
            {
                Class trans_class = (Class)_transports.get(type);
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

    public Channel getChannel(String id)
    {
        return (Channel)_channels.get(id);
    }

    public Channel getChannel(String id,boolean create)
    {
        Channel channel = (Channel)_channels.get(id);
        if (channel==null)
        {
            channel=new Channel(id,this);
            _channels.put(id,channel);
        }
        return channel;
    }

    Map handle(Client client, Transport transport, Map message)
    {
        String channel_id= (String)message.get("channel");

        Map response= new HashMap();
        response.put("channel",channel_id);
        Handler handler= (Handler)_handlers.get(channel_id);
        if (handler==null)
            handler=(Handler)_handlers.get("*");
        
        if (!handler.handle(client,transport, message, response))
            return null;
        
        return response;
    }

    interface Handler 
    {
        boolean handle(Client client, Transport transport, Map message, Map reply);
    }

    class DefaultHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            String channel_id= (String)message.get("channel");
            
            Channel channel = getChannel(channel_id);
            Object data = message.get("data");
            
            if (channel!=null && data!=null)
            {
                channel.send(data);
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
    
    class HandshakeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client!=null)
                throw new IllegalStateException();
            
            client = new Client();
            _clients.put(client.getId(),client);
           
            String channel_id = "/meta/connections/"+client.getId();
            Channel channel = new Channel(channel_id,Bayeux.this);
            _channels.put(channel_id,channel);
            
            // TODO actually do authentication
            
            reply.put("supportedConnectionTypes",new String[] {"long-polling"});
            reply.put("authSuccessful",Boolean.TRUE);
            reply.put("clientId",client.getId());
            reply.put("version",new Double(0.1));
            reply.put("minimumVersion",new Double(0.1));
            return true;
        }
    }
    
    class ConnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client==null)
                throw new IllegalStateException("No client");
            String type = (String)message.get("connectionType");
            client.setConnectionType(type);
            String channel_id = "/meta/connections/"+client.getId();
            Channel channel = getChannel(channel_id);
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
    
    class ReconnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            // TODO check other parameters.
            // TODO check transport is the same
            
            if (client==null)
                throw new IllegalStateException("No client");
            String type = (String)message.get("connectionType");
            client.setConnectionType(type);
            String channel_id = "/meta/connections/"+client.getId();
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            reply.put("connectionId",channel_id);
            reply.put("timestamp",_dateCache.format(System.currentTimeMillis()));
            transport.setPolling(true);
            return false;  // TODO - this should be true... once JSON arrays can be handled by javascript.
        }
    }
    
    class DisconnectHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }
    
    class SubscribeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client==null)
                throw new IllegalStateException("No client");
            
            String channel_id=(String)message.get("subscription");
            Channel channel=getChannel(channel_id,true);
            channel.addSubscriber(client);
            // TODO actually do authentication
            
            reply.put("subscription",channel.getId());            
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            return true;
        }
    }
    
    class UnsubscribeHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            if (client==null)
                return false;
            
            String channel_id=(String)message.get("subscription");
            Channel channel=getChannel(channel_id,false);
            if (channel!=null)
                channel.removeSubscriber(client);
            
            reply.put("subscription",channel.getId());            
            reply.put("successful",Boolean.TRUE);
            reply.put("error","");
            return true;
        }
    }
    
    class StatusHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }
    
    class PingHandler implements Handler
    {
        public boolean handle(Client client, Transport transport, Map message, Map reply)
        {
            return false;
        }
    }

}
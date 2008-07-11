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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.cometd.Bayeux;
import org.cometd.Channel;
import org.cometd.Client;
import org.cometd.DataFilter;
import org.cometd.Extension;
import org.cometd.Message;
import org.cometd.SecurityPolicy;
import org.mortbay.util.ajax.JSON;


/* ------------------------------------------------------------ */
/**
 * @author gregw
 * @author aabeling: added JSONP transport
 * 
 */
public abstract class AbstractBayeux extends MessagePool implements Bayeux
{   
    public static final ChannelId META_ID=new ChannelId(META);
    public static final ChannelId META_CONNECT_ID=new ChannelId(META_CONNECT);
    public static final ChannelId META_CLIENT_ID=new ChannelId(META_CLIENT);
    public static final ChannelId META_DISCONNECT_ID=new ChannelId(META_DISCONNECT);
    public static final ChannelId META_HANDSHAKE_ID=new ChannelId(META_HANDSHAKE);
    public static final ChannelId META_PING_ID=new ChannelId(META_PING);
    public static final ChannelId META_STATUS_ID=new ChannelId(META_STATUS);
    public static final ChannelId META_SUBSCRIBE_ID=new ChannelId(META_SUBSCRIBE);
    public static final ChannelId META_UNSUBSCRIBE_ID=new ChannelId(META_UNSUBSCRIBE);
    

    private static final Map<String,Object> EXT_JSON_COMMENTED=new HashMap<String,Object>(2){
        {
            this.put("json-comment-filtered",Boolean.TRUE);
        }
    };
    
    
    private HashMap<String,Handler> _handlers=new HashMap<String,Handler>();
    
    private ChannelImpl _root = new ChannelImpl("/",this);
    private ConcurrentHashMap<String,ClientImpl> _clients=new ConcurrentHashMap<String,ClientImpl>();
    protected SecurityPolicy _securityPolicy=new DefaultPolicy();
    protected Object _advice;
    protected int _adviceVersion=0;
    protected Object _unknownAdvice=new JSON.Literal("{\"reconnect\":\"handshake\",\"interval\":500}");
    protected int _logLevel;
    protected long _timeout=240000;
    protected long _interval=0;
    protected long _maxInterval=30000;
    protected boolean _JSONCommented;
    protected boolean _initialized;
    protected ConcurrentHashMap<String, List<String>> _browser2client=new ConcurrentHashMap<String, List<String>>();
    protected int _multiFrameInterval=-1;
    protected JSON.Literal _multiFrameAdvice; 
    
    protected boolean _directDeliver=true;
    protected boolean _requestAvailable;
    protected ThreadLocal<HttpServletRequest> _request = new ThreadLocal<HttpServletRequest>();
    
    transient ServletContext _context;
    transient Random _random;
    transient ConcurrentHashMap<String, ChannelId> _channelIdCache;
    protected Handler _publishHandler;
    protected Handler _metaPublishHandler;

    protected List<Extension> _extensions=new CopyOnWriteArrayList<Extension>();
    protected JSON.Literal _transports=new JSON.Literal("[\""+Bayeux.TRANSPORT_LONG_POLL+ "\",\""+Bayeux.TRANSPORT_CALLBACK_POLL+"\"]");
    
    /* ------------------------------------------------------------ */
    /**
     * @param context.
     *            The logLevel init parameter is used to set the logging to
     *            0=none, 1=info, 2=debug
     */
    protected AbstractBayeux()
    {
        _publishHandler=new PublishHandler();
        _metaPublishHandler=new MetaPublishHandler();
        _handlers.put(META_HANDSHAKE,new HandshakeHandler());
        _handlers.put(META_CONNECT,new ConnectHandler());
        _handlers.put(META_DISCONNECT,new DisconnectHandler());
        _handlers.put(META_SUBSCRIBE,new SubscribeHandler());
        _handlers.put(META_UNSUBSCRIBE,new UnsubscribeHandler());
        _handlers.put(META_PING,new PingHandler());
        
        setTimeout(getTimeout());
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated user {@link Channel#addFilter}
     * @param channels
     *            A {@link ChannelId}
     * @param filter
     *            The filter instance to apply to new channels matching the
     *            pattern
     */
    public void addFilter(String channels, DataFilter filter)
    {
        synchronized (this)
        {
            ChannelImpl channel = (ChannelImpl)getChannel(channels,true);
            channel.addDataFilter(filter);
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @deprecated user {@link Channel#removeFilter}
     * @see dojox.cometd.Bayeux#removeFilter(java.lang.String, dojox.cometd.DataFilter)
     */
    public void removeFilter(String channels, DataFilter filter)
    {
        synchronized (this)
        {
            ChannelImpl channel = (ChannelImpl)getChannel(channels,false);
            if (channel!=null)
                channel.removeDataFilter(filter);
        }
    }

    /* ------------------------------------------------------------ */
    public void addExtension(Extension ext)
    {
        _extensions.add(ext);
    }
    
    /* ------------------------------------------------------------ */
    public List<Extension> getExtensions()
    {
        // TODO - remove this hack of a method!
        return _extensions;
    }

    /* ------------------------------------------------------------ */
    public void removeExtension(Extension ext)
    {
        _extensions.remove(ext);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @return
     */
    public ChannelImpl getChannel(ChannelId id)
    {
        return _root.getChild(id);
    }

    /* ------------------------------------------------------------ */
    public ChannelImpl getChannel(String id)
    {
        ChannelId cid=getChannelId(id);
        if (cid.depth()==0)
            return null;
        return _root.getChild(cid);
    }

    /* ------------------------------------------------------------ */
    public Channel getChannel(String id, boolean create)
    {
        synchronized(this)
        {
            ChannelImpl channel=getChannel(id);

            if (channel==null && create)
            {
                channel=new ChannelImpl(id,this);
                _root.addChild(channel);
                
                if (isLogInfo())
                    logInfo("newChannel: "+channel);
            }
            return channel;
        }
    }
    
    /* ------------------------------------------------------------ */
    public ChannelId getChannelId(String id)
    {
        ChannelId cid = _channelIdCache.get(id);
        if (cid==null)
        {
            // TODO shrink cache!
            cid=new ChannelId(id);
            _channelIdCache.put(id,cid);
        }
        return cid;
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.Bx#getClient(java.lang.String)
     */
    public Client getClient(String client_id)
    {
        synchronized(this)
        {
            if (client_id==null)
                return null;
            Client client = _clients.get(client_id);
            return client;
        }
    }

    /* ------------------------------------------------------------ */
    public Set<String> getClientIDs()
    {
        return _clients.keySet();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The maximum time in ms to wait between polls before timing out a client
     */
    public long getMaxInterval()
    {
        return _maxInterval;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the logLevel. 0=none, 1=info, 2=debug
     */
    public int getLogLevel()
    {
        return _logLevel;
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.Bx#getSecurityPolicy()
     */
    public SecurityPolicy getSecurityPolicy()
    {
        return _securityPolicy;
    }

    /* ------------------------------------------------------------ */
    public long getTimeout()
    {
        return _timeout;
    }

    /* ------------------------------------------------------------ */
    public long getInterval()
    {
        return _interval;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return true if published messages are directly delivered to subscribers. False if
     * a new message is to be created that holds only supported fields.
     */
    public boolean isDirectDeliver()
    {
        return _directDeliver;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param directDeliver true if published messages are directly delivered to subscribers. False if
     * a new message is to be created that holds only supported fields.
     */
    public void setDirectDeliver(boolean directDeliver)
    {
        _directDeliver = directDeliver;
    }
    
    /* ------------------------------------------------------------ */
    /** Handle a Bayeux message.
     * This is normally only called by the bayeux servlet or a test harness.
     * @param client The client if known
     * @param transport The transport to use for the message
     * @param message The bayeux message.
     */
    public String handle(ClientImpl client, Transport transport, Message message) throws IOException
    {
        String channel_id=message.getChannel();
        
        Handler handler=(Handler)_handlers.get(channel_id);
        if (handler!=null)
        {
            // known meta channel
            ListIterator<Extension> iter = _extensions.listIterator(_extensions.size());
            while(iter.hasPrevious())
                message=iter.previous().rcvMeta(message);
            
            handler.handle(client,transport,message);
            _metaPublishHandler.handle(client,transport,message);
        }
        else if (channel_id.startsWith(META_SLASH))
        {
            // unknown meta channel
            ListIterator<Extension> iter = _extensions.listIterator(_extensions.size());
            while(iter.hasPrevious())
                message=iter.previous().rcvMeta(message);
            _metaPublishHandler.handle(client,transport,message);
        }
        else
        {
            // non meta channel
            handler=_publishHandler;
            ListIterator<Extension> iter = _extensions.listIterator(_extensions.size());
            while(iter.hasPrevious())
                message=iter.previous().rcv(message);
            handler.handle(client,transport,message);
        }

        return channel_id;
    }

    /* ------------------------------------------------------------ */
    public boolean hasChannel(String id)
    {
        ChannelId cid=getChannelId(id);
        return _root.getChild(cid)!=null;
    }

    /* ------------------------------------------------------------ */
    public boolean isInitialized()
    {
        return _initialized;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the commented
     */
    public boolean isJSONCommented()
    {
        return _JSONCommented;
    }

    /* ------------------------------------------------------------ */
    public boolean isLogDebug()
    {
        return _logLevel>1;
    }

    /* ------------------------------------------------------------ */
    public boolean isLogInfo()
    {
        return _logLevel>0;
    }
    
    /* ------------------------------------------------------------ */
    public void logDebug(String message)
    {
        if (_logLevel>1)
            _context.log(message);
    }

    /* ------------------------------------------------------------ */
    public void logDebug(String message, Throwable th)
    {
        if (_logLevel>1)
            _context.log(message,th);
    }

    /* ------------------------------------------------------------ */
    public void logWarn(String message, Throwable th)
    {
        _context.log(message+": "+th.toString());
    }

    /* ------------------------------------------------------------ */
    public void logWarn(String message)
    {
        _context.log(message);
    }

    /* ------------------------------------------------------------ */
    public void logInfo(String message)
    {
        if (_logLevel>0)
            _context.log(message);
    }

    /* ------------------------------------------------------------ */
    public Client newClient(String idPrefix)
    {
        ClientImpl client = new ClientImpl(this,idPrefix);
        return client;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @deprecated use {@link #newClient(String)}
     */
    public Client newClient(String idPrefix,org.cometd.Listener listener)
    {
        ClientImpl client = new ClientImpl(this,idPrefix);
        client.setListener(listener);
        return client;
    }

    /* ------------------------------------------------------------ */
    public abstract ClientImpl newRemoteClient();

    /* ------------------------------------------------------------ */
    /** Create new transport object for a bayeux message
     * @param client The client
     * @param message the bayeux message
     * @return the negotiated transport.
     */
    public Transport newTransport(ClientImpl client, Map<?,?> message)
    {
        if (isLogDebug())
            logDebug("newTransport: client="+client+",message="+message);

        Transport result=null;

        try
        {
            String type=client==null?null:client.getConnectionType();
            if (type==null)
                type=(String)message.get(Bayeux.CONNECTION_TYPE_FIELD);

            if (Bayeux.TRANSPORT_CALLBACK_POLL.equals(type) || type==null) 
            {
                String jsonp=(String)message.get(Bayeux.JSONP_PARAMETER);
                if(jsonp!=null)
                    result=new JSONPTransport(client!=null&&client.isJSONCommented(),jsonp);
                else
                    result=new JSONTransport(client!=null&&client.isJSONCommented());
            }
            else
                result=new JSONTransport(client!=null&&client.isJSONCommented());
                
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }

        if (isLogDebug())
            logDebug("newTransport: result="+result);
        return result;
    }

    /* ------------------------------------------------------------ */
    /** Publish data to a channel.
     * Creates a message and delivers it to the root channel.
     * @param to
     * @param from
     * @param data
     * @param msgId
     */
    protected void doPublish(ChannelId to, Client from, Object data, String msgId)
    {
        Message msg = newMessage();
        msg.put(CHANNEL_FIELD,to.toString());
        
        if (msgId==null)
        {
            long id=msg.hashCode()
            ^(to==null?0:to.hashCode())
            ^(from==null?0:from.hashCode());
            id=id<0?-id:id;
            msg.put(ID_FIELD,Long.toString(id,36));
        }
        else
            msg.put(ID_FIELD,msgId);
            
        msg.put(DATA_FIELD,data);
        
        for (Extension e:_extensions)
            msg=e.send(msg);
        _root.doDelivery(to,from,msg);
        ((MessageImpl)msg).decRef();
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated use {@link Channel#publish(Client, Object, String)}
     */
    public void publish(Client fromClient, String toChannelId, Object data, String msgId)
    {
        doPublish(getChannelId(toChannelId),fromClient,data,msgId);
    }


    /* ------------------------------------------------------------ */
    /** (non-Javadoc)
     * @deprecated use {@link Client#deliver(Client, Message)}
     * @see org.cometd.Bayeux#deliver(org.cometd.Client, org.cometd.Client, java.lang.String, org.cometd.Message)
     */
    public void deliver(Client fromClient,Client toClient, String toChannel, Message message)
    {
        if (toChannel!=null)
            message.put(Bayeux.CHANNEL_FIELD,toChannel);

        if (toClient!=null)
            toClient.deliver(fromClient,message);
    }
    

    /* ------------------------------------------------------------ */
    public boolean removeChannel(ChannelId channelId)
    {
        // TODO Auto-generated method stub
        return false;
    }

    /* ------------------------------------------------------------ */
    protected String newClientId(long variation, String idPrefix)
    {
        if (idPrefix==null)
            return Long.toString(getRandom(),36)+Long.toString(variation,36);
        else
            return idPrefix+"_"+Long.toString(getRandom(),36);
    }

    /* ------------------------------------------------------------ */
    protected void addClient(ClientImpl client,String idPrefix)
    {
        while(true)
        {
            String id = newClientId(client.hashCode(),idPrefix);
            client.setId(id);
            
            ClientImpl other = _clients.putIfAbsent(id,client);
            if (other==null)
                return;
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.Bx#removeClient(java.lang.String)
     */
    public Client removeClient(String client_id)
    {
        ClientImpl client;
        synchronized(this)
        {
            if (client_id==null)
                return null;
            client = _clients.remove(client_id);
        }
        if (client!=null)
        {
            client.unsubscribeAll();
        }
        return client;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param ms The maximum time in ms to wait between polls before timing out a client
     */
    public void setMaxInterval(long ms)
    {
        _maxInterval=ms;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param commented the commented to set
     */
    public void setJSONCommented(boolean commented)
    {
        _JSONCommented=commented;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param logLevel
     *            the logLevel: 0=none, 1=info, 2=debug
     */
    public void setLogLevel(int logLevel)
    {
        _logLevel=logLevel;
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.Bx#setSecurityPolicy(org.mortbay.cometd.SecurityPolicy)
     */
    public void setSecurityPolicy(SecurityPolicy securityPolicy)
    {
        _securityPolicy=securityPolicy;
    }

    
    /* ------------------------------------------------------------ */
    public void setTimeout(long ms)
    {
        _timeout = ms;
        generateAdvice();
    }

    
    /* ------------------------------------------------------------ */
    public void setInterval(long ms)
    {
        _interval = ms;
        generateAdvice();
    }

    /* ------------------------------------------------------------ */
    void generateAdvice()
    {
        setAdvice(new JSON.Literal("{\"reconnect\":\"retry\",\"interval\":"+getInterval()+",\"timeout\":"+getTimeout()+"}"));        
    }

    /* ------------------------------------------------------------ */
    /**
     * @return TRUE if {@link #getCurrentRequest()} will return the current request
     */
    public boolean isRequestAvailable()
    {
        return _requestAvailable;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param requestAvailable TRUE if {@link #getCurrentRequest()} will return the current request
     */
    public void setRequestAvailable(boolean requestAvailable)
    {
        _requestAvailable=requestAvailable;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the current request if {@link #isRequestAvailable()} is true, else null
     */
    public HttpServletRequest getCurrentRequest()
    {
        return _request.get();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the current request if {@link #isRequestAvailable()} is true, else null
     */
    void setCurrentRequest(HttpServletRequest request)
    {
        _request.set(request);
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @deprecated use {@link Channel#subscribe(Client)}
     * @see dojox.cometd.Bayeux#subscribe(java.lang.String, dojox.cometd.Client)
     */
    public void subscribe(String toChannel, Client subscriber)
    {
        ChannelImpl channel = (ChannelImpl)getChannel(toChannel,true);
        if (channel!=null)
            channel.subscribe(subscriber);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @deprecated use {@link Channel#subscribe(Client)}
     */
    public void unsubscribe(String toChannel, Client subscriber)
    {
        ChannelImpl channel = (ChannelImpl)getChannel(toChannel);
        if (channel!=null)
            channel.unsubscribe(subscriber);
    }

    
    /* ------------------------------------------------------------ */
    /**
     * @return the multiFrameInterval in milliseconds
     */
    public int getMultiFrameInterval()
    {
        return _multiFrameInterval;
    }

    /* ------------------------------------------------------------ */
    /**
     * The time a client should delay between reconnects when multiple
     * connections from the same browser are detected. This effectively 
     * produces traditional polling.
     * @param multiFrameInterval the multiFrameInterval to set
     */
    public void setMultiFrameInterval(int multiFrameInterval)
    {
        _multiFrameInterval=multiFrameInterval;
        if (multiFrameInterval>0)
            _multiFrameAdvice=new JSON.Literal("{\"reconnect\":\"retry\",\"interval\":"+_multiFrameInterval+",\"multiple-clients\":true,\"timeout\":"+getTimeout()+"}");
        else
            _multiFrameAdvice=new JSON.Literal("{\"reconnect\":\"none\",\"multiple-clients\":true,\"timeout\":"+getTimeout()+"}");
        
    }


    /* ------------------------------------------------------------ */
    public Object getAdvice()
    {
        return _advice;
    }


    /* ------------------------------------------------------------ */
    public void setAdvice(Object advice)
    {
        synchronized(this)
        {
            _advice=advice;
            _adviceVersion++;
        }
    }
    
    /* ------------------------------------------------------------ */
    public Collection<Channel> getChannels()
    {
        List<Channel> channels = new ArrayList<Channel>();
        _root.getChannels(channels);
        return channels;
    }

    /* ------------------------------------------------------------ */
    public Collection<Client> getClients()
    {
        synchronized(this)
        {
            return new ArrayList<Client>(_clients.values());
        }
    }

    /* ------------------------------------------------------------ */
    public boolean hasClient(String clientId)
    {
        synchronized(this)
        {
            if (clientId==null)
                return false;
            return _clients.containsKey(clientId);
        }
    }

    /* ------------------------------------------------------------ */
    public Channel removeChannel(String channelId)
    {
        Channel channel = getChannel(channelId);
        channel.remove();
        return channel;
    }

    /* ------------------------------------------------------------ */
    protected void initialize(ServletContext context)
    {
        synchronized(this)
        {
            _initialized=true;
            _context=context;
            try
            {
                _random=SecureRandom.getInstance("SHA1PRNG");
            }
            catch (Exception e)
            {
                context.log("Could not get secure random for ID generation",e);
                _random=new Random();
            }
            _random.setSeed(_random.nextLong()^hashCode()^(context.hashCode()<<32)^Runtime.getRuntime().freeMemory());
            _channelIdCache=new ConcurrentHashMap<String, ChannelId>();
            
            _root.addChild(new ServiceChannel(Bayeux.SERVICE));
            
        }
    }

    /* ------------------------------------------------------------ */
    long getRandom()
    {
        long l=_random.nextLong();
        return l<0?-l:l;
    }

    /* ------------------------------------------------------------ */
    void clientOnBrowser(String browserId,String clientId)
    {
        List<String> clients=_browser2client.get(browserId);
        if (clients==null)
        {
            List<String> new_clients=new CopyOnWriteArrayList<String>();
            clients=_browser2client.putIfAbsent(browserId,new_clients);
            if (clients==null)
                clients=new_clients;
        }
        clients.add(clientId);
    }

    /* ------------------------------------------------------------ */
    void clientOffBrowser(String browserId,String clientId)
    {
        List<String> clients=_browser2client.get(browserId);
        if (clients!=null)
            clients.remove(clientId);
    }
    
    /* ------------------------------------------------------------ */
    List<String> clientsOnBrowser(String browserId)
    {
        List<String> clients=_browser2client.get(browserId);
        if (clients==null)
            return Collections.emptyList();
        return clients;
    }
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class DefaultPolicy implements SecurityPolicy
    {
        public boolean canHandshake(Message message)
        {
            return true;
        }
        
        public boolean canCreate(Client client, String channel, Message message)
        {
            return client!=null && !channel.startsWith(Bayeux.META_SLASH);
        }

        public boolean canSubscribe(Client client, String channel, Message message)
        {
	    if (client!=null && ("/**".equals(channel) || "/*".equals(channel)))
	        return false;
            return client!=null && !channel.startsWith(Bayeux.META_SLASH);
        }

        public boolean canPublish(Client client, String channel, Message message)
        {
            return client!=null || client==null && Bayeux.META_HANDSHAKE.equals(channel);
        }

    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected abstract class Handler
    {
        abstract void handle(ClientImpl client, Transport transport, Message message) throws IOException;
        abstract ChannelId getMetaChannelId();
        void unknownClient(Transport transport,String channel) throws IOException
        {
            MessageImpl reply=newMessage();
            
            reply.put(CHANNEL_FIELD,channel);
            reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
            reply.put(ERROR_FIELD,"402::Unknown client");
            reply.put("advice",new JSON.Literal("{\"reconnect\":\"handshake\"}"));
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class ConnectHandler extends Handler
    {
        protected String _metaChannel=META_CONNECT;

        ChannelId getMetaChannelId()
        {
            return META_CONNECT_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {      
            if (client==null)
            {
                unknownClient(transport,_metaChannel);
                return;
            }

            // is this the first connect message?
            String type=client.getConnectionType();
            boolean polling=true;
            if (type==null)
            {
                type=(String)message.get(Bayeux.CONNECTION_TYPE_FIELD);
                client.setConnectionType(type);
                polling=false;
            }
            
            Object advice = message.get(ADVICE_FIELD);
            if (advice!=null)
            {
            	Long timeout=(Long)((Map)advice).get("timeout");
            	if (timeout!=null && timeout.longValue()>0)
            		client.setTimeout(timeout.longValue());
            	else
            		client.setTimeout(0);
            }
            else
        		client.setTimeout(0);
            
            advice=null; 
        
            if (polling && _multiFrameInterval>0 && client.getBrowserId()!=null)
            {
                List<String> clients=clientsOnBrowser(client.getBrowserId());
                int count=clients.size();
                if (count>1)
                {
                    polling=clients.get(0).equals(client.getId());
                    advice=_multiFrameAdvice;
                    client.setAdviceVersion(-1);
                }
            }

            synchronized(this)
            {
                if (client.getAdviceVersion()!=_adviceVersion && (client.getAdviceVersion()>=0||advice==null))
                {
                    advice=_advice;
                    client.setAdviceVersion(_adviceVersion);
                }
            }
           
            // reply to connect message
            String id=message.getId(); 

            Message reply=newMessage(message);
            
            reply.put(CHANNEL_FIELD,META_CONNECT);
            reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);
            if (advice!=null)
                reply.put(ADVICE_FIELD,advice);
            if (id!=null)
                reply.put(ID_FIELD,id);

            
            if (polling)
                transport.setPollReply(reply);
            else
            {
                for (Extension e:_extensions)
                    reply=e.sendMeta(reply);
                transport.send(reply);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class DisconnectHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return META_DISCONNECT_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            if (client==null)
            {
                unknownClient(transport,META_DISCONNECT);
                return;
            }
            if (isLogInfo())
                logInfo("Disconnect "+client.getId());
                
            client.remove(false);
            
            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,META_DISCONNECT);
            reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);
            String id=message.getId(); 
            if (id!=null)
                reply.put(ID_FIELD,id);

            for (Extension e:_extensions)
                reply=e.sendMeta(reply);
            
            Message pollReply = transport.getPollReply();
            if (pollReply!=null)
            {
                for (Extension e:_extensions)
                    pollReply=e.sendMeta(pollReply);
                transport.send(pollReply);
                transport.setPollReply(null);
            }
            transport.send(reply);
        }
    }


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class HandshakeHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return META_HANDSHAKE_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            if (client!=null)
                throw new IllegalStateException();

            if (_securityPolicy!=null && !_securityPolicy.canHandshake(message))
            {
                Message reply=newMessage(message);
                reply.put(CHANNEL_FIELD,META_HANDSHAKE);
                reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
                reply.put(ERROR_FIELD,"403::Handshake denied");

                for (Extension e:_extensions)
                    reply=e.sendMeta(reply);
                
                transport.send(reply);
                return;
            }
            
            client=newRemoteClient();

            Map<?,?> ext = (Map<?,?>)message.get(EXT_FIELD);

            boolean commented=_JSONCommented && ext!=null && Boolean.TRUE.equals(ext.get("json-comment-filtered"));
            
            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,META_HANDSHAKE);
            reply.put("version","1.0");
            reply.put("minimumVersion","0.9");
            if (isJSONCommented())
                reply.put(EXT_FIELD,EXT_JSON_COMMENTED);

            if (client!=null)
            {
                reply.put("supportedConnectionTypes",_transports);
                reply.put("successful",Boolean.TRUE);
                reply.put(CLIENT_FIELD,client.getId());
                if (_advice!=null)
                    reply.put(ADVICE_FIELD,_advice);
                client.setJSONCommented(commented);
                transport.setJSONCommented(commented);
            }
            else
            {
                reply.put(Bayeux.SUCCESSFUL_FIELD,Boolean.FALSE);
                if (_advice!=null)
                    reply.put(ADVICE_FIELD,_advice);
            }

            if (isLogDebug())
                logDebug("handshake.handle: reply="+reply);

            String id=message.getId();
            if (id!=null)
                reply.put(ID_FIELD,id);

            for (Extension e:_extensions)
                reply=e.sendMeta(reply);
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class PublishHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return null;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            String channel_id=message.getChannel();
            
            if (client==null && message.containsKey(CLIENT_FIELD))
            {
                unknownClient(transport,channel_id);
                return;
            }
            
            String id=message.getId();

            ChannelId cid=getChannelId(channel_id);
            Object data=message.get(Bayeux.DATA_FIELD);

            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,channel_id);
            if (id!=null)
                reply.put(ID_FIELD,id);
                
            if (data!=null&&_securityPolicy.canPublish(client,channel_id,message))
            {
                reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);

                for (Extension e:_extensions)
                    reply=e.sendMeta(reply);
                
                transport.send(reply);
                if (_directDeliver)
                {
                    message.remove(CLIENT_FIELD);
                    for (Extension e:_extensions)
                        message=e.send(message);
                    _root.doDelivery(cid,client,message);
                }
                else
                    doPublish(cid,client,data,id==null?null:id);
            }
            else
            {
                reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
                reply.put(ERROR_FIELD,"403::Publish denied");

                for (Extension e:_extensions)
                    reply=e.sendMeta(reply);
                transport.send(reply);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class MetaPublishHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return null;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            String channel_id=message.getChannel();
            
            if (client==null && !META_HANDSHAKE.equals(channel_id))
            {
                // unknown client
                return;
            }
            
            if(_securityPolicy.canPublish(client,channel_id,message))
            {
                _root.doDelivery(getChannelId(channel_id),client,message);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class SubscribeHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return META_SUBSCRIBE_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            if (client==null)
            {
                unknownClient(transport,META_SUBSCRIBE);
                return;
            }

            String subscribe_id=(String)message.get(SUBSCRIPTION_FIELD);

            // select a random channel ID if none specifified
            if (subscribe_id==null)
            {
                subscribe_id=Long.toString(getRandom(),36);
                while (getChannel(subscribe_id)!=null)
                    subscribe_id=Long.toString(getRandom(),36);
            }

            ChannelId cid=null;
            boolean can_subscribe=false;
            
            if (subscribe_id.startsWith(Bayeux.SERVICE_SLASH))
            {
                can_subscribe=true;
            }
            else if (subscribe_id.startsWith(Bayeux.META_SLASH))
            {
                can_subscribe=false;
            }
            else
            {
                cid=getChannelId(subscribe_id);
                can_subscribe=_securityPolicy.canSubscribe(client,subscribe_id,message);
            }
                
            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,META_SUBSCRIBE);
            reply.put(SUBSCRIPTION_FIELD,subscribe_id);

            if (can_subscribe)
            {
                if (cid!=null)
                {
                    ChannelImpl channel=getChannel(cid);
                    if (channel==null&&_securityPolicy.canCreate(client,subscribe_id,message))
                        channel=(ChannelImpl)getChannel(subscribe_id, true);

                    if (channel!=null)
                        channel.subscribe(client);
                    else
                        can_subscribe=false;
                }
                        
                if (can_subscribe)
                {
                    reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);
                }
                else 
                {
                    reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
                    reply.put(ERROR_FIELD,"403::cannot create");
                }
            }
            else
            {
                reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
                reply.put(ERROR_FIELD,"403::cannot subscribe");
                
            }

            String id=message.getId(); 
            if (id!=null)
                reply.put(ID_FIELD,id);
            for (Extension e:_extensions)
                reply=e.sendMeta(reply);
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class UnsubscribeHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return META_UNSUBSCRIBE_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            if (client==null)
            {
                unknownClient(transport,META_UNSUBSCRIBE);
                return;
            }

            String channel_id=(String)message.get(SUBSCRIPTION_FIELD);
            ChannelImpl channel=getChannel(channel_id);
            if (channel!=null)
                channel.unsubscribe(client);

            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,META_UNSUBSCRIBE);
            if (channel!=null)
            {
                channel.unsubscribe(client);
                reply.put(SUBSCRIPTION_FIELD,channel.getId());
                reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);
            }
            else
                reply.put(SUCCESSFUL_FIELD,Boolean.FALSE);
                
            String id=message.getId(); 
            if (id!=null)
                reply.put(ID_FIELD,id);
            for (Extension e:_extensions)
                reply=e.sendMeta(reply);
            transport.send(reply);
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class PingHandler extends Handler
    {
        ChannelId getMetaChannelId()
        {
            return META_PING_ID;
        }
        
        public void handle(ClientImpl client, Transport transport, Message message) throws IOException
        {
            Message reply=newMessage(message);
            reply.put(CHANNEL_FIELD,META_PING);
            reply.put(SUCCESSFUL_FIELD,Boolean.TRUE);
                
            String id=message.getId(); 
            if (id!=null)
                reply.put(ID_FIELD,id);
            for (Extension e:_extensions)
                reply=e.sendMeta(reply);
            transport.send(reply);
        }
    }

    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class ServiceChannel extends ChannelImpl
    {
        ServiceChannel(String id)
        {
            super(id,AbstractBayeux.this);
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.cometd.ChannelImpl#addChild(org.mortbay.cometd.ChannelImpl)
         */
        public void addChild(ChannelImpl channel)
        {
            super.addChild(channel);
            setPersistent(true);
        }

        /* ------------------------------------------------------------ */
        public void subscribe(Client client)
        {
            if (client.isLocal())
                super.subscribe(client);
        }
        
    }



}

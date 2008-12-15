// ========================================================================
// Copyright 2006-20078 Mort Bay Consulting Pty. Ltd.
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
// ========================================================================

package org.mortbay.cometd.client;

import java.io.IOException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import org.cometd.Bayeux;
import org.cometd.Client;
import org.cometd.ClientListener;
import org.cometd.Listener;
import org.cometd.Message;
import org.cometd.MessageListener;
import org.cometd.RemoveListener;
import org.mortbay.cometd.MessageImpl;
import org.mortbay.cometd.MessagePool;
import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpSchemes;
import org.mortbay.jetty.client.Address;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpConnection;
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.log.Log;
import org.mortbay.util.ArrayQueue;
import org.mortbay.util.QuotedStringTokenizer;
import org.mortbay.util.ajax.JSON;


/* ------------------------------------------------------------ */
/** Bayeux protocol Client.
 * <p>
 * Implements a Bayeux Ajax Push client as part of the cometd project.
 *
 * @see http://cometd.com
 * @author gregw
 *
 */
public class BayeuxClient extends AbstractLifeCycle implements Client, MetaEvent
{
   

    private HttpClient _client;
    private MessagePool _msgPool;
    private Address _address;
    private HttpExchange _pull;
    private HttpExchange _push;
    private String _uri="/cometd";
    private boolean _initialized=false;
    private boolean _disconnecting=false;
    private boolean _handshook=false;
    private String _clientId;
    private org.cometd.Listener _listener;
    private List<RemoveListener> _rListeners;
    private List<MessageListener> _mListeners;
    private Queue<Message> _inQ;  // queue of incoming messages used if no listener available. Used as the lock object for all incoming operations.
    private Queue<Message> _outQ; // queue of outgoing messages. Used as the lock object for all outgoing operations.
    private int _batch;
    private boolean _formEncoded;
    private Map<String, Cookie> _cookies=new ConcurrentHashMap<String, Cookie>();
    private Advice _advice;
    private Timer _timer;
    private int _backoffInterval = 1000;
    private int _backoffMaxRetries = 60; //equivalent to 60 seconds
   

    /* ------------------------------------------------------------ */
    public BayeuxClient(HttpClient client, Address address, String uri, Timer timer) throws IOException
    {
        _client=client;
        _address=address;
        _uri=uri;

        _msgPool = new MessagePool();
        _inQ=new ArrayQueue<Message>();
        _outQ=new ArrayQueue<Message>();
        
        _timer = timer;
        if (_timer == null)
            _timer = new Timer("DefaultBayeuxClientTimer", true);
    }
    
    public BayeuxClient(HttpClient client, Address address, String uri) throws IOException
    {
        this (client, address, uri, new Timer("DefaultBayeuxClientTimer", true));
    }

    /**
     * If unable to connect/handshake etc, even if following the
     * interval in the advice, wait for this interval and try
     * again, up to a maximum of _backoffRetries
     * @param interval
     */
    public void setBackOffInterval (int interval)
    {
        _backoffInterval = interval;
    }
    
    public int getBackoffInterval ()
    {
        return _backoffInterval;
    }
    
    public void setBackoffMaxRetries (int retries)
    {
        _backoffMaxRetries = retries;
    }
    
    public int getBackoffMaxRetries ()
    {
        return _backoffMaxRetries;
    }
    
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * Returns the clientId
     * @see dojox.cometd.Client#getId()
     */
    public String getId()
    {
        return _clientId;
    }

    

    protected void doStart() throws Exception
    {
        super.doStart();
        synchronized (_outQ)
        {
            if (!_initialized && _pull==null)
            {
                _pull=new Handshake();
                send((Exchange)_pull, false);
            }
        }
    }


    protected void doStop() throws Exception
    {
        super.doStop();
    }


   

    /* ------------------------------------------------------------ */
    public boolean isPolling()
    {
        synchronized (_outQ)
        {
            return isRunning() && (_pull!=null);
        }
    }

    /* ------------------------------------------------------------ */
    /** (non-Javadoc)
     * @deprecated use {@link #deliver(Client, String, Object, String)}
     * @see org.cometd.Client#deliver(org.cometd.Client, java.util.Map)
     */
    public void deliver(Client from, Message message)
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");
        
        synchronized (_inQ)
        {
            if (_mListeners==null)
                _inQ.add(message);
            else
            {
                for (MessageListener l : _mListeners)
                    l.deliver(from,this,message);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#deliver(dojox.cometd.Client, java.lang.String, java.lang.Object, java.lang.String)
     */
    public void deliver(Client from, String toChannel, Object data, String id)
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");
        
        Message message = new MessageImpl();

        message.put(Bayeux.CHANNEL_FIELD,toChannel);
        message.put(Bayeux.DATA_FIELD,data);
        if (id!=null)
            message.put(Bayeux.ID_FIELD,id);

        synchronized (_inQ)
        {
            if (_mListeners==null)
                _inQ.add(message);
            else
            {
                for (MessageListener l : _mListeners)
                    l.deliver(from,this,message);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated
     */
    public org.cometd.Listener getListener()
    {
        synchronized (_inQ)
        {
            return _listener;
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#hasMessages()
     */
    public boolean hasMessages()
    {
        synchronized (_inQ)
        {
            return _inQ.size()>0;
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#isLocal()
     */
    public boolean isLocal()
    {
        return false;
    }


    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#subscribe(java.lang.String)
     */
    private void publish(Message msg)
    {   
        if (!isRunning())
            throw new IllegalStateException("Not running");

        synchronized (_outQ)
        {
            _outQ.add(msg);

            if (_batch==0&&_initialized&&_push==null)
            {
                _push=new Publish();
                try
                {
                    send((Exchange)_push, false);
                }
                catch (Exception e)
                {
                    Log.warn("Publish: ", e);
                    metaPublishFail(((Publish)_push).getOutboundMessages());
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#publish(java.lang.String, java.lang.Object, java.lang.String)
     */
    public void publish(String toChannel, Object data, String msgId)
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");

        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,toChannel);
        msg.put(Bayeux.DATA_FIELD,data);
        if (msgId!=null)
            msg.put(Bayeux.ID_FIELD,msgId);
        publish(msg);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#subscribe(java.lang.String)
     */
    public void subscribe(String toChannel)
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");

        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,Bayeux.META_SUBSCRIBE);
        msg.put(Bayeux.SUBSCRIPTION_FIELD,toChannel);
        publish(msg);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#unsubscribe(java.lang.String)
     */
    public void unsubscribe(String toChannel)
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");

        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,Bayeux.META_UNSUBSCRIBE);
        msg.put(Bayeux.SUBSCRIPTION_FIELD,toChannel);
        publish(msg);
    }

    
    /* ------------------------------------------------------------ */
    /**
     *  Disconnect this client.
     */
    public void remove()
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");

        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,Bayeux.META_DISCONNECT);

        synchronized (_outQ)
        {
            _outQ.add(msg);

            _initialized=false;
            _disconnecting=true;

            metaDisconnect();
            if (_batch==0&&_initialized&&_push==null)
            {
                _push=new Publish();
                send((Exchange)_push, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated
     */
    public void setListener(org.cometd.Listener listener)
    {
        synchronized (_inQ)
        {
            if (_listener!=null)
                removeListener(_listener);
            _listener=listener;
            if (_listener!=null)
                addListener(_listener);
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * Removes all available messages from the inbound queue.
     * If a listener is set then messages are not queued.
     * @see dojox.cometd.Client#takeMessages()
     */
    public List<Message> takeMessages()
    {
        synchronized (_inQ)
        {
            LinkedList<Message> list=new LinkedList<Message>(_inQ);
            _inQ.clear();
            return list;
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#endBatch()
     */
    public void endBatch()
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");
        synchronized (_outQ)
        {
            if (--_batch<=0)
            {
                _batch=0;
                if ((_initialized||_disconnecting)&&_push==null&&_outQ.size()>0)
                {
                    _push=new Publish();
                    send((Exchange)_push, false);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#startBatch()
     */
    public void startBatch()
    {
        if (!isRunning())
            throw new IllegalStateException("Not running");
        
        synchronized (_outQ)
        {
            _batch++;
        }
    }

    /* ------------------------------------------------------------ */
    /** Customize an Exchange.
     * Called when an exchange is about to be sent to allow Cookies
     * and Credentials to be customized.  Default implementation sets
     * any cookies
     */
    protected void customize(HttpExchange exchange)
    {
        StringBuilder buf=null;
        for (Cookie cookie : _cookies.values())
        {
	    if (buf==null)
	        buf=new StringBuilder();
            else
	        buf.append("; ");
	    buf.append(cookie.getName()); // TODO quotes
	    buf.append("=");
	    buf.append(cookie.getValue()); // TODO quotes
        }
	if (buf!=null)
            exchange.addRequestHeader(HttpHeaders.COOKIE,buf.toString());
    }

    /* ------------------------------------------------------------ */
    public void setCookie(Cookie cookie)
    {
        _cookies.put(cookie.getName(),cookie);
    }
    
   

    /* ------------------------------------------------------------ */
    /** The base class for all bayeux exchanges.
     */
    protected class Exchange extends HttpExchange.ContentExchange
    {
        Object[] _responses;
        int _connectFailures;
        int _backoffRetries  = 0;
        String _jsonOutboundMessages;
        
        
        Exchange(String info)
        { 
            setMethod("POST");
            setScheme(HttpSchemes.HTTP_BUFFER);
            setAddress(_address);
            setURI(_uri+"/"+info);
            setRequestContentType(_formEncoded?"application/x-www-form-urlencoded;charset=utf-8":"text/json;charset=utf-8");     
        }
        
        public int getBackoffRetries ()
        {
            return _backoffRetries;
        }

        public void incBackoffRetries ()
        {
            ++_backoffRetries;
        }

        protected void setMessage(String message)
        {
            try
            {
                if (_formEncoded)
                    setRequestContent(new ByteArrayBuffer("message="+URLEncoder.encode(message,"utf-8")));
                else
                    setRequestContent(new ByteArrayBuffer(message,"utf-8"));
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }

        protected void setMessages(Queue<Message> messages)
        {
            try
            {
                for (Message msg : messages)
                {
                    msg.put(Bayeux.CLIENT_FIELD,_clientId);
                }
                _jsonOutboundMessages=JSON.toString(messages);

                if (_formEncoded)
                    setRequestContent(new ByteArrayBuffer("message="+URLEncoder.encode(_jsonOutboundMessages,"utf-8")));
                else
                    setRequestContent(new ByteArrayBuffer(_jsonOutboundMessages,"utf-8"));

            }
            catch (Exception e)
            {
                Log.warn(e);
            } 
        }

        /* ------------------------------------------------------------ */
        protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
        {
            super.onResponseStatus(version,status,reason);
        }

        /* ------------------------------------------------------------ */
        protected void onResponseHeader(Buffer name, Buffer value) throws IOException
        {
            super.onResponseHeader(name,value);
            
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            if (HttpHeaders.CACHE.getOrdinal(name)==HttpHeaders.SET_COOKIE_ORDINAL)
            {
                String cname=null;
                String cvalue=null;

                QuotedStringTokenizer tok=new QuotedStringTokenizer(value.toString(),"=;",false,false);
                tok.setSingle(false);

                if (tok.hasMoreElements())
                    cname=tok.nextToken();
                if (tok.hasMoreElements())
                    cvalue=tok.nextToken();

                Cookie cookie=new Cookie(cname,cvalue);

                while (tok.hasMoreTokens())
                {
                    String token=tok.nextToken();
                    if ("Version".equalsIgnoreCase(token))
                        cookie.setVersion(Integer.parseInt(tok.nextToken()));
                    else if ("Comment".equalsIgnoreCase(token))
                        cookie.setComment(tok.nextToken());
                    else if ("Path".equalsIgnoreCase(token))
                        cookie.setPath(tok.nextToken());
                    else if ("Domain".equalsIgnoreCase(token))
                        cookie.setDomain(tok.nextToken());
                    else if ("Expires".equalsIgnoreCase(token))
                    {
                        tok.nextToken();
                        // TODO
                    }
                    else if ("Max-Age".equalsIgnoreCase(token))
                    {
                        tok.nextToken();
                        // TODO
                    }
                    else if ("Secure".equalsIgnoreCase(token))
                        cookie.setSecure(true);
                }

                BayeuxClient.this.setCookie(cookie);
            }
        }

        /* ------------------------------------------------------------ */
        protected void onResponseComplete() throws IOException
        {            
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            super.onResponseComplete();

            if (getResponseStatus()==200)
            {
                String content = getResponseContent();
                //TODO
                if (content==null || content.length()==0)
                    throw new IllegalStateException();
                _responses=_msgPool.parse(content);
            }
        }

        /* ------------------------------------------------------------ */
        protected void onExpire()
        {
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            super.onExpire();
            if (!send (this, true))
                Log.warn("Retries exhausted"); //giving up
        }

        /* ------------------------------------------------------------ */
        protected void onConnectionFailed(Throwable ex)
        {
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            super.onConnectionFailed(ex);

            if (!send (this, true))
                Log.warn("Retries exhausted", ex);
        }

        /* ------------------------------------------------------------ */
        protected void onException(Throwable ex)
        {
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            super.onException(ex);
            if (!send (this, true))
                Log.warn("Retries exhausted", ex);
        }
    }

    /* ------------------------------------------------------------ */
    /** The Bayeux handshake exchange.
     * Negotiates a client Id and initializes the protocol.
     *
     */
    protected class Handshake extends Exchange
    {
        final static String __HANDSHAKE="[{"+"\"channel\":\"/meta/handshake\","+"\"version\":\"0.9\","+"\"minimumVersion\":\"0.9\""+"}]";

        Handshake()
        {
            super("handshake");
            setMessage(__HANDSHAKE);
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.cometd.client.BayeuxClient.Exchange#onResponseComplete()
         */
        protected void onResponseComplete() throws IOException
        {
            super.onResponseComplete();
            if (getResponseStatus()==200&&_responses!=null&&_responses.length>0)
            {
                Map<?,?> response=(Map<?,?>)_responses[0];
                Boolean successful=(Boolean)response.get(Bayeux.SUCCESSFUL_FIELD);

                //Get advice if there is any
                Map adviceField = (Map)response.get(Bayeux.ADVICE_FIELD);
                if (adviceField != null)
                    _advice = new Advice(adviceField);   

                if (successful!=null&&successful.booleanValue())
                {
                    if (Log.isDebugEnabled()) Log.debug("Successful handshake, sending connect");
                    _clientId=(String)response.get(Bayeux.CLIENT_FIELD);
                    _pull=new Connect();
                    send((Exchange)_pull, false);
                }
                else
                {  
                    if (_advice != null && _advice.isReconnectNone())
                        throw new IOException("Handshake failed with advice reconnect=none :"+_responses[0]);
                    else if (_advice != null && _advice.isReconnectHandshake())
                    {
                        _pull = new Handshake();
                        if (!send ((Exchange)_pull, true))
                            throw new IOException("Handshake, retries exhausted");
                    }
                    else //assume retry = reconnect?
                    {
                        _pull = new Connect();
                        if (!send((Exchange)_pull, true))
                            throw new IOException("Connect after handshake, retries exhausted");
                    }
                }

                metaHandshake(successful.booleanValue(), successful.booleanValue() && _handshook);
                if (successful.booleanValue())
                    _handshook = true;
            }
            else
            {
                setMessage(__HANDSHAKE);
                this.reset();
                if (!send (this, true))
                    throw new IOException("Handshake, retries exhausted");
            }
        }


         /* ------------------------------------------------------------ */
         protected void onExpire()
         {
             if (Log.isDebugEnabled()) Log.debug("HANDSHAKE: Connection timed out "+this);
             setMessage(__HANDSHAKE);
             super.onExpire(); 
         }
 
         /* ------------------------------------------------------------ */
         protected void onConnectionFailed(Throwable ex)
         {
             if (Log.isDebugEnabled()) Log.debug("HANDSHAKE: Got connection fail "+this);
             setMessage(__HANDSHAKE);
             super.onConnectionFailed(ex); 
         }
 
         /* ------------------------------------------------------------ */
         protected void onException(Throwable ex)
         { 
             if (Log.isDebugEnabled()) Log.debug("HANDSHAKE: Got exception "+this);
             setMessage(__HANDSHAKE);
             super.onException(ex);
         }
    }

    /* ------------------------------------------------------------ */
    /** The Bayeux Connect exchange.
     * Connect exchanges implement the long poll for Bayeux.
     */
    protected class Connect extends Exchange
    {
        String _connectString;
        Connect()
        {
            super("connect");
            _connectString = "{"+"\"channel\":\"/meta/connect\","+"\"clientId\":\""+_clientId+"\","+"\"connectionType\":\"long-polling\""+"}";
            setMessage(_connectString);
        }

        protected void onResponseComplete() throws IOException
        {
            super.onResponseComplete();
            if (getResponseStatus()==200&&_responses!=null&&_responses.length>0)
            {
                try
                {
                    startBatch();

                    for (int i=0; i<_responses.length; i++)
                    {
                        Message msg=(Message)_responses[i];
                        
                        //get advice if there is any
                        Map adviceField = (Map)msg.get(Bayeux.ADVICE_FIELD);
                        if (adviceField != null)
                            _advice = new Advice(adviceField);
                        
                        if (Bayeux.META_CONNECT.equals(msg.get(Bayeux.CHANNEL_FIELD)))
                        {
                            Boolean successful=(Boolean)msg.get(Bayeux.SUCCESSFUL_FIELD);
                            if (successful!=null&&successful.booleanValue())
                            {
                                if (!isInitialized())
                                {
                                    setInitialized(true);
                                    synchronized (_outQ)
                                    {
                                        if (_outQ.size()>0)
                                        {
                                            _push=new Publish();
                                            send((Exchange)_push, false);
                                        }
                                    }
                                }
                                //send a Connect (ie longpoll) possibly with delay according to interval advice
                                _pull = new Connect();
                                send((Exchange)_pull, true);
                                metaConnect(true);
                            }
                            else
                            {
                                //received a failure to our connect message, check the advice to see what to do:
                                //reconnect: none = hard error
                                //reconnect: handshake = send a handshake message
                                //reconnect: retry = send another connect, possibly using interval
                                
                                setInitialized(false);
                                if (_advice != null && _advice.isReconnectNone())
                                    throw new IOException("Connect failed, advice reconnect=none");                      
                                else if (_advice != null && _advice.isReconnectHandshake())
                                {
                                    if (Log.isDebugEnabled()) Log.debug("connect received success=false, advice is to rehandshake");
                                    _pull=new Handshake();
                                    send((Exchange)_pull, true);
                                }
                                else // assume retry = reconnect
                                {
                                    if (Log.isDebugEnabled()) Log.debug("Assuming retry=reconnect");
                                    setMessage(_connectString);
                                    if (!send (this, true))
                                        throw new IOException("Connect, retries exhausted");
                                }
                                metaConnect(false);
                            }
                        }
                        deliver(null,msg);
                    }
                }
                finally
                {
                    endBatch();
                }
            }
            else
            {
                Log.warn("Connect, error="+getResponseStatus());
                setMessage(_connectString);
                if (!send(this, true))
                    throw new IOException("Connect, retries exhausted");
            }
        }   
        
      
        
        
        /* ------------------------------------------------------------ */
        protected void onExpire()
        {
            if (Log.isDebugEnabled()) Log.debug("CONNECT: Connection timed out "+this);
            setInitialized(false);
            setMessage(_connectString);
            super.onExpire();
        }

        /* ------------------------------------------------------------ */
        protected void onConnectionFailed(Throwable ex)
        {
            if (Log.isDebugEnabled()) Log.debug("CONNECT: Got connection fail "+this);
            setInitialized(false);
            setMessage(_connectString);
            super.onConnectionFailed(ex);
        }

        /* ------------------------------------------------------------ */
        protected void onException(Throwable ex)
        { 
            if (Log.isDebugEnabled()) Log.debug("CONNECT: Got exception "+this);
            setInitialized(false);
            setMessage(_connectString);
            super.onException(ex);
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * Publish message exchange.
     * Sends messages to bayeux server and handles any messages received as a result.
     */
    protected class Publish extends Exchange
    {
        Publish()
        {
            super("publish");
            synchronized (_outQ)
            {
                if (_outQ.size()==0)
                    return;
                setMessages(_outQ);
                _outQ.clear();
            }
        }
        
        protected Message[] getOutboundMessages ()
        {
            try
            {
                return _msgPool.parse(_jsonOutboundMessages);
            }
            catch (IOException e)
            {
                Log.warn("Error converting outbound messages");
                if (Log.isDebugEnabled()) Log.debug(e);
                return null;
            }
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.cometd.client.BayeuxClient.Exchange#onResponseComplete()
         */
        protected void onResponseComplete() throws IOException
        {         
            super.onResponseComplete();
            try
            {
                synchronized (_outQ)
                {
                    startBatch();
                    _push=null;
                }

                if (getResponseStatus()==200&&_responses!=null&&_responses.length>0)
                {
                    for (int i=0; i<_responses.length; i++)
                    {
                        Message msg=(Message)_responses[i];
                        deliver(null,msg);
                    }
                }
                else
                {
                    Log.warn("Publish, error="+getResponseStatus());
                }
            }
            finally
            {
                endBatch();
            }
        }
        
        /* ------------------------------------------------------------ */
        protected void onExpire()
        {
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }

            Log.warn("Publish: Connection timed out");
            metaPublishFail(this.getOutboundMessages());
        }

        /* ------------------------------------------------------------ */
        protected void onConnectionFailed(Throwable ex)
        {
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            Log.warn("Publish: Got connection fail ", ex);
            metaPublishFail(this.getOutboundMessages());
        }

        /* ------------------------------------------------------------ */
        protected void onException(Throwable ex)
        { 
            if (!isRunning())
            {
                Log.warn("Not running");
                return;
            }
            
            Log.warn("Publish: Got exception ",ex);
            metaPublishFail(this.getOutboundMessages());
        }
    }

    public void addListener(ClientListener listener)
    {
        synchronized(_inQ)
        {
            if (listener instanceof MessageListener)
            {
                if (_mListeners==null)
                    _mListeners=new ArrayList<MessageListener>();
                _mListeners.add((MessageListener)listener);
            }
            if (listener instanceof RemoveListener)
            {
                if (_rListeners==null)
                    _rListeners=new ArrayList<RemoveListener>();
                _rListeners.add((RemoveListener)listener);
            }
        }
    }

    public void removeListener(ClientListener listener)
    {
        synchronized(_inQ)
        {
            if (listener instanceof MessageListener)
            {
                if (_mListeners!=null)
                    _mListeners.remove((MessageListener)listener);
            }
            if (listener instanceof RemoveListener)
            {
                if (_rListeners!=null)
                    _rListeners.remove((RemoveListener)listener);
            }
        }
    }

    public int getMaxQueue()
    {
        return -1;
    }

    public Queue<Message> getQueue()
    {
        return _inQ;
    }

    public void setMaxQueue(int max)
    {
        if (max!=-1)
            throw new UnsupportedOperationException();
    }
    

    
    /**
     * Send the exchange, possibly using a backoff.
     * 
     * @param exchange
     * @param backoff if true, use backoff algorithm to send
     * @return
     */
    protected boolean send (final Exchange exchange, boolean backoff)
    {
        if (isRunning())
        {
            if (backoff)
            {
                int retries = exchange.getBackoffRetries();
                if (Log.isDebugEnabled()) Log.debug("Send with backoff, retries="+retries+" for "+exchange);
                if (retries < _backoffMaxRetries)
                {
                    exchange.incBackoffRetries();
                    long interval = (_advice != null ? _advice.getInterval() : 0) + (retries * _backoffInterval);

                    if (interval > 0)
                    {
                        TimerTask task = new TimerTask()
                        {
                            public void run()
                            {
                                try
                                {
                                    send(exchange);           
                                }
                                catch (IOException e)
                                {
                                    Log.warn("Delayed send, retry: ", e);
                                    send(exchange, true); //start backing off
                                }
                            }
                        };
                        if (Log.isDebugEnabled()) Log.debug("Delayed send: "+interval);
                        _timer.schedule(task, interval);
                    }
                    else
                    {
                        try
                        {  
                            send (exchange);
                        }
                        catch (IOException e)
                        {
                            Log.warn("Send, retry on fail: ", e);
                            return send (exchange, true); //start backing off
                        }
                    }
                    return true;
                }
                else
                    return false;
            }
            else
            {
                try
                {
                    send(exchange);
                    return true;
                } 
                catch (IOException e)
                {
                    Log.warn("Send, retry on fail: ", e);
                    return send (exchange, true); //start backing off
                }
            }
        }
        else
        {
            Log.warn("Not running");
            return false;
        }
           
    }
     
     
     
     /**
      * Send the exchange.
      * 
      * @param exchange
      * @throws IOException
      */
    protected void send (HttpExchange exchange)
    throws IOException
    {
        exchange.reset(); //ensure at start state
        customize(exchange);

        try
        {
            if (Log.isDebugEnabled()) Log.debug("Send: using any connection="+exchange);
            _client.send(exchange); //use any connection
        }
        catch (IOException e)
        {
            Log.warn("Send", e);
            throw e;
        }
    }
     
     /**
      * False when we have received a success=false message in response to a Connect,
      * or we have had an exception when sending or receiving a Connect.
      * 
      * True when handshake and then connect has happened.
      * @param b
      */
     protected void setInitialized (boolean b)
     {
         _initialized = b;
     }
     
     protected boolean isInitialized ()
     {
         return _initialized;
     }
     
     public void metaConnect(boolean success)
     {        
     }
 
     public void metaDisconnect()
     {        
     }
 
     public void metaHandshake(boolean success, boolean reestablish)
     {        
     }
 
     public void metaPublishFail(Message[] messages)
     {
     }
}

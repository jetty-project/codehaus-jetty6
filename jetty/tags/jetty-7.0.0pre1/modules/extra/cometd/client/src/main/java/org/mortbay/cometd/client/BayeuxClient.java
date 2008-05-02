// ========================================================================
// Copyright 2006-2007 Mort Bay Consulting Pty. Ltd.
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
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.Cookie;

import org.mortbay.cometd.MessageImpl;
import org.mortbay.cometd.MessagePool;
import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpSchemes;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpConnection;
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.log.Log;
import org.mortbay.util.QuotedStringTokenizer;
import org.mortbay.util.ajax.JSON;

import dojox.cometd.Bayeux;
import dojox.cometd.Client;
import dojox.cometd.Listener;
import dojox.cometd.RemoveListener;
import dojox.cometd.Message;
import dojox.cometd.MessageListener;

/* ------------------------------------------------------------ */
/** Bayeux protocol Client.
 * <p>
 * Implements a Bayeux Ajax Push client as part of the cometd project.
 * 
 * @see http://cometd.com
 * @author gregw
 *
 */
public class BayeuxClient extends MessagePool implements Client
{
    private HttpClient _client;
    private HttpConnection _clientConnection;
    private InetSocketAddress _address;
    private HttpExchange _pull;
    private HttpExchange _push;
    private String _uri="/cometd";
    private boolean _initialized=false;
    private boolean _disconnecting=false;
    private String _clientId;
    private Listener _listener;
    private List<RemoveListener> _rListeners;
    private List<MessageListener> _mListeners;
    private List<Message> _inQ;  // queue of incoming messages used if no listener available. Used as the lock object for all incoming operations.
    private List<Message> _outQ; // queue of outgoing messages. Used as the lock object for all outgoing operations.
    private int _batch;
    private boolean _formEncoded;
    private Map<String, Cookie> _cookies=new ConcurrentHashMap<String, Cookie>();

    /* ------------------------------------------------------------ */
    public BayeuxClient(HttpClient client, InetSocketAddress address, String uri) throws IOException
    {
        _client=client;
        _address=address;
        _uri=uri;

        _inQ=new LinkedList<Message>();
        _outQ=new LinkedList<Message>();
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

    /* ------------------------------------------------------------ */
    public void start() throws UnknownHostException, IOException
    {
        synchronized (_outQ)
        {
            if (!_initialized && _pull==null)
                _pull=new Handshake();
        }
    }
    
    /* ------------------------------------------------------------ */
    private void checkConnection() throws UnknownHostException, IOException
    {
        synchronized (_outQ)
        {
            if (_clientConnection==null)
            {
                HttpDestination destination = _client.getDestination(_address,false);
                _clientConnection=destination.getConnection();
                if (_clientConnection==null)
                    throw new IOException("unable to open connection to "+_address);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public boolean isPolling()
    {
        synchronized (_outQ)
        {
            return _pull!=null;
        }
    }

    /* ------------------------------------------------------------ */
    /** (non-Javadoc)
     * @deprecated use {@link #deliver(Client, String, Object, String)}
     * @see dojox.cometd.Client#deliver(dojox.cometd.Client, java.util.Map)
     */
    public void deliver(Client from, Message message)
    {
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
    public Listener getListener()
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
        synchronized (_outQ)
        {
            _outQ.add(msg);

            if (_batch==0&&_initialized&&_push==null)
                _push=new Publish();
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#publish(java.lang.String, java.lang.Object, java.lang.String)
     */
    public void publish(String toChannel, Object data, String msgId)
    {
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
        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,Bayeux.META_UNSUBSCRIBE);
        msg.put(Bayeux.SUBSCRIPTION_FIELD,toChannel);
        publish(msg);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#remove(boolean)
     */
    public void remove(boolean timeout)
    {
        Message msg=new MessageImpl();
        msg.put(Bayeux.CHANNEL_FIELD,Bayeux.META_DISCONNECT);

        synchronized (_outQ)
        {
            _outQ.add(msg);

            _initialized=false;
            _disconnecting=true;

            if (_batch==0&&_initialized&&_push==null)
                _push=new Publish();

        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated
     */
    public void setListener(Listener listener)
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
        synchronized (_outQ)
        {
            if (--_batch<=0)
            {
                _batch=0;
                if ((_initialized||_disconnecting)&&_push==null&&_outQ.size()>0)
                    _push=new Publish();
            }
        }
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#startBatch()
     */
    public void startBatch()
    {
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
    private class Exchange extends HttpExchange.ContentExchange
    {
        Object[] _responses;
        int _connectFailures;

        Exchange(String info)
        {
            setMethod("POST");
            setScheme(HttpSchemes.HTTP_BUFFER);
            setAddress(_address);
            setURI(_uri+"/"+info);

            setRequestContentType(_formEncoded?"application/x-www-form-urlencoded;charset=utf-8":"text/json;charset=utf-8");
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

        protected void setMessages(List<Message> messages)
        {
            try
            {
                for (Message msg : messages)
                {
                    msg.put(Bayeux.CLIENT_FIELD,_clientId);
                }
                String json=JSON.toString(messages);

                if (_formEncoded)
                    setRequestContent(new ByteArrayBuffer("message="+URLEncoder.encode(json,"utf-8")));
                else
                    setRequestContent(new ByteArrayBuffer(json,"utf-8"));

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
            super.onResponseComplete();

            if (getResponseStatus()==200)
            {
                _responses=parse(getResponseContent());
            }
        }

        /* ------------------------------------------------------------ */
        protected void onExpire()
        {
            super.onExpire();
        }

        /* ------------------------------------------------------------ */
        protected void onConnectionFailed(Throwable ex)
        {
            super.onConnectionFailed(ex);
            if (++_connectFailures<5)
            {
                try
                {
                    _client.send(this);
                }
                catch (IOException e)
                {
                    Log.warn(e);
                }
            }
        }

        /* ------------------------------------------------------------ */
        protected void onException(Throwable ex)
        {
            super.onException(ex);
        }

    }

    /* ------------------------------------------------------------ */
    /** The Bayeux handshake exchange.
     * Negotiates a client Id and initializes the protocol.
     *
     */
    private class Handshake extends Exchange
    {
        final static String __HANDSHAKE="[{"+"\"channel\":\"/meta/handshake\","+"\"version\":\"0.9\","+"\"minimumVersion\":\"0.9\""+"}]";

        Handshake()
        {
            super("handshake");
            setMessage(__HANDSHAKE);

            try
            {
                customize(this);
                checkConnection();
                _clientConnection.send(this);
                //_client.send(this);
            }
            catch (IOException e)
            {
                _clientConnection=null;
                Log.warn(e);
            }
        }

        /* ------------------------------------------------------------ */
        /* (non-Javadoc)
         * @see org.mortbay.jetty.client.HttpExchange#onException(java.lang.Throwable)
         */
        protected void onException(Throwable ex)
        {
            Log.warn("Handshake:"+ex);
            Log.debug(ex);
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
                if (successful!=null&&successful.booleanValue())
                {
                    _clientId=(String)response.get(Bayeux.CLIENT_FIELD);
                    _pull=new Connect();
                }
                else
                    throw new IOException("Handshake failed:"+_responses[0]);
            }
            else
            {
                throw new IOException("Handshake failed: "+getResponseStatus());
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** The Bayeux Connect exchange.
     * Connect exchanges implement the long poll for Bayeux.
     */
    private class Connect extends Exchange
    {
        Connect()
        {
            super("connect");
            String connect="{"+"\"channel\":\"/meta/connect\","+"\"clientId\":\""+_clientId+"\","+"\"connectionType\":\"long-polling\""+"}";
            setMessage(connect);

            try
            {
                customize(this);
                checkConnection();
                _clientConnection.send(this);
                // _client.send(this);
            }
            catch (IOException e)
            {
                _clientConnection=null;
                Log.warn(e);
            }
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

                        if (Bayeux.META_CONNECT.equals(msg.get(Bayeux.CHANNEL_FIELD)))
                        {
                            Boolean successful=(Boolean)msg.get(Bayeux.SUCCESSFUL_FIELD);
                            if (successful!=null&&successful.booleanValue())
                            {
                                if (!_initialized)
                                {
                                    _initialized=true;
                                    synchronized (_outQ)
                                    {
                                        if (_outQ.size()>0)
                                            _push=new Publish();
                                    }
                                }

                                _pull=new Connect();
                            }
                            else
                                throw new IOException("Connect failed:"+_responses[0]);
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
                throw new IOException("Connect failed: "+getResponseStatus());
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * Publish message exchange.
     * Sends messages to bayeux server and handles any messages received as a result.
     */
    private class Publish extends Exchange
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
            try
            {
                customize(this);
                _client.send(this);
            }
            catch (IOException e)
            {
                Log.warn(e);
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
                    throw new IOException("Reconnect failed: "+getResponseStatus());
                }
            }
            finally
            {
                endBatch();
            }
        }
    }

    public void addListener(EventListener listener)
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

    public void removeListener(EventListener listener)
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

}

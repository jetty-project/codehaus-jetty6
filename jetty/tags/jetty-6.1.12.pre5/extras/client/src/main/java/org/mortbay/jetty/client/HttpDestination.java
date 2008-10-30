//========================================================================
//Copyright 2006-2007 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================
package org.mortbay.jetty.client;


import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import javax.servlet.http.Cookie;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.client.security.Authorization;
import org.mortbay.jetty.client.security.SecurityListener;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.log.Log;

/**
* @author Greg Wilkins
* @author Guillaume Nodet
*/
public class HttpDestination
{
    private ByteArrayBuffer _hostHeader;
    private final Address _address;
    private final LinkedList<HttpConnection> _connections = new LinkedList<HttpConnection>();
    private final ArrayList<HttpConnection> _idle = new ArrayList<HttpConnection>();
    private final HttpClient _client;
    private final boolean _ssl;
    private int _maxConnections;
    private int _pendingConnections=0;
    private ArrayBlockingQueue<Object> _newQueue = new ArrayBlockingQueue<Object>(10,true);
    private int _newConnection=0;
    private Address _proxy;
    private Authorization _proxyAuthentication;
    private PathMap _authorizations;
    private List<Cookie> _cookies;

    public void dump() throws IOException
    {
        synchronized (this)
        {
            System.err.println(this);
            System.err.println("connections="+_connections.size());
            System.err.println("idle="+_idle.size());
            System.err.println("pending="+_pendingConnections);
            for (HttpConnection c : _connections)
            {
                if (!c.isIdle())
                    c.dump();
            }
        }
    }
    
    /* The queue of exchanged for this destination if connections are limited */
    private LinkedList<HttpExchange> _queue=new LinkedList<HttpExchange>();

    /* ------------------------------------------------------------ */
    HttpDestination(HttpClient pool, Address address, boolean ssl, int maxConnections)
    {
        _client=pool;
        _address=address;
        _ssl=ssl;
        _maxConnections=maxConnections;
        String addressString = address.getHost();
        if (address.getPort() != (_ssl ? 443 : 80)) addressString += ":" + address.getPort();
        _hostHeader = new ByteArrayBuffer(addressString);
    }

    /* ------------------------------------------------------------ */
    public Address getAddress()
    {
        return _address;
    }

    /* ------------------------------------------------------------ */
    public Buffer getHostHeader()
    {
        return _hostHeader;
    }
    
    /* ------------------------------------------------------------ */
    public HttpClient getHttpClient()
    {
        return _client;
    }

    /* ------------------------------------------------------------ */
    public boolean isSecure()
    {
        return _ssl;
    }
    
    /* ------------------------------------------------------------ */
    public void addAuthorization(String pathSpec,Authorization authorization)
    {
        synchronized (this)
        {
            if (_authorizations==null)
                _authorizations=new PathMap();
            _authorizations.put(pathSpec,authorization);
        }
        
        // TODO query and remove methods
    }

    /* ------------------------------------------------------------------------------- */
    public void addCookie(Cookie cookie)
    {
        synchronized (this)
        {
            if (_cookies==null)
                _cookies=new ArrayList<Cookie>();
            _cookies.add(cookie);
        }
        
        // TODO query, remove and age methods
    }
    
    /* ------------------------------------------------------------------------------- */
    public HttpConnection getConnection() throws IOException
    {
        HttpConnection connection = getIdleConnection();

        while (connection==null)
        {
            synchronized(this)
            {
                _newConnection++;
                startNewConnection();
            }

            try
            {
                Object o =_newQueue.take();
                if (o instanceof HttpConnection)
                    connection=(HttpConnection)o;
                else
                    throw (IOException)o;
            }
            catch (InterruptedException e)
            {
                Log.ignore(e);
            }
        }
        return connection;
    }
    
    /* ------------------------------------------------------------------------------- */
    public HttpConnection getIdleConnection() throws IOException
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            long idleTimeout=_client.getIdleTimeout();
 
            // Find an idle connection
            while (_idle.size() > 0)
            {
                HttpConnection connection = _idle.remove(_idle.size()-1);
                long last = connection.getLast();
                if (connection.getEndPoint().isOpen() && (last==0 || ((now-last)<idleTimeout)) )
                    return connection;
                else
                {
                    _connections.remove(connection);
                    connection.getEndPoint().close();
                }
            }

            return null;
        }
    }

    /* ------------------------------------------------------------------------------- */
    protected void startNewConnection() 
    {
        try
        {
            synchronized (this)
            {
                _pendingConnections++;
            }
            _client._connector.startConnection(this);
        }
        catch(Exception e)
        {
            onConnectionFailed(e);
        }
    }

    /* ------------------------------------------------------------------------------- */
    public void onConnectionFailed(Throwable throwable)
    {
        Throwable connect_failure=null;
        
        synchronized (this)
        {
            _pendingConnections--;
            if (_newConnection>0)
            {
                connect_failure=throwable;
                _newConnection--;
            }
            else if (_queue.size()>0)
            {
                HttpExchange ex=_queue.removeFirst();
                ex.getEventListener().onConnectionFailed(throwable);
            }
        }

        if(connect_failure!=null)
        {
            try
            {
                _newQueue.put(connect_failure);
            }
            catch (InterruptedException e)
            {
                Log.ignore(e);
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    public void onException(Throwable throwable)
    {
        synchronized (this)
        {
            _pendingConnections--;
            if (_queue.size()>0)
            {
                HttpExchange ex=_queue.removeFirst();
                ex.getEventListener().onException(throwable);
                ex.setStatus(HttpExchange.STATUS_EXCEPTED);
            }
        }
    }
    
    /* ------------------------------------------------------------------------------- */
    public void onNewConnection(HttpConnection connection) throws IOException
    {
        HttpConnection q_connection=null;
        
        synchronized (this)
        {
            _pendingConnections--;
            _connections.add(connection);
            
            if (_newConnection>0)
            {
                q_connection=connection;
                _newConnection--;
            }
            else if (_queue.size()==0)
            {
                _idle.add(connection);
            }
            else
            {
                HttpExchange ex=_queue.removeFirst();
                connection.send(ex);
            }
        }

        if (q_connection!=null)
        {
            try
            {
                _newQueue.put(q_connection);
            }
            catch (InterruptedException e)
            {
                Log.ignore(e);
            }
        }
    }

    /* ------------------------------------------------------------------------------- */
    public void returnConnection(HttpConnection connection, boolean close) throws IOException
    {
        if (close)
        {
            try
            {
                connection.close();
            }
            catch(IOException e)
            {
                Log.ignore(e);
            }
        }
        
        if (!_client.isStarted())
            return;
        
        if (!close && connection.getEndPoint().isOpen())
        {
            synchronized (this)
            {
                if (_queue.size()==0)
                {
                    connection.setLast(System.currentTimeMillis());
                    _idle.add(connection);
                }
                else
                {
                    HttpExchange ex=_queue.removeFirst();
                    connection.send(ex);
                }
                this.notifyAll();
            }
        }
        else 
        {
            synchronized (this)
            {
                _connections.remove(connection);
                if (!_queue.isEmpty())
                    startNewConnection();
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public void send(HttpExchange ex) throws IOException
    {
        LinkedList<String> listeners = _client.getRegisteredListeners();

        if (listeners != null)
        {
            // Add registered listeners, fail if we can't load them
            for (int i = listeners.size(); i > 0; --i)
            {
                String listenerClass = listeners.get(i - 1);

                try
                {
                    Class listener = Class.forName(listenerClass);
                    Constructor constructor = listener.getDeclaredConstructor(HttpDestination.class, HttpExchange.class);
                    HttpEventListener elistener = (HttpEventListener) constructor.newInstance(this, ex);
                    ex.setEventListener(elistener);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    throw new IOException("Unable to instantiate registered listener for destination: " + listenerClass );
                }
            }
        }

        // Security is supported by default and should be the first consulted
        if ( _client.hasRealms() )
        {
            ex.setEventListener( new SecurityListener( this, ex ) );
        }
        
        doSend(ex);
    }
    
    /* ------------------------------------------------------------ */
    public void resend(HttpExchange ex) throws IOException
    {
        ex.getEventListener().onRetry();
        doSend(ex);
    }
    
    /* ------------------------------------------------------------ */
    protected void doSend(HttpExchange ex) throws IOException
    {
        // add cookies
        // TODO handle max-age etc.
        if (_cookies!=null)
        {
            StringBuilder buf=null;
            for (Cookie cookie : _cookies)
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
                ex.addRequestHeader(HttpHeaders.COOKIE,buf.toString());
        }
        
        // Add any known authorizations
        if (_authorizations!=null)
        {
            Authorization auth= (Authorization)_authorizations.match(ex.getURI());
            if (auth !=null)
                ((Authorization)auth).setCredentials(ex);
        }
       
        synchronized(this)
        {
            //System.out.println( "Sending: " + ex.toString() );

            HttpConnection connection=null;
            if (_queue.size()>0 || (connection=getIdleConnection())==null || !connection.send(ex))
            {
                _queue.add(ex);
                if (_connections.size()+_pendingConnections <_maxConnections)
                {
                     startNewConnection();
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    public synchronized String toString()
    {
        return "HttpDestination@" + hashCode() + "//" + _address.getHost() + ":" + _address.getPort() + "(" + _connections.size() + "," + _idle.size() + "," + _queue.size() + ")";
    }
    
    /* ------------------------------------------------------------ */
    public synchronized String toDetailString()
    {
        StringBuilder b = new StringBuilder();
        b.append(toString());
        b.append('\n');
        synchronized(this)
        {
            for (HttpConnection connection : _connections)
            {
                if (connection._exchange!=null)
                {
                    b.append(connection.toDetailString());
                    if (_idle.contains(connection))
                        b.append(" IDLE");
                    b.append('\n');
                }
            }
        }
        b.append("--");
        b.append('\n');
        
        return b.toString();
    }

    /* ------------------------------------------------------------ */
    public void setProxy(Address proxy)
    {
        _proxy=proxy;
    }

    /* ------------------------------------------------------------ */
    public Address getProxy()
    {
        return _proxy;
    }

    /* ------------------------------------------------------------ */
    public Authorization getProxyAuthentication()
    {
        return _proxyAuthentication;
    }

    /* ------------------------------------------------------------ */
    public void setProxyAuthentication(Authorization authentication)
    {
        _proxyAuthentication = authentication;
    }

    /* ------------------------------------------------------------ */
    public boolean isProxied()
    {
        return _proxy!=null;
    }

    /* ------------------------------------------------------------ */
    public void close() throws IOException
    {
        synchronized (this)
        {
            for (HttpConnection connection : _connections)
            {
                connection.close();
            }
        }
    }
    
}

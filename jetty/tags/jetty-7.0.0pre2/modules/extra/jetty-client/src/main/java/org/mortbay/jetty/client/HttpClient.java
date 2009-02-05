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

package org.mortbay.jetty.client;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.mortbay.component.LifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractBuffers;
import org.mortbay.jetty.HttpSchemes;
import org.mortbay.jetty.client.security.DefaultRealmResolver;
import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.client.security.SecurityRealmResolver;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.mortbay.thread.Timeout;

/**
 * Http Client.
 * 
 * HttpClient is the main active component of the client API implementation.
 * It is the opposite of the Connectors in standard Jetty, in that it listens 
 * for responses rather than requests.   Just like the connectors, there is a
 * blocking socket version and a non-blocking NIO version (implemented as nested classes
 * selected by {@link #setConnectorType(int)}).
 * 
 * The an instance of {@link HttpExchange} is passed to the {@link #send(HttpExchange)} method 
 * to send a request.  The exchange contains both the headers and content (source) of the request
 * plus the callbacks to handle responses.   A HttpClient can have many exchanges outstanding
 * and they may be queued on the {@link HttpDestination} waiting for a {@link HttpConnection},
 * queued in the {@link HttpConnection} waiting to be transmitted or pipelined on the actual
 * TCP/IP connection waiting for a response.
 * 
 * The {@link HttpDestination} class is an aggregation of {@link HttpConnection}s for the 
 * same host, port and protocol.   A destination may limit the number of connections 
 * open and they provide a pool of open connections that may be reused.   Connections may also 
 * be allocated from a destination, so that multiple request sources are not multiplexed
 * over the same connection.
 * 
 * @see {@link HttpExchange}
 * @see {@link HttpDestination}
 * @author Greg Wilkins
 * @author Matthew Purland
 * @author Guillaume Nodet
 */
public class HttpClient extends AbstractBuffers
{
    public static final int CONNECTOR_SOCKET=0;
    public static final int CONNECTOR_SELECT_CHANNEL=2;

    private int _connectorType=CONNECTOR_SOCKET;
    private boolean _useDirectBuffers=true;
    private int _maxConnectionsPerAddress=32;
    private Map<InetSocketAddress, HttpDestination> _destinations=new HashMap<InetSocketAddress, HttpDestination>();
    ThreadPool _threadPool;
    Connector _connector;
    private long _idleTimeout=20000;
    private long _timeout=320000;
    int _soTimeout = 10000;
    private Timeout _timeoutQ = new Timeout();
    private InetSocketAddress _proxy;
    private Set<InetAddress> _noProxy;

    private SecurityRealmResolver _realmResolver;    

    
    
    /* ------------------------------------------------------------------------------- */
    public void send(HttpExchange exchange) throws IOException
    {
        boolean ssl=HttpSchemes.HTTPS_BUFFER.equalsIgnoreCase(exchange.getScheme());
        exchange.setStatus(HttpExchange.STATUS_WAITING_FOR_CONNECTION);
        HttpDestination destination=getDestination(exchange.getAddress(),ssl);
        destination.send(exchange);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the threadPool
     */
    public ThreadPool getThreadPool()
    {
        return _threadPool;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param threadPool the threadPool to set
     */
    public void setThreadPool(ThreadPool threadPool)
    {
        _threadPool=threadPool;
    }

    /* ------------------------------------------------------------------------------- */
    public HttpDestination getDestination(InetSocketAddress remote, boolean ssl) throws UnknownHostException, IOException
    {
        if (remote==null)
            throw new UnknownHostException("Remote socket address cannot be null.");

        synchronized (_destinations)
        {
            HttpDestination destination=_destinations.get(remote);
            if (destination==null)
            {
                destination=new HttpDestination(this,remote,ssl,_maxConnectionsPerAddress);
                if (_proxy!=null && (_noProxy==null || !_noProxy.contains(remote.getAddress())))
                    destination.setProxy(_proxy);
                _destinations.put(remote,destination);
            }
            return destination;
        }
    } 

    /* ------------------------------------------------------------ */
    public void schedule(Timeout.Task task)
    {
        _timeoutQ.schedule(task); 
    }

    /* ------------------------------------------------------------ */
    public void cancel(Timeout.Task task)
    {
        task.cancel();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Get whether the connector can use direct NIO buffers.
     */
    public boolean getUseDirectBuffers()
    {
        return _useDirectBuffers;
    }

    /* ------------------------------------------------------------ */
    public void setSecurityRealmResolver( SecurityRealmResolver resolver )
    {
        _realmResolver = resolver;
    }

    /* ------------------------------------------------------------ */
    /**
     * returns the SecurityRealmResolver registered with the HttpClient or null
     * 
     * @return 
     */
    public SecurityRealmResolver getSecurityRealmResolver()
    {
        return _realmResolver;
    }
    
    /* ------------------------------------------------------------ */
    public boolean hasRealms()
    {
        return _realmResolver==null?false:true;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set to use NIO direct buffers.
     * 
     * @param direct
     *            If True (the default), the connector can use NIO direct
     *            buffers. Some JVMs have memory management issues (bugs) with
     *            direct buffers.
     */
    public void setUseDirectBuffers(boolean direct)
    {
        _useDirectBuffers=direct;
    }

    /* ------------------------------------------------------------ */
    /**
     * Get the type of connector (socket, blocking or select) in use.
     */
    public int getConnectorType()
    {
        return _connectorType;
    }

    /* ------------------------------------------------------------ */
    public void setConnectorType(int connectorType)
    {
        this._connectorType=connectorType;
    }

    /* ------------------------------------------------------------ */
    /**
     * Create a new NIO buffer. If using direct buffers, it will create a direct
     * NIO buffer, other than an indirect buffer.
     */
    @Override
    protected Buffer newBuffer(int size)
    {
        if (_connectorType!=CONNECTOR_SOCKET)
        {
            Buffer buf=null;
            if (size==getHeaderBufferSize())
                buf=new NIOBuffer(size,NIOBuffer.INDIRECT);
            else
                buf=new NIOBuffer(size,_useDirectBuffers?NIOBuffer.DIRECT:NIOBuffer.INDIRECT);
            return buf;
        }
        else
        {
            return new ByteArrayBuffer(size);
        }
    }

    /* ------------------------------------------------------------ */
    public int getMaxConnectionsPerAddress()
    {
        return _maxConnectionsPerAddress;
    }

    /* ------------------------------------------------------------ */
    public void setMaxConnectionsPerAddress(int maxConnectionsPerAddress)
    {
        _maxConnectionsPerAddress=maxConnectionsPerAddress;
    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        super.doStart();
        
        _timeoutQ.setNow();
        _timeoutQ.setDuration(_timeout);
        
        if(_threadPool==null)
        {
            QueuedThreadPool pool = new QueuedThreadPool();
            pool.setMaxThreads(16);
            pool.setDaemon(true);
            _threadPool=pool;
        }
        
        
        if (_threadPool instanceof QueuedThreadPool)
        {
            ((QueuedThreadPool)_threadPool).setName("HttpClient");
        }
        if (_threadPool instanceof LifeCycle)
        {
            ((LifeCycle)_threadPool).start();
        }

        
        if (_connectorType==CONNECTOR_SELECT_CHANNEL)
        {
            
            _connector=new SelectConnector(this);
        }
        else
        {
            _connector=new SocketConnector(this);
        }
        _connector.start();
        
        _threadPool.dispatch(new Runnable(){
            public void run()
            {
                while (isStarted())
                {
                    _timeoutQ.setNow();
                    _timeoutQ.tick();
                    try
                    {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
            }
        });
        
    }

    /* ------------------------------------------------------------ */
    protected void doStop() throws Exception
    {
        _connector.stop();
        _connector=null;
        if (_threadPool instanceof LifeCycle)
        {
            ((LifeCycle)_threadPool).stop();
        }
        for (HttpDestination destination : _destinations.values())
        {
            destination.close();
        }
        
        _timeoutQ.cancelAll();
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    interface Connector extends LifeCycle
    {
        public void startConnection(HttpDestination destination) throws IOException;

    }

    SSLContext getLooseSSLContext() throws IOException
    {
        // Create a trust manager that does not validate certificate
        // chains
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
        {
            public java.security.cert.X509Certificate[] getAcceptedIssuers()
            {
                return null;
            }

            public void checkClientTrusted( java.security.cert.X509Certificate[] certs, String authType )
            {
            }

            public void checkServerTrusted( java.security.cert.X509Certificate[] certs, String authType )
            {
            }
        } };

        HostnameVerifier hostnameVerifier = new HostnameVerifier()
        {
            public boolean verify( String urlHostName, SSLSession session )
            {
                System.out.println( "Warning: URL Host: " + urlHostName + " vs." + session.getPeerHost() );
                return true;
            }
        };

        // Install the all-trusting trust manager
        try
        {
            // TODO real trust manager
            SSLContext sslContext = SSLContext.getInstance( "SSL" );
            sslContext.init( null, trustAllCerts, new java.security.SecureRandom() );
            return sslContext;
        }
        catch ( Exception e )
        {
            throw new IOException( "issue ignoring certs" );
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the period in milliseconds a {@link HttpConnection} can be idle for before it is closed.
     */
    public long getIdleTimeout()
    {
        return _idleTimeout;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param ms the period in milliseconds a {@link HttpConnection} can be idle for before it is closed.
     */
    public void setIdleTimeout(long ms)
    {
        _idleTimeout=ms;
    }

    /* ------------------------------------------------------------ */
    public int getSoTimeout() 
    {
        return _soTimeout;
    }

    /* ------------------------------------------------------------ */
    public void setSoTimeout(int so) 
    {
        _soTimeout = so;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the period in ms that an exchange will wait for a response from the server.
     */
    public long getTimeout()
    {
        return _timeout;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param ms the period in ms that an exchange will wait for a response from the server.
     */
    public void setTimeout(long ms)
    {
        _timeout=ms;
    }

    /* ------------------------------------------------------------ */
    public InetSocketAddress getProxy()
    {
        return _proxy;
    }

    /* ------------------------------------------------------------ */
    public void setProxy(InetSocketAddress proxy)
    {
        this._proxy = proxy;
    }

    /* ------------------------------------------------------------ */
    public boolean isProxied()
    {
        return this._proxy!=null;
    }

    /* ------------------------------------------------------------ */
    public Set<InetAddress> getNoProxy()
    {
        return _noProxy;
    }

    /* ------------------------------------------------------------ */
    public void setNoProxy(Set<InetAddress> noProxyAddresses)
    {
        _noProxy = noProxyAddresses;
    }

    /* ------------------------------------------------------------ */
    public int maxRetries()
    {
        // TODO configurable
        return 3;
    }
    
    

}
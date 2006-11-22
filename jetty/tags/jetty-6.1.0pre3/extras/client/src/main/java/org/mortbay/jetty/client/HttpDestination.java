package org.mortbay.jetty.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.LinkedList;

public class HttpDestination
{
    private InetSocketAddress _address;
    private ArrayList<HttpConnection> _connections=new ArrayList<HttpConnection>();
    private LinkedList<HttpExchange> _exchanges=new LinkedList<HttpExchange>();
    private HttpConnectionPool _pool;

    
    /* ------------------------------------------------------------ */
    HttpDestination(HttpConnectionPool pool, InetSocketAddress address)
    {
        _pool=pool;
        _address=address;
    }

    /* ------------------------------------------------------------ */
    public InetSocketAddress getAddress()
    {
        return _address;
    }

    /* ------------------------------------------------------------------------------- */
    public HttpConnection getConnection(boolean ssl,boolean blockForIdle) 
        throws UnknownHostException, IOException
    {
        // TODO pool
        
        return newConnection(ssl);   
    }

    /* ------------------------------------------------------------------------------- */
    public HttpConnection newConnection(boolean ssl) 
        throws UnknownHostException, IOException
    {
        SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(false);
        channel.connect(_address);

        // TODO
        return null;
    }

    /* ------------------------------------------------------------ */
    public void send(HttpExchange ex) 
        throws IOException
    {
        _exchanges.add(ex);
        
        // TODO schedule stuff
        
    }
    
    /* ------------------------------------------------------------ */
    public void setAddress(InetSocketAddress address)
    {
        _address=address;
    }
}
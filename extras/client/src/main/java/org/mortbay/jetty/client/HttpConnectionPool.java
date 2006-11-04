package org.mortbay.jetty.client;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.io.Buffer;
import org.mortbay.io.nio.NIOBuffer;
import org.mortbay.jetty.AbstractBuffers;


public class HttpConnectionPool extends AbstractBuffers
{
    private boolean _useDirectBuffers=true;
    private Map<InetSocketAddress,HttpDestination> _destinations = new HashMap<InetSocketAddress,HttpDestination>();
    
    /* ------------------------------------------------------------------------------- */
    public HttpConnection getConnection(SocketAddress remote, boolean ssl,boolean blockForIdle) 
        throws UnknownHostException, IOException
    {
        // TODO pool
        
        return null;
    }
 
    /* ------------------------------------------------------------------------------- */
    public boolean getUseDirectBuffers()
    {
        return _useDirectBuffers;
    }

    /* ------------------------------------------------------------------------------- */
    /**
     * @param direct If True (the default), the connector can use NIO direct buffers.
     * Some JVMs have memory management issues (bugs) with direct buffers.
     */
    public void setUseDirectBuffers(boolean direct)
    {
        _useDirectBuffers=direct;
    }
    

    /* ------------------------------------------------------------------------------- */
    @Override
    protected Buffer newBuffer(int size)
    {
        Buffer buf = null;
        if (size==getHeaderBufferSize())
            buf= new NIOBuffer(size, NIOBuffer.INDIRECT);
        else
            buf = new NIOBuffer(size, _useDirectBuffers?NIOBuffer.DIRECT:NIOBuffer.INDIRECT);
        return buf;
    }
    
}

//========================================================================
//$Id: Connector.java,v 1.7 2005/11/25 21:01:45 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.io.IOException;

import org.mortbay.component.LifeCycle;
import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;
import org.mortbay.util.ajax.Continuation;

/** HTTP Connector.
 * Implementations of this interface provide connectors for the HTTP protocol.
 * A connector receives requests (normally from a socket) and calls the 
 * handle method of the Handler object.  These operations are performed using
 * threads from the ThreadPool set on the connector.
 * 
 * When a connector is registered with an instance of Server, then the server
 * will set itself as both the ThreadPool and the Handler.  Note that a connector
 * can be used without a Server if a thread pool and handler are directly provided.
 * 
 * @author gregw
 *
 */
public interface Connector extends LifeCycle, Buffers
{ 
    /* ------------------------------------------------------------ */
    /**
     * Opens the connector 
     * @throws IOException
     */
    public void open() throws IOException;
    
    public void close() throws IOException;

    public void setServer(Server server);
    public Server getServer();

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the headerBufferSize.
     */
    int getHeaderBufferSize();
    
    /* ------------------------------------------------------------ */
    /**
     * Set the size of the buffer to be used for request and response headers.
     * An idle connection will at most have one buffer of this size allocated.
     * @param headerBufferSize The headerBufferSize to set.
     */
    void setHeaderBufferSize(int headerBufferSize);
    
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestBufferSize.
     */
    int getRequestBufferSize();
    
    /* ------------------------------------------------------------ */
    /**
     * Set the size of the content buffer for receiving requests. 
     * These buffers are only used for active connections that have
     * requests with bodies that will not fit within the header buffer.
     * @param requestBufferSize The requestBufferSize to set.
     */
    void setRequestBufferSize(int requestBufferSize);
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the responseBufferSize.
     */
    int getResponseBufferSize();
    
    /* ------------------------------------------------------------ */
    /**
     * Set the size of the content buffer for sending responses. 
     * These buffers are only used for active connections that are sending 
     * responses with bodies that will not fit within the header buffer.
     * @param responseBufferSize The responseBufferSize to set.
     */
    void setResponseBufferSize(int responseBufferSize);
    

    /* ------------------------------------------------------------ */
    int getIntegralPort();
    String getIntegralScheme();
    boolean isIntegral(Request request);

    /* ------------------------------------------------------------ */
    int getConfidentialPort();
    String getConfidentialScheme();
    boolean isConfidential(Request request);

    /* ------------------------------------------------------------ */
    /** Customize a request for an endpoint.
     * Called on every request to allow customization of the request for
     * the particular endpoint (eg security properties from a SSL connection).
     * @param endpoint
     * @param request
     * @throws IOException
     */
    void customize(EndPoint endpoint, Request request) throws IOException;
    
    Continuation newContinuation();
    
    String getHost();
    void setHost(String hostname);

    /* ------------------------------------------------------------ */
    /**
     * @param port The port fto listen of for connections or 0 if any available
     * port may be used.
     */
    void setPort(int port);
    
    /* ------------------------------------------------------------ */
    /**
     * @return The configured port for the connector or 0 if any available
     * port may be used.
     */
    int getPort();
    
    /* ------------------------------------------------------------ */
    /**
     * @return The actual port the connector is listening on or -1 if there 
     * is no port or the connector is not open.
     */
    int getLocalPort();
    
    long getMaxIdleTime();
    void setMaxIdleTime(long ms);
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    Object getConnection();
}

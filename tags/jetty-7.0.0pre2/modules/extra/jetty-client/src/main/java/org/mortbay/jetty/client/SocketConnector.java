/**
 * 
 */
package org.mortbay.jetty.client;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.EndPoint;
import org.mortbay.io.bio.SocketEndPoint;
import org.mortbay.jetty.client.HttpClient.Connector;
import org.mortbay.log.Log;

class SocketConnector extends AbstractLifeCycle implements HttpClient.Connector
{
    /**
     * 
     */
    private final HttpClient _httpClient;

    /**
     * @param httpClient
     */
    SocketConnector(HttpClient httpClient)
    {
        _httpClient = httpClient;
    }

    public void startConnection(final HttpDestination destination) throws IOException
    {
        Socket socket=null;
        
        if ( destination.isSecure() )
        {
            SSLContext sslContext = _httpClient.getLooseSSLContext();
            socket = sslContext.getSocketFactory().createSocket();
        }
        else
        {
            System.out.println("Using Regular Socket");
            socket = SocketFactory.getDefault().createSocket();                
        }
       
        socket.connect(destination.isProxied()?destination.getProxy():destination.getAddress());
        
        EndPoint endpoint=new SocketEndPoint(socket);
        
        final HttpConnection connection=new HttpConnection(_httpClient,endpoint,_httpClient.getHeaderBufferSize(),_httpClient.getRequestBufferSize());
        connection.setDestination(destination);
        destination.onNewConnection(connection);
        _httpClient.getThreadPool().dispatch(new Runnable()
        {
            public void run()
            {
                try
                {
                    connection.handle();
                }
                catch (IOException e)
                {
                    if (e instanceof InterruptedIOException)
                        Log.ignore(e);
                    else
                    {
                        Log.warn(e);
                        destination.onException(e);
                    }
                }
            }
        });
             
    }
}
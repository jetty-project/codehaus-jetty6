package org.mortbay.jetty.client.network;

import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.client.HttpEventListener;

public class NetworkResolver implements  HttpEventListener
{

    public void onConnectionFailed( Throwable ex )
    {
        // TODO Auto-generated method stub
        
    }

    public void onException( Throwable ex )
    {
        // TODO Auto-generated method stub
        
    }

    public void onExpire()
    {
        // TODO Auto-generated method stub
        
    }

    public void onRequestCommitted()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onRequestComplete()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseComplete()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseContent( Buffer content )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseHeader( Buffer name, Buffer value )
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseHeaderComplete()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseStatus( Buffer version, int status, Buffer reason )
        throws IOException
    {
        /* TODO deal with redirects?
        else if ( HttpServletResponse.SC_MOVED_PERMANENTLY == status )
        {
            _continueConversation = true;
            _requiresRedirect = true;
        }
        */
    }


    public void attemptResolution( HttpExchange exchange ) throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public boolean canResolve()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean requiresResolution()
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void onRetry()
    {
        // TODO Auto-generated method stub
        
    }

}

package org.mortbay.jetty.client.webdav;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.log.Log;


public class PropfindExchange extends HttpExchange
{
    boolean _propertyExists = false;
    private boolean _isComplete = false;

    /* ------------------------------------------------------------ */
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        if ( status == HttpServletResponse.SC_OK )
        {
            Log.debug( "PropfindExchange:Status: Exists" );
            _propertyExists = true;
        }
        else
        {
            Log.debug( "PropfindExchange:Status: Not Exists" );
        }

        super.onResponseStatus(version, status, reason);
    }

    public boolean exists()
    {
        return _propertyExists;
    }

    public void waitTilCompletion() throws InterruptedException
    {
        synchronized (this)
        {
            while ( !_isComplete)
            {
                this.wait();
            }
        }
    }

    protected void onResponseComplete() throws IOException
    {
        _isComplete = true;

        super.onResponseComplete();
    }

}
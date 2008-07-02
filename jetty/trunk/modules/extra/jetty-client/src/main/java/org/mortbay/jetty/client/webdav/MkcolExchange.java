package org.mortbay.jetty.client.webdav;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.CachedExchange;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


public class MkcolExchange extends CachedExchange
{
    boolean createdSuccessfully = false;
    private boolean _isComplete = false;

    public MkcolExchange()
    {
        super(true);
    }

    /* ------------------------------------------------------------ */
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        if ( status == HttpServletResponse.SC_CREATED )
        {
            System.err.println( "MkcolExchange:Status: Successfully created resource" );
            createdSuccessfully = true;
        }

        super.onResponseStatus(version, status, reason);
    }

    public boolean created()
    {
        return createdSuccessfully;
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
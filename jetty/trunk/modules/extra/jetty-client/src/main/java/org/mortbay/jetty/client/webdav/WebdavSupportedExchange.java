package org.mortbay.jetty.client.webdav;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpExchange;

import java.io.IOException;


public class WebdavSupportedExchange extends HttpExchange
{
    private boolean _webdavSupported = false;
    private boolean _isComplete = false;

    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        System.err.println("WebdavSupportedExchange:Header:" + name.toString() + " / " + value.toString() );
        if ( "DAV".equals( name.toString() ) )
        {
            if ( value.toString().indexOf( "1" ) >= 0 || value.toString().indexOf( "2" ) >= 0 )
            {
                _webdavSupported = true;
            }
        }

        super.onResponseHeader(name, value);
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

    public boolean isWebdavSupported()
    {
        return _webdavSupported;
    }
}

package org.mortbay.jetty.client.webdav;

import org.mortbay.jetty.client.*;
import org.mortbay.jetty.client.security.SecurityListener;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.io.Buffer;
import org.mortbay.log.Log;
import org.mortbay.util.URIUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 */
public class WebdavListener extends HttpEventListenerWrapper
{
    private HttpDestination _destination;
    private HttpExchange _exchange;

    private boolean _webdavEnabled;

    public WebdavListener(HttpDestination destination, HttpExchange ex)
    {
        // Start of sending events through to the wrapped listener
        // Next decision point is the onResponseStatus
        super(ex.getEventListener(),true);
        _destination=destination;
        _exchange=ex;

        // We'll only enable webdav if this is a PUT request
        if ( HttpMethods.PUT.equalsIgnoreCase( _exchange.getMethod() ) )
        {
            if ( _destination.getHttpClient().isWebdavEnabled() )
            {
                _webdavEnabled = true;
            }
        }
    }

    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        System.err.println("WebdavListener:Response Status: " + status );

        // The dav spec says that CONFLICT should be returned when the parent collection doesn't exist but I am seeing
        // FORBIDDEN returned instead so running with that.
        if ( status == HttpServletResponse.SC_CONFLICT || status == HttpServletResponse.SC_FORBIDDEN )
        {
            if ( _webdavEnabled )
            {
                System.err.println("WebdavListener:Response Status: dav enabled, taking a stab at resolving put issue" );

                setDelegating( false ); // stop delegating, we can try and fix this request
            }
            else
            {
                System.err.println("WebdavListener:Response Status: Webdav Disabled" );
                setDelegating( true ); // just make sure we delegate
            }
        }
        else
        {
            setDelegating( true );
        }

        super.onResponseStatus(version, status, reason);
    }

    public void onResponseComplete() throws IOException
    {
        if ( isDelegating() )
        {
            super.onResponseComplete();
        }
        else
        {
            try
            {
                // we have some work to do before retrying this
                if ( resolveCollectionIssues() )
                {
                    setDelegating( true );
                    _destination.resend(_exchange);
                }
                else
                {
                    // admit defeat but retry because someone else might have 
                    setDelegating( true );
                    super.onResponseComplete();
                }
            }
            catch ( IOException ioe )
            {
                System.err.println("WebdavListener:Complete:IOException: might not be dealing with dav server, delegate");
                super.onResponseComplete();
            }
        }
    }


    /**
     * walk through the steps to try and resolve missing parent collection issues via webdav
     *
     * @return
     * @throws IOException
     */
    private boolean resolveCollectionIssues() throws IOException
    {

        String uri = _exchange.getURI();
        String[] uriCollection = _exchange.getURI().split("/");
        int checkNum = uriCollection.length;
        int rewind = 0;

        String parentUri = URIUtil.parentPath( uri );
        while ( !checkExists( parentUri ) )
        {
            ++rewind;
            parentUri = URIUtil.parentPath( parentUri );
        }

        // confirm webdav is supported for this collection
        if ( checkWebdavSupported() )
        {
            for (int i = 0; i < rewind;)
            {
                makeCollection(parentUri + "/" + uriCollection[checkNum - rewind - 1]);
                parentUri = parentUri + "/" + uriCollection[checkNum - rewind - 1];
                --rewind;
            }
        }
        else
        {
            return false;
        }

        return true;
    }

    private boolean checkExists( String uri ) throws IOException
    {
        PropfindExchange propfindExchange = new PropfindExchange();
        propfindExchange.setAddress( _exchange.getAddress() );
        propfindExchange.setMethod( HttpMethods.GET ); // PROPFIND acts wonky, just use get
        propfindExchange.setScheme( _exchange.getScheme() );
        propfindExchange.setEventListener( new SecurityListener( _destination, propfindExchange ) );
        propfindExchange.setConfigureListeners( false );
        propfindExchange.setURI( uri );

        _destination.send( propfindExchange );

        try
        {
            propfindExchange.waitTilCompletion();

            return propfindExchange.exists();
        }
        catch ( InterruptedException ie )
        {
            Log.ignore( ie );                  
            return false;
        }
    }

    private boolean makeCollection( String uri ) throws IOException
    {
        MkcolExchange mkcolExchange = new MkcolExchange();
        mkcolExchange.setAddress( _exchange.getAddress() );
        mkcolExchange.setMethod( "MKCOL " + uri + " HTTP/1.1" );
        mkcolExchange.setScheme( _exchange.getScheme() );
        mkcolExchange.setEventListener( new SecurityListener( _destination, mkcolExchange ) );
        mkcolExchange.setConfigureListeners( false );
        mkcolExchange.setURI( uri );

        _destination.send( mkcolExchange );

        try
        {
            mkcolExchange.waitTilCompletion();

            return mkcolExchange.exists();
        }
        catch ( InterruptedException ie )
        {
            Log.ignore( ie );
            return false;
        }
    }

    
    private boolean checkWebdavSupported() throws IOException
    {
        WebdavSupportedExchange supportedExchange = new WebdavSupportedExchange();
        supportedExchange.setAddress( _exchange.getAddress() );
        supportedExchange.setMethod( HttpMethods.OPTIONS );
        supportedExchange.setScheme( _exchange.getScheme() );
        supportedExchange.setEventListener( new SecurityListener( _destination, supportedExchange ) );
        supportedExchange.setConfigureListeners( false );
        supportedExchange.setURI( _exchange.getURI() );

        _destination.send( supportedExchange );

        try
        {
            supportedExchange.waitTilCompletion();
            return supportedExchange.isWebdavSupported();
        }
        catch (InterruptedException ie )
        {            
            Log.ignore( ie );
            return false;
        }

    }

}

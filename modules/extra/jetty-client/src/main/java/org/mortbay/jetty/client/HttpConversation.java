package org.mortbay.jetty.client;

//========================================================================
//Copyright 2006-2008 Mort Bay Consulting Pty. Ltd.
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


import org.mortbay.jetty.client.network.NetworkResolver;
import org.mortbay.jetty.client.protocol.ProtocolResolver;
import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.client.security.Authentication;
import org.mortbay.jetty.client.security.SecurityResolver;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.util.StringUtil;
import org.mortbay.io.Buffer;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.net.InetSocketAddress;
import java.io.IOException;

/**
 * Provides a wrapper around an HttpExchange object that implements authentication
 * and allows for negotiation for the on top of the given Exchange.
 * 
 * Multiple authentication mechanisms can be registered with the conversation as well 
 * as multiple security realms where the credentials exist.  This allows for the conversation
 * to try multiple authentication options transparently to the calling code.
 * 
 * Callbacks are provided for success and failure notification since usage of this class is 
 * asynchronous. 
 * 
 * @author Jesse McConnell <jesse@codehaus.org>
 *
 */
public class HttpConversation implements HttpExchangeListener
{

    private HttpExchange _exchange;
    private HttpDestination _destination;
    private SecurityResolver _securityResolver = new SecurityResolver();
    private NetworkResolver _networkResolver = new NetworkResolver();
    private ProtocolResolver _protocolResolver = new ProtocolResolver();
          
    private int _attempts = 0;
    private boolean _initialized = false;
    
    public HttpConversation( )
    {       
    }
    
    public HttpConversation( HttpExchange exchange )
    {
        _exchange = exchange;
    }
    
    public HttpConversation( HttpDestination destination )
    {
        _destination = destination;
    }
    
    public HttpConversation( HttpDestination destination, HttpExchange exchange )
    {
        _destination = destination;
        _exchange = exchange;
    } 

    public void setHttpExchange( HttpExchange exchange )
    {
        _exchange = exchange;
    }
    
    private void initialize() throws IOException
    {
        if ( _destination == null )
        {
            throw new IOException( "unable to start conversation, no destination set." );
        }
        
        if ( _exchange == null )
        {
            throw new IOException( "unable to start conversation, no exchange set." );
        }
        
        // order is important on these listeners, this must come last
        _exchange.setListeners( new HttpExchangeListener[]{ _networkResolver, _securityResolver, _protocolResolver, this } );
        _initialized = true;
    }
    
    /*-----------*/
    /* Callbacks */

    /** 
     * Callback method indicating the HttpConversation completed successfully. 
     */
    public void success()
    {

    }

    /**
     * Callback method indicating the HttpConversation failed for the specified reason.
     */
    public void failure( String reason )
    {

    }

    /**
     * Callback method indicating the HttpConversation failed for the specified reason and the given exception.
     * @param t
     */
    public void failure( String reason, Throwable t )
    {

    }
    
    /*-----------*/

    /**
     * Wait for the return status the exchange and if we need to continue the conversation, we 
     * have gotten here in a way where we can't continue the conversation so we failed. 
     */
    public boolean waitForSuccess() throws InterruptedException
    {
        _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );

        while ( continueConversation() )
        {
            _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );
        }

        if ( _networkResolver.requiresResolution() || _securityResolver.requiresResolution() || _protocolResolver.requiresResolution() )
        {
            return false; // we require resolution but are in a state where we can't continue to false
        }
        else
        {
            return true; // nothing needs resolution, exchange is completed, we are gtg
        }
    }
    
    /**
     * 
     */
    public boolean continueConversation()
    {
        if ( _networkResolver.requiresResolution() )
        {
            if ( _networkResolver.canResolve() )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        if ( _securityResolver.requiresResolution() )
        {
            if ( _securityResolver.canResolve() )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        if ( _protocolResolver.requiresResolution() )
        {
            if ( _protocolResolver.canResolve() )
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }

    /**
     * the scheme we will talk with
     * 
     * @return
     */
    public Buffer getScheme()
    {
        return _exchange.getScheme();
    }

    /**
     * the address we are going to talk to
     * @return
     */
    public InetSocketAddress getAddress()
    {
        return _exchange.getAddress();
    }

    /**
     * We need a destination (someone to talk to) in order to start
     * 
     * @param destination
     */
    public void setHttpDestination( HttpDestination destination )
    {
        _destination = destination;
    }


    /**
     * Starts the conversation off.
     * 
     * @throws IOException
     */
    public void start() throws IOException
    {
        synchronized (this)
        {
 
            if ( !_initialized )
            {
                initialize();
            }
            
            _exchange.setStatus(HttpExchange.STATUS_WAITING_FOR_CONNECTION);
            _destination.send(_exchange);
            ++_attempts;           
        }
    }

    public void onException(Throwable ex)
    {
        failure( ex.getMessage(), ex );
    }

    public void onExpire()
    {
        failure( "Conversation Expired" );
    }

    public void onRequestCommitted() throws IOException
    {

    }

    public void onRequestComplete() throws IOException
    {

    }

    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        
    }

    public void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        
    }

    public void onResponseHeaderComplete() throws IOException
    {

    }

    public void onResponseContent(Buffer content) throws IOException
    {

    }

    public void onResponseComplete() throws IOException
    {
        if ( _networkResolver.requiresResolution() )
        {
            if ( _networkResolver.canResolve() )
            {
                _networkResolver.attemptResolution( _exchange );
                ++_attempts;
                _destination.send( _exchange );                
                return;
            }
            else
            {
                failure( "Network Resolution Phase" );
            }
        }

        if ( _securityResolver.requiresResolution() )
        {
            if ( _securityResolver.canResolve() )
            {
                _securityResolver.attemptResolution( _exchange );
                ++_attempts;
                _destination.send( _exchange );
                return;
            }
            else
            {
                failure( "Security Resolution Phase" );
            }
        }

        if ( _protocolResolver.requiresResolution() && _protocolResolver.canResolve() )
        {
            if ( _protocolResolver.canResolve() )
            {
                _protocolResolver.attemptResolution( _exchange );
                ++_attempts;
                _destination.send( _exchange );
                return;
            }
            else
            {
                failure( "Protocol Resolution Phase" );
            }
        }

        success();
    }

    public void onConnectionFailed(Throwable ex)
    {
        failure( ex.getMessage(), ex ); 
    }

    public void enableSecurityRealm( SecurityRealm realm )
    {
        _securityResolver.addSecurityRealm( realm );
    }
    
    public void enableAuthentication( Authentication authentication )
    {
        _securityResolver.addAuthentication( authentication );
    }
}

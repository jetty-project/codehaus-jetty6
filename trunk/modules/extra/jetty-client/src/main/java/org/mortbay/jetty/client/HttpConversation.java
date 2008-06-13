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


import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.client.security.Authentication;
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
    private List<SecurityRealm> _realmList;
    private Map<String,Authentication> _authenticationMap;
    private String _url;
    private String _authenticationType = null;
    private Map<String, String> _authenticationDetails;
    private boolean _continueConversation = false;
    
    // 301
    //private boolean _requiresRedirect = false;
    
    // 401 
    private boolean _requiresAuthenticationChallenge = false;
    
    
    private int _attempts = 0;


    /*
     * TODO figure out how the exchange will get populated into the wrapper, maybe we need an exchange factory based
     * on classname with the ability to register new exchange types
     *
     */
    public HttpConversation( HttpExchange exchange )
    {
        _exchange = exchange;
        _exchange.setListener( this );

    }   

    /*-----------*/
    /* Callbacks */ // first pass

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

    public boolean waitForSuccess() throws InterruptedException
    {
        _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );

        while ( _continueConversation && anotherAttemptPossible() )
        {
            _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );
        }

        if ( _continueConversation )
        {
            return false;
        }
        else
        {
            return true;
        }
    }

    public Buffer getScheme()
    {
        return _exchange.getScheme();
    }

    public InetSocketAddress getAddress()
    {
        return _exchange.getAddress();
    }

    public void setHttpDestination( HttpDestination destination )
    {
        _destination = destination;
    }


    public void send() throws IOException
    {
        synchronized (this)
        {
            _exchange.setStatus(HttpExchange.STATUS_WAITING_FOR_CONNECTION);

            System.out.println("sending initial attempt");
            _destination.send(_exchange);
            ++_attempts;
        }

    }

    private void prepareNextAttempt() throws IOException
    {
        // TODO need to clean out exchange?

        if ( _requiresAuthenticationChallenge )
        {
            if ( _authenticationType == null )
            {
                throw new IOException( "Another Attempt Required::Authentication::missing type" );
            }
            if ( _authenticationMap.containsKey( _authenticationType.toLowerCase() ) )
            {
                Authentication auth = _authenticationMap.get( _authenticationType.toLowerCase() );

                if ( _realmList.size() >= _attempts )  // iterate through the authentication possibilities in order
                {
                    auth.setCredentials( _exchange, _realmList.get( _attempts - 1 ), _authenticationDetails );
                }
                else
                {
                    failure( "Another Attempt Required::Authentication::invalid state::exhausted authentication" );
                }
            }
            else
            {
                throw new IOException( "Another Attempt Required::Authentication::requires unknown authentication mechanism::" + _authenticationType );
            }
        }
    }

    private boolean anotherAttemptPossible() 
    {
		if (_requiresAuthenticationChallenge) 
		{
			if (_realmList.size() >= _attempts) 
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
        System.out.println("Response Status: " + status );
        if ( HttpServletResponse.SC_UNAUTHORIZED == status )
        {
            _continueConversation = true;
            _requiresAuthenticationChallenge = true;
        }
        /* TODO deal with redirects?
        else if ( HttpServletResponse.SC_MOVED_PERMANENTLY == status )
        {
        	_continueConversation = true;
        	_requiresRedirect = true;
        }
        */
        
    }

    public void onResponseHeader(Buffer name, Buffer value) throws IOException
    {
        System.out.println("Header Seen: " + name.toString() + "/" + value.toString() );

        int header = HttpHeaders.CACHE.getOrdinal(name);
        switch (header)
        {
            case HttpHeaders.WWW_AUTHENTICATE_ORDINAL:

                if ( value.toString().indexOf(" ") == -1 )
                {
                    _authenticationType = value.toString();
                }
                else
                {
                
                    String authResponse = value.toString();
                    
                    _authenticationType = authResponse.substring( 0, authResponse.indexOf( " " ) ); 
                    authResponse = authResponse.substring( authResponse.indexOf( " " ) + 1, authResponse.length() );

                    while ( authResponse.indexOf( "=" ) != -1 )
                    {
                        String itemName = authResponse.substring( 0, authResponse.indexOf( "=" ) );
                        authResponse = authResponse.substring( itemName.length() + 2, authResponse.length() ); // wack the ="                        
                        
                        String itemValue = authResponse.substring( 0, authResponse.indexOf( "\"" ) );
                        authResponse = authResponse.substring( itemValue.length() + 1, authResponse.length() );
                                                                                       
                        if (_authenticationDetails == null)
                        {
                            _authenticationDetails = new HashMap<String,String>();
                            _authenticationDetails.put(itemName, itemValue);
                        }
                        else
                        {
                            _authenticationDetails.put(itemName, itemValue);
                        }
                    }
                }
                break;
        }
    }

    public void onResponseHeaderComplete() throws IOException
    {

    }

    public void onResponseContent(Buffer content) throws IOException
    {

    }

    public void onResponseComplete() throws IOException
    {
        if ( _continueConversation && anotherAttemptPossible() ) // If state says we can continue, do so
        {
            synchronized ( this )
            {
                prepareNextAttempt();
                _continueConversation = false;
                ++_attempts;                
                _destination.send(_exchange);
                return;
            }
        }
        else if ( _continueConversation ) // we need another attempt, but can't to fail
        {
            failure( "unable to continue conversation" );
        }
        else // we must be successful
        {
            success();
        }
    }

    public void onConnectionFailed(Throwable ex)
    {
        failure( ex.getMessage(), ex ); 
    }

    public String getUrl()
    {
        return _url;
    }

    public void setUrl(String url)
    {
        this._url = url;
    }

    public List getRealmList()
    {
        return _realmList;
    }

    public void addSecurityRealm(SecurityRealm realm)
    {
        if ( _realmList == null )
        {
            _realmList = new LinkedList();
        }
        _realmList.add(_realmList.size(), realm );
    }

    public Map getAuthenticationMap()
    {
        return _authenticationMap;
    }

    public void addAuthentication(Authentication authentication)
    {
        if ( _authenticationMap == null )
        {
            _authenticationMap = new HashMap();
        }
        _authenticationMap.put( authentication.getAuthType().toLowerCase(), authentication );
    }
}

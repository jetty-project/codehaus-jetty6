package org.mortbay.jetty.client;

import org.mortbay.jetty.client.security.SecurityRealm;
import org.mortbay.jetty.client.security.Authentication;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.io.Buffer;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.net.InetSocketAddress;
import java.io.IOException;


public class HttpExchangeWrapper implements ExchangeListener
{

    private HttpExchange _exchange; // should we clone _exchange when we detect we need to retry?

    private HttpDestination _destination;
    private List<SecurityRealm> _realmList;
    private Map<String,Authentication> _authenticationMap;
    private String _url;
    private String _authenticationType = null;
    private Map _authenticationDetails;
    private boolean _requiresAnotherAttempt = false;
    private boolean _requiresAuthenticationChallenge = false;
    private int _attempts = 0;


    /*
     * TODO figure out how the exchange will get populated into the wrapper, maybe we need an exchange factory based
     * on classname with the ability to register new exchange types
     *
     */
    public HttpExchangeWrapper( HttpExchange exchange )
    {
        _exchange = exchange;
        _exchange.setListener( this );

    }   

    /*-----------*/
    /* Callbacks */ // first pass

    public void success()
    {

    }

    public void failure()
    {

    }

    public void failure( Throwable t )
    {

    }

    /*-----------*/

    public boolean waitForSuccess() throws InterruptedException
    {
        _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );

        while ( _requiresAnotherAttempt && anotherAttemptPossible() )
        {
            _exchange.waitForStatus( HttpExchange.STATUS_COMPLETED );
        }

        if ( _requiresAnotherAttempt )
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
                    failure( new IOException("Another Attempt Required::Authentication::invalid state::exhausted authentication") );
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
        if ( _realmList.size() >= _attempts )
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void onException(Throwable ex)
    {
        failure( ex );
    }

    public void onExpire()
    {
        failure();
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
        if ( status == HttpServletResponse.SC_UNAUTHORIZED )
        {
            _requiresAnotherAttempt = true;
            _requiresAuthenticationChallenge = true;
        }
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
                    StringTokenizer strtok = new StringTokenizer(value.toString(), " ");
                    _authenticationType = strtok.nextToken().toLowerCase();

                    while (strtok.hasMoreTokens())
                    {
                        String hashItem = strtok.nextToken();
                        String itemName = hashItem.substring(0, hashItem.indexOf("="));
                        String itemValue = hashItem.substring(hashItem.indexOf("=") + 1, hashItem.length());

                        if (itemValue.startsWith("\""))
                        {
                            itemValue = itemValue.substring(1, itemValue.length());
                        }

                        if (itemValue.endsWith("\""))
                        {
                            itemValue = itemValue.substring(0, itemValue.length() - 1);
                        }

                        if (_authenticationDetails == null)
                        {
                            _authenticationDetails = new HashMap();
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
        System.out.println("response completed " + _attempts );
        if ( _requiresAnotherAttempt && anotherAttemptPossible() ) // If state says we can continue, do so
        {
            System.out.println("requires another attempt and making one");
            synchronized ( this )
            {
                prepareNextAttempt();
                _requiresAnotherAttempt = false;
                ++_attempts;                
                _destination.send(_exchange);
                return;
            }
        }
        else if ( _requiresAnotherAttempt ) // we need another attempt, but can't to fail
        {
            failure( new IOException("attempt " + _attempts));
        }
        else // we must be successful
        {
            success();
        }
    }

    public void onConnectionFailed(Throwable ex)
    {
        failure( ex ); 
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
        _realmList.add( realm );
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

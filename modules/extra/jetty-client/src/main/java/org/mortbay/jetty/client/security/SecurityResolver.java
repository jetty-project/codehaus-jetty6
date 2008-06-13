package org.mortbay.jetty.client.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.client.HttpExchangeListener;
import org.mortbay.jetty.client.HttpResolver;
import org.mortbay.util.StringUtil;

public class SecurityResolver implements HttpResolver, HttpExchangeListener
{
    private String _authenticationType = null;
    private Map<String, String> _authenticationDetails;
    private Map<String,Authentication> _authenticationMap;
    private List<SecurityRealm> _realmList;
    
    private boolean _requiresResolution = false;
    
    private int _attempts = 0; // TODO remember to settle on winning solution
    
    public void attemptResolution( HttpExchange exchange ) throws IOException
    {
        if ( _authenticationType == null )
        {
            throw new IOException( "Another Attempt Required::Authentication::missing type" );
        }
        if ( _authenticationMap.containsKey( _authenticationType.toLowerCase() ) )
        {
            Authentication auth = _authenticationMap.get( _authenticationType.toLowerCase() );

            if ( _realmList.size() > _attempts )  // iterate through the authentication possibilities in order
            {
                auth.setCredentials( exchange, _realmList.get( _attempts ), _authenticationDetails );
                ++_attempts;
            }
            else
            {
                throw new IOException( "Another Attempt Required::Authentication::invalid state::exhausted authentication" );
            }
        }
        else
        {
            throw new IOException( "Another Attempt Required::Authentication::requires unknown authentication mechanism::" + _authenticationType );
        }
    }

    public boolean canResolve()
    {
        
            if (_realmList.size() > _attempts) 
            {
                return true; // we have more securityRealms that we can attempt to authenticate against
            } 
            else 
            {
                return false;
            }
        
      
    }

    public boolean requiresResolution()
    {
        return _requiresResolution;
    }

    public List getRealmList()
    {
        return _realmList;
    }

    public void addSecurityRealm(SecurityRealm realm)
    {
        if ( _realmList == null )
        {
            _realmList = new LinkedList<SecurityRealm>();
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
            _authenticationMap = new HashMap<String,Authentication>();
        }
        _authenticationMap.put( authentication.getAuthType().toLowerCase(), authentication );
    }
    
    /**
     * scrapes an authentication type from the authString
     * 
     * @param authString
     * @return
     */
    protected String scrapeAuthenticationType( String authString )
    {
        String authType;

        if ( authString.indexOf( " " ) == -1 )
        {
            authType = authString.toString().trim();
        }
        else
        {
            String authResponse = authString.toString();
            authType = authResponse.substring( 0, authResponse.indexOf( " " ) ).trim();
        }
        return authType;
    }
    
    /**
     * scrapes a set of authentication details from the authString
     * 
     * @param authString
     * @return
     */
    protected Map<String, String> scrapeAuthenticationDetails( String authString )
    {
        Map<String, String> authenticationDetails = new HashMap<String, String>();
 
        authString = authString.substring( authString.indexOf( " " ) + 1, authString.length() );
        
        StringTokenizer strtok = new StringTokenizer( authString, ",");
        
        while ( strtok.hasMoreTokens() )
        {
            String[] pair = strtok.nextToken().split( "=" );
            if ( pair.length == 2 )
            {
                String itemName = pair[0].trim();
                String itemValue = pair[1].trim();
                
                itemValue = StringUtil.unquote( itemValue );
                
                authenticationDetails.put( itemName, itemValue );
            }
            else
            {
                throw new IllegalArgumentException( "unable to process authentication details" );
            }      
        }
        return authenticationDetails;
    }

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
        System.out.println( "SecurityResolver:Header: " + name.toString() + " / " + value.toString() );
        int header = HttpHeaders.CACHE.getOrdinal(name);
        switch (header)
        {
            case HttpHeaders.WWW_AUTHENTICATE_ORDINAL:
                
                String authString = value.toString();
                _authenticationType = scrapeAuthenticationType( authString );
                if ( value.toString().indexOf( "=" ) != -1 ) // there are details to scrape
                {                    
                    _authenticationDetails = scrapeAuthenticationDetails( authString );
                }
                
                break;
        }
    }

    public void onResponseHeaderComplete()
        throws IOException
    {
        // TODO Auto-generated method stub
        
    }

    public void onResponseStatus( Buffer version, int status, Buffer reason )
        throws IOException
    {
        System.out.println("SecurityResolver:Response Status: " + status );
        if ( HttpServletResponse.SC_UNAUTHORIZED == status ) // we need to resolve
        {
            _requiresResolution = true;
        }
        else // we don't need to resolve, and if we had to last time, make sure its false
        {
            _requiresResolution = false;
        }
    }  
    
    
}

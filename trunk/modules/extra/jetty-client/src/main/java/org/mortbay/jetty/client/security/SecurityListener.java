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
import org.mortbay.jetty.client.HttpDestination;
import org.mortbay.jetty.client.HttpEventListenerWrapper;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.client.HttpEventListener;
import org.mortbay.util.StringUtil;

public class SecurityListener extends HttpEventListenerWrapper
{
    private HttpDestination _destination;
    private HttpExchange _exchange;
    
    private int _attempts = 0; // TODO remember to settle on winning solution

    public SecurityListener(HttpDestination destination, HttpExchange ex)
    {
        // Start of sending events through to the wrapped listener
        // Next decision point is the onResponseStatus
        super(ex.getEventListener(),true);
        _destination=destination;
        _exchange=ex;
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

    @Override
    public void onResponseStatus( Buffer version, int status, Buffer reason )
        throws IOException
    {
        System.out.println("SecurityResolver:Response Status: " + status );
        if ( status == HttpServletResponse.SC_UNAUTHORIZED && _attempts<_destination.getHttpClient().maxRetries()) 
        {
            // Let's absorb events until we have done some retries
            setDelegating(false);
        }
        else 
        {
            setDelegating(true);
        }
        super.onResponseStatus(version,status,reason);
    }


    @Override
    public void onResponseHeader( Buffer name, Buffer value )
        throws IOException
    {
        System.out.println( "SecurityResolver:Header: " + name.toString() + " / " + value.toString() );
        if (!isDelegating())
        {
            int header = HttpHeaders.CACHE.getOrdinal(name);
            switch (header)
            {
                case HttpHeaders.WWW_AUTHENTICATE_ORDINAL:

                    // TODO don't hard code this bit.
                    String authString = value.toString();
                    String type = scrapeAuthenticationType( authString );
                    

                    // TODO maybe avoid this map creation
                    Map<String,String> details = scrapeAuthenticationDetails( authString );
                    String pathSpec="/"; // TODO work out the real path spec
                    SecurityRealm realm = _destination.getHttpClient().getRealm( details.get("realm") ); // TODO work our realm correctly 
                    
                    if ("digest".equalsIgnoreCase(type))
                    {
                        _destination.addAuthorization("/",new DigestAuthentication(realm,details));
                        
                    }
                    else if ("basic".equalsIgnoreCase(type))
                    {
                        _destination.addAuthorization(pathSpec,new BasicAuthentication(realm));
                    }
                    
                    break;
            }
        }
        super.onResponseHeader(name,value);
    }
    
    
    
    
    @Override
    public void onResponseComplete() throws IOException
    {
        if (!isDelegating())
        {
            // TODO
            
            
            _destination.resend(_exchange);
        }
        super.onResponseComplete();
    }

    @Override
    public void onRetry()
    {
        _attempts++;
        setDelegating(true);
        super.onRetry();
    }  
    
    
}

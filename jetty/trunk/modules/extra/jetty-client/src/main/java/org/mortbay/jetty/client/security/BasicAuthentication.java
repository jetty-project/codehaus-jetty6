package org.mortbay.jetty.client.security;

import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.util.StringUtil;

import java.io.IOException;
import java.util.Map;


public class BasicAuthentication implements Authentication
{
    public String getAuthType()
    {
        return "basic";
    }

    public void setCredentials( HttpExchange exchange, SecurityRealm realm, Map details ) throws IOException
    {
        String authenticationString = getAuthType() + " " + B64Code.encode( realm.getPrincipal() + ":" + realm.getCredentials(), StringUtil.__ISO_8859_1);

        System.out.println("Auth test - " + authenticationString + " " + realm.getPrincipal() + ":" + realm.getCredentials());

        //  Set a header with a value of 'basic foo' where foo is username:password encoded
        exchange.setRequestHeader( HttpHeaders.WWW_AUTHENTICATE, authenticationString);
    }
}

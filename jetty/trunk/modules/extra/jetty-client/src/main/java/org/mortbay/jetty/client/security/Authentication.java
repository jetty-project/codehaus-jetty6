package org.mortbay.jetty.client.security;

import org.mortbay.jetty.client.HttpExchange;

import java.io.IOException;
import java.util.Map;


public interface Authentication
{
    public String getAuthType();

    public void setCredentials( HttpExchange exchange, SecurityRealm realm, Map details ) throws IOException;

}

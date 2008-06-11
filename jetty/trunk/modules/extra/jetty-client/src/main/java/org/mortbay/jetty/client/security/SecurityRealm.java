package org.mortbay.jetty.client.security;


public interface SecurityRealm
{
    public String getName();

    public String getPrincipal();

    public String getCredentials();

}

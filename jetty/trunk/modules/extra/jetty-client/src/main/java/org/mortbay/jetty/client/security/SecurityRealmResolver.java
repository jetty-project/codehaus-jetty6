package org.mortbay.jetty.client.security;

import java.io.IOException;

import org.mortbay.jetty.client.HttpDestination;

public interface SecurityRealmResolver
{

    public void addSecurityRealm( SecurityRealm realm );
     
    public SecurityRealm getRealm( String realmName, HttpDestination destination, String path ) throws IOException;
    
}

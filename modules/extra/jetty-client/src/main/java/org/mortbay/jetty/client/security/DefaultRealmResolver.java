package org.mortbay.jetty.client.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.client.HttpDestination;

public class DefaultRealmResolver implements SecurityRealmResolver
{
    private Map<String, SecurityRealm>_realmMap;  
    
    public void addSecurityRealm( SecurityRealm realm )
    {
        if (_realmMap == null)
        {
            _realmMap = new HashMap<String, SecurityRealm>();
        }
        _realmMap.put( realm.getId(), realm );
    }
    
    public SecurityRealm getRealm( String realmName, HttpDestination destination, String path ) throws IOException
    {
        return _realmMap.get( realmName );
    }

}

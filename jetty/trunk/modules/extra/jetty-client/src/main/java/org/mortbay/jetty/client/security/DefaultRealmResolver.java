package org.mortbay.jetty.client.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.client.HttpDestination;

public class DefaultRealmResolver implements SecurityRealmResolver
{
    private Map<String, SecurityRealm>_realmList;  
    
    public void addSecurityRealm( SecurityRealm realm )
    {
        if (_realmList == null)
        {
            _realmList = new HashMap<String, SecurityRealm>();
        }
        _realmList.put( realm.getId(), realm );
    }
    
    public SecurityRealm getRealm( String realmName, HttpDestination destination, String path ) throws IOException
    {
        return _realmList.get( realmName );
    }

}

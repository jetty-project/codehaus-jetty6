package org.mortbay.jetty.security;

//========================================================================
//Copyright 1998-2010 Mort Bay Consulting Pty. Ltd.
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

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.ietf.jgss.GSSContext;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.ietf.jgss.GSSManager;
import org.ietf.jgss.GSSName;
import org.ietf.jgss.Oid;
import org.mortbay.jetty.Request;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;

/*
 * Implementation of a jetty user realm for spnego support. 
 *	
 * See the README.spengo file in the etc directory for information on configuring spnego
 *
 * Based on some of the work for supporting spnego in Apache Geronimo by Ashish Jain
 */
@SuppressWarnings("restriction")
public class SpnegoUserRealm implements UserRealm
{
    private String _realmName;
    private String _config;
    private Resource _configResource;
    private String _targetName;

   // private Base64 base64 = new Base64();

    // private BASE64Decoder base64Decoder = new BASE64Decoder();
    // private BASE64Encoder base64Encoder = new BASE64Encoder();

    public SpnegoUserRealm()
    {
        super();
    }

    public SpnegoUserRealm(String name, String config) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        super();

        setName(name);
        setConfig(config);

    }

    public Resource getConfigResource()
    {
        return _configResource;
    }

    /* ------------------------------------------------------------ */
    /**
     * Load realm users from properties file. The property file maps usernames to password specs followed by an optional comma separated list of role names.
     * 
     * @param config
     *            Filename or url of user properties file.
     * @exception IOException
     */
    public void setConfig(String config) throws IOException
    {
        _config = config;
        _configResource = Resource.newResource(_config);
        loadConfig();

    }

    public void setName(String name)
    {
        _realmName = name;
    }

    protected void loadConfig() throws IOException
    {
        Properties properties = new Properties();

        properties.load(getConfigResource().getInputStream());

        _targetName = properties.getProperty("targetName");

        dumpConfig(); // display configuration if debug enabled
    }

    public Principal authenticate(String username, Object credentials, Request request)
    {
        try
        {
            byte[] token = B64Code.decode(username);

            
            GSSManager manager = GSSManager.getInstance();

            Oid krb5Oid = new Oid("1.3.6.1.5.5.2"); // http://java.sun.com/javase/6/docs/technotes/guides/security/jgss/jgss-features.html
            GSSName gssName = manager.createName(_targetName,null);// GSSName.NT_USER_NAME);
            GSSCredential serverCreds = manager.createCredential(gssName,GSSCredential.INDEFINITE_LIFETIME,krb5Oid,GSSCredential.ACCEPT_ONLY);
            GSSContext gContext = manager.createContext(serverCreds);

            if (gContext == null)
            {
                Log.debug("SpnegoUserRealm: failed to establish GSSContext");
            }
            else
            {
                while (!gContext.isEstablished())
                {
                    token = gContext.acceptSecContext(token,0,token.length);
                }
                if (gContext.isEstablished())
                {
                    Log.debug("SpnegoUserRealm: established a security context");
                    Log.debug("Client Principal is: " + gContext.getSrcName());
                    Log.debug("Server Principal is: " + gContext.getTargName());

                    GSSName srcName = gContext.getSrcName();
                    String encodedToken = new String(B64Code.encode(token));

                    SpnegoUser user = new SpnegoUser(srcName.toString(),encodedToken);

                    // set the role to the logged in domain
                    String clientPrincipal = gContext.getSrcName().toString();
                    String role = clientPrincipal.substring(clientPrincipal.indexOf('@') + 1);
                    user.addRole(role);
                    Log.debug("Client Role: " + role);

                    return user;
                }
                else
                {
                    Log.debug("SpnegoUserRealm: failed to establish a security context");
                }
            }
        }
        catch (GSSException e)
        {
        	Log.debug("error processing token for spnego authentication", e.getMessage());
        	
        	if ( Log.isDebugEnabled() )
        	{
        		e.printStackTrace();
        	}
        }
        
        return null;
    }

    public class SpnegoUser implements Principal
    {
        String _name;
        String _token;
        List<String> _roles;

        public SpnegoUser(String name, String token)
        {
            _name = name;
            _token = token;
        }

        /* ------------------------------------------------------------ */
        private UserRealm getUserRealm()
        {
            return SpnegoUserRealm.this;
        }

        public String getName()
        {
            return _name;
        }

        public boolean isAuthenticated()
        {
            return true;
        }

        public String toString()
        {
            return getName();
        }

        public String getToken()
        {
            return _token;
        }

        public void addRole(String role)
        {
            if (_roles == null)
            {
                _roles = new ArrayList(1);
            }
            _roles.add(role);
        }

        public List getRoles()
        {
            if (_roles != null)
            {
                return _roles;
            }
            else
            {
                return Collections.EMPTY_LIST;
            }
        }

    }

    @Override
    public String getName()
    {
        return _realmName;
    }

    @Override
    public Principal getPrincipal(String token)
    {
        return null;
    }

    @Override
    public boolean reauthenticate(Principal user)
    {
        return false;
    }

    @Override
    public boolean isUserInRole(Principal user, String role)
    {
        if (user instanceof SpnegoUser)
        {
            List roles = ((SpnegoUser)user).getRoles();

            for (Iterator i = roles.iterator(); i.hasNext();)
            {
                if (role.equals((String)i.next()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void disassociate(Principal user)
    {

    }

    @Override
    public Principal pushRole(Principal user, String role)
    {
        if (user instanceof SpnegoUser)
        {
            ((SpnegoUser)user).addRole(role);
        }
        return user;
    }

    @Override
    public Principal popRole(Principal user)
    {
        return null;
    }

    @Override
    public void logout(Principal user)
    {

    }

    private void dumpConfig()
    {
        Log.debug("SpnegoUserRealm");
        Log.debug(" - targetName = " + _targetName);
    }
}

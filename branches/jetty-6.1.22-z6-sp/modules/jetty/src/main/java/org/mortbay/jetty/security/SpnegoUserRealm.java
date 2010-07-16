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
import java.math.BigInteger;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import org.apache.commons.codec.binary.Base64;
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

    private boolean _populateRoleData = false;
    private String _ldapUrl;
    private String _ldapLoginName;
    private String _ldapLoginPassword;
    private String _searchBase;
    private String _ldapContextFactory; 

    private Base64 base64 = new Base64();
    //private BASE64Decoder base64Decoder = new BASE64Decoder();
    //private BASE64Encoder base64Encoder = new BASE64Encoder();

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
        
        String populate = properties.getProperty("populateRoleData");
        if ( populate != null )
        {
            _populateRoleData = populate.equalsIgnoreCase("true");
        }
        
        _ldapUrl = properties.getProperty("ldapUrl");
        _ldapLoginName = properties.getProperty("ldapLoginName");
        _ldapLoginPassword = properties.getProperty("ldapLoginPassword");
        _ldapContextFactory = properties.getProperty("ldapContextFactory");
        
        // we need a default for the ldap context factory
        if (_ldapContextFactory == null )
        {
            _ldapContextFactory = "com.sun.jndi.ldap.LdapCtxFactory";
        }
        
        dumpConfig(); // display configuration if debug enabled
    }

    public Principal authenticate(String username, Object credentials, Request request)
    {
        try
        {     
            //byte[] token = base64Decoder.decodeBuffer(username);
        	byte[] token = base64.decode(username);
        	
            GSSManager manager = GSSManager.getInstance();
            
            Oid krb5Oid = new Oid("1.3.6.1.5.5.2"); // http://java.sun.com/javase/6/docs/technotes/guides/security/jgss/jgss-features.html
            GSSName gssName = manager.createName(_targetName, null);// GSSName.NT_USER_NAME);
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
                    String encodedToken = base64.encodeToString(token);
                    
                    SpnegoUser user = new SpnegoUser(srcName.toString(),encodedToken);
                    
                    if ( _populateRoleData )
                    {
                        try
                        {
                            populateRoleData(user);
                        }
                        catch (Exception e)
                        {
                            Log.info("SpnegoUserRealm: failed to populate user role data");
                            e.printStackTrace();
                        }
                    }
                   
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
            e.printStackTrace();
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
            if ( _roles != null )
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
        if ( user instanceof SpnegoUser )
        {
            List roles = ((SpnegoUser)user).getRoles();
            
            for ( Iterator i = roles.iterator();i.hasNext();)
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
	public void disassociate(Principal user) {

	}

	@Override
	public Principal pushRole(Principal user, String role) {
		if (user instanceof SpnegoUser) {
			((SpnegoUser) user).addRole(role);
		}
		return user;
	}

	@Override
	public Principal popRole(Principal user) {
		return null;
	}

	@Override
	public void logout(Principal user) {

	}

	public void populateRoleData(Principal user) throws Exception {
		String at = "@";
		DirContext ctx = null;
		int indexOfAt = user.getName().toString().indexOf(at);
		String userName = user.getName().toString().substring(0, indexOfAt);
		SearchControls searchCtls = new SearchControls();
		String returnedAtts[] = { "primaryGroupID", "memberOf", "objectSid;binary" };
		String searchFilter = "(&(objectClass=user)(cn=" + userName + "))";
		String groupSearchFilter = null;
		int totalResults = 0;
		try {
			ctx = getConnection();
			if (ctx == null) {
				Log.info("SpnegoUserRealm: Failed to get a directory context object");
				throw new Exception(
						"SpnegoUserRealm: Failed to get a directory context object");
			}
			searchCtls.setReturningAttributes(returnedAtts);
			searchCtls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			// Search for objects using the filter
			NamingEnumeration<SearchResult> answer = ctx.search(_searchBase,
					searchFilter, searchCtls);
			// Loop through the search results
			while (answer.hasMoreElements()) {
				SearchResult sr = answer.next();
				totalResults++;
				Attributes attrs = sr.getAttributes();
				if (attrs != null) {
					try {
						byte[] userSid = (byte[]) attrs.get("objectSid;binary")
								.get();
						Integer primaryGroupId = new Integer((String) attrs
								.get("primaryGroupID").get());
						byte[] groupRid = integerToFourBytes(primaryGroupId);
						byte[] groupSid = userSid.clone();
						// Replace the last four bytes to construct
						// groupSid
						for (int i = 0; i < 4; ++i) {
							groupSid[groupSid.length - 1 - i] = groupRid[i];
						}
						groupSearchFilter = "(&(objectSid="
								+ binaryToStringSID(groupSid) + "))";
						Attribute answer1 = attrs.get("memberOf");
						for (int i = 0; i < answer1.size(); i++) {
							String str = answer1.get(i).toString();
							String str1[] = str.split("CN=");
							pushRole(user,
									str1[1].substring(0, str1[1].indexOf(",")));
						}
					} catch (NullPointerException e) {
						throw new Exception(
								"SpnegoUserRealm: Errors listing attributes: "
										+ e);
					}
				}
			}
			// Search for objects using the group search filter
			NamingEnumeration<SearchResult> answer2 = ctx.search(_searchBase,
					groupSearchFilter, searchCtls);
			// Loop through the search results
			while (answer2.hasMoreElements()) {
				SearchResult sr = answer2.next();
				String str1[] = sr.getName().split("CN=");
				pushRole(user, str1[1].substring(0, str1[1].indexOf(",")));
			}

		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				} catch (Exception e) {
				}
			}
		}

	}
 
    /*
     * Establishes a connection with the Ldap server
     */
    private DirContext getConnection() throws NamingException
    {
        Hashtable<String, String> env = new Hashtable<String, String>();
        DirContext ctx = null;
        env.put(Context.INITIAL_CONTEXT_FACTORY,_ldapContextFactory);
        if (_ldapLoginName != null && _ldapLoginName.length() > 0)
        {
            env.put(Context.SECURITY_PRINCIPAL,_ldapLoginName);
        }
        if (_ldapLoginPassword != null && _ldapLoginPassword.length() > 0)
        {
            env.put(Context.SECURITY_CREDENTIALS,_ldapLoginPassword);
        }
        env.put(Context.PROVIDER_URL,_ldapUrl);
        try
        {
            ctx = new InitialLdapContext(env,null);
        }
        catch (NamingException e)
        {
            throw new NamingException("SpnegoUserRealm: Instantiation of Ldap Context failed");
        }
        return ctx;
    }

    /**
     * Converts a binary SID to a string
     */
    private static String binaryToStringSID(byte[] sidBytes)
    {
        StringBuffer sidString = new StringBuffer();
        sidString.append("S-");
        // Add SID revision
        sidString.append(Byte.toString(sidBytes[0]));
        // Next six bytes are issuing authority value
        sidString.append("-0x");
        sidString.append(new BigInteger(new byte[]
        { 127, sidBytes[6], sidBytes[5], sidBytes[4], sidBytes[3], sidBytes[2], sidBytes[1] }).toString(16).substring(2));
        // Next byte is the sub authority count including RID
        int saCount = sidBytes[7];
        // Get sub authority values as groups of 4 bytes
        for (int i = 0; i < saCount; ++i)
        {
            int idxAuth = 8 + i * 4;
            sidString.append("-0x");
            sidString.append(new BigInteger(new byte[]
            { 127, sidBytes[idxAuth + 3], sidBytes[idxAuth + 2], sidBytes[idxAuth + 1], sidBytes[idxAuth] }).toString(16).substring(2));
        }
        return sidString.toString();
    }

    /**
     * Convert an integer to four bytes
     */
    private static byte[] integerToFourBytes(int i)
    {
        byte[] b = new byte[4];
        b[0] = (byte)((i & 0xff000000) >>> 24);
        b[1] = (byte)((i & 0x00ff0000) >>> 16);
        b[2] = (byte)((i & 0x0000ff00) >>> 8);
        b[3] = (byte)((i & 0x000000ff));
        return b;
    }
    
    
    private void dumpConfig()
    {
        Log.debug("SpnegoUserRealm");
        Log.debug(" - targetName = " + _targetName);
        Log.debug(" - populateRoleData = " + _populateRoleData);
        Log.debug(" - ldapUrl = " + _ldapUrl );
        Log.debug(" - ldapLoginName = " + _ldapLoginName );
        Log.debug(" - ldapLoginPassword = " + _ldapLoginPassword );
        Log.debug(" - ldapContextFactory = " + _ldapContextFactory );
    }
}

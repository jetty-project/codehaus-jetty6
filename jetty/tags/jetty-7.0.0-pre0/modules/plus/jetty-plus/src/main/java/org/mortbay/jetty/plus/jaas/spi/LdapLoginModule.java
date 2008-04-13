package org.mortbay.jetty.plus.jaas.spi;

// ========================================================================
// Copyright 2007 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.mortbay.jetty.plus.jaas.callback.ObjectCallback;
import org.mortbay.jetty.security.Credential;
import org.mortbay.log.Log;


/**
 * 
 * A LdapLoginModule for use with JAAS setups
 * 
 * The jvm should be started with the following parameter:
 * <br><br>
 * <code>
 * -Djava.security.auth.login.config=etc/ldap-loginModule.conf
 * </code>
 * <br><br>
 * and an example of the ldap-loginModule.conf would be:
 * <br><br>
 * <pre>
 * ldaploginmodule {  
 *    org.mortbay.jetty.plus.jaas.spi.LdapLoginModule required
 *    debug="true"
 *    contextFactory="com.sun.jndi.ldap.LdapCtxFactory"
 *    hostname="ldap-directory_host"
 *    port="389"
 *    bindDn="cn=Directory Manager"
 *    bindPassword="directory"
 *    authenticationMethod="simple"
 *    forceBindingLogin="false"
 *    userBaseDn="ou=people,dc=alcatel"
 *    userRdnAttribute="uid"
 *    userIdAttribute="uid"
 *    userPasswordAttribute="userPassword"
 *    userObjectClass="inetOrgPerson"
 *    roleBaseDn="ou=groups,dc=alcatel"
 *    roleNameAttribute="cn"
 *    roleMemberAttribute="uniqueMember"
 *    roleObjectClass="groupOfUniqueNames";
 *    }; 
 *  </pre>   
 *
 * @author Jesse McConnell <jesse@codehaus.org>
 * @author Frederic Nizery <frederic.nizery@alcatel-lucent.fr>
 */
public class LdapLoginModule extends AbstractLoginModule
{
    /**
     * hostname of the ldap server
     */
    private String _hostname;

    /**
     * port of the ldap server
     */
    private int _port;

    /**
     * Context.SECURITY_AUTHENTICATION
     */
    private String _authenticationMethod;

    /**
     * Context.INITIAL_CONTEXT_FACTORY
     */
    private String _contextFactory;

    /**
     * root DN used to connect to
     */
    private String _bindDn;

    /**
     * password used to connect to the root ldap context
     */
    private String _bindPassword;

    /**
     * object class of a user
     */
    private String _userObjectClass = "inetOrgPerson";

    /**
     * attribute that the principal is located
     */
    private String _userRdnAttribute = "uid";

    /**
     * attribute that the principal is located
     */
    private String _userIdAttribute = "cn";

    /**
     * name of the attribute that a users password is stored under
     * <p/>
     * NOTE: not always accessible, see force binding login
     */
    private String _userPasswordAttribute = "userPassword";

    /**
     * base DN where users are to be searched from
     */
    private String _userBaseDn;

    /**
     * base DN where role membership is to be searched from
     */
    private String _roleBaseDn;

    /**
     * object class of roles
     */
    private String _roleObjectClass = "groupofuniquenames";

    /**
     * name of the attribute that a username would be under a role class
     */
    private String _roleMemberAttribute = "uniqueMember";

    /**
     * the name of the attribute that a role would be stored under
     */
    private String _roleNameAttribute = "roleName";

    /**
     * if the getUserInfo can pull a password off of the user then
     * password comparison is an option for authn, to force binding
     * login checks, set this to true
     */
    private boolean _forceBindingLogin = false;

    private DirContext _rootContext;



    /**
     * get the available information about the user
     * <p/>
     * for this LoginModule, the credential can be null which will result in a
     * binding ldap authentication scenario
     * <p/>
     * roles are also an optional concept if required
     *
     * @param username
     * @return
     * @throws Exception
     */
    public UserInfo getUserInfo(String username) throws Exception
    {
        String pwdCredential = getUserCredentials(username);
        pwdCredential = convertCredentialLdapToJetty(pwdCredential);

        //String md5Credential = Credential.MD5.digest("foo");
        //byte[] ba = digestMD5("foo");
        //System.out.println(md5Credential + "  " + ba );
        Credential credential = Credential.getCredential(pwdCredential);
        List roles = getUserRoles(username);

        return new UserInfo(username, credential, roles);
    }


    protected String doRFC2254Encoding(String inputString)
    {
        StringBuffer buf = new StringBuffer(inputString.length());
        for (int i = 0; i < inputString.length(); i++)
        {
            char c = inputString.charAt(i);
            switch (c)
            {
                case '\\':
                    buf.append("\\5c");
                    break;
                case '*':
                    buf.append("\\2a");
                    break;
                case '(':
                    buf.append("\\28");
                    break;
                case ')':
                    buf.append("\\29");
                    break;
                case '\0':
                    buf.append("\\00");
                    break;
                default:
                    buf.append(c);
                    break;
            }
        }
        return buf.toString();
    }


    /**
     * attempts to get the users credentials from the users context
     * <p/>
     * NOTE: this is not an user authenticated operation
     *
     * @param username
     * @return
     * @throws LoginException
     */
    private String getUserCredentials(String username) throws LoginException
    {

        String ldapCredential = null;

        SearchControls ctls = new SearchControls();

        ctls.setCountLimit(1);

        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String filter = "(&(objectClass=" + _userObjectClass + ")(" + _userIdAttribute + "=" + username + "))";

        Log.debug("Searching for users with filter: \'" + filter + "\'" + " from base dn: " + _userBaseDn);

        try
        {

            NamingEnumeration<SearchResult> results = _rootContext.search(_userBaseDn, filter, ctls);

            Log.debug("Found user?: " + results.hasMoreElements());

            if (results.hasMoreElements())
            {
                SearchResult result = results.nextElement();

                Attributes attributes = result.getAttributes();

                Attribute attribute = attributes.get(_userPasswordAttribute);
                if (attribute != null)
                {
                    try
                    {
                        byte[] value = (byte[]) attribute.get();

                        ldapCredential = new String(value);
                    }
                    catch (NamingException e)
                    {
                        Log.debug("no password available under attribute: " + _userPasswordAttribute);
                    }

                }
            }
            else
            {
                throw new LoginException("User not found.");
            }
        }
        catch (NamingException e)
        {
            throw new LoginException("Root context binding failure.");
        }

        Log.debug("user cred is: " + ldapCredential);

        return ldapCredential;
    }

    /**
     * attempts to get the users roles from the root context
     * <p/>
     * NOTE: this is not an user authenticated operation
     *
     * @param username
     * @return
     * @throws LoginException
     */
    private List getUserRoles(String username) throws LoginException
    {
        ArrayList roleList = new ArrayList();

        SearchControls ctls = new SearchControls();

        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        if (_roleBaseDn != null)
        {
            String userDn = _userRdnAttribute + "=" + username + "," + _userBaseDn;
            String filter = "(&(objectClass=" + _roleObjectClass + ")(" + _roleMemberAttribute + "=" + userDn + "))";
            //filter  = doRFC2254Encoding(filter)
            try
            {
                NamingEnumeration<SearchResult> results = _rootContext.search(_roleBaseDn, filter, ctls);

                Log.debug("Found user roles?: " + results.hasMoreElements());

                while (results.hasMoreElements())
                {
                    SearchResult result = results.nextElement();

                    Attributes attributes = result.getAttributes();

                    if (attributes != null)
                    {
                        Attribute roleAttribute = attributes.get(_roleNameAttribute);

                        if (roleAttribute != null)
                        {
                            NamingEnumeration roles = roleAttribute.getAll();
                            while (roles.hasMore())
                            {
                                String roleName = (String) roles.next();
                                roleList.add(roleName);
                            }
                        }
                    }
                }
            }
            catch (NamingException e)
            {
                throw new LoginException("error obtaining roles for " + username);
            }
        }

        return roleList;
    }


    /**
     * since ldap uses a context bind for valid authentication checking, we override login()
     * <p/>
     * if credentials are not available from the users context or if we are forcing the binding check
     * then we try a binding authentication check, otherwise if we have the users encoded password then
     * we can try authentication via that mechanic
     *
     * @return
     * @throws LoginException
     */
    public boolean login() throws LoginException
    {
        try
        {
            if (getCallbackHandler() == null)
            {
                throw new LoginException("No callback handler");
            }

            Callback[] callbacks = configureCallbacks();
            getCallbackHandler().handle(callbacks);

            String webUserName = ((NameCallback) callbacks[0]).getName();
            Object webCredential = ((ObjectCallback) callbacks[1]).getObject();

            if ((webUserName == null) || (webCredential == null))
            {
                setAuthenticated(false);
                return isAuthenticated();
            }

            UserInfo userInfo = getUserInfo(webUserName);

            setCurrentUser(new JAASUserInfo(userInfo));

            // if the userInfo query didn't pick up a password then opt for bindingLogin
            if (userInfo.getCredential() == null || _forceBindingLogin)
            {
                return bindingLogin(webUserName, webCredential);
            }
            else
            {             
                if (webCredential instanceof String)
                {
                    return credentialLogin(Credential.getCredential((String) webCredential));
                }
                else
                {
                    return credentialLogin(webCredential);
                }
            }

        }
        catch (UnsupportedCallbackException e)
        {
            throw new LoginException("Error obtaining callback information.");
        }
        catch (IOException e)
        {
            throw new LoginException("IO Error performing login.");
        }
        catch (Exception e)
        {
            throw new LoginException("Error obtaining user info.");
        }
    }

    /**
     * password supplied authentication check
     *
     * @param webCredential
     * @return
     * @throws LoginException
     */

    protected boolean credentialLogin(Object webCredential) throws LoginException
    {
        setAuthenticated(getCurrentUser().checkCredential(webCredential));
        return isAuthenticated();
    }

    /**
     * binding authentication check
     * This methode of authentication works only if the user branch of the DIT (ldap tree)
     * has an ACI (acces controle instruction) that allow the access to any user or at least
     * for the user that logs in.
     * 
     * @param username
     * @param password
     * @return
     * @throws LoginException
     */
    
    protected boolean bindingLogin(String username, Object password) throws LoginException
    {
        SearchControls ctls = new SearchControls();

        ctls.setCountLimit(1);

        ctls.setDerefLinkFlag(true);
        ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);

        String filter = "(&(objectClass=" + _userObjectClass + ")(" + _userIdAttribute + "=" + username + "))";

        Log.info("Searching for users with filter: \'" + filter + "\'" + " from base dn: " + _userBaseDn);

        try
        {

            NamingEnumeration<SearchResult> results = _rootContext.search(_userBaseDn, filter, ctls);

            Log.info("Found user?: " + results.hasMoreElements());

            if (results.hasMoreElements())
            {
                SearchResult result = results.nextElement();

                String userDn = result.getNameInNamespace();

                Log.info("Attempting Authenication: + " + userDn);

                Hashtable environment = getEnvironment();
                environment.put(Context.SECURITY_PRINCIPAL, userDn);
                environment.put(Context.SECURITY_CREDENTIALS, password);

                try
                {
                    DirContext userContext = new InitialDirContext( environment );

                }
                catch (AuthenticationException e)
                {
                    Log.info("Authentication failed for: " + userDn);
                    throw new LoginException();
                }
            catch (NamingException ne)
        {
            throw new LoginException("Context binding failure.");
        }
               
                setAuthenticated(true);

                return true;
            }
            else
            {
                throw new LoginException("User not found.");
            }
        }
        catch (NamingException e)
        {
            throw new LoginException("Context binding failure.");
        }

    }


    /**
     * Init LoginModule.
     * Called once by JAAS after new instance is created.
     *
     * @param subject
     * @param callbackHandler
     * @param sharedState
     * @param options
     */
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map sharedState,
                           Map options)
    {

        super.initialize(subject, callbackHandler, sharedState, options);

        _hostname = (String) options.get("hostname");
        _port = Integer.parseInt((String) options.get("port"));
        _contextFactory = (String) options.get("contextFactory");
        _bindDn = (String) options.get("bindDn");
        _bindPassword = (String) options.get("bindPassword");
        _authenticationMethod = (String) options.get("authenticationMethod");

        _userBaseDn = (String) options.get("userBaseDn");

        _roleBaseDn = (String) options.get("roleBaseDn");

        if (options.containsKey("forceBindingLogin"))
        {
            _forceBindingLogin = Boolean.parseBoolean((String) options.get("forceBindingLogin"));
        }

        if (options.containsKey("userObjectClass"))
        {
            _userObjectClass = (String) options.get("userObjectClass");
        }

        if (options.containsKey("userRdnAttribute"))
        {
            _userRdnAttribute = (String) options.get("userRdnAttribute");
        }

        if (options.containsKey("userIdAttribute"))
        {
            _userIdAttribute = (String) options.get("userIdAttribute");
        }

        if (options.containsKey("userPasswordAttribute"))
        {
            _userPasswordAttribute = (String) options.get("userPasswordAttribute");
        }

        if (options.containsKey("roleObjectClass"))
        {
            _roleObjectClass = (String) options.get("roleObjectClass");
        }
        if (options.containsKey("roleMemberAttribute"))
        {
            _roleMemberAttribute = (String) options.get("roleMemberAttribute");
        }
        if (options.containsKey("roleNameAttribute"))
        {
            _roleNameAttribute = (String) options.get("roleNameAttribute");
        }

        try
        {
            _rootContext = new InitialDirContext(getEnvironment());
        }
        catch (NamingException ex)
        {
            throw new IllegalStateException("Unable to establish root context", ex);
        }

    }

    /**
     * get the context for connection
     *
     * @return
     */
    public Hashtable<Object, Object> getEnvironment()
    {
        Properties env = new Properties();

        env.put(Context.INITIAL_CONTEXT_FACTORY, _contextFactory);

        if (_hostname != null)
        {
            if (_port != 0)
            {
                env.put(Context.PROVIDER_URL, "ldap://" + _hostname + ":" + _port + "/");
            }
            else
            {
                env.put(Context.PROVIDER_URL, "ldap://" + _hostname + "/");
            }
        }

        if (_authenticationMethod != null)
        {
            env.put(Context.SECURITY_AUTHENTICATION, _authenticationMethod);
        }

        if (_bindDn != null)
        {
            env.put(Context.SECURITY_PRINCIPAL, _bindDn);
        }

        if (_bindPassword != null)
        {
            env.put(Context.SECURITY_CREDENTIALS, _bindPassword);
        }

        return env;
    }

    public static String convertCredentialJettyToLdap( String encryptedPassword )
    {
        if ( encryptedPassword.toUpperCase().startsWith( "MD5:" ) )
        {
            String epwd = encryptedPassword.substring( "MD5:".length(), encryptedPassword.length() );
            return "{MD5}" + epwd;
        }
        else if ( encryptedPassword.toUpperCase().startsWith( "CRYPT:" ) )
        {
            String epwd = encryptedPassword.substring( "CRYPT:".length(), encryptedPassword.length() );
            return "{CRYPT}" + epwd;
        }
        else
        {
            return encryptedPassword;
        }
    }

    public static String convertCredentialLdapToJetty( String encryptedPassword )
    {
        if ( encryptedPassword.toUpperCase().startsWith( "{MD5}" ) )
        {
            String epwd = encryptedPassword.substring( "{MD5}".length(), encryptedPassword.length() );
            return "MD5:" + epwd;
        }
        else if ( encryptedPassword.toUpperCase().startsWith( "{CRYPT}" ) )
        {
            String epwd = encryptedPassword.substring( "{CRYPT}".length(), encryptedPassword.length() );
            return "CRYPT:" + epwd;
        }
        else
        {
            return encryptedPassword;
        }
    }

    public static byte[] digestMD5(String pwd) throws LoginException
    {
        MessageDigest md;

        byte[] barray;
        try
        {
            md = MessageDigest.getInstance("MD5");
            barray = pwd.getBytes("ISO-8859-1");//todo try w/ UTF8
        }
        catch (UnsupportedEncodingException e)
        {
            throw new LoginException();
        }
        catch (NoSuchAlgorithmException e1)
        {
            throw new LoginException();
        }
        for (int i = 0; i < barray.length; i++)
        {
            md.update(barray[i]);
        }
        String mdString = md.toString();

        return md.digest();

    }
}
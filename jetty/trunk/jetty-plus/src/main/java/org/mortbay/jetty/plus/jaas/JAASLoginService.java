// ========================================================================
// $Id$
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


import org.mortbay.jetty.plus.jaas.callback.ObjectCallback;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.server.LoginCallback;
import org.mortbay.jetty.util.log.Log;

/* ---------------------------------------------------- */
/** JAASLoginService
 * <p>
 *
 * <p><h4>Notes</h4>
 * <p>
 *
 * <p><h4>Usage</h4>
 * 
 *
 *
 * 
 * @org.apache.xbean.XBean element="jaasUserRealm" description="Creates a UserRealm suitable for use with JAAS"
 */
public class JAASLoginService implements LoginService
{
    public static String DEFAULT_ROLE_CLASS_NAME = "org.mortbay.jetty.plus.jaas.JAASRole";
    public static String[] DEFAULT_ROLE_CLASS_NAMES = {DEFAULT_ROLE_CLASS_NAME};
	
    protected String[] _roleClassNames = DEFAULT_ROLE_CLASS_NAMES;
    protected String _callbackHandlerClass;
    protected String _realmName;
    protected String _loginModuleName;
    protected JAASUserPrincipal _defaultUser = new JAASUserPrincipal(null, null, null);
    
 

    /* ---------------------------------------------------- */
    /**
     * Constructor.
     *
     */
    public JAASLoginService()
    {
    }
    

    /* ---------------------------------------------------- */
    /**
     * Constructor.
     *
     * @param name the name of the realm
     */
    public JAASLoginService(String name)
    {
        this();
        _realmName = name;
        _loginModuleName = name;
    }


    /* ---------------------------------------------------- */
    /**
     * Get the name of the realm.
     *
     * @return name or null if not set.
     */
    public String getName()
    {
        return _realmName;
    }


    /* ---------------------------------------------------- */
    /**
     * Set the name of the realm
     *
     * @param name a <code>String</code> value
     */
    public void setName (String name)
    {
        _realmName = name;
    }



    /**
     * Set the name to use to index into the config
     * file of LoginModules.
     *
     * @param name a <code>String</code> value
     */
    public void setLoginModuleName (String name)
    {
        _loginModuleName = name;
    }


    public void setCallbackHandlerClass (String classname)
    {
        _callbackHandlerClass = classname;
    }
    
    public void setRoleClassNames (String[] classnames)
    {
        ArrayList<String> tmp = new ArrayList<String>();
        
        if (classnames != null)
            tmp.addAll(Arrays.asList(classnames));
         
        if (!tmp.contains(DEFAULT_ROLE_CLASS_NAME))
            tmp.add(DEFAULT_ROLE_CLASS_NAME);
        _roleClassNames = tmp.toArray(new String[tmp.size()]);
    }

    public String[] getRoleClassNames()
    {
        return _roleClassNames;
    }

    public void login(final LoginCallback loginCallback) throws ServerAuthException
    {
        try
        {
            CallbackHandler callbackHandler = new CallbackHandler()
            {

                public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
                {
                    for (Callback callback: callbacks)
                    {
                        if (callback instanceof NameCallback)
                        {
                            ((NameCallback)callback).setName(loginCallback.getUserName());
                        }
                        else if (callback instanceof PasswordCallback)
                        {
                            ((PasswordCallback)callback).setPassword((char[]) loginCallback.getCredential());
                        }
                        else if (callback instanceof ObjectCallback)
                        {
                            ((ObjectCallback)callback).setObject(loginCallback.getCredential());
                        }
                    }
                }
            };
            //set up the login context
            //TODO jaspi requires we provide the Configuration parameter
            Subject subject = loginCallback.getSubject();
            LoginContext loginContext = new LoginContext(_loginModuleName, subject, callbackHandler);

            loginContext.login();

            //login success
            JAASUserPrincipal userPrincipal = new JAASUserPrincipal(getUserName(callbackHandler), loginCallback.getSubject(), loginContext);
            subject.getPrincipals().add(userPrincipal);
            loginCallback.setUserPrincipal(userPrincipal);
            loginCallback.setRoles(getGroups(subject));
            loginCallback.setSuccess(true);
        }
        catch (LoginException e)
        {
            Log.warn(e);
            loginCallback.setSuccess(false);
        }
        catch (IOException e)
        {
            Log.warn(e);
            loginCallback.setSuccess(false);
        }
        catch (UnsupportedCallbackException e)
        {
           Log.warn(e);
           loginCallback.setSuccess(false);
        }
       
    }

    private String getUserName(CallbackHandler callbackHandler) throws IOException, UnsupportedCallbackException
    {
        NameCallback nameCallback = new NameCallback("foo");
        callbackHandler.handle(new Callback[] {nameCallback});
        return nameCallback.getName();
    }

    public void logout(Subject subject) throws ServerAuthException
    {
//        loginCallback.clearPassword();
        Set<JAASUserPrincipal> userPrincipals = subject.getPrincipals(JAASUserPrincipal.class);
        if (userPrincipals.size() != 1)
        {
            throw new ServerAuthException("logout implausible, wrong number of user principals: " + userPrincipals);
        }
        LoginContext loginContext = userPrincipals.iterator().next().getLoginContext();
        try
        {
            loginContext.logout();
        }
        catch (LoginException e)
        {
            throw new ServerAuthException("Failed to log out: "+e.getMessage());
        }
    }


    private String[] getGroups (Subject subject)
    {
        //get all the roles of the various types
        String[] roleClassNames = getRoleClassNames();
        Collection<String> groups = new LinkedHashSet<String>();
        try
        {
            for (String roleClassName : roleClassNames)
            {
                Class load_class = Thread.currentThread().getContextClassLoader().loadClass(roleClassName);
                Set<Principal> rolesForType = subject.getPrincipals(load_class);
                for (Principal principal : rolesForType)
                {
                    groups.add(principal.getName());
                }
            }
            
            return groups.toArray(new String[groups.size()]);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

}

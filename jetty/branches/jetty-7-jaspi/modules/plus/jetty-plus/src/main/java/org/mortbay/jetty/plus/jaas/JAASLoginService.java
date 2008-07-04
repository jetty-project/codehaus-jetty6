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
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.auth.message.AuthException;

import org.mortbay.jetty.security.jaspi.modules.LoginCredentials;
import org.mortbay.jetty.security.jaspi.modules.LoginResult;
import org.mortbay.jetty.security.jaspi.modules.LoginService;
import org.mortbay.jetty.security.jaspi.modules.UserPasswordLoginCredentials;


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
	
    protected String[] roleClassNames = DEFAULT_ROLE_CLASS_NAMES;
    protected String callbackHandlerClass;
    protected String realmName;
    protected String loginModuleName;
    protected JAASUserPrincipal defaultUser = new JAASUserPrincipal(null, null, null);
    
 

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
        realmName = name;
    }


    /* ---------------------------------------------------- */
    /**
     * Get the name of the realm.
     *
     * @return name or null if not set.
     */
    public String getName()
    {
        return realmName;
    }


    /* ---------------------------------------------------- */
    /**
     * Set the name of the realm
     *
     * @param name a <code>String</code> value
     */
    public void setName (String name)
    {
        realmName = name;
    }



    /**
     * Set the name to use to index into the config
     * file of LoginModules.
     *
     * @param name a <code>String</code> value
     */
    public void setLoginModuleName (String name)
    {
        loginModuleName = name;
    }


    public void setCallbackHandlerClass (String classname)
    {
        callbackHandlerClass = classname;
    }
    
    public void setRoleClassNames (String[] classnames)
    {
        ArrayList<String> tmp = new ArrayList<String>();
        
        if (classnames != null)
            tmp.addAll(Arrays.asList(classnames));
         
        if (!tmp.contains(DEFAULT_ROLE_CLASS_NAME))
            tmp.add(DEFAULT_ROLE_CLASS_NAME);
        roleClassNames = tmp.toArray(new String[tmp.size()]);
    }

    public String[] getRoleClassNames()
    {
        return roleClassNames;
    }

    public LoginResult login(Subject subject, final LoginCredentials loginCredentials) throws AuthException
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
                            ((NameCallback)callback).setName(((UserPasswordLoginCredentials)loginCredentials).getUsername());
                        }
                        else if (callback instanceof PasswordCallback)
                        {
                            ((PasswordCallback)callback).setPassword(((UserPasswordLoginCredentials)loginCredentials).getPassword());
                        }
                    }
                }
            };
            //set up the login context
            //TODO jaspi requires we provide the Configuration parameter
            LoginContext loginContext = new LoginContext(loginModuleName, subject, callbackHandler);

            loginContext.login();

            //login success
            JAASUserPrincipal userPrincipal = new JAASUserPrincipal(getUserName(callbackHandler), subject, loginContext);
            subject.getPrincipals().add(userPrincipal);
            return new LoginResult(true,userPrincipal, getGroups(subject), subject);
        }
        catch (LoginException e)
        {
            return new LoginResult(false,null,null,null);
        }
        catch (IOException e)
        {
            return new LoginResult(false,null,null,null);
        }
        catch (UnsupportedCallbackException e)
        {
            return new LoginResult(false,null,null,null);
        }
    }

    private String getUserName(CallbackHandler callbackHandler) throws IOException, UnsupportedCallbackException
    {
        NameCallback nameCallback = new NameCallback("foo");
        callbackHandler.handle(new Callback[] {nameCallback});
        return nameCallback.getName();
    }

    public void logout(Subject subject) throws AuthException
    {
        Set<JAASUserPrincipal> userPrincipals = subject.getPrincipals(JAASUserPrincipal.class);
        if (userPrincipals.size() != 1)
        {
            throw new AuthException("logout implausible, wrong number of user principals: " + userPrincipals);
        }
        LoginContext loginContext = userPrincipals.iterator().next().getLoginContext();
        try
        {
            loginContext.logout();
        }
        catch (LoginException e)
        {
            throw new AuthException("Failed to log out: "+e.getMessage());
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

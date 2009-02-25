// ========================================================================
// $Id$
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.security;

import java.io.PrintStream;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.Subject;

import org.mortbay.jetty.http.security.Credential;
import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.server.LoginCallback;
import org.mortbay.jetty.util.component.AbstractLifeCycle;


public abstract class AbstractLoginService extends AbstractLifeCycle implements LoginService
{
    protected static final String[] NO_ROLES = new String[0];
    protected Map<String, User> _users=new ConcurrentHashMap<String, User>();
    protected String _realmName;
   
    

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public AbstractLoginService()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm Name
     */
    public AbstractLoginService(String name)
    {
        _realmName=name;
    }
    
    public AbstractLoginService (String name, Map<String, User> users)
    {
        _realmName=name;
        _users = users;
    }
    
    /* ------------------------------------------------------------ */
    public void login(LoginCallback loginCallback) throws ServerAuthException
    {
        KnownUser user= getKnownUser(loginCallback.getUserName());
        
        if (user != null && user.authenticate(loginCallback.getCredential()))
        {
            loginCallback.getSubject().getPrincipals().add(user);
            loginCallback.setUserPrincipal(user);
            loginCallback.setGroups(user.roles);
            loginCallback.setSuccess(true);
        }
    }

    protected KnownUser getKnownUser(String userName)
    {
        return (KnownUser) _users.get(userName);
    }

    public void logout(Subject subject) throws ServerAuthException
    {
        subject.getPrincipals(KnownUser.class).clear();
    }
    
    
    
    /* ------------------------------------------------------------ */
    /** 
     * @return The realm name. 
     */
    public String getName()
    {
        return _realmName;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param name The realm name 
     */
    public void setName(String name)
    {
        _realmName=name;
    }
    
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Realm["+_realmName+"]=="+_users.keySet();
    }
    
    /* ------------------------------------------------------------ */
    public void dump(PrintStream out)
    {
        out.println(this+":");
        out.println(super.toString());
    }
    
    /* ------------------------------------------------------------ */
    /** Put user into realm.
     * Called by implementations to put the user data loaded from
     * file/db etc into the user structure.
     * @param userName User name
     * @param userInfo a User instance, or a String password or Password instance
     * @return User instance
     */
    protected synchronized Object putUser(String userName, Object userInfo)
    {
        final User user;
        if (userInfo instanceof User)
            user=(User)userInfo;
        else if (userInfo instanceof Password)
            user=new KnownUser(userName,(Password)userInfo);
        else if (userInfo != null)
            user=new KnownUser(userName,Credential.getCredential(userInfo.toString()));
        else
            user=null;
        
        _users.put(userName,user);
        
        return user;
    }

  
   
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class User implements Principal
    {
        String[] roles = NO_ROLES;
        
        public String getName()
        {
            return "Anonymous";
        }
                
        public boolean isAuthenticated()
        {
            return false;
        }
        
        public String toString()
        {
            return getName();
        }        
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class KnownUser extends User
    {
        private String _userName;
        private Credential _cred;
        
        /* -------------------------------------------------------- */
        KnownUser(String name,Credential credential)
        {
            _userName=name;
            _cred=credential;
        }
        
        public KnownUser(String name, Credential credential, String[] roles)
        {
            this(name, credential);
            this.roles = roles;
        }

        /* -------------------------------------------------------- */
        boolean authenticate(Object credentials)
        {
            return _cred!=null && _cred.check(credentials);
        }
        
        /* ------------------------------------------------------------ */
        public String getName()
        {
            return _userName;
        }
        
        /* -------------------------------------------------------- */
        public boolean isAuthenticated()
        {
            return true;
        }
    }
}


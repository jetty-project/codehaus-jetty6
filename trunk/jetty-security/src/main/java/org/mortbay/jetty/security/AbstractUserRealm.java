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
import java.util.HashSet;
import java.util.List;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.UserRealm;
import org.mortbay.jetty.http.security.Credential;
import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.util.component.AbstractLifeCycle;

public abstract class AbstractUserRealm extends AbstractLifeCycle implements UserRealm, SSORealm
{

    /** HttpContext Attribute to set to activate SSO.
     */
    public static final String __SSO = "org.mortbay.http.SSO";
    
    protected HashMap _users=new HashMap();
    protected HashMap _roles=new HashMap(7);
    protected String _realmName;
    protected SSORealm _ssoRealm;
    

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public AbstractUserRealm()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm Name
     */
    public AbstractUserRealm(String name)
    {
        _realmName=name;
    }
    
    
    
    /* ------------------------------------------------------------ */
    public void disassociate(Principal user)
    {
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
    public Principal getPrincipal(String username)
    {
        return (Principal)_users.get(username);
    }

    /* ------------------------------------------------------------ */
    /** Check if a user is in a role.
     * @param user The user, which must be from this realm 
     * @param roleName 
     * @return True if the user can act in the role.
     */
    public synchronized boolean isUserInRole(Principal user, String roleName)
    {
        if (user instanceof WrappedUser)
            return ((WrappedUser)user).isUserInRole(roleName);
         
        if (user==null || !(user instanceof User) || ((User)user).getUserRealm()!=this)
            return false;
        
        HashSet userSet = (HashSet)_roles.get(roleName);
        return userSet!=null && userSet.contains(user.getName());
    }

    public void logout(Principal user)
    {  
    }

    
    /* ------------------------------------------------------------ */
    public Principal authenticate(String username,Object credentials,Request request)
    {
        KnownUser user;
        synchronized (this)
        {
            user = (KnownUser)_users.get(username);
        }
        if (user==null)
            return null;
        
        if (user.authenticate(credentials))
            return user;
        
        return null;
    }

    /* ------------------------------------------------------------ */
    public Principal pushRole(Principal user, String role)
    {
        if (user==null)
            user=new User();
        
        return new WrappedUser(user,role);
    }

    /* ------------------------------------------------------------ */
    public Principal popRole(Principal user)
    {
        WrappedUser wu = (WrappedUser)user;
        return wu.getUserPrincipal();
    }
    
    /* -------------------------------------------------------- */
    public boolean reauthenticate(Principal user)
    {
        return ((User)user).isAuthenticated();
    }
    
    
    /* ------------------------------------------------------------ */
    /** 
     * @return The SSORealm to delegate single sign on requests to.
     */
    public SSORealm getSSORealm()
    {
        return _ssoRealm;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the SSORealm.
     * A SSORealm implementation may be set to enable support for SSO.
     * @param ssoRealm The SSORealm to delegate single sign on requests to.
     */
    public void setSSORealm(SSORealm ssoRealm)
    {
        _ssoRealm = ssoRealm;
    }
    
    /* ------------------------------------------------------------ */
    public Credential getSingleSignOn(Request request,Response response)
    {
        if (_ssoRealm!=null)
            return _ssoRealm.getSingleSignOn(request,response);
        return null;
    }
    
    /* ------------------------------------------------------------ */
    public void setSingleSignOn(Request request,Response response,Principal principal,Credential credential)
    {
        if (_ssoRealm!=null)
            _ssoRealm.setSingleSignOn(request,response,principal,credential);
    }
    
    /* ------------------------------------------------------------ */
    public void clearSingleSignOn(String username)
    {
        if (_ssoRealm!=null)
            _ssoRealm.clearSingleSignOn(username);
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
        out.println(_roles);
    }
    
    /* ------------------------------------------------------------ */
    /** Put user into realm.
     * Called by implementations to put the user data loaded from
     * file/db etc into the user structure.
     * @param name User name
     * @param credentials String password, Password or UserPrinciple
     *                    instance. 
     * @return Old UserPrinciple value or null
     */
    protected synchronized Object putUser(Object name, Object credentials)
    {
        if (credentials instanceof Principal)
            return _users.put(name.toString(),credentials);
        
        if (credentials instanceof Password)
            return _users.put(name,new KnownUser(name.toString(),(Password)credentials));
        if (credentials != null)
            return _users.put(name,new KnownUser(name.toString(),Credential.getCredential(credentials.toString())));
        return null;
    }

    /* ------------------------------------------------------------ */
    /** Add a user to a role.
     * Called by implementations to put the user's role data, loaded
     * from file/db/whatever into the roles structure.
     * @param userName 
     * @param roleName 
     */
    protected synchronized void putUserRole(String userName, String roleName)
    {
        HashSet userSet = (HashSet)_roles.get(roleName);
        if (userSet==null)
        {
            userSet=new HashSet(11);
            _roles.put(roleName,userSet);
        }
        userSet.add(userName);
    }
   
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class User implements Principal
    {
        List roles=null;

        /* ------------------------------------------------------------ */
        private UserRealm getUserRealm()
        {
            return AbstractUserRealm.this;
        }
        
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
    protected class KnownUser extends User
    {
        private String _userName;
        private Credential _cred;
        
        /* -------------------------------------------------------- */
        KnownUser(String name,Credential credential)
        {
            _userName=name;
            _cred=credential;
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

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    protected class WrappedUser extends User
    {   
        private Principal user;
        private String role;

        WrappedUser(Principal user, String role)
        {
            this.user=user;
            this.role=role;
        }

        Principal getUserPrincipal()
        {
            return user;    
        }

        public String getName()
        {
            return "role:"+role;
        }
                
        public boolean isAuthenticated()
        {
            return true;
        }
        
        public boolean isUserInRole(String role)
        {
            return this.role.equals(role);
        }
    }
}

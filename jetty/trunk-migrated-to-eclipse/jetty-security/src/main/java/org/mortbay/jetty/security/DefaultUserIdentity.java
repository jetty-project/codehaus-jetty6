//========================================================================
//Copyright 2009 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.security;

import java.security.Principal;

import javax.security.auth.Subject;

import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.security.Authentication.Status;
import org.mortbay.jetty.server.UserIdentity;


/* ------------------------------------------------------------ */
/**
 * The default implementation of UserIdentity.
 *
 */
public class DefaultUserIdentity implements UserIdentity
{
    /* Cache successful authentications for BASIC and DIGEST to avoid creation on every request */
    public final Authentication SUCCESSFUL_BASIC = new DefaultAuthentication(Status.SUCCESS,Constraint.__BASIC_AUTH,this);
    public final Authentication SUCCESSFUL_DIGEST = new DefaultAuthentication(Status.SUCCESS,Constraint.__BASIC_AUTH,this);
    
    private final Subject _subject;
    private final Principal _userPrincipal;
    private final String[] _roles;
    
    public DefaultUserIdentity(Subject subject, Principal userPrincipal, String[] roles)
    {
        _subject=subject;
        _userPrincipal=userPrincipal;
        _roles=roles;
    }

    public String[] getRoles()
    {
        return _roles;
    }

    public Subject getSubject()
    {
        return _subject;
    }

    public Principal getUserPrincipal()
    {
        return _userPrincipal;
    }

    public boolean isUserInRole(String role)
    {
        for (String r :_roles)
            if (r.equals(role))
                return true;
        return false;
    }

    
}
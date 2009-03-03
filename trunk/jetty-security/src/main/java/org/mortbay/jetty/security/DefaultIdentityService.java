package org.mortbay.jetty.security;

import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;

import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.UserIdentity;
import org.mortbay.jetty.server.UserIdentity.Context;
import org.mortbay.jetty.util.component.AbstractLifeCycle;

public class DefaultIdentityService implements IdentityService
{
    public DefaultIdentityService()
    {
    }
    
    public UserIdentity associate(UserIdentity user, Context context)
    {
        return user;
    }

    public void disassociate(UserIdentity user)
    {
    }

    public RunAsToken associateRunAs(RunAsToken token)
    {
        return null;
    }

    public void disassociateRunAs(RunAsToken token)
    {
    }
    
    public RunAsToken newRunAsToken(String runAsName)
    {
        return new RoleRunAsToken(runAsName);
    }

    public UserIdentity newUserIdentity(final Subject subject, final Principal userPrincipal, final String[] roles)
    {
        return new UserIdentity()
        {
            public String[] getRoles()
            {
                return roles;
            }

            public Subject getSubject()
            {
                return subject;
            }

            public Principal getUserPrincipal()
            {
                return userPrincipal;
            }

            public boolean isUserInRole(String role)
            {
                for (String r :roles)
                    if (r.equals(role))
                        return true;
                return false;
            }
        };
    }

}

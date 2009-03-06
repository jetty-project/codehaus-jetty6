package org.mortbay.jetty.security;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.UserIdentity;
import org.mortbay.jetty.server.UserIdentity.Scope;
import org.mortbay.jetty.util.component.AbstractLifeCycle;


/* ------------------------------------------------------------ */
/**
 * Default Identity Service implementation.
 * This service handles only role reference maps passed in an
 * associated {@link UserIdentity.Scope}.  If there are roles
 * refs present, then associate will wrap the UserIdentity with one
 * that uses the role references in the {@link UserIdentity#isUserInRole(String)}
 * implementation. All other operations are effectively noops.
 *
 */
public class DefaultIdentityService implements IdentityService
{
    public DefaultIdentityService()
    {
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * If there are roles refs present in the scope, then wrap the UserIdentity 
     * with one that uses the role references in the {@link UserIdentity#isUserInRole(String)}
     */
    public UserIdentity associate(UserIdentity user, Scope scope)
    {
        Map<String,String> roleRefMap=scope.getRoleRefMap();
        if (roleRefMap!=null && roleRefMap.size()>0)
            return new RoleRefUserIdentity(user,roleRefMap);
        return user;
    }

    public void disassociate(UserIdentity lastUser)
    {
    }

    public RunAsToken associateRunAs(RunAsToken token)
    {
        return null;
    }

    public void disassociateRunAs(RunAsToken lastToken)
    {
    }
    
    public RunAsToken newRunAsToken(String runAsName)
    {
        return new RoleRunAsToken(runAsName);
    }

    public UserIdentity newUserIdentity(final Subject subject, final Principal userPrincipal, final String[] roles)
    {
        return new DefaultUserIdentity(subject,userPrincipal,roles);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Wrapper UserIdentity used to apply RoleRef map.
     *
     */
    public static class RoleRefUserIdentity implements UserIdentity
    {
        final private UserIdentity _delegate;
        final private Map<String,String> _roleRefMap;

        public RoleRefUserIdentity(final UserIdentity user, final Map<String, String> roleRefMap)
        {
            _delegate=user;
            _roleRefMap=roleRefMap;
        }

        public String[] getRoles()
        {
            return _delegate.getRoles();
        }
        
        public Subject getSubject()
        {
            return _delegate.getSubject();
        }

        public Principal getUserPrincipal()
        {
            return _delegate.getUserPrincipal();
        }

        public boolean isUserInRole(String role)
        {
            String link=_roleRefMap.get(role);
            return _delegate.isUserInRole(link==null?role:link);
        }
        
    }
}

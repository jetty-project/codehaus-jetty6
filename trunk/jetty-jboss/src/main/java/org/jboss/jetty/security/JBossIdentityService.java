package org.jboss.jetty.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.RoleRunAsToken;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.server.UserIdentity.Scope;
import org.jboss.logging.Logger;
import org.jboss.security.RealmMapping;
import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

public class JBossIdentityService extends DefaultIdentityService
{
    private RealmMapping _realmMapping;
    private Logger _log;
    
    
    public class JBossUserIdentity implements UserIdentity
    {
        private Subject _subject;
        private Principal _principal;
        
        public JBossUserIdentity(Subject subject, Principal principal)
        {
            _subject = subject;
            _principal = principal;
        }
        
        
        public String[] getRoles()
        {
            // TODO Auto-generated method stub
            return null;
        }

        public Subject getSubject()
        {
            return _subject;
        }

        public Principal getUserPrincipal()
        {
            return _principal;
        }

        public boolean isUserInRole(String role)
        {
            boolean isUserInRole = false;
            Set requiredRoles = Collections.singleton(new SimplePrincipal(role));
            if (_realmMapping != null
                    && _realmMapping.doesUserHaveRole(this._principal,requiredRoles))
            {
                if (_log.isDebugEnabled())
                    _log.debug("JBossUserPrincipal: " + _principal + " is in Role: " + role);

                isUserInRole = true;
            }
            else
            {
                if (_log.isDebugEnabled())
                    _log.debug("JBossUserPrincipal: " + _principal + " is NOT in Role: " + role);
            }

            return isUserInRole;

        }
    }
    
    
    public JBossIdentityService (RealmMapping realmMapping, String realmName)
    {
        _realmMapping = realmMapping;
        _log = Logger.getLogger(JBossIdentityService.class.getName() + "#"+ realmName);
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
        
        SecurityAssociation.setPrincipal(((JBossUserIdentity)user).getUserPrincipal());
        SecurityAssociation.setCredential(((JBossUserIdentity)user).getSubject().getPrivateCredentials().toArray()[0]);
        SecurityAssociation.setSubject(((JBossUserIdentity)user).getSubject());
        
        return user;
    }

    public void disassociate(UserIdentity scoped)
    {
        SecurityAssociation.clear();
    }

    public RoleRunAsToken associateRunAs(UserIdentity identity, RunAsToken token)
    {
        String role = ((RoleRunAsToken)token).getRunAsRole();
        String user = (identity==null?null:identity.getUserPrincipal().getName());
        RunAsIdentity runAs = new RunAsIdentity(role, user);
        SecurityAssociation.pushRunAsIdentity(runAs);
        return null;
    }

    public void disassociateRunAs(RoleRunAsToken lastToken)
    {
    }
    
    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles)
    {
        return new JBossUserIdentity(subject, userPrincipal);
    }

}

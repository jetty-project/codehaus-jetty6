package org.jboss.jetty.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import javax.security.auth.Subject;

import org.eclipse.jetty.security.DefaultIdentityService;
import org.eclipse.jetty.security.RoleRunAsToken;
import org.eclipse.jetty.security.RunAsToken;
import org.eclipse.jetty.server.UserIdentity;
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
            //No equivalent method on JBoss - not needed anyway
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

        public boolean isUserInRole(String role, UserIdentity.Scope scope)
        {
            if (_log.isDebugEnabled()) _log.debug("Checking role "+role+" for user "+_principal.getName());
            boolean isUserInRole = false;
            Set requiredRoles = Collections.singleton(new SimplePrincipal(role));
            if (_realmMapping != null && _realmMapping.doesUserHaveRole(this._principal,requiredRoles))
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
    
    
    public JBossIdentityService (String realmName)
    {
        _log = Logger.getLogger(JBossIdentityService.class.getName() + "#"+ realmName);
    }
    
    public void setRealmMapping (RealmMapping realmMapping)
    {
        _realmMapping = realmMapping;
    }
    
   
    @Override
    public Object associate(UserIdentity user)
    {
        if (user == null)
        {
            if (_log.isDebugEnabled()) _log.debug("Disassociating user "+user);
            SecurityAssociation.clear();
        }
        else
        { 
            if (_log.isDebugEnabled()) _log.debug("Associating user "+user);
            SecurityAssociation.setPrincipal(user.getUserPrincipal());
            SecurityAssociation.setCredential(user.getSubject().getPrivateCredentials());
            SecurityAssociation.setSubject(user.getSubject());          
        }
        return user;
    }
    
    @Override
    public void disassociate(Object previous)
    {
        if (_log.isDebugEnabled()) _log.debug("Disassociating user "+previous);
        SecurityAssociation.clear();
    }

    public Object setRunAs(UserIdentity identity, RunAsToken token)
    {
        String role = ((RoleRunAsToken)token).getRunAsRole();
        String user = (identity==null?null:identity.getUserPrincipal().getName());
        RunAsIdentity runAs = new RunAsIdentity(role, user);
        SecurityAssociation.pushRunAsIdentity(runAs);
        return null;
    }

    public void unsetRunAs(Object lastToken)
    {
        SecurityAssociation.popRunAsIdentity();
    }
    
    public UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles)
    {
        if (_log.isDebugEnabled()) _log.debug("Creating new JBossUserIdentity for user "+userPrincipal.getName());
        return new JBossUserIdentity(subject, userPrincipal);
    }

}

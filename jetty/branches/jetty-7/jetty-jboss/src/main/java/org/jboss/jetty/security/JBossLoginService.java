package org.jboss.jetty.security;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.Collections;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.security.auth.Subject;

import org.eclipse.jetty.security.IdentityService;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.server.UserIdentity;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.jboss.logging.Logger;
import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
import org.jboss.security.SubjectSecurityManager;


public class JBossLoginService extends AbstractLifeCycle implements LoginService
{
    private String _realmName;
    private final Logger _log;
    protected JBossIdentityService _identityService;
    private AuthenticationManager _authMgr;
    private RealmMapping _realmMapping;
    private SubjectSecurityManager _subjSecMgr;

    /* ------------------------------------------------------------ */
    public JBossLoginService (String realmName)
    {
        _realmName = realmName;
        _log = Logger.getLogger(JBossLoginService.class.getName() + "#"+ _realmName);
        _identityService = new JBossIdentityService (_realmName);
    }

    /* ------------------------------------------------------------ */
    public IdentityService getIdentityService()
    {
        return _identityService;
    }

    /* ------------------------------------------------------------ */
    public String getName()
    {
        return _realmName;
    }

    /* ------------------------------------------------------------ */
    public UserIdentity login(String username, Object credentials)
    {        
        if (_log.isDebugEnabled()) _log.debug("authenticating: Name:" + username + " Password:****"/* +credentials */);
      
        UserIdentity identity = null;
        
        if (credentials == null || credentials instanceof java.lang.String)
        {
            if (credentials == null) credentials = "";
            char[] passwordChars = ((String)credentials).toCharArray();
            Principal principal = new SimplePrincipal(username);
            Subject subject = new Subject(false,Collections.singleton(principal),Collections.emptySet(),Collections.singleton(passwordChars));
            if (_subjSecMgr != null && _subjSecMgr.isValid(principal, passwordChars, subject))
            {
                if (_log.isDebugEnabled())
                    _log.debug("authenticated: " + username);

                // TODO what about roles?
                identity =_identityService.newUserIdentity(subject,principal,null);
            }
           
        }
        else if (credentials instanceof X509Certificate[])
        {
            //JBossUserRealm makes a username out of the credential info
            X509Certificate[] certs = (X509Certificate[]) credentials;
            
            StringBuffer buff = new StringBuffer();
            String serialNumber = certs[0].getSerialNumber().toString(16).toUpperCase();
            if (serialNumber.length() % 2 != 0) buff.append("0");
            buff.append(serialNumber);
            buff.append(" ");
            buff.append(certs[0].getIssuerDN().toString());
            String pname = buff.toString();
            
            Principal principal = new SimplePrincipal(pname);
            Subject subject = new Subject(false,Collections.singleton(principal),Collections.emptySet(),Collections.singleton(certs));
            if (_subjSecMgr != null && _subjSecMgr.isValid(principal, certs, subject))
            {
                if (_log.isDebugEnabled())
                    _log.debug("authenticated: " + principal);
                // TODO what about roles?
                identity =_identityService.newUserIdentity(subject,principal,null);
            }
        }
        
        if (identity == null)
        {
            _log.warn("authentication failure: " + username);
        }
        
        
        return identity;
    }

    /* ------------------------------------------------------------ */
    public void  logout(UserIdentity id) 
    {
        // TODO
    }

    /* ------------------------------------------------------------ */
    public boolean validate(UserIdentity user)
    {
        // TODO is this right?
        return _subjSecMgr.isValid(user.getUserPrincipal(),
                user.getSubject().getPrivateCredentials().iterator().next(),
                user.getSubject());
    }


    /* ------------------------------------------------------------ */
    public void setIdentityService(IdentityService service)
    {
        if (service instanceof JBossIdentityService)
            _identityService = (JBossIdentityService)service;
        else
            throw new IllegalArgumentException ("IdentityService must be instanceof JBossIdentityService");
    }

    /* ------------------------------------------------------------ */
    public void doStart() throws Exception
    {
        try
        {
            InitialContext iniCtx = new InitialContext();
            Context securityCtx = (Context) iniCtx.lookup("java:comp/env/security");
            _authMgr = (AuthenticationManager) securityCtx.lookup("securityMgr"); 
            _realmMapping = (RealmMapping) securityCtx.lookup("realmMapping");
            _identityService.setRealmMapping(_realmMapping);
            iniCtx = null;

            if (_authMgr instanceof SubjectSecurityManager)
                _subjSecMgr = (SubjectSecurityManager) _authMgr;
        }
        catch (NamingException e)
        {
            _log.error("java:comp/env/security does not appear to be correctly set up", e);
        }
        
        super.doStart();
    }

    public String toString()
    {
        return "JBossLoginService: "+_realmName;
    }
}

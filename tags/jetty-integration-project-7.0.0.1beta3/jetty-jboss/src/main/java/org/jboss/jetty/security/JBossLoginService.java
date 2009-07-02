package org.jboss.jetty.security;

import java.security.Principal;
import java.security.cert.X509Certificate;

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
    
    public JBossLoginService (String realmName)
    {
        _realmName = realmName;
        _log = Logger.getLogger(JBossLoginService.class.getName() + "#"+ _realmName);
        _identityService = new JBossIdentityService (_realmName);
    }
    
    public IdentityService getIdentityService()
    {
        return _identityService;
    }

    public String getName()
    {
        return _realmName;
    }

    public UserIdentity login(String username, Object credentials)
    {        
        
        if (_log.isDebugEnabled()) _log.debug("authenticating: Name:" + username + " Password:****"/* +credentials */);
      
        UserIdentity identity = null;
        
        if (credentials == null || credentials instanceof java.lang.String)
        {
            Subject subjectCopy = new Subject();
            if (credentials == null) credentials = "";
            char[] passwordChars = ((String)credentials).toCharArray();
            Principal principal = new SimplePrincipal(username);
            if (_subjSecMgr != null && _subjSecMgr.isValid(principal, passwordChars, subjectCopy))
            {
                if (_log.isDebugEnabled())
                    _log.debug("authenticated: " + username);
                
                identity =_identityService.newUserIdentity(subjectCopy,principal,null);
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
           
            Subject subjectCopy = new Subject();
            if (_subjSecMgr != null && _subjSecMgr.isValid(principal, certs, subjectCopy))
            {
                if (_log.isDebugEnabled())
                    _log.debug("authenticated: " + principal);
   
                identity =_identityService.newUserIdentity(subjectCopy,principal,null);
            }
        }
        
        if (identity == null)
        {
            _log.warn("authentication failure: " + username);
        }
        
        
        return identity;
    }
    

    public void logout(UserIdentity user)
    {
        try
        {
            java.util.ArrayList servers = MBeanServerFactory.findMBeanServer(null);
            if (servers.size() != 1)
                _log.warn("More than one MBeanServer found, choosing first");
            MBeanServer server = (MBeanServer) servers.get(0);

            server.invoke(new ObjectName("jboss.security:service=JaasSecurityManager"),
                                         "flushAuthenticationCache",
                                         new Object[] { user.getUserPrincipal().getName(), user.getUserPrincipal() }, 
                                         new String[] {"java.lang.String", "java.security.Principal" });
        }
        catch (Exception e)
        {
            _log.error(e);
        }
        catch (Error err)
        {
            _log.error(err);
        }
    }
    
    
    public void setIdentityService(IdentityService service)
    {
        if (service instanceof JBossIdentityService)
            _identityService = (JBossIdentityService)service;
        else
            throw new IllegalArgumentException ("IdentityService must be instanceof JBossIdentityService");
    }


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

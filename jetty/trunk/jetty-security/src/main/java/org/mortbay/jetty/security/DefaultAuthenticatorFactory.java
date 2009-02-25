package org.mortbay.jetty.security;

import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;

import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.security.Authenticator.Configuration;
import org.mortbay.jetty.security.authentication.BasicAuthenticator;
import org.mortbay.jetty.security.authentication.ClientCertAuthenticator;
import org.mortbay.jetty.security.authentication.DigestAuthenticator;
import org.mortbay.jetty.security.authentication.FormAuthenticator;
import org.mortbay.jetty.security.authentication.LazyAuthenticator;
import org.mortbay.jetty.security.authentication.SessionCachingAuthenticator;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.util.log.Log;

/* ------------------------------------------------------------ */
/**
 * The Default Authenticator Factory.
 * Uses the {@link Configuration#getAuthMethod()} to select an {@link Authenticator} from: <ul>
 * <li>{@link BasicAuthenticator}</li>
 * <li>{@link DigestAuthenticator}</li>
 * <li>{@link FormAuthenticator}</li>
 * <li>{@link ClientCertAuthenticator}</li>
 * </ul>
 * If {@link Configuration#isLazy()} is true, the Authenticator is wrapped with a {@link LazyAuthenticator}
 * instance. The FormAuthenticator is always wrapped in a {@link SessionCachingAuthenticator}.
 * <p>
 * If a {@link LoginService} has not been set on this factory, then
 * the service is selected by searching the {@link Server#getBeans(Class)} results for
 * a service that matches the realm name, else the first LoginService found is used.
 *
 */
public class DefaultAuthenticatorFactory implements Authenticator.Factory
{
    LoginService _loginService;
    
    public Authenticator getAuthenticator(Server server, ServletContext context, Configuration configuration)
    {
        return newAuthenticator(server,context,configuration,findLoginService(server,context,configuration));
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the loginService
     */
    public LoginService getLoginService()
    {
        return _loginService;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param loginService the loginService to set
     */
    public void setLoginService(LoginService loginService)
    {
        _loginService = loginService;
    }

    /* ------------------------------------------------------------ */
    /**
     * If {@link #setLoginService(LoginService)} has set a service then return
     * that service, otherwise
     * the {@link Server#getBeans(Class)} method is used to find a {@link LoginService}
     * instance, either by matching {@link Configuration#getRealmName()} or by
     * picking the first found.
     */
    protected LoginService findLoginService(Server server, ServletContext context, Configuration configuration)
    {
        if (_loginService!=null)
            return _loginService;
        
        // find a login service
        final String realm = configuration.getRealmName();
        
        LoginService login=null;
        List beans=server.getBeans(LoginService.class);
        for (Iterator iter=beans.iterator();realm!=null&&login==null&& iter.hasNext();)
        {
            LoginService ls = (LoginService)iter.next();
            if (realm.equals(ls.getName()))
                login=ls;        
        }
        if (login==null && beans.size()>0)
            login=(LoginService)beans.get(0);
    
        if (login==null && realm!=null)
        {
            Log.warn("No LoginService found for "+this+" realm="+realm);
        }
        
        return login;
    }
        

    /* ------------------------------------------------------------ */
    /**
     * Use the configuration instance to create an Authentication instance.
     * If no authMethod is available and there is a loginService, then BASIC
     * authentication will be used, else null will be returned.
     * @param server
     * @param context
     * @param configuration
     * @param loginService
     * @return The constructed and configured {@link Authenticator}
     */
    protected Authenticator newAuthenticator(Server server, ServletContext context, Configuration configuration, LoginService loginService)
    {
        String auth=configuration.getAuthMethod();
        Authenticator authenticator=null;
        
        if (auth==null || Constraint.__BASIC_AUTH.equalsIgnoreCase(auth))
            authenticator=new BasicAuthenticator(loginService);
        else if (Constraint.__DIGEST_AUTH.equalsIgnoreCase(auth))
            authenticator=new DigestAuthenticator(loginService);
        else if (Constraint.__FORM_AUTH.equalsIgnoreCase(auth))
            authenticator=new SessionCachingAuthenticator(new FormAuthenticator(configuration.getInitParameter(FormAuthenticator.__FORM_LOGIN_PAGE),
                    configuration.getInitParameter(FormAuthenticator.__FORM_ERROR_PAGE),loginService));
        if (Constraint.__CERT_AUTH.equalsIgnoreCase(auth)||Constraint.__CERT_AUTH2.equalsIgnoreCase(auth))
            authenticator=new ClientCertAuthenticator(loginService);
        
        if (configuration.isLazy())
            authenticator=new LazyAuthenticator(authenticator);
        
        return authenticator;
    }
}

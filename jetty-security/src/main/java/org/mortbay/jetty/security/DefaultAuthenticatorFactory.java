package org.mortbay.jetty.security;

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
        String auth=configuration.getAuthMethod();
        Authenticator authenticator=null;
        
        if (auth==null || Constraint.__BASIC_AUTH.equalsIgnoreCase(auth))
            authenticator=new BasicAuthenticator();
        else if (Constraint.__DIGEST_AUTH.equalsIgnoreCase(auth))
            authenticator=new DigestAuthenticator();
        else if (Constraint.__FORM_AUTH.equalsIgnoreCase(auth))
            authenticator=new SessionCachingAuthenticator(new FormAuthenticator());
        if (Constraint.__CERT_AUTH.equalsIgnoreCase(auth)||Constraint.__CERT_AUTH2.equalsIgnoreCase(auth))
            authenticator=new ClientCertAuthenticator();
        
        if (configuration.isLazy() && authenticator!=null)
            authenticator=new LazyAuthenticator(authenticator);
        
        return authenticator;
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

}

package org.mortbay.jetty.security.jaspi;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.servlet.ServletContext;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.DefaultAuthenticatorFactory;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServletCallbackHandler;
import org.mortbay.jetty.security.Authenticator.Configuration;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.util.log.Log;

public class JaspiAuthenticatorFactory extends DefaultAuthenticatorFactory
{
    private static String MESSAGE_LAYER = "HTTP";
    
    private Subject _serviceSubject;
    private String _serverName;
    

    /* ------------------------------------------------------------ */
    /**
     * @return the serviceSubject
     */
    public Subject getServiceSubject()
    {
        return _serviceSubject;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param serviceSubject the serviceSubject to set
     */
    public void setServiceSubject(Subject serviceSubject)
    {
        _serviceSubject = serviceSubject;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the serverName
     */
    public String getServerName()
    {
        return _serverName;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param serverName the serverName to set
     */
    public void setServerName(String serverName)
    {
        _serverName = serverName;
    }

    /* ------------------------------------------------------------ */
    protected Authenticator newAuthenticator(Server server, ServletContext context, Configuration configuration, LoginService loginService)
    {
        Authenticator authenticator=null;
        try 
        {
            AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
            RegistrationListener listener = new RegistrationListener()
            {
                public void notify(String layer, String appContext)
                {}
            };

            Subject serviceSubject=findServiceSubject(server);
            String serverName=findServerName(server,serviceSubject);
            
            
            String appContext = serverName + " " + context.getContextPath();
            AuthConfigProvider authConfigProvider = authConfigFactory.getConfigProvider(MESSAGE_LAYER,appContext,listener);
            if (authConfigProvider != null)
            {
                ServletCallbackHandler servletCallbackHandler = new ServletCallbackHandler(loginService);
                ServerAuthConfig serverAuthConfig = authConfigProvider.getServerAuthConfig(MESSAGE_LAYER,appContext,servletCallbackHandler);
                if (serverAuthConfig != null)
                {
                    Map map = new HashMap();
                    for (String key : configuration.getInitParameterNames())
                        map.put(key,configuration.getInitParameter(key));
                    authenticator= new JaspiAuthenticator(appContext,serverAuthConfig,map,servletCallbackHandler,
                                serviceSubject,
                                configuration.isLazy());
                }
            }
        } 
        catch (AuthException e) 
        {
            Log.warn(e);
        }
        return authenticator;
    }

    /* ------------------------------------------------------------ */
    /** Find a service Subject.
     * If {@link #setServiceSubject(Subject)} has not been used to 
     * set a subject, then the {@link Server#getBeans(Class)} method is
     * used to look for a Subject.
     */
    protected Subject findServiceSubject(Server server)
    {
        if (_serviceSubject!=null)
            return _serviceSubject;
        List subjects = server.getBeans(Subject.class);
        if (subjects.size()>0)
            return (Subject)subjects.get(0);
        return null;
    }

    /* ------------------------------------------------------------ */
    /** Find a servername.
     * If {@link #setServerName(String)} has not been called, then
     * use the name of the a principal in the service subject.
     * If not found, return "server".
     */
    protected String findServerName(Server server, Subject subject)
    {
        if (_serverName!=null)
            return _serverName;
        if (subject!=null)
        {
            Set<Principal> principals = subject.getPrincipals();
            if (principals!=null && !principals.isEmpty())
                return principals.iterator().next().getName();
        }
        
        return "server";
    }
}

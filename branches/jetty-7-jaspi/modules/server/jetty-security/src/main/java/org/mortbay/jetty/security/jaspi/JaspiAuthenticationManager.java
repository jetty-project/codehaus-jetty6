package org.mortbay.jetty.security.jaspi;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;

import org.mortbay.jetty.security.AbstractAuthenticationManager;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthentication;
import org.mortbay.jetty.security.ServletCallbackHandler;

public class JaspiAuthenticationManager extends AbstractAuthenticationManager
{
    private static String MESSAGE_LAYER = "HTTP";
    private ServerAuthentication _serverAuthentication;
    private String appContext;
    private Map authConfigProperties;
    private Subject serviceSubject;
    
    public JaspiAuthenticationManager ()
    {}

    public void setAppContext(String appContext) {
        this.appContext = appContext;
    }

    public void setAuthConfigProperties(Map authConfigProperties) {
        this.authConfigProperties = authConfigProperties;
    }

    public void setServiceSubject(Subject serviceSubject) {
        this.serviceSubject = serviceSubject;
    }

    protected void doStart() throws Exception
    {
        super.doStart();

        AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
        RegistrationListener listener = new RegistrationListener()
        {

            public void notify(String layer, String appContext)
            {}

        };
        AuthConfigProvider authConfigProvider = authConfigFactory.getConfigProvider(MESSAGE_LAYER, appContext, listener);
        LoginService loginService = (LoginService)getSecurityHandler().getUserRealm();
        ServletCallbackHandler servletCallbackHandler = new ServletCallbackHandler(loginService);
        ServerAuthConfig serverAuthConfig = authConfigProvider.getServerAuthConfig(MESSAGE_LAYER, appContext, servletCallbackHandler);
        //TODO appContext is supposed to be server-name<space>context-root
        _serverAuthentication = new JaspiServerAuthentication(appContext, serverAuthConfig, authConfigProperties, servletCallbackHandler, serviceSubject, getAllowLazyAuth());
    }

    protected void doStop() throws Exception
    {
        super.doStop();
    }

    public ServerAuthentication getServerAuthentication() {
        return _serverAuthentication;
    }
}

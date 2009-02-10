package org.mortbay.jetty.security.jaspi;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.RegistrationListener;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.AuthException;

import org.mortbay.jetty.security.DefaultAuthenticationManager;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServletCallbackHandler;

public class JaspiAuthenticationManager extends DefaultAuthenticationManager
{
    private static String MESSAGE_LAYER = "HTTP";

    public JaspiAuthenticationManager ()
    {}

    protected void doStart() throws Exception
    {
        try {
            AuthConfigFactory authConfigFactory = AuthConfigFactory.getFactory();
            RegistrationListener listener = new RegistrationListener()
            {

                public void notify(String layer, String appContext)
                {}

            };
            String appContext = getServerName() + " " + getContextRoot();
            AuthConfigProvider authConfigProvider = authConfigFactory.getConfigProvider(MESSAGE_LAYER, appContext, listener);
            if (authConfigProvider != null) {
                LoginService loginService = (LoginService)getSecurityHandler().getUserRealm();
                ServletCallbackHandler servletCallbackHandler = new ServletCallbackHandler(loginService);
                ServerAuthConfig serverAuthConfig = authConfigProvider.getServerAuthConfig(MESSAGE_LAYER, appContext, servletCallbackHandler);
                if (serverAuthConfig != null) {
                    _serverAuthentication = new JaspiServerAuthentication(appContext, serverAuthConfig, getAuthConfigProperties(), servletCallbackHandler, getServiceSubject(), getAllowLazyAuth());
                }
            }
        } catch (AuthException e) {
            //log??
        }
        if (_serverAuthentication == null) {
            super.doStart();
        }
    }

}

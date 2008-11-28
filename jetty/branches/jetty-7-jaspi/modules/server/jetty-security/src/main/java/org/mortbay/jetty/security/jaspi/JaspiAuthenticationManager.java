package org.mortbay.jetty.security.jaspi;

import javax.security.auth.message.ServerAuth;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

import org.mortbay.jetty.handler.SecurityHandler;
import org.mortbay.jetty.security.AbstractAuthenticationManager;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.ServerAuthentication;
import org.mortbay.jetty.security.ServletCallbackHandler;
import org.mortbay.jetty.security.jaspi.modules.BasicAuthModule;
import org.mortbay.jetty.security.jaspi.modules.ClientCertAuthModule;
import org.mortbay.jetty.security.jaspi.modules.DigestAuthModule;
import org.mortbay.jetty.security.jaspi.modules.FormAuthModule;

public class JaspiAuthenticationManager extends AbstractAuthenticationManager
{
    private ServerAuthentication _serverAuthentication;
    private ServerAuthContext _serverAuthContext;
    private ServerAuth _serverAuth;
    
    public JaspiAuthenticationManager ()
    {}
    
    public void setServerAuth (ServerAuth serverAuth)
    {
        //TODO set a jaspi auth module to use, possibly third party
        _serverAuth = serverAuth;
    }
    
    
    protected void doStart() throws Exception
    {
        super.doStart();

        if (getAuthMethod() != null && !"".equals(getAuthMethod()))
        {
            LoginService loginService = (LoginService)getSecurityHandler().getUserRealm();

            ServletCallbackHandler callbackHandler=new ServletCallbackHandler(loginService);

            //allow a jaspi auth module to be plugged in - only use our defaults if we
            //haven't been given one to use
            if (_serverAuth == null)
            {
                if (Constraint.__FORM_AUTH.equals(getAuthMethod()))
                {
                    _serverAuthContext = new FormAuthModule(callbackHandler, getLoginPage(), getErrorPage());
                } 
                else if (Constraint.__BASIC_AUTH.equals(getAuthMethod()))
                {
                    _serverAuthContext = new BasicAuthModule(callbackHandler, loginService.getName());
                } 
                else if (Constraint.__DIGEST_AUTH.equals(getAuthMethod()))
                {
                    _serverAuthContext = new DigestAuthModule(callbackHandler, loginService.getName());
                } 
                else if (Constraint.__CERT_AUTH.equals(getAuthMethod()) ||
                        Constraint.__CERT_AUTH2.equals(getAuthMethod()))
                {
                    //TODO figure out how to configure max handshake?
                    _serverAuthContext = new ClientCertAuthModule(callbackHandler);
                }           
                else
                    throw new IllegalStateException ("Unrecognized auth method: "+getAuthMethod());
            }

            //TODO - how does the ServerAuth module relate to the ServerAuthContext? Can we plug in a
            //3rd party jaspi module by passing it to our JaspiServerAuthentication?

            String appContext = null;
            ServerAuthConfig serverAuthConfig = new SimpleAuthConfig(appContext, _serverAuthContext);
            _serverAuthentication = new JaspiServerAuthentication(appContext,
                                                                  serverAuthConfig,
                                                                  null,
                                                                  callbackHandler,
                                                                  //TODO??
                                                                  null,
                                                                  getAllowLazyAuth());
        }
    }

    protected void doStop() throws Exception
    {
        super.doStop();
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) 
    throws ServerAuthException
    {
        if (_serverAuthentication == null)
            return null;
        
        return _serverAuthentication.secureResponse(messageInfo, validatedUser);
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) 
    throws ServerAuthException
    {
        if (_serverAuthentication == null)
            return null;
        
       return _serverAuthentication.validateRequest(messageInfo);
    }
}

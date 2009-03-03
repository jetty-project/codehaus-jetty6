package org.mortbay.jetty.security.authentication;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.IdentityService;
import org.mortbay.jetty.security.LoginService;

public abstract class LoginAuthenticator implements Authenticator
{
    protected LoginService _loginService;
    protected IdentityService _identityService;

    protected LoginAuthenticator()
    {
    }

    public void setConfiguration(Configuration configuration)
    {
        _loginService=configuration.getLoginService();
        if (_loginService==null)
            throw new IllegalStateException("No LoginService for "+this);
        _identityService=configuration.getIdentityService();
        if (_identityService==null)
            throw new IllegalStateException("No IdentityService for "+this);
    }
    
    public LoginService getLoginService()
    {
        return _loginService;
    }

}
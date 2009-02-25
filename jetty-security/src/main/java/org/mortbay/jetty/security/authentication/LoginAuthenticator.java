package org.mortbay.jetty.security.authentication;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.LoginService;

public abstract class LoginAuthenticator implements Authenticator
{
    protected final LoginService _loginService;

    public LoginAuthenticator(LoginService loginService)
    {
        _loginService=loginService;
    }

    public LoginService getLoginService()
    {
        return _loginService;
    }

}
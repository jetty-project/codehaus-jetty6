package org.mortbay.jetty.security.authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.Authentication;

public class DelegateAuthenticator implements Authenticator
{
    protected final Authenticator _delegate;

    public String getAuthMethod()
    {
        return _delegate.getAuthMethod();
    }
    
    public DelegateAuthenticator(Authenticator delegate)
    {
        _delegate=delegate;
    }

    public Authenticator getDelegate()
    {
        return _delegate;
    }

    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean manditory) throws ServerAuthException
    {
        return _delegate.validateRequest(request, response, manditory);
    }

    public Authentication.Status secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication validatedUser) throws ServerAuthException
    {
        return _delegate.secureResponse(req,res, mandatory, validatedUser);
    }
    
}
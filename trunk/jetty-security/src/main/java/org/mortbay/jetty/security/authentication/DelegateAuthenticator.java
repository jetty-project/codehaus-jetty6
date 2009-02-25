package org.mortbay.jetty.security.authentication;

import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.Authenticator;

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

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException
    {
        return _delegate.secureResponse(messageInfo,validatedUser);
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        return _delegate.validateRequest(messageInfo);
    }
    
}
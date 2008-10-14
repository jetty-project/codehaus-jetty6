package org.mortbay.jetty.handler;

import org.mortbay.jetty.Authenticator;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HandlerContainer;
import org.mortbay.jetty.UserRealm;

public interface SecurityHandler extends Handler, HandlerContainer
{
    void setHandler(Handler handler);
    Handler getHandler();

    Authenticator getAuthenticator();

    UserRealm getUserRealm();
}

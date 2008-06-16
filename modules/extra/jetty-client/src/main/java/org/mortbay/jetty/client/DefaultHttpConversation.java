package org.mortbay.jetty.client;

import org.mortbay.jetty.client.security.BasicAuthentication;
import org.mortbay.jetty.client.security.DigestAuthentication;

public class DefaultHttpConversation extends HttpConversation
{
    public DefaultHttpConversation()
    {
        enableAuthentication( new BasicAuthentication() );
        enableAuthentication( new DigestAuthentication() );
    }
}

package org.mortbay.jetty.client;

public class AsyncSslSecurityListenerTest extends SslSecurityListenerTest
{

    protected void setUp() throws Exception
    {
        _type = HttpClient.CONNECTOR_SELECT_CHANNEL;
        super.setUp();
    }

}
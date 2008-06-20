package org.mortbay.jetty.client;

public class AsyncSslHttpExchangeTest extends SslHttpExchangeTest
{

    protected void setUp() throws Exception
    {
        startServer();
        _httpClient=new HttpClient();
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setMaxConnectionsPerAddress(2);
        _httpClient.start();
    }

}
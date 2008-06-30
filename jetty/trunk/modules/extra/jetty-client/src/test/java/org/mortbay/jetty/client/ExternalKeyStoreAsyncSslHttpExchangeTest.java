package org.mortbay.jetty.client;

import java.io.File;

public class ExternalKeyStoreAsyncSslHttpExchangeTest extends SslHttpExchangeTest
{

    protected void setUp() throws Exception
    {
        _scheme="https://";
        startServer();
        _httpClient=new HttpClient();
        _httpClient.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        _httpClient.setMaxConnectionsPerAddress(2);

        String keystore = System.getProperty("user.dir") + File.separator + "src" + File.separator + "test" + File.separator + "resources" + File.separator
                + "keystore";

        _httpClient.setKeyStoreLocation( keystore );
        _httpClient.start();
    }

}
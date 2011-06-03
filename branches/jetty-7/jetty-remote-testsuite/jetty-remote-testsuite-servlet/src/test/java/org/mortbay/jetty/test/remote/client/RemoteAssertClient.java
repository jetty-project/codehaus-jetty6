package org.mortbay.jetty.test.remote.client;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.json.JSONException;

public class RemoteAssertClient extends SimpleRequest
{
    public RemoteAssertClient(URI serverURI) throws UnknownHostException
    {
        super(serverURI);
    }

    public RemoteAssertResults getResults(String relativePath) throws IOException, JSONException
    {
        return new RemoteAssertResults(getString(relativePath));
    }
}

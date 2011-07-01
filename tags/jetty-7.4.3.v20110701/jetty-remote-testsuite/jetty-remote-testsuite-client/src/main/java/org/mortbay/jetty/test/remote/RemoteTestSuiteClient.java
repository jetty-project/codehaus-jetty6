package org.mortbay.jetty.test.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.UnknownHostException;

import org.eclipse.jetty.toolchain.test.IO;
import org.json.JSONException;

public class RemoteTestSuiteClient
{
    private URI baseUri;

    public RemoteTestSuiteClient(URI serverURI) throws UnknownHostException
    {
        this.baseUri = serverURI;
    }

    public RemoteTestSuiteResults getResults(String relativePath) throws IOException, JSONException
    {
        URI uri = this.baseUri.resolve(relativePath);
        System.out.println("GET (Remote TestSuite Results): " + uri.toASCIIString());

        InputStream in = null;
        InputStreamReader reader = null;
        HttpURLConnection connection = null;

        try
        {
            connection = (HttpURLConnection)uri.toURL().openConnection();
            connection.connect();
            if (HttpURLConnection.HTTP_OK != connection.getResponseCode())
            {
                String body = getPotentialBody(connection);
                String err = String.format("GET request failed (%d %s) %s%n%s",connection.getResponseCode(),connection.getResponseMessage(),
                        uri.toASCIIString(),body);
                throw new IOException(err);
            }
            in = connection.getInputStream();
            reader = new InputStreamReader(in);
            StringWriter writer = new StringWriter();
            IO.copy(reader,writer);
            return new RemoteTestSuiteResults(writer.toString());
        }
        finally
        {
            IO.close(reader);
            IO.close(in);
        }
    }

    /**
     * Attempt to obtain the body text if available. Do not throw an exception if body is unable to be fetched.
     * 
     * @param connection
     *            the connection to fetch the body content from.
     * @return the body content, if present.
     */
    private String getPotentialBody(HttpURLConnection connection)
    {
        InputStream in = null;
        InputStreamReader reader = null;
        try
        {
            in = connection.getInputStream();
            reader = new InputStreamReader(in);
            StringWriter writer = new StringWriter();
            IO.copy(reader,writer);
            return writer.toString();
        }
        catch (IOException e)
        {
            return "<no body:" + e.getMessage() + ">";
        }
        finally
        {
            IO.close(reader);
            IO.close(in);
        }
    }

}

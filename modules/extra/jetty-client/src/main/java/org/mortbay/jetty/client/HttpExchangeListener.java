package org.mortbay.jetty.client;

import org.mortbay.io.Buffer;

import java.io.IOException;


public interface HttpExchangeListener
{

    public void onRequestCommitted() throws IOException;


    public void onRequestComplete() throws IOException;


    public void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException;


    public void onResponseHeader(Buffer name, Buffer value) throws IOException;

    public void onResponseHeaderComplete() throws IOException;

    public void onResponseContent(Buffer content) throws IOException;


    public void onResponseComplete() throws IOException;


    public void onConnectionFailed(Throwable ex);


    public void onException(Throwable ex);


    public void onExpire();

}

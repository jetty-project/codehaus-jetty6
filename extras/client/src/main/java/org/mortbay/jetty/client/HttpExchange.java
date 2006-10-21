package org.mortbay.jetty.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.SimpleBuffers;
import org.mortbay.io.bio.StringEndPoint;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.MimeTypes;

/**
 * VERY rough start to a client API - inpired by javascript XmlHttpRequest.
 * 
 * @author gregw
 * 
 */
public class HttpExchange
{
    /* ------------------------------------------------------------ */
    public static void main(String[] args) throws IOException
    {
        // This is an example of how the API can be used.

        // this is the API that will be used.
        HttpExchange ex=new HttpExchange()
        {
            protected void onProgress(Buffer content, boolean last)
            {
                System.err.println("Response content="+content);
                super.onProgress(content,last);
            }

            protected void onSuccess(int status)
            {
                System.err.println("Response status="+status);
                super.onSuccess(status);
            }
        };

        // Set the destination
        ex.setURL("http://localhost:8080/test/dump/info");

        ex.setMethod("GET"); // Optional. GET by default
        ex.setHost("localhost"); // May use setURL instead
        ex.setPort(8080); // Optional. 80 by default
        ex.setURI("/test/dump/info");
        ex.setRequestContentType(MimeTypes.FORM_ENCODED);
        ex.addRequestHeader("arbitrary","value");
        ex.setRequestContent(new ByteArrayBuffer("param=value\n"));

        ex.send();
        ex.send(true); // pipelined on last used connection

    }

    String _host;
    String _method;
    int _port;
    Buffer _requestContent;
    HttpFields _requestFields=new HttpFields();

    Buffer _responseContent;
    HttpFields _responseFields=new HttpFields();
    String _scheme;
    String _uri;

    public void addRequestHeader(String name, String value)
    {
        getRequestFields().add(name,value);
    }

    public String getHost()
    {
        return _host;
    }

    public String getMethod()
    {
        return _method;
    }

    public int getPort()
    {
        return _port;
    }

    public Buffer getRequestContent()
    {
        return _requestContent;
    }

    public HttpFields getRequestFields()
    {
        return _requestFields;
    }

    public Buffer getResponseContent()
    {
        return _responseContent;
    }

    public String getScheme()
    {
        return _scheme;
    }

    public String getURI()
    {
        return _uri;
    }

    public boolean isIdemPotent()
    {
        return !HttpMethods.POST.equals(_method);
    }

    public boolean isStreaming()
    {
        return false;
    }

    public void send()
    {
        // TODO Auto-generated method stub

    }

    public void send(boolean b)
    {
        // TODO Auto-generated method stub

    }

    public void setHost(String host)
    {
        _host=host;
        if (_host!=null)
        {
            if (_port>0)
                _requestFields.put(HttpHeaders.HOST_BUFFER,_host+":"+_port);
            else
                _requestFields.put(HttpHeaders.HOST_BUFFER,host);
        }
        else
            _requestFields.remove(HttpHeaders.HOST_BUFFER);
    }

    public void setMethod(String method)
    {
        _method=method;
    }

    public void setPort(int port)
    {
        _port=port;
        if (_host!=null&&_port>0)
            _requestFields.put(HttpHeaders.HOST_BUFFER,_host+":"+port);
    }

    public void setRequestContent(Buffer requestContent)
    {
        _requestContent=requestContent;
    }

    public void setRequestContentType(String value)
    {
        getRequestFields().put(HttpHeaders.CONTENT_TYPE_BUFFER,value);
    }

    public void setResponseContent(Buffer responseContent)
    {
        _responseContent=responseContent;
    }

    public void setScheme(String scheme)
    {
        _scheme=scheme;
    }

    public void setURI(String uri)
    {
        _uri=uri;
    }

    public void setURL(String url)
    {
        HttpURI uri=new HttpURI(url);
        String scheme=uri.getScheme();
        if (scheme!=null)
            setScheme(scheme);
        String host=uri.getHost();
        if (host!=null)
            setHost(host);
        int port=uri.getPort();
        if (port>0)
            setPort(port);
        String completePath=uri.getCompletePath();
        if (completePath!=null)
            setURI(completePath);
    }

    protected void onProgress(Buffer content, boolean last)
    {
        // TODO Auto-generated method stub

    }

    protected void onSuccess(int status)
    {
        // TODO Auto-generated method stub

    }

    protected boolean refillRequestContent()
    {
        return _requestContent!=null&&_requestContent.hasContent();
    }

}

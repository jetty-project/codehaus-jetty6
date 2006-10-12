package org.mortbay.jetty.client;

import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.SimpleBuffers;
import org.mortbay.io.bio.StringEndPoint;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpHeaderValues;
import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.MimeTypes;

/**
 * VERY rough start to a client API
 * 
 * @author gregw
 *
 */
public class Exchange
{
    String _method;
    String _uri;
    HttpFields _requestFields = new HttpFields();
    HttpFields _responseFields = new HttpFields();
    Buffer _requestContent;
    Buffer _responseContent;
    
    public Buffer getRequestContent()
    {
        return _requestContent;
    }

    public void setRequestContent(Buffer requestContent)
    {
        _requestContent=requestContent;
    }

    public Buffer getResponseContent()
    {
        return _responseContent;
    }

    public void setResponseContent(Buffer responseContent)
    {
        _responseContent=responseContent;
    }

    public boolean isIdemPotent()
    {
        return !HttpMethods.POST.equals(_method);
    }
    
    public String getMethod()
    {
        return _method;
    }
    
    public void setMethod(String method)
    {
        _method=method;
    }
    
    public HttpFields getRequestFields()
    {
        return _requestFields;
    }
    
    public void setRequestFields(HttpFields requestFields)
    {
        _requestFields=requestFields;
    }
    public HttpFields getResponseFields()
    {
        return _responseFields;
    }
    public void setResponseFields(HttpFields responseFields)
    {
        _responseFields=responseFields;
    }
    public String getUri()
    {
        return _uri;
    }
    public void setUri(String uri)
    {
        _uri=uri;
    }
    
    protected boolean refillRequestContent()
    {
        return _requestContent!=null && _requestContent.hasContent();
    }
    
    protected boolean emptyResponseContent()
    {
        return false;
    }

    public boolean isStreaming()
    {
        return false;
    }
    


    /* ------------------------------------------------------------ */
    public static void main (String[] args) throws IOException
    {
        // this will be setup elsewhere
        Buffer[] buffers= { new ByteArrayBuffer(8192),new ByteArrayBuffer(8192),new ByteArrayBuffer(8192) };
        SimpleBuffers sbuffers= new SimpleBuffers(buffers);
        StringEndPoint endp = new StringEndPoint();
        HttpConnection connection = new HttpConnection(sbuffers,endp,8192,8192);
        
        // this is the API that will be used.
        Exchange ex=new Exchange();
        ex.getRequestFields().add("arbitrary","value");
        ex.getRequestFields().add(HttpHeaders.CONTENT_TYPE_BUFFER,MimeTypes.FORM_ENCODED_BUFFER);
        ex.setMethod("GET");
        ex.setUri("/test/dump/info");
        ex.setRequestContent(new ByteArrayBuffer("param=value\n"));
        
        // this will be done by the connection pool.
        connection.sendExchange(ex);
        
        // something else will call this
        connection.handle();
        System.err.println(endp.getOutput());
        endp.setInput("HTTP/1.1 200 OK\nServer: fake\n\n<h1>Test</h1>");
        connection.handle();
        

    }
    
}

package org.mortbay.jetty;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.Cookie;

import org.mortbay.io.Buffer;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.ByteArrayEndPoint;
import org.mortbay.io.SimpleBuffers;
import org.mortbay.io.View;
import org.mortbay.io.bio.StringEndPoint;
import org.mortbay.util.ByteArrayOutputStream2;

/* ------------------------------------------------------------ */
/** Test support class.
 * Assist with parsing and generating HTTP requests and responses
 * @author gregw
 *
 */
public class HttpTester
{
    HttpFields _fields=new HttpFields();
    String _method;
    String _uri;
    String _version;
    int _status;
    String _reason;
    ByteArrayOutputStream2 _parsedContent;
    byte[] _genContent;
    
    public HttpTester()
    {
    }
    
    public void reset()
    {
        _fields.clear();
         _method=null;
         _uri=null;
         _version=null;
         _status=0;
         _reason=null;
         _parsedContent=null;
         _genContent=null;
    }

    /* ------------------------------------------------------------ */
    /**
     * Parse one HTTP request or response
     * @param rawHTTP Raw HTTP to parse
     * @return Any unparsed data in the rawHTTP (eg pipelined requests)
     * @throws IOException
     */
    public String parse(String rawHTTP) throws IOException
    {
        ByteArrayBuffer buf = new ByteArrayBuffer(rawHTTP);
        View view = new View(buf);
        HttpParser parser = new HttpParser(view,new PH());
        parser.parse();
        return view.toString();
    }

    /* ------------------------------------------------------------ */
    public String generate() throws IOException
    {
        Buffer bb=new ByteArrayBuffer(32*1024);
        Buffer sb=new ByteArrayBuffer(8*1024);
        StringEndPoint endp = new StringEndPoint();
        HttpGenerator generator = new HttpGenerator(new SimpleBuffers(new Buffer[]{sb,bb}),endp, sb.capacity(), bb.capacity());
        
        if (_method!=null)
        {
            generator.setRequest(getMethod(),getUri());
            if (_version==null)
                generator.setVersion(HttpVersions.HTTP_1_1_ORDINAL);
            else
                generator.setVersion(HttpVersions.CACHE.getOrdinal(HttpVersions.CACHE.lookup(_version)));
            generator.completeHeader(_fields,false);
            if (_genContent!=null)
                generator.addContent(new View(new ByteArrayBuffer(_genContent)),false);
            else if (_parsedContent!=null)
                generator.addContent(new ByteArrayBuffer(_parsedContent.toByteArray()),false);
        }
        

        generator.complete();
        generator.flush();
        return endp.getOutput();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the method
     */
    public String getMethod()
    {
        return _method;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param method the method to set
     */
    public void setMethod(String method)
    {
        _method=method;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the reason
     */
    public String getReason()
    {
        return _reason;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param reason the reason to set
     */
    public void setReason(String reason)
    {
        _reason=reason;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the status
     */
    public int getStatus()
    {
        return _status;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param status the status to set
     */
    public void setStatus(int status)
    {
        _status=status;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the uri
     */
    public String getUri()
    {
        return _uri;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param uri the uri to set
     */
    public void setUri(String uri)
    {
        _uri=uri;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the version
     */
    public String getVersion()
    {
        return _version;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param version the version to set
     */
    public void setVersion(String version)
    {
        _version=version;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     * @throws IllegalArgumentException
     * @see org.mortbay.jetty.HttpFields#add(java.lang.String, java.lang.String)
     */
    public void add(String name, String value) throws IllegalArgumentException
    {
        _fields.add(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param date
     * @see org.mortbay.jetty.HttpFields#addDateField(java.lang.String, long)
     */
    public void addDateField(String name, long date)
    {
        _fields.addDateField(name,date);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     * @see org.mortbay.jetty.HttpFields#addLongField(java.lang.String, long)
     */
    public void addLongField(String name, long value)
    {
        _fields.addLongField(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param cookie
     * @see org.mortbay.jetty.HttpFields#addSetCookie(javax.servlet.http.Cookie)
     */
    public void addSetCookie(Cookie cookie)
    {
        _fields.addSetCookie(cookie);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @return
     * @see org.mortbay.jetty.HttpFields#getDateField(java.lang.String)
     */
    public long getDateField(String name)
    {
        return _fields.getDateField(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.HttpFields#getFieldNames()
     */
    public Enumeration getFieldNames()
    {
        return _fields.getFieldNames();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @return
     * @throws NumberFormatException
     * @see org.mortbay.jetty.HttpFields#getLongField(java.lang.String)
     */
    public long getLongField(String name) throws NumberFormatException
    {
        return _fields.getLongField(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @return
     * @see org.mortbay.jetty.HttpFields#getStringField(java.lang.String)
     */
    public String getStringField(String name)
    {
        return _fields.getStringField(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @return
     * @see org.mortbay.jetty.HttpFields#getValues(java.lang.String)
     */
    public Enumeration getValues(String name)
    {
        return _fields.getValues(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     * @see org.mortbay.jetty.HttpFields#put(java.lang.String, java.lang.String)
     */
    public void put(String name, String value)
    {
        _fields.put(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param date
     * @see org.mortbay.jetty.HttpFields#putDateField(java.lang.String, long)
     */
    public void putDateField(String name, long date)
    {
        _fields.putDateField(name,date);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     * @see org.mortbay.jetty.HttpFields#putLongField(java.lang.String, long)
     */
    public void putLongField(String name, long value)
    {
        _fields.putLongField(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @see org.mortbay.jetty.HttpFields#remove(java.lang.String)
     */
    public void remove(String name)
    {
        _fields.remove(name);
    }
    
    /* ------------------------------------------------------------ */
    public String getContent()
    {
        if (_parsedContent!=null)
            return _parsedContent.toString();
        if (_genContent!=null)
            return new String(_genContent);
        return null;
    }
    
    /* ------------------------------------------------------------ */
    public void setContent(String content)
    {
        _parsedContent=null;
        _genContent=content.getBytes();
    }
    
    
    private class PH extends HttpParser.EventHandler
    {
        public void startRequest(Buffer method, Buffer url, Buffer version) throws IOException
        {
            reset();
            _method=method.toString();
            _uri=url.toString();
            _version=version.toString();
        }

        public void startResponse(Buffer version, int status, Buffer reason) throws IOException
        {
            reset();
            _version=version.toString();
            _status=status;
            _reason=reason.toString();
        }
        
        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            _fields.add(name,value);
        }

        public void headerComplete() throws IOException
        {
        }

        public void messageComplete(long contextLength) throws IOException
        {
        }
        
        public void content(Buffer ref) throws IOException
        {
            if (_parsedContent==null)
                _parsedContent=new ByteArrayOutputStream2();
            _parsedContent.write(ref.asArray());
        }
    }
    
    
    public static void main(String[] args) throws Exception
    {
        HttpTester tester = new HttpTester();
        
        String remainder = tester.parse(
            "GET /uri HTTP/1.1\r\n"+
            "Host: fakehost\r\n"+
            "Content-Length: 10\r\n" +
            "\r\n"+
            "01234567\r\n"+
            "GET /next HTTP/1.0\r\n"+
            "\r\n");
       
        System.err.println(tester.getMethod());
        System.err.println(tester.getUri());
        System.err.println(tester.getVersion());
        System.err.println(tester.getStringField("Host"));
        System.err.println(tester.getContent());
        System.err.println(remainder);
        
        System.err.println(tester.generate());
    }

}

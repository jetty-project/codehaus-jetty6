package org.mortbay.io.tx;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.Permission;
import java.util.List;
import java.util.Map;

import org.mortbay.io.IO;
import org.mortbay.log.Log;

/** Handler for the pseudo URL protocol tx.
 * This URLStreamHandler wraps other URLStream so that the context can be buffered
 * and potentially transformed.   For example, the URL "tx:file:/etc/passwd" will
 * wrap the URL "file:/etc/passwd" and the contents loaded into a byte array.  The 
 * current thread and it's parents will then be inspected looking for a loader that
 * implements the {@link org.mortbay.io.tx.Transformer} interface, which if found
 * is used to transform the bytes of the content before streaming them as the content
 * of the URL.
 * 
 * @see {@link org.mortbay.jetty.webapp.TransformingWebAppClassLoader}
 * @author gregw
 *
 */
public class Handler extends URLStreamHandler
{
    public final static String PROTOCOL="tx";
    class BufferedURLConnection extends URLConnection
    {
        URLConnection _connection;

        protected byte[] _content;

        URL _src;

        public BufferedURLConnection(URL url, URL src)
        {
            super(url);
            _src=src;
            try
            {
                if (_connection==null)
                    _connection=_src.openConnection();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }

        public void addRequestProperty(String key, String value)
        {
            _connection.addRequestProperty(key,value);
        }

        public void connect() throws IOException
        {
            _connection.connect();
        }

        private void doContent() 
        {
            try
            {
                if (_content==null)
                {
                    InputStream in=_connection.getInputStream();
                    if (in!=null)
                    {
                        ByteArrayOutputStream out=new ByteArrayOutputStream();
                        IO.copy(in,out);
                        _content=out.toByteArray();
                        
                        _content=transform(_src,_content);
                    }
                }
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        
        /** Transform content
         * Search the current context classloader and it's parents for one that
         * implements {@link Transformer} interface.  If found, use this to transform the 
         * content.
         * @param src
         * @param content
         * @return
         */
        protected byte[] transform(URL src, byte[] content)
        {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            while (loader!=null && !(loader instanceof Transformer))
                loader=loader.getParent();
            if (loader!=null)
                content=((Transformer)loader).transform(src,content);
            return content;
        }

        public boolean equals(Object obj)
        {
            return _connection.equals(obj);
        }

        public boolean getAllowUserInteraction()
        {
            return _connection.getAllowUserInteraction();
        }

        public int getConnectTimeout()
        {
            return _connection.getConnectTimeout();
        }

        public Object getContent() throws IOException
        {
            System.err.println("getContent "+_src);
            return _connection.getContent();
            // eturn getInputStream();
        }

        public Object getContent(Class[] classes) throws IOException
        {
            System.err.println("getContent(classes) "+_src);
            return _connection.getContent(classes);
            // return getInputStream();
        }

        public String getContentEncoding()
        {
            return _connection.getContentEncoding();
        }

        public int getContentLength()
        {
            doContent();
            if (_content!=null)
                return _content.length;
            return _connection.getContentLength();
        }

        public String getContentType()
        {
            return _connection.getContentType();
        }

        public long getDate()
        {
            return _connection.getDate();
        }

        public boolean getDefaultUseCaches()
        {
            return _connection.getDefaultUseCaches();
        }

        public boolean getDoInput()
        {
            return _connection.getDoInput();
        }

        public boolean getDoOutput()
        {
            return _connection.getDoOutput();
        }

        public long getExpiration()
        {
            return _connection.getExpiration();
        }

        public String getHeaderField(int n)
        {
            return _connection.getHeaderField(n);
        }

        public String getHeaderField(String name)
        {
            return _connection.getHeaderField(name);
        }

        public long getHeaderFieldDate(String name, long Default)
        {
            return _connection.getHeaderFieldDate(name,Default);
        }

        public int getHeaderFieldInt(String name, int Default)
        {
            return _connection.getHeaderFieldInt(name,Default);
        }

        public String getHeaderFieldKey(int n)
        {
            return _connection.getHeaderFieldKey(n);
        }

        public Map getHeaderFields()
        {
            return _connection.getHeaderFields();
        }

        public long getIfModifiedSince()
        {
            return _connection.getIfModifiedSince();
        }

        public InputStream getInputStream() throws IOException
        {
            doContent();
            if (_content==null)
                throw new IOException("no content");   
            return new ByteArrayInputStream(_content);
        }

        public long getLastModified()
        {
            return _connection.getLastModified();
        }

        public OutputStream getOutputStream() throws IOException
        {
            return _connection.getOutputStream();
        }

        public Permission getPermission() throws IOException
        {
            return _connection.getPermission();
        }

        public int getReadTimeout()
        {
            return _connection.getReadTimeout();
        }

        public Map getRequestProperties()
        {
            return _connection.getRequestProperties();
        }

        public String getRequestProperty(String key)
        {
            return _connection.getRequestProperty(key);
        }

        public URL getURL()
        {
            return _connection.getURL();
        }

        public boolean getUseCaches()
        {
            return _connection.getUseCaches();
        }

        public int hashCode()
        {
            return _connection.hashCode();
        }

        public void setAllowUserInteraction(boolean allowuserinteraction)
        {
            _connection.setAllowUserInteraction(allowuserinteraction);
        }

        public void setConnectTimeout(int timeout)
        {
            _connection.setConnectTimeout(timeout);
        }

        public void setDefaultUseCaches(boolean defaultusecaches)
        {
            _connection.setDefaultUseCaches(defaultusecaches);
        }

        public void setDoInput(boolean doinput)
        {
            _connection.setDoInput(doinput);
        }

        public void setDoOutput(boolean dooutput)
        {
            _connection.setDoOutput(dooutput);
        }

        public void setIfModifiedSince(long ifmodifiedsince)
        {
            _connection.setIfModifiedSince(ifmodifiedsince);
        }

        public void setReadTimeout(int timeout)
        {
            _connection.setReadTimeout(timeout);
        }

        public void setRequestProperty(String key, String value)
        {
            _connection.setRequestProperty(key,value);
        }

        public void setUseCaches(boolean usecaches)
        {
            _connection.setUseCaches(usecaches);
        }

        public String toString()
        {
            return PROTOCOL+":"+_connection.toString();
        }

    }

    public static void register()
    {
        String handlers=System.getProperty("java.protocol.handler.pkgs");
        if (handlers!=null && handlers.indexOf("org.mortbay.io")>=0)
            return;
        handlers=(handlers==null?"":(handlers+"|"))+"org.mortbay.io";
        Log.info("set -Djava.protocol.handler.pkgs="+handlers);
        System.setProperty("java.protocol.handler.pkgs",handlers);
        
        try
        {
            new URL(PROTOCOL+":file:/");
        }
        catch(MalformedURLException e)
        {
            Log.ignore(e);
            Log.info("setURLStreanHandlerFactory for org.mortbay.io.tx.Handler");
            URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory()
                    {
                        Handler _handler=new Handler();
                        public URLStreamHandler createURLStreamHandler(String protocol)
                        {
                            if (PROTOCOL.equals(protocol))
                                return _handler;
                            return null;
                        }
                    });
        }
    }

    protected URLConnection openConnection(URL url) throws IOException
    {
        String s=url.toString();
        assert s.startsWith("tx:");
        return new BufferedURLConnection(url,new URL(s.substring(3)));
    }

}

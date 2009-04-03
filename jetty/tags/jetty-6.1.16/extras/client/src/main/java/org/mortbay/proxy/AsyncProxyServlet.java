// ========================================================================
// Copyright 2006-2007 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.proxy;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpSchemes;
import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.client.Address;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.util.IO;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;



/**
 * EXPERIMENTAL Proxy servlet.
 * @author gregw
 *
 */
public class AsyncProxyServlet implements Servlet
{
    HttpClient _client;

    protected HashSet<String> _DontProxyHeaders = new HashSet<String>();
    {
        _DontProxyHeaders.add("proxy-connection");
        _DontProxyHeaders.add("connection");
        _DontProxyHeaders.add("keep-alive");
        _DontProxyHeaders.add("transfer-encoding");
        _DontProxyHeaders.add("te");
        _DontProxyHeaders.add("trailer");
        _DontProxyHeaders.add("proxy-authorization");
        _DontProxyHeaders.add("proxy-authenticate");
        _DontProxyHeaders.add("upgrade");
    }

    private ServletConfig config;
    private ServletContext context;

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException
    {
        this.config=config;
        this.context=config.getServletContext();

        _client=new HttpClient();
        //_client.setConnectorType(HttpClient.CONNECTOR_SOCKET);
        _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        try
        {
            _client.start();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletConfig()
     */
    public ServletConfig getServletConfig()
    {
        return config;
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#service(javax.servlet.ServletRequest, javax.servlet.ServletResponse)
     */
    public void service(ServletRequest req, ServletResponse res) throws ServletException,
            IOException
    {
        final HttpServletRequest request = (HttpServletRequest)req;
        final HttpServletResponse response = (HttpServletResponse)res;
        if ("CONNECT".equalsIgnoreCase(request.getMethod()))
        {
            handleConnect(request,response);
        }
        else
        {
            final InputStream in=request.getInputStream();
            final OutputStream out=response.getOutputStream();
            final Continuation continuation = ContinuationSupport.getContinuation(request,request);


            if (!continuation.isPending())
            {
                final byte[] buffer = new byte[4096]; // TODO avoid this!
                String uri=request.getRequestURI();
                if (request.getQueryString()!=null)
                    uri+="?"+request.getQueryString();

                HttpURI url=proxyHttpURI(request.getScheme(),
                        request.getServerName(),
                        request.getServerPort(),
                        uri);
                
                if (url==null)
                {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    return;
                }

                HttpExchange exchange = new HttpExchange()
                {

                    protected void onRequestCommitted() throws IOException
                    {
                    }

                    protected void onRequestComplete() throws IOException
                    {
                    }

                    protected void onResponseComplete() throws IOException
                    {
                        continuation.resume();
                    }

                    protected void onResponseContent(Buffer content) throws IOException
                    {
                        // TODO Avoid this copy
                        while (content.hasContent())
                        {
                            int len=content.get(buffer,0,buffer.length);
                            out.write(buffer,0,len);  // May block here for a little bit!
                        }
                    }

                    protected void onResponseHeaderComplete() throws IOException
                    {
                    }

                    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
                    {
                        if (reason!=null && reason.length()>0)
                            response.setStatus(status,reason.toString());
                        else
                            response.setStatus(status);

                    }

                    protected void onResponseHeader(Buffer name, Buffer value) throws IOException
                    {
                        String s = name.toString().toLowerCase();
                        if (!_DontProxyHeaders.contains(s))
                            response.addHeader(name.toString(),value.toString());
                    }

                };
                
                exchange.setVersion(request.getProtocol());
                exchange.setMethod(request.getMethod());
                
                exchange.setURL(url.toString());
                
                // check connection header
                String connectionHdr = request.getHeader("Connection");
                if (connectionHdr!=null)
                {
                    connectionHdr=connectionHdr.toLowerCase();
                    if (connectionHdr.indexOf("keep-alive")<0  &&
                            connectionHdr.indexOf("close")<0)
                        connectionHdr=null;
                }

                // copy headers
                boolean xForwardedFor=false;
                boolean hasContent=false;
                long contentLength=-1;
                Enumeration enm = request.getHeaderNames();
                while (enm.hasMoreElements())
                {
                    // TODO could be better than this!
                    String hdr=(String)enm.nextElement();
                    String lhdr=hdr.toLowerCase();

                    if (_DontProxyHeaders.contains(lhdr))
                        continue;
                    if (connectionHdr!=null && connectionHdr.indexOf(lhdr)>=0)
                        continue;

                    if ("content-type".equals(lhdr))
                        hasContent=true;
                    if ("content-length".equals(lhdr))
                        contentLength=request.getContentLength();

                    Enumeration vals = request.getHeaders(hdr);
                    while (vals.hasMoreElements())
                    {
                        String val = (String)vals.nextElement();
                        if (val!=null)
                        {
                            exchange.setRequestHeader(lhdr,val);
                            xForwardedFor|="X-Forwarded-For".equalsIgnoreCase(hdr);
                        }
                    }
                }

                // Proxy headers
                exchange.setRequestHeader("Via","1.1 (jetty)");
                if (!xForwardedFor)
                    exchange.addRequestHeader("X-Forwarded-For",
                            request.getRemoteAddr());

                if (hasContent)
                    exchange.setRequestContentSource(in);

                _client.send(exchange);

                continuation.suspend(30000);
            }
        }
    }


    /* ------------------------------------------------------------ */
    protected HttpURI proxyHttpURI(String scheme, String serverName, int serverPort, String uri)
        throws MalformedURLException
    {
        return new HttpURI(scheme+"://"+serverName+":"+serverPort+uri);
    }

    /* ------------------------------------------------------------ */
    public void handleConnect(HttpServletRequest request,
                              HttpServletResponse response)
        throws IOException
    {
        String uri = request.getRequestURI();

        String port = "";
        String host = "";

        int c = uri.indexOf(':');
        if (c>=0)
        {
            port = uri.substring(c+1);
            host = uri.substring(0,c);
            if (host.indexOf('/')>0)
                host = host.substring(host.indexOf('/')+1);
        }

        InetSocketAddress inetAddress = new InetSocketAddress (host, Integer.parseInt(port));

        //if (isForbidden(HttpMessage.__SSL_SCHEME,addrPort.getHost(),addrPort.getPort(),false))
        //{
        //    sendForbid(request,response,uri);
        //}
        //else
        {
            InputStream in=request.getInputStream();
            OutputStream out=response.getOutputStream();

            Socket socket = new Socket(inetAddress.getAddress(),inetAddress.getPort());

            response.setStatus(200);
            response.setHeader("Connection","close");
            response.flushBuffer();



            IO.copyThread(socket.getInputStream(),out);
            IO.copy(in,socket.getOutputStream());
        }
    }




    /* (non-Javadoc)
     * @see javax.servlet.Servlet#getServletInfo()
     */
    public String getServletInfo()
    {
        return "Proxy Servlet";
    }

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#destroy()
     */
    public void destroy()
    {

    }
    
    public static class Transparent extends AsyncProxyServlet
    {
        String _prefix;
        String _server;
        int _port;
        
        public Transparent()
        {    
        }
        
        public Transparent(String prefix,String server, int port)
        {
            _prefix=prefix;
            _server=server;
            _port=port;
        }
        
        protected HttpURI proxyHttpURI(final String scheme, final String serverName, int serverPort, final String uri) throws MalformedURLException
        {
            if (!uri.startsWith(_prefix))
                return null;
            HttpURI url = super.proxyHttpURI(scheme,_server,_port,uri.substring(_prefix.length()));
            return url;
        }
    }


    public static void main(String[] args)
        throws Exception
    {
        Server proxy = new Server();
        //SelectChannelConnector connector = new SelectChannelConnector();
        Connector connector = new SocketConnector();
        connector.setPort(8888);
        proxy.addConnector(connector);
        Context context = new Context(proxy,"/",0);
        context.addServlet(new ServletHolder(new AsyncProxyServlet.Transparent("","www.google.com",80)), "/");

        proxy.start();
        proxy.join();
    }
}

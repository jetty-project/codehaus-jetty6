//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.proxy;

import java.io.IOException;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ConcurrentMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.HttpMethods;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.thread.QueuedThreadPool;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

/* ------------------------------------------------------------ */
/** Ebay proxy servlet.
 * 
 * Can be used as servlet or directly as a main.
 * @author gregw
 *
 */
public class AsyncProxy extends HttpServlet
{
    static final long serialVersionUID = 1L;

    private String _remoteUrl;
    private HttpClient _client=new HttpClient();
    private ServletContext _context;


    public static void main(String[] args)
    throws Exception
    {
        Server server = new Server();

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(8090);
        connector.setAcceptors(4);
        connector.setHeaderBufferSize(4096);
        connector.setRequestBufferSize(8182);
        connector.setResponseBufferSize(32768);
        server.addConnector(connector);

        QueuedThreadPool threadpool = new QueuedThreadPool();
        threadpool.setMinThreads(20);
        threadpool.setMaxThreads(50);
        threadpool.setMaxIdleTimeMs(60000);
        server.setThreadPool(threadpool);

        Context context = new Context(server,"/",Context.SESSIONS);
        ServletHolder servlet=new ServletHolder(new AsyncProxy());
        servlet.setInitParameter("remoteUrl","http://localhost:8080/test/dump");
        context.addServlet(servlet, "/*");
        context.setAttribute("threadpool",threadpool);

        server.start();
        server.join();
    }
    
    public void init() throws ServletException 
    {
        try
        {
            _context = getServletConfig().getServletContext();
            _remoteUrl = getServletConfig().getInitParameter("remoteUrl");
            if (_remoteUrl==null)
                _remoteUrl="http://localhost:8080/test/dump";

            ThreadPool threadpool = (ThreadPool)_context.getAttribute("threadpool");
            if (threadpool==null)
            {
                QueuedThreadPool qthreadpool = new QueuedThreadPool();
                qthreadpool.setMinThreads(20);
                qthreadpool.setMaxThreads(50);
                qthreadpool.setMaxIdleTimeMs(60000);
                threadpool=qthreadpool;
            }
            
            _client.setThreadPool(threadpool);
            _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
            _client.setMaxConnectionsPerAddress(10000); // needed as all go to same address!
            _client.setHeaderBufferSize(4096);
            _client.setRequestBufferSize(8182);
            _client.setResponseBufferSize(32768);

            _client.start();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, final HttpServletResponse response)
    throws ServletException, IOException 
    {
        final Continuation continuation = ContinuationSupport.getContinuation(req, null);

        if (!continuation.isPending())
        {
            final String sessionId = req.getSession(true).getId();
            System.out.println("");
            System.out.println("Session id: " + sessionId + " ");

            if (req.getParameter("remoteUrl") != null) 
            {
                _remoteUrl = req.getParameter("remoteUrl");  
                _context.log("Changed remoteUrl: "+_remoteUrl);
            }

            String remoteUri = req.getParameter("remoteUri");
            if (remoteUri == null) 
                remoteUri = "/remoteApp/page";

            String url = _remoteUrl + remoteUri;                       

            String latencyStr = req.getParameter("latency");
            int latency = latencyStr == null ? 240 : Integer.parseInt(latencyStr);

            // If the file being served has other URIs in it, like say images,
            // subsequent calls are made to the remote app to retrieve those.
            // We don't want to set new delay times and status codes then.
            if (remoteUri.endsWith("page")) 
            {
                Integer[] params = generateRandomParams(latency);

                url+="?sessionId="+sessionId+
                "&delay="+params[0]+
                "&code="+params[1];
                System.out.println(url);
            } 
            else 
            {
                System.out.println(url);
            }
            
            // Create the HTTP exchange as an anonymous object 
            HttpExchange exchange = new HttpExchange()
            {
                long _start=System.currentTimeMillis();
                ServletOutputStream _out;

                /* ------------------------------------------------------------ */
                protected void onException(Throwable ex) 
                {
                    try
                    {        
                        System.out.println("  Exception to " + sessionId + " in " + (System.currentTimeMillis() - _start));
                             
                        response.sendError(500);
                        continuation.resume();
                    }
                    catch(Exception e)
                    {
                        _context.log("exception",e);
                    }
                }

                /* ------------------------------------------------------------ */
                protected void onExpire()
                {
                    try
                    {
                        System.out.println("  Timeout for " + sessionId + " in " + (System.currentTimeMillis() - _start));
                        
                        response.sendError(500);
                        continuation.resume();
                    }
                    catch(Exception e)
                    {
                        _context.log("exception",e);
                    }
                }

                /* ------------------------------------------------------------ */
                protected void onResponseContent(Buffer content) throws IOException
                {
                    byte[] array = content.array();
                    if(array!=null)
                        _out.write(array,content.getIndex(),content.length());
                    else
                        _out.write(content.asArray()); // TODO consider avoiding this extra buffer!
                }

                /* ------------------------------------------------------------ */
                protected void onResponseHeaderComplete() throws IOException
                {
                    _out=response.getOutputStream();
                }

                /* ------------------------------------------------------------ */
                protected void onResponseHeader(Buffer name, Buffer value) throws IOException
                {
                    response.setHeader(name.toString(),value.toString());
                }

                /* ------------------------------------------------------------ */
                protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
                {
                    response.setStatus(status,reason.toString());               
                    System.out.println("  onResponse() - Status:" + status + " - " + " " + sessionId);
                }

                /* ------------------------------------------------------------ */
                protected void onResponseComplete() throws IOException
                {
                    System.out.println("  Responded to " + sessionId + " in " + (System.currentTimeMillis() - _start));
                    continuation.resume();
                }
            };
            exchange.setMethod(HttpMethods.GET);
            exchange.setURL(url);

            try 
            {
                _client.send(exchange);
                continuation.suspend(100*1000);
            } 
            catch (RetryRequest r) 
            {
                throw r;
            } 
            catch (Exception e) 
            {
                e.printStackTrace();
            }
        } 
    }

    /* ------------------------------------------------------------ */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException 
    {
        doGet(req, resp);
    }



    /* ------------------------------------------------------------ */
    public static Integer[] generateRandomParams(int latency) 
    {
        Integer[] params = new Integer[2];
        final int[] codes = new int[] { 200, 302, 403, 404, 500};

        Random generator = new Random();

        // generate a random delay between 10ms and 10 + latency ms
        int delay = generator.nextInt(latency) + 10;
        params[0] = new Integer(delay);

        // generate randomly one of http status codes.
        int index = generator.nextInt(codes.length);
        params[1] = new Integer(codes[index]);

        return params;
    }
}

//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.server;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.server.Connector;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.LocalConnector;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.handler.HandlerWrapper;

public class AsyncContextTest extends TestCase
{
    protected Server _server = new Server();
    protected SuspendHandler _handler = new SuspendHandler();
    protected LocalConnector _connector;

    protected void setUp() throws Exception
    {
        _connector = new LocalConnector();
        _server.setConnectors(new Connector[]{ _connector });
        _server.setHandler(_handler);
        _server.start();

    }

    protected void tearDown() throws Exception
    {
        _server.stop();
    }

    public void testSuspendResume() throws Exception
    {
        String response;
        
        _handler.setRead(0);
        _handler.setSuspendFor(1000);

        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(-1);
        check("TIMEOUT",process(null));

        _handler.setSuspendFor(10000);
        
        _handler.setResumeAfter(0);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process(null));
        
        _handler.setResumeAfter(100);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process(null));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(0);
        check("COMPLETED",process(null));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(200);
        check("COMPLETED",process(null));   

        _handler.setRead(-1);
        
        _handler.setResumeAfter(0);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process("wibble"));
        
        _handler.setResumeAfter(100);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process("wibble"));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(0);
        check("COMPLETED",process("wibble"));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(100);
        check("COMPLETED",process("wibble"));
        

        _handler.setRead(6);
        
        _handler.setResumeAfter(0);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process("wibble"));
        
        _handler.setResumeAfter(100);
        _handler.setCompleteAfter(-1);
        check("RESUMED",process("wibble"));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(0);
        check("COMPLETED",process("wibble"));
        
        _handler.setResumeAfter(-1);
        _handler.setCompleteAfter(100);
        check("COMPLETED",process("wibble"));
    }

    protected void check(String content,String response)
    {
        assertEquals("HTTP/1.1 200 OK",response.substring(0,15));
        assertTrue(content.contains(content));
    }
    
    public synchronized String process(String content) throws Exception
    {
        String request = "GET / HTTP/1.1\r\n" + "Host: localhost\r\n";
        
        if (content==null)
            request+="\r\n";
        else
            request+="Content-Length: "+content.length()+"\r\n" + "\r\n" + content;

        _connector.reopen();
        String response = _connector.getResponses(request);
        return response;
    }
    
    private static class SuspendHandler extends HandlerWrapper
    {
        private int _read;
        private long _suspendFor=-1;
        private long _resumeAfter=-1;
        private long _completeAfter=-1;
        
        public SuspendHandler()
        {}
        

        public int getRead()
        {
            return _read;
        }

        public void setRead(int read)
        {
            _read = read;
        }

        public long getSuspendFor()
        {
            return _suspendFor;
        }

        public void setSuspendFor(long suspendFor)
        {
            _suspendFor = suspendFor;
        }

        public long getResumeAfter()
        {
            return _resumeAfter;
        }

        public void setResumeAfter(long resumeAfter)
        {
            _resumeAfter = resumeAfter;
        }

        public long getCompleteAfter()
        {
            return _completeAfter;
        }

        public void setCompleteAfter(long completeAfter)
        {
            _completeAfter = completeAfter;
        }



        public void handle(String target, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException
        {
            final Request base_request = (request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
            
            if (DispatcherType.REQUEST.equals(request.getDispatcherType()))
            {
                if (_read>0)
                {
                    byte[] buf=new byte[_read];
                    request.getInputStream().read(buf);
                }
                else if (_read<0)
                {
                    InputStream in = request.getInputStream();
                    int b=in.read();
                    while(b!=-1)
                        b=in.read();
                }

                if (_suspendFor>0)
                    request.setAsyncTimeout(_suspendFor);
                request.addAsyncListener(__asyncListener);
                final AsyncContext asyncContext = request.startAsync();
                
                if (_completeAfter>0)
                {
                    new Thread() {
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(_completeAfter);
                                response.getOutputStream().print("COMPLETED");
                                response.setStatus(200);
                                base_request.setHandled(true);
                                asyncContext.complete();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                else if (_completeAfter==0)
                {
                    response.getOutputStream().print("COMPLETED");
                    response.setStatus(200);
                    base_request.setHandled(true);
                    asyncContext.complete();
                }
                
                if (_resumeAfter>0)
                {
                    new Thread() {
                        public void run()
                        {
                            try
                            {
                                Thread.sleep(_resumeAfter);
                                asyncContext.dispatch();
                            }
                            catch(Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                else if (_resumeAfter==0)
                {
                    asyncContext.dispatch();
                }
            }
            else if (request.getAttribute("TIMEOUT")!=null)
            {
                response.setStatus(200);
                response.getOutputStream().print("TIMEOUT");
                base_request.setHandled(true);
            }
            else
            {
                response.setStatus(200);
                response.getOutputStream().print("RESUMED");
                base_request.setHandled(true);
            }
        }
    }
    
    
    private static AsyncListener __asyncListener = 
        new AsyncListener()
    {
        public void onComplete(AsyncEvent event) throws IOException
        {
        }

        public void onTimeout(AsyncEvent event) throws IOException
        {
            event.getRequest().setAttribute("TIMEOUT",Boolean.TRUE);
            event.getRequest().getAsyncContext().dispatch();
        }
        
    };
}

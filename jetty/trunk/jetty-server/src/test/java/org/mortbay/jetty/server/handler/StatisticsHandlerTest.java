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

package org.mortbay.jetty.server.handler;

import java.io.IOException;

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
import org.mortbay.jetty.server.handler.StatisticsHandler;

public class StatisticsHandlerTest extends TestCase
{
    protected Server _server = new Server();
    protected LocalConnector _connector;

    private StatisticsHandler _statsHandler;
    
    protected void setUp() throws Exception
    {
        _statsHandler = new StatisticsHandler();
        _server.setHandler(_statsHandler);
        
        _connector = new LocalConnector();
        _server.setConnectors(new Connector[]{ _connector });
        _server.start();

    }

    protected void tearDown() throws Exception
    {
        _server.stop();
    }

    /* TODO fix this
    public void testSuspendedStats() throws Exception
    {
        process(new ResumeHandler());
        process(new SuspendHandler());
        process();

        assertEquals(3,_statsHandler.getRequests());
        assertEquals(1,_statsHandler.getRequestsTimedout());
        assertEquals(1,_statsHandler.getRequestsResumed());
    }
    */

    // TODO: keep it active without blocking
    // public void testActiveStats() throws Exception
    // {
    // process(new ActiveHandler(_lock));
    // process(new ActiveHandler(_lock));
    //        
    // assertEquals(2, _statsHandler.getRequests());
    // assertEquals(2, _statsHandler.getRequestsActive());
    // assertEquals(2, _statsHandler.getRequestsActiveMax());
    // assertEquals(0, _statsHandler.getRequestsActiveMin());
    //        
    // _statsHandler.statsReset();
    // assertEquals(2, _statsHandler.getRequestsActive());
    // assertEquals(2, _statsHandler.getRequestsActiveMax());
    // assertEquals(2, _statsHandler.getRequestsActiveMin());
    //        
    // process();
    // assertEquals(1, _statsHandler.getRequests());
    // assertEquals(2, _statsHandler.getRequestsActive());
    // assertEquals(3, _statsHandler.getRequestsActiveMax());
    // assertEquals(2, _statsHandler.getRequestsActiveMin());
    // }

    public void testDurationStats() throws Exception
    {
        process(new DurationHandler(200));
        process(new DurationHandler(500));

        isApproximately(200,_statsHandler.getRequestsDurationMin());
        isApproximately(500,_statsHandler.getRequestsDurationMax());
        isApproximately(350,_statsHandler.getRequestsDurationAve());
        isApproximately(700,_statsHandler.getRequestsDurationTotal());

        isApproximately(200,_statsHandler.getRequestsActiveDurationMin());
        isApproximately(500,_statsHandler.getRequestsActiveDurationMax());
        isApproximately(350,_statsHandler.getRequestsActiveDurationAve());
        isApproximately(700,_statsHandler.getRequestsActiveDurationTotal());

        _statsHandler.statsReset();
        assertEquals(0,_statsHandler.getRequestsDurationMin());
        assertEquals(0,_statsHandler.getRequestsDurationMax());
        assertEquals(0,_statsHandler.getRequestsDurationAve());
        assertEquals(0,_statsHandler.getRequestsDurationTotal());
        assertEquals(0,_statsHandler.getRequestsActiveDurationMin());
        assertEquals(0,_statsHandler.getRequestsActiveDurationMax());
        assertEquals(0,_statsHandler.getRequestsActiveDurationAve());
        assertEquals(0,_statsHandler.getRequestsActiveDurationTotal());
    }

    /*
    public void testDurationWithSuspend() throws Exception
    {
        int processDuration = 100;
        long[] suspendFor = new long[]
        { 200, 400, 600 };
        int suspendDuration = 0;
        for (long i : suspendFor)
            suspendDuration += i;

        process(new DurationSuspendHandler(processDuration,suspendFor));

        isApproximately(processDuration,_statsHandler.getRequestsActiveDurationTotal());
        isApproximately(processDuration + suspendDuration,_statsHandler.getRequestsDurationTotal());

    }
    */

    /* TODO fix
    public void testResponses() throws Exception
    {
        // all return 200
        process();
        assertEquals(1,_statsHandler.getResponses2xx());

        // don't count the suspend.
        process(new ResumeHandler());
        assertEquals(2,_statsHandler.getResponses2xx());

        process(new SuspendHandler(1));
        assertEquals(3,_statsHandler.getResponses2xx());
        
    }
    */

    /* TODO fix
    public void testComplete() throws Exception
    {
        int initialDelay = 200;
        int completeDuration = 500;
        
        
        synchronized(_server)
        {
            process(new SuspendCompleteHandler(initialDelay, completeDuration, _server));
            
            try 
            {
                _server.wait();
            }
            catch(InterruptedException e)
            {
            }
        }
        
        isApproximately(initialDelay,_statsHandler.getRequestsActiveDurationTotal());
        // fails; twice the expected value
        //TODO failed in jaspi branch
//        isApproximately(initialDelay + completeDuration,_statsHandler.getRequestsDurationTotal());
    }
    */
    
    public void process() throws Exception
    {
        process(null);
    }

    public synchronized void process(HandlerWrapper customHandler) throws Exception
    {
        _statsHandler.setHandler(customHandler);

        String request = "GET / HTTP/1.1\r\n" + "Host: localhost\r\n" + "Content-Length: 6\r\n" + "\r\n" + "test\r\n";

        _connector.reopen();
        _connector.getResponses(request);
        _statsHandler.setHandler(null);

    }

    private void isApproximately(long expected, long actual)
    {
        assertTrue("expected " + expected + "; got " + actual,actual > expected / 2);
        assertTrue("expected " + expected + "; got " + actual,actual < (expected * 3) / 2);
    }

    private static class ActiveHandler extends HandlerWrapper
    {
        private Object _lock;

        public ActiveHandler(Object lock)
        {
            _lock = lock;
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if (!request.isAsyncStarted())
            {
                try
                {
                    synchronized (_lock)
                    {
                        _lock.wait();
                    }
                }
                catch (InterruptedException e)
                {
                }
            }
        }

    }

    private static class SuspendHandler extends HandlerWrapper
    {
        private int _suspendFor;

        public SuspendHandler()
        {
            _suspendFor = 10;
        }

        public SuspendHandler(int suspendFor)
        {
            _suspendFor = suspendFor;
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if (!request.isAsyncStarted())
            {
                request.setAsyncTimeout(_suspendFor);
                request.startAsync();
            }
        }

    }

    private static class ResumeHandler extends HandlerWrapper
    {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if (!request.isAsyncStarted())
            {
                request.setAsyncTimeout(100000);
                request.startAsync().dispatch();
            }
        }

    }

    private static class DurationHandler extends HandlerWrapper
    {
        private int _duration;

        public DurationHandler(int duration)
        {
            _duration = duration;
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            if (!request.isAsyncStarted())
            {
                try
                {
                    Thread.sleep(_duration);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }

    }

    private static class DurationSuspendHandler extends HandlerWrapper
    {
        private int _duration;
        private long[] _suspendFor;

        public DurationSuspendHandler(int duration, long[] suspendFor)
        {
            _duration = duration;
            _suspendFor = suspendFor;
        }

        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {

            Integer i = (Integer)request.getAttribute("i");
            if (i == null)
                i = 0;

            if (i < _suspendFor.length)
            {
                request.setAsyncTimeout(_suspendFor[i]);
                request.startAsync();
                request.setAttribute("i",i + 1);
                return;
            }
            else
            {
                try
                {
                    Thread.sleep(_duration);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }

        }

    }
    
    private class SuspendCompleteHandler extends HandlerWrapper
    {
        private long _initialDuration;
        private long _completeDuration;
        private Object _lock;
        public SuspendCompleteHandler(int initialDuration, int completeDuration, Object lock)
        {
            _initialDuration = initialDuration;
            _completeDuration = completeDuration;
            _lock = lock;
        }
        
        public void handle(String target, final HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            final Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
            
            if(!request.isAsyncStarted())
            {
                try
                {
                    Thread.sleep(_initialDuration);
                } catch (InterruptedException e1)
                {
                }
                
                request.setAsyncTimeout(_completeDuration*10);
                request.startAsync();
                
                (new Thread() {
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(_completeDuration);
                            request.getAsyncContext().complete();
                            
                            synchronized(_lock)
                            {
                                _lock.notify();
                            }
                        }
                        catch(InterruptedException e)
                        {
                        }
                    }
                }).start();
            }
        }
   
    }
}

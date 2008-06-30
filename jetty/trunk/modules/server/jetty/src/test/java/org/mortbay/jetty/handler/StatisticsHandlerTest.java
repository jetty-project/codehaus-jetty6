package org.mortbay.jetty.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;

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
        _server.setConnectors(new Connector[]
        { _connector });
        _server.start();

    }

    protected void tearDown() throws Exception
    {
        // synchronized(_lock)
        // {
        // _lock.notifyAll();
        // }

        _server.stop();
    }

    public void testSuspendedStats() throws Exception
    {
        process(new ResumeHandler());
        process(new SuspendHandler());
        process();

        assertEquals(3,_statsHandler.getRequests());
        assertEquals(1,_statsHandler.getRequestsTimedout());
        assertEquals(1,_statsHandler.getRequestsResumed());
    }

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

    public void testResponses() throws Exception
    {
        // all return 200
        process();
        assertEquals(1,_statsHandler.getResponses2xx());

        // one for the suspend, one for the resume
        process(new ResumeHandler());
        assertEquals(3,_statsHandler.getResponses2xx());

        process(new SuspendHandler(1));
        assertEquals(4,_statsHandler.getResponses2xx());
    }

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
        isApproximately(initialDelay + completeDuration,_statsHandler.getRequestsDurationTotal());
    }
    
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

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            if (request.isInitial())
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

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            Request base_request = (request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
            if (base_request.isInitial())
            {
                base_request.suspend(_suspendFor);
            }
        }

    }

    private static class ResumeHandler extends HandlerWrapper
    {

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            if (request.isInitial())
            {
                request.suspend(100000);
                request.resume();
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

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            if (request.isInitial())
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

        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {

            Integer i = (Integer)request.getAttribute("i");
            if (i == null)
                i = 0;

            if (i < _suspendFor.length)
            {
                request.suspend(_suspendFor[i]);
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
        
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
        {
            final Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
            
            if(base_request.isInitial())
            {
                try
                {
                    Thread.sleep(_initialDuration);
                } catch (InterruptedException e1)
                {
                }
                
                base_request.suspend(_completeDuration * 10);
                
                (new Thread() {
                    public void run()
                    {
                        try
                        {
                            Thread.sleep(_completeDuration);
                            base_request.complete();
                            
                            synchronized(_lock)
                            {
                                _lock.notify();
                            }
                        }
                        catch(IOException e)
                        {
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

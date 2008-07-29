package org.mortbay.servlet;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class QoSFilterTest extends TestCase 
{
    private ServletTester _tester;
    private LocalConnector[] _connectors;
    private CountDownLatch _doneRequests;
    private final int NUM_CONNECTIONS = 20;
    private final int NUM_LOOPS = 4;
    private final int MAX_QOS = 5;
    
    protected void setUp() throws Exception 
    {
        _tester = new ServletTester();
        _tester.setContextPath("/context");
        _tester.addServlet(TestServlet.class, "/test");
        TestServlet.__maxSleepers=0;
        TestServlet.__sleepers=0;
     
        _connectors = new LocalConnector[NUM_CONNECTIONS];
        for(int i = 0; i < _connectors.length; ++i)
            _connectors[i] = _tester.createLocalConnector();
        
        _doneRequests = new CountDownLatch(NUM_CONNECTIONS*NUM_LOOPS);
        
        _tester.start();
    }
        
    protected void tearDown() throws Exception 
    {
        _tester.stop();
    }

    public void testNoFilter() throws Exception
    {    
        for(int i = 0; i < NUM_CONNECTIONS; ++i )
        {
            new Thread(new Worker(i)).start();
        }
        
        _doneRequests.await(10,TimeUnit.SECONDS);
        
        assertTrue(TestServlet.__maxSleepers>MAX_QOS);
        assertTrue(TestServlet.__maxSleepers<=NUM_CONNECTIONS);
    }

    public void testQosFilter() throws Exception
    {    
        FilterHolder holder = new FilterHolder(QoSFilter2.class);
        holder.setInitParameter(QoSFilter.MAX_REQUESTS_INIT_PARAM, ""+MAX_QOS);
        _tester.getContext().getServletHandler().addFilterWithMapping(holder,"/*",Handler.REQUEST);
        
        for(int i = 0; i < NUM_CONNECTIONS; ++i )
        {
            new Thread(new Worker(i)).start();
        }
        
        _doneRequests.await(10,TimeUnit.SECONDS);
        assertEquals(MAX_QOS,TestServlet.__maxSleepers);
    }
    
    class Worker implements Runnable {
        private int _num;
        public Worker(int num)
        {
            _num = num;
        }

        public void run()
        {
            for (int i=0;i<NUM_LOOPS;i++)
            {
                HttpTester request = new HttpTester();
                HttpTester response = new HttpTester();

                request.setMethod("GET");
                request.setHeader("host", "tester");
                request.setURI("/context/test?priority="+(_num%QoSFilter.__DEFAULT_MAX_PRIORITY));
                request.setHeader("num", _num+"");
                try
                {
                    String responseString = _tester.getResponses(request.generate(), _connectors[_num]);
                    int index=-1;
                    if((index = responseString.indexOf("HTTP", index+1))!=-1)
                    {
                        responseString = response.parse(responseString);
                        _doneRequests.countDown();
                    }
                }
                catch (IOException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                catch (Exception e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
     
    }
    
    public static class TestServlet extends HttpServlet implements Servlet
    {
        private static int __sleepers;
        private static int __maxSleepers;
         
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            try
            {
                synchronized(TestServlet.class)
                {
                    __sleepers++;
                    if(__sleepers > __maxSleepers)
                        __maxSleepers = __sleepers;
                }

                Thread.sleep(200);

                synchronized(TestServlet.class)
                {
                    __sleepers--;
                    if(__sleepers > __maxSleepers)
                        __maxSleepers = __sleepers;
                }

                response.setContentType("text/plain");
                response.getWriter().println("DONE!");    
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                response.sendError(500);
            }  
        }
    }
    
    public static class QoSFilter2 extends QoSFilter
    {
        public int getPriority(ServletRequest request)
        {
            String p = ((HttpServletRequest)request).getParameter("priority");
            if (p!=null)
                return Integer.parseInt(p);
            return 0;
        }
    }
    
}

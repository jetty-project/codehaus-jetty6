package org.mortbay.servlet;

import java.io.IOException;
import java.net.Socket;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.testing.ServletTester;
import org.mortbay.util.IO;

import junit.framework.TestCase;

public class DosFilterTest extends TestCase
{
    private ServletTester _tester;
    private String _host;
    private int _port;
    private DoSFilter2 _filter;
    
    protected void setUp() throws Exception 
    {
        _tester = new ServletTester();
        HttpURI uri=new HttpURI(_tester.createSocketConnector(true));
        _host=uri.getHost();
        _port=uri.getPort();
        
        _tester.setContextPath("/ctx");
        _tester.addServlet(TestServlet.class, "/test");
        
        FilterHolder dos=_tester.addFilter(DoSFilter2.class,"/*",0);
        dos.setInitParameter("maxRequestsPerSec","4");
        dos.setInitParameter("delayMs","200");
        dos.setInitParameter("throttledRequests","1");
        dos.setInitParameter("waitMs","10");
        dos.setInitParameter("throttleMs","4000");
        _tester.start();
        
        _filter=(DoSFilter2)dos.getFilter();
        
    }
        
    protected void tearDown() throws Exception 
    {
        _tester.stop();
    }
    
    private String doRequests(String requests, int loops, long pause0,long pause1,String request)
        throws Exception
    {
        Socket socket = new Socket(_host,_port);
        socket.setSoTimeout(30000);
        
        for (int i=loops;i-->0;)
        {
            socket.getOutputStream().write(requests.getBytes("UTF-8"));
            socket.getOutputStream().flush();
            if (i>0 && pause0>0)
                Thread.sleep(pause0);
        }
        if (pause1>0)
            Thread.sleep(pause1);
        socket.getOutputStream().write(request.getBytes("UTF-8"));
        socket.getOutputStream().flush();
        
        String response = IO.toString(socket.getInputStream(),"UTF-8");
        socket.close();
        return response;
    }
    
    private int count(String responses,String substring)
    {
        int count=0;
        int i=responses.indexOf(substring);
        while (i>=0)
        {
            count++;
            i=responses.indexOf(substring,i+substring.length());
        }
        
        return count;
    }
    
    public void testEvenLowRateIP()
        throws Exception
    {
        String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
        String last="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
        String responses = doRequests(request,11,300,300,last);   
        assertEquals(12,count(responses,"HTTP/1.1 200 OK"));
        assertEquals(0,count(responses,"DoSFilter:"));
    }
    
    public void testBurstLowRateIP()
        throws Exception
    {
        String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
        String last="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
        String responses = doRequests(request+request+request+request,2,1100,1100,last);   
        
        assertEquals(9,count(responses,"HTTP/1.1 200 OK"));
        assertEquals(0,count(responses,"DoSFilter:"));
    }
    
    public void testDelayedIP()
        throws Exception
    {
        String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
        String last="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
        String responses = doRequests(request+request+request+request+request,2,1100,1100,last);
        
        assertEquals(11,count(responses,"HTTP/1.1 200 OK"));
        assertEquals(2,count(responses,"DoSFilter: delayed"));
    }
    
    public void testThrottledIP()
        throws Exception
    {
        Thread other = new Thread()
        {
            public void run()
            {
                try
                {
                    // Cause a delay, then sleep while holding pass
                    String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
                    String last="GET /ctx/test?sleep=2000 HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
                    String responses = doRequests(request+request+request+request,1,0,0,last);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        other.start();
        Thread.sleep(500);
        
        String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
        String last="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
        String responses = doRequests(request+request+request+request,1,0,0,last);
        
        assertEquals(5,count(responses,"HTTP/1.1 200 OK"));
        assertEquals(1,count(responses,"DoSFilter: delayed"));
        assertEquals(1,count(responses,"DoSFilter: throttled"));
        assertEquals(0,count(responses,"DoSFilter: unavailable"));
        
        other.join();
    }
    
    public void testUnavilableIP()
        throws Exception
    {
        Thread other = new Thread()
        {
            public void run()
            {
                try
                {
                    // Cause a delay, then sleep while holding pass
                    String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
                    String last="GET /ctx/test?sleep=5000 HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
                    String responses = doRequests(request+request+request+request,1,0,0,last);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        other.start();
        Thread.sleep(500);
        
        String request="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\n\r\n";
        String last="GET /ctx/test HTTP/1.1\r\nHost: localhost\r\nConnection: close\r\n\r\n";
        String responses = doRequests(request+request+request+request,1,0,0,last);
        
        assertEquals(4,count(responses,"HTTP/1.1 200 OK"));
        assertEquals(1,count(responses,"HTTP/1.1 503"));
        assertEquals(1,count(responses,"DoSFilter: delayed"));
        assertEquals(1,count(responses,"DoSFilter: throttled"));
        assertEquals(1,count(responses,"DoSFilter: unavailable"));
        
        other.join();
    }
    
    
    public static class TestServlet extends HttpServlet implements Servlet
    {
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            if (request.getParameter("session")!=null)
                request.getSession(true);
            if (request.getParameter("sleep")!=null)
            {
                try
                {
                    Thread.sleep(Long.parseLong(request.getParameter("sleep")));
                }
                catch(InterruptedException e)
                {    
                }
            }
            response.setContentType("text/plain");
            response.getWriter().println("TestServlet "+request.getRequestURI());
        }
    }
    
    public static class DoSFilter2 extends DoSFilter
    {
    }
    
    
}

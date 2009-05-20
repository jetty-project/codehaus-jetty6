package org.mortbay.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpURI;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.testing.ServletTester;
import org.mortbay.log.Log;

public class CloseableDosFilterTest extends DosFilterTest 
{
    protected void setUp() throws Exception 
    {
        _tester = new ServletTester();
        HttpURI uri=new HttpURI(_tester.createSocketConnector(true));
        _host=uri.getHost();
        _port=uri.getPort();
        
        _tester.setContextPath("/ctx");
        _tester.addServlet(TestServlet.class, "/*");
        
        FilterHolder dos=_tester.addFilter(CloseableDoSFilter2.class,"/dos/*",0);
        dos.setInitParameter("maxRequestsPerSec","4");
        dos.setInitParameter("delayMs","200");
        dos.setInitParameter("throttledRequests","1");
        dos.setInitParameter("waitMs","10");
        dos.setInitParameter("throttleMs","4000");
        dos.setInitParameter("remotePort", "false");
        dos.setInitParameter("insertHeaders", "true");
        
        FilterHolder quickTimeout = _tester.addFilter(CloseableDoSFilter2.class,"/timeout/*",0);
        quickTimeout.setInitParameter("maxRequestsPerSec","4");
        quickTimeout.setInitParameter("delayMs","200");
        quickTimeout.setInitParameter("throttledRequests","1");
        quickTimeout.setInitParameter("waitMs","10");
        quickTimeout.setInitParameter("throttleMs","4000");
        quickTimeout.setInitParameter("remotePort", "false");
        quickTimeout.setInitParameter("insertHeaders", "true");
        quickTimeout.setInitParameter("maxRequestMs", _maxRequestMs + "");

        _tester.start();

    }
    
    public static class CloseableDoSFilter2 extends CloseableDoSFilter
 	{
    	public void closeConnection(HttpServletRequest request, HttpServletResponse response, Thread thread)
    	{
    		try 
    		{
    			response.getWriter().append("DoSFilter: timeout");
    			response.flushBuffer();
    			super.closeConnection(request,response,thread);
    		}
    		catch (Exception e)
    		{
    			Log.warn(e);
    		}
    	}
 	}
}

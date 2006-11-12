package org.mortbay.jetty.servlet.jsr77;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.UnavailableException;

import org.mortbay.jetty.servlet.ServletHolder;

public class Jsr77ServletHolder extends ServletHolder 
{

	private ServletStatsImpl _servletStats = null;

	public Jsr77ServletHolder() 
	{
	}

	public Jsr77ServletHolder(Servlet servlet)
	{
		super(servlet);
	}
	
	public Jsr77ServletHolder(Class servlet) 
	{
		super(servlet);
	}
	
	public void doStart() throws Exception 
	{
		super.doStart();
		_servletStats = new ServletStatsImpl(getServlet().getServletConfig().getServletName());
	}
	
	public void handle(ServletRequest request, ServletResponse response) 
		throws ServletException, UnavailableException, IOException 
	{
        long startTime =0L;
        long endTime = 0L;
        try
        {
            //start statistic gathering - get the name of Servlet for which this filter will apply, and therefore
            //on whose behalf we are gathering statistics???
            startTime = System.currentTimeMillis();
            super.handle(request, response);
        }
        finally
        {
            //finish statistic gathering
            endTime = System.currentTimeMillis();
            TimeStatisticImpl statistic = (TimeStatisticImpl)_servletStats.getServiceTime();
            statistic.addSample(endTime-startTime, endTime);

        }
	}
	
	public ServletStatsImpl getServletStats()
	{
		return this._servletStats;
	}
}

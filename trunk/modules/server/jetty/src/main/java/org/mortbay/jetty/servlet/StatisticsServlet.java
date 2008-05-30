package org.mortbay.jetty.servlet;

//========================================================================
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
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

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.StatisticsHandler;
import org.mortbay.log.Log;             

public class StatisticsServlet extends HttpServlet
{
    boolean _restrictToLocalhost = true; // defaults to true
    private Server _server = null;
    private StatisticsHandler _statsHandler;
    private MemoryMXBean _memoryBean;

    public void init( ) throws ServletException
    {
        _memoryBean = ManagementFactory.getMemoryMXBean();

        ServletContext context = getServletContext();
        ContextHandler.SContext scontext = (ContextHandler.SContext)context;
        _server = scontext.getContextHandler().getServer();


        Handler[] handlers = _server.getHandlers();

        if ( handlers != null )
        {
            for (Handler handler: handlers )
            {
                /* todo look for existing stats handler
                if ( StatisticsHandler.class.getName().equals( handler.getClass().getName() ) );
                {
                    Log.info( "StatisticsHandler already installed." );
                    _statsHandler = (StatisticsHandler)handler;
                    break;
                }
                */
            }

            if ( _statsHandler == null )
            {
                Log.info( "Installing Statistics Handler" );
                _statsHandler = new StatisticsHandler();
                _server.addHandler( _statsHandler );
            }
        }

        if ( getInitParameter(  "restrictToLocalhost" ) != null )
        {
            _restrictToLocalhost = "true".equals( getInitParameter("restrictToLocalhost") );
        }

    }

    public void doPost(HttpServletRequest sreq, HttpServletResponse sres) throws ServletException, IOException
    {
        doGet(sreq, sres);
    }

    protected void doGet( HttpServletRequest req, HttpServletResponse resp ) throws ServletException, IOException
    {

        if ( _restrictToLocalhost )
        {
            if ( !"127.0.0.1".equals( req.getRemoteAddr() ) )
            {
                resp.sendError( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
                return;
            }
        }

        if ( req.getParameter( "xml" ) != null && "true".equals( req.getParameter( "xml" ) ) )
        {
            sendXmlResponse( resp );
        }
        else
        {
            sendTextResponse( resp );
        }

    }

    private void sendXmlResponse( HttpServletResponse response ) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "<statistics>\n" );

        sb.append( "  <requests>" ).append( _statsHandler.getRequests() ).append( "</requests>\n" );
        sb.append( "  <heapMemoryUsage>" ).append( _memoryBean.getHeapMemoryUsage().getUsed() ).append( "</heapMemoryUsage>\n" );
        sb.append( "  <nonHeapMemoryUsage>" ).append(_memoryBean.getNonHeapMemoryUsage().getUsed() ).append( "</nonHeapMemoryUsage>\n" );

        sb.append( "</statistics>\n");
        response.setContentType("text/xml");
        PrintWriter pout = null;
        pout = response.getWriter();
        pout.write( sb.toString() );
    }

    private void sendTextResponse( HttpServletResponse response ) throws IOException
    {

        StringBuilder sb = new StringBuilder();

        sb.append( "Statistics:<br/>\n" );

        sb.append( "Requests: " + _statsHandler.getRequests() ).append( "<br/>\n" );
        sb.append( "Heap memory usage: " + _memoryBean.getHeapMemoryUsage().getUsed() + " bytes" ).append( "<br />\n" );
        sb.append( "Non-heap memory usage: " + _memoryBean.getNonHeapMemoryUsage().getUsed() + " bytes" ).append( "<br />\n" );

        response.setContentType("text/html");
        PrintWriter pout = null;
        pout = response.getWriter();
        pout.write( sb.toString() );

    }
}
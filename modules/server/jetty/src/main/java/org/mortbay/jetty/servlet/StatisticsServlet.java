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

import org.mortbay.jetty.Connector;
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
    private Connector[] _connectors;

    public void init() throws ServletException
    {
        _memoryBean = ManagementFactory.getMemoryMXBean();

        ServletContext context = getServletContext();
        ContextHandler.SContext scontext = (ContextHandler.SContext) context;
        _server = scontext.getContextHandler().getServer();

        Handler handler = _server.getChildHandlerByClass(StatisticsHandler.class);

        if (handler != null)
        {
            _statsHandler = (StatisticsHandler) handler;
        } 
        else
        {
            Log.info("Installing Statistics Handler");
            _statsHandler = new StatisticsHandler();
            _server.addHandler(_statsHandler);
        }


        _connectors = _server.getConnectors();

        if (getInitParameter("restrictToLocalhost") != null)
        {
            _restrictToLocalhost = "true".equals(getInitParameter("restrictToLocalhost"));
        }

    }

    public void doPost(HttpServletRequest sreq, HttpServletResponse sres) throws ServletException, IOException
    {
        doGet(sreq, sres);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {

        if (_restrictToLocalhost)
        {
            if (!"127.0.0.1".equals(req.getRemoteAddr()))
            {
                resp.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                return;
            }
        }

        if (req.getParameter("xml") != null && "true".equals(req.getParameter("xml")))
        {
            sendXmlResponse(resp);
        } else
        {
            sendTextResponse(resp);
        }

    }

    private void sendXmlResponse(HttpServletResponse response) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        sb.append("<statistics>\n");

        sb.append("  <requests>").append(_statsHandler.getRequests()).append("</requests>\n");
        sb.append("  <heapMemoryUsage>").append(_memoryBean.getHeapMemoryUsage().getUsed()).append("</heapMemoryUsage>\n");
        sb.append("  <nonHeapMemoryUsage>").append(_memoryBean.getNonHeapMemoryUsage().getUsed()).append("</nonHeapMemoryUsage>\n");

        sb.append("</statistics>\n");
        response.setContentType("text/xml");
        PrintWriter pout = null;
        pout = response.getWriter();
        pout.write(sb.toString());
    }

    private void sendTextResponse(HttpServletResponse response) throws IOException
    {

        StringBuilder sb = new StringBuilder();

        sb.append("<h1>Statistics:</h1>\n");

        sb.append("<h2>Requests:</h2>\n");
        sb.append("Statistics gathering started " + _statsHandler.getStatsOnMs() + "ms ago").append("<br />\n");
        sb.append("Total requests: " + _statsHandler.getRequests()).append("<br />\n");
        sb.append("Total requests timed out: " + _statsHandler.getRequestsTimedout()).append("<br />\n");
        sb.append("Total requests resumed: " + _statsHandler.getRequestsResumed()).append("<br />\n");
        sb.append("Current requests active: " + _statsHandler.getRequestsActive()).append("<br />\n");
        sb.append("Min concurrent requests active: " + _statsHandler.getRequestsActiveMin()).append("<br />\n");
        sb.append("Max concurrent requests active: " + _statsHandler.getRequestsActiveMax()).append("<br />\n");
        sb.append("Total requests duration: " + _statsHandler.getRequestsDurationTotal()).append("<br />\n");
        sb.append("Average request duration: " + _statsHandler.getRequestsDurationAve()).append("<br />\n");
        sb.append("Min request duration: " + _statsHandler.getRequestsDurationMin()).append("<br />\n");
        sb.append("Max request duration: " + _statsHandler.getRequestsDurationMax()).append("<br />\n");

        sb.append("1xx responses: " + _statsHandler.getResponses1xx()).append("<br />\n");
        sb.append("2xx responses: " + _statsHandler.getResponses2xx()).append("<br />\n");
        sb.append("3xx responses: " + _statsHandler.getResponses3xx()).append("<br />\n");
        sb.append("4xx responses: " + _statsHandler.getResponses4xx()).append("<br />\n");
        sb.append("5xx responses: " + _statsHandler.getResponses5xx()).append("<br />\n");

        sb.append("<h2>Connections:</h2>\n");
        for (Connector connector : _connectors)
        {
            sb.append("<h3>" + connector.getName() + "</h3>");
            
            if (connector.getStatsOn())
            {
                sb.append("Statistics gathering started " +  connector.getStatsOnMs() + "ms ago").append("<br />\n");
                sb.append("Total requests: " +  connector.getRequests()).append("<br />\n");
                sb.append("Total connections: " +  connector.getConnections()).append("<br />\n");
                sb.append("Current connections open: " + connector.getConnectionsOpen());
                sb.append("Min concurrent connections open: " +  connector.getConnectionsOpenMin()).append("<br />\n");
                sb.append("Max concurrent connections open: " +  connector.getConnectionsOpenMax()).append("<br />\n");
                sb.append("Total connections duration: " +  connector.getConnectionsDurationTotal()).append("<br />\n");
                sb.append("Average connection duration: " +  connector.getConnectionsDurationAve()).append("<br />\n");
                sb.append("Average requests per connection: " +  connector.getConnectionsRequestsAve()).append("<br />\n");
                sb.append("Min requests per connection: " +  connector.getConnectionsRequestsMin()).append("<br />\n");
                sb.append("Max requests per connection: " +  connector.getConnectionsRequestsMax()).append("<br />\n");
            }
            else
            {
                sb.append("Statistics gathering off.\n");
            }
                
        }

        sb.append("<h2>Memory:</h2>\n");
        sb.append("Heap memory usage: " + _memoryBean.getHeapMemoryUsage().getUsed() + " bytes").append("<br />\n");
        sb.append("Non-heap memory usage: " + _memoryBean.getNonHeapMemoryUsage().getUsed() + " bytes").append("<br />\n");

        response.setContentType("text/html");
        PrintWriter pout = null;
        pout = response.getWriter();
        pout.write(sb.toString());

    }
}
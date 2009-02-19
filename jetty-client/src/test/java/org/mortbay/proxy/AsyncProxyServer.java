package org.mortbay.proxy;

import org.mortbay.jetty.server.Connector;
import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.nio.SelectChannelConnector;
import org.mortbay.jetty.server.servlet.FilterHolder;
import org.mortbay.jetty.server.servlet.ServletHandler;
import org.mortbay.jetty.server.servlet.ServletHolder;

public class AsyncProxyServer
{
    public static void main(String[] args)
        throws Exception
    {
        Server server = new Server();
        Connector connector=new SelectChannelConnector();
        connector.setPort(8888);
        server.setConnectors(new Connector[]{connector});
        
        ServletHandler handler=new ServletHandler();
        server.setHandler(handler);
        
        FilterHolder gzip = handler.addFilterWithMapping("org.mortbay.jetty.server.server.servlet.GzipFilter","/*",0);
        gzip.setAsyncSupported(true);
        gzip.setInitParameter("minGzipSize","256");
        ServletHolder proxy = handler.addServletWithMapping("org.mortbay.proxy.AsyncProxyServlet","/");
        proxy.setAsyncSupported(true);
        
        server.start();
        server.join();
    }
}

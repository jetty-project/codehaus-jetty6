package org.mortbay.proxy;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;

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
        
        FilterHolder gzip = handler.addFilterWithMapping("org.mortbay.servlet.GzipFilter","/*",0);
        ServletHolder proxy = handler.addServletWithMapping("org.mortbay.proxy.AsyncProxyServlet","/");
        
        server.start();
        server.join();
    }
}

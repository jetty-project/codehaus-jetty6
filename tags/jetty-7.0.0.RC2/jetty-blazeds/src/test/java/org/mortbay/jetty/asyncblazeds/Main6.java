package org.mortbay.jetty.asyncblazeds;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;



public class Main6
{
    public static void main(String[] args)
    throws Exception        
    {               
        Server server = new Server();
        Connector connector=new SelectChannelConnector();
        connector.setPort(8086);
        server.setConnectors(new Connector[]{connector});
        
        HandlerCollection contexts = new HandlerCollection();
        Context messageBroker = new Context(contexts, "/samples", WebAppContext.SESSIONS );
        ServletHolder holder = messageBroker.addServlet("flex.messaging.MessageBrokerServlet", "/messagebroker/*");
        holder.setInitParameter("services.configuration.file",System.getProperty("user.dir") + "/src/test/resources/services-config.xml");
        FilterHolder filter = messageBroker.addFilter(org.eclipse.jetty.continuation.ContinuationFilter.class,"/*", 0);
        
        ContextHandler polling=new ContextHandler("/polling");
        ResourceHandler pollingClient = new ResourceHandler();
        pollingClient.setWelcomeFiles(new String[]{"index.html"});
        pollingClient.setResourceBase("src/test/resources/polling-chat");
        
        polling.setHandler(pollingClient);

        ContextHandler async=new ContextHandler("/async");
        ResourceHandler asyncClient = new ResourceHandler();
        asyncClient.setWelcomeFiles(new String[]{"index.html"});
        asyncClient.setResourceBase("src/test/resources/async-chat");
        async.setHandler(asyncClient);

        contexts.setHandlers(new Handler[]{messageBroker,polling, async});
        
        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler()});
        
        server.setHandler(handlers);

        server.start();
        server.join();
    }
}

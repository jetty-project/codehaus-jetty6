package org.mortbay.jetty.asyncblazeds;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

public class Main
{
    public static void main(String[] args)
    throws Exception        
    {               
        Server server = new Server();
        Connector connector=new SelectChannelConnector();
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        
        HandlerCollection contexts = new HandlerCollection();
        ServletContextHandler messageBroker = new ServletContextHandler(contexts, "/samples", WebAppContext.SESSIONS );
        ServletHolder holder = messageBroker.addServlet("flex.messaging.MessageBrokerServlet", "/messagebroker/*");
        holder.setInitParameter("services.configuration.file",System.getProperty("user.dir") + "/src/test/resources/services-config.xml");

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

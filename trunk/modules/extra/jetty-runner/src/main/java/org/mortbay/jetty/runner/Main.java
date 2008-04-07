package org.mortbay.jetty.runner;

import java.util.HashMap;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            if (args.length>0&&args[0].equalsIgnoreCase("--help"))
            {
                usage(null);
            }
            else if (args.length>0&&args[0].equalsIgnoreCase("--version"))
            {
                System.err.println("org.mortbay.jetty.Server: "+Server.getVersion());
                System.exit(1);
            }


            Server server=null;
            ContextHandlerCollection contexts=null;
            RequestLogHandler requestLogHandler=null;

            String logFile=null;
            String contextPath="/";
            boolean contextPathSet=false;
            int port=8080;
            for (int i=0;i<args.length;i++)
            {
                if ("--port".equals(args[i]))
                    port=Integer.parseInt(args[++i]);
                else if ("--log".equals(args[i]))
                    logFile=args[++i];
                else if ("--path".equals(args[i]))
                {
                    contextPath=args[++i];
                    contextPathSet=true;
                }
                else
                {
                    if (server==null)
                    {
                        // build the server
                        server = new Server(port);

                        HandlerCollection handlers = new HandlerCollection();
                        contexts = new ContextHandlerCollection();
                        requestLogHandler = new RequestLogHandler();
                        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
                        server.setHandler(handlers);

                    }

                    // Create a context
                    Resource ctx = Resource.newResource(args[i]);
                    if (!ctx.exists())
                        usage("Context '"+ctx+"' does not exist");

                    // Configure the context
                    if (!ctx.isDirectory() && ctx.toString().toLowerCase().endsWith(".xml"))
                    {
                        // It is a context config file
                        XmlConfiguration xmlConfiguration=new XmlConfiguration(ctx.getURL());
                        HashMap<String,Object> properties = new HashMap<String,Object>();
                        properties.put("Server", server);
                        xmlConfiguration.setProperties(properties);
                        ContextHandler handler=(ContextHandler)xmlConfiguration.configure();
                        contexts.addHandler(handler);
                        if (contextPathSet)
                            handler.setContextPath(contextPath);
                    }
                    else
                    {
                        // assume it is a WAR file
                        WebAppContext webapp = new WebAppContext(contexts,ctx.toString(),contextPath);
                    }
                }
            }

            if (server==null)
                usage("No Contexts defined");
            server.setStopAtShutdown(true);
            server.setSendServerVersion(true);

            if (logFile!=null)
            {
                NCSARequestLog requestLog = new NCSARequestLog(logFile);
                requestLog.setExtended(false);
                requestLogHandler.setRequestLog(requestLog);
            }

            server.start();
            server.join();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            usage(null);
        }
    }
    
    public static void usage(String error)
    {
        if (error!=null)
            System.err.println("ERROR: "+error);
        System.err.println("Usage: java [-DDEBUG] [-Djetty.home=dir] -jar jetty-runner.jar [--help|--version] [ server opts] [[ context opts] context ...] ");
        System.err.println("Server Options:");
        System.err.println(" --log file         - request log filename (with optional 'yyyy_mm_dd' wildcard");
        System.err.println(" --port n           - port to listen on (default 8080)");
        System.err.println("Context Options:");
        System.err.println(" --path /path       - context path (default /)");
        System.err.println(" context            - WAR file, web app dir or context.xml file");
        System.exit(1);
    }
}

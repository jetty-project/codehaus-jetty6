package org.mortbay.jetty.runner;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;

public class Runner
{
    protected Server _server;
    protected URLClassLoader _classLoader;
    protected List<URL> _classpath=new ArrayList<URL>();
    protected ContextHandlerCollection _contexts;
    protected RequestLogHandler _logHandler;
    protected String logFile;
    
    
    public Runner()
    {
        
    }
    
    public void configure(String[] args) throws Exception
    {
        // handle classpath bits first so we can initialize the log mechanism.
        

        for (int i=0;i<args.length;i++)
        {
            if ("--lib".equals(args[i]))
            {
                Resource lib = Resource.newResource(args[++i]);
                if (!lib.exists() || !lib.isDirectory())
                    usage("No such lib directory "+lib);
                expandJars(lib);
            }
            else if ("--jar".equals(args[i]))
            {
                Resource jar = Resource.newResource(args[++i]);
                if (!jar.exists() || jar.isDirectory())
                    usage("No such jar "+jar);
                _classpath.add(jar.getURL());
            }
            else if ("--classes".equals(args[i]))
            {
                Resource classes = Resource.newResource(args[++i]);
                if (!classes.exists() || !classes.isDirectory())
                    usage("No such classes directory "+classes);
                _classpath.add(classes.getURL());
            }
            else if (args[i].startsWith("--"))
                i++;
        }

        initClassLoader();
        Log.initialize();
        
        if (System.getProperties().containsKey("DEBUG"))
            Log.getLog().setDebugEnabled(true);
        
        Log.info("Runner");
        Log.debug("Runner classpath {}",_classpath);
        
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
            else if ("--lib".equals(args[i]))
            {
            }
            else if ("--jar".equals(args[i]))
            {
            }
            else if ("--classes".equals(args[i]))
            {
            }
            else
            {
                if (_server==null)
                {
                    // build the server
                    _server = new Server(port);

                    HandlerCollection handlers = new HandlerCollection();
                    _contexts = new ContextHandlerCollection();
                    _logHandler = new RequestLogHandler();
                    handlers.setHandlers(new Handler[]{_contexts,new DefaultHandler(),_logHandler});
                    _server.setHandler(handlers);

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
                    properties.put("Server", _server);
                    xmlConfiguration.setProperties(properties);
                    ContextHandler handler=(ContextHandler)xmlConfiguration.configure();
                    _contexts.addHandler(handler);
                    if (contextPathSet)
                        handler.setContextPath(contextPath);
                }
                else
                {
                    // assume it is a WAR file
                    WebAppContext webapp = new WebAppContext(_contexts,ctx.toString(),contextPath);
                }
            }
        }

        if (_server==null)
            usage("No Contexts defined");
        _server.setStopAtShutdown(true);
        _server.setSendServerVersion(true);

        if (logFile!=null)
        {
            NCSARequestLog requestLog = new NCSARequestLog(logFile);
            requestLog.setExtended(false);
            _logHandler.setRequestLog(requestLog);
        }

    }
    

    public void run() throws Exception
    {
        _server.start();
        _server.join();

    }

    protected void expandJars(Resource lib) throws IOException
    {
        String[] list = lib.list();
        if (list==null)
            return;
        
        for (String path : list)
        {
            if (".".equals(path) || "..".equals(path))
                continue;
            
            Resource item = lib.addPath(path);
            
            if (item.isDirectory())
                expandJars(item);
            else
            {
                if (path.toLowerCase().endsWith(".jar") ||
                    path.toLowerCase().endsWith(".zip"))
                {
                    URL url = item.getURL();
                    _classpath.add(url);
                }
            }
        }
    }
    
    protected void initClassLoader()
    {
        if (_classLoader==null && _classpath!=null && _classpath.size()>0)
        {
            ClassLoader context=Thread.currentThread().getContextClassLoader();

            if (context==null)
                _classLoader=new URLClassLoader(_classpath.toArray(new URL[_classpath.size()]));
            else
                _classLoader=new URLClassLoader(_classpath.toArray(new URL[_classpath.size()]),context);
            
            Thread.currentThread().setContextClassLoader(_classLoader);
        }   
    }
    
    public void usage(String error)
    {
        if (error!=null)
            System.err.println("ERROR: "+error);
        System.err.println("Usage: java [-DDEBUG] [-Djetty.home=dir] -jar jetty-runner.jar [--help|--version] [ server opts] [[ context opts] context ...] ");
        System.err.println("Server Options:");
        System.err.println(" --log file         - request log filename (with optional 'yyyy_mm_dd' wildcard");
        System.err.println(" --port n           - port to listen on (default 8080)");
        System.err.println(" --jar file         - a jar to be added to the classloader");
        System.err.println(" --lib dir          - a directory of jars to be added to the classloader");
        System.err.println(" --classes dir      - a directory of classes to be added to the classloader");
        System.err.println("Context Options:");
        System.err.println(" --path /path       - context path (default /)");
        System.err.println(" context            - WAR file, web app dir or context.xml file");
        System.exit(1);
    }
    
    public static void main(String[] args)
    {
        Runner runner = new Runner();
        
        try
        {
            if (args.length>0&&args[0].equalsIgnoreCase("--help"))
            {
                runner.usage(null);
            }
            else if (args.length>0&&args[0].equalsIgnoreCase("--version"))
            {
                System.err.println("org.mortbay.jetty.Server: "+Server.getVersion());
                System.exit(1);
            }
            
            runner.configure(args);
            runner.run();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            runner.usage(null);
        }
    }
}

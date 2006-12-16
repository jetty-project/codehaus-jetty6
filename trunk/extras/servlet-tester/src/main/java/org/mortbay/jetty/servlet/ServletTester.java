package org.mortbay.jetty.servlet;

import java.util.Enumeration;
import java.util.EventListener;

import org.mortbay.jetty.LocalConnector;
import org.mortbay.jetty.Server;
import org.mortbay.util.Attributes;



/* ------------------------------------------------------------ */
/** Testing support for servlets and filters.
 * 
 * Allows a programatic setup of a context with servlets and filters for 
 * testing.  Raw HTTP requests may be sent to the context and responses received.
 * To avoid handling raw HTTP see {@link org.mortbay.jetty.HttpTester}.
 * <pre>
 *      ServletTester tester=new ServletTester();
 *      tester.setContextPath("/context");
 *      tester.addServlet(TestServlet.class, "/servlet/*");
 *      tester.addServlet("org.mortbay.jetty.servlet.DefaultServlet", "/");
 *      tester.start();
 *      String response = tester.getResponses("GET /context/servlet/info HTTP/1.0\r\n\r\n");
 * </pre>
 * 
 * @see org.mortbay.jetty.HttpTester
 * @author gregw
 *
 */
public class ServletTester
{
    Server server = new Server();
    LocalConnector connector = new LocalConnector();
    Context context = new Context(Context.SESSIONS|Context.SECURITY);
    
    public ServletTester()
    {
        try
        {
            server.setSendServerVersion(false);
            server.addConnector(connector);
            server.addHandler(context);
        }
        catch (Error e)
        {
            throw e;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    /* ------------------------------------------------------------ */
    public void start() throws Exception
    {
        server.start();
    }
    
    /* ------------------------------------------------------------ */
    public void stop() throws Exception
    {
        server.stop();
    }
    
    /* ------------------------------------------------------------ */
    /** Get raw HTTP responses from raw HTTP requests.
     * Multiple requests and responses may be handled, but only if
     * persistent connections conditions apply.
     * @param rawRequests String of raw HTTP requests
     * @return String of raw HTTP responses
     * @throws Exception
     */
    public String getResponses(String rawRequests) throws Exception
    {
        connector.reopen();
        //System.err.println(">>>>\n"+rawRequests);
        String responses = connector.getResponses(rawRequests);
        //System.err.println("<<<<\n"+responses);
        return responses;
    }
    

    /* ------------------------------------------------------------ */
    /**
     * @param listener
     * @see org.mortbay.jetty.handler.ContextHandler#addEventListener(java.util.EventListener)
     */
    public void addEventListener(EventListener listener)
    {
        context.addEventListener(listener);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param filterClass
     * @param pathSpec
     * @param dispatches
     * @return
     * @see org.mortbay.jetty.servlet.Context#addFilter(java.lang.Class, java.lang.String, int)
     */
    public FilterHolder addFilter(Class filterClass, String pathSpec, int dispatches)
    {
        return context.addFilter(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param filterClass
     * @param pathSpec
     * @param dispatches
     * @return
     * @see org.mortbay.jetty.servlet.Context#addFilter(java.lang.String, java.lang.String, int)
     */
    public FilterHolder addFilter(String filterClass, String pathSpec, int dispatches)
    {
        return context.addFilter(filterClass,pathSpec,dispatches);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param servlet
     * @param pathSpec
     * @return
     * @see org.mortbay.jetty.servlet.Context#addServlet(java.lang.Class, java.lang.String)
     */
    public ServletHolder addServlet(Class servlet, String pathSpec)
    {
        return context.addServlet(servlet,pathSpec);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param className
     * @param pathSpec
     * @return
     * @see org.mortbay.jetty.servlet.Context#addServlet(java.lang.String, java.lang.String)
     */
    public ServletHolder addServlet(String className, String pathSpec)
    {
        return context.addServlet(className,pathSpec);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @return
     * @see org.mortbay.jetty.handler.ContextHandler#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name)
    {
        return context.getAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.handler.ContextHandler#getAttributeNames()
     */
    public Enumeration getAttributeNames()
    {
        return context.getAttributeNames();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.handler.ContextHandler#getAttributes()
     */
    public Attributes getAttributes()
    {
        return context.getAttributes();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.handler.ContextHandler#getResourceBase()
     */
    public String getResourceBase()
    {
        return context.getResourceBase();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name
     * @param value
     * @see org.mortbay.jetty.handler.ContextHandler#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value)
    {
        context.setAttribute(name,value);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param classLoader
     * @see org.mortbay.jetty.handler.ContextHandler#setClassLoader(java.lang.ClassLoader)
     */
    public void setClassLoader(ClassLoader classLoader)
    {
        context.setClassLoader(classLoader);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param contextPath
     * @see org.mortbay.jetty.handler.ContextHandler#setContextPath(java.lang.String)
     */
    public void setContextPath(String contextPath)
    {
        context.setContextPath(contextPath);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param eventListeners
     * @see org.mortbay.jetty.handler.ContextHandler#setEventListeners(java.util.EventListener[])
     */
    public void setEventListeners(EventListener[] eventListeners)
    {
        context.setEventListeners(eventListeners);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param resourceBase
     * @see org.mortbay.jetty.handler.ContextHandler#setResourceBase(java.lang.String)
     */
    public void setResourceBase(String resourceBase)
    {
        context.setResourceBase(resourceBase);
    }
    
}

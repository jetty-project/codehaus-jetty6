package org.mortbay.jetty.tests.webapp.policy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.Properties;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckerServlet extends HttpServlet
{
    private static final long serialVersionUID = -1677050154010585657L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Properties props = new Properties();
        Checker checker = new Checker();

        String pathInfo = req.getPathInfo();
        if (pathInfo == null)
        {
            System.out.println("ERROR: No test method specified.");
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String testname = pathInfo;
        if (testname.startsWith("/"))
        {
            testname = testname.substring(1);
        }

        Method testmethod = findTestMethod(testname);
        if (testmethod == null)
        {
            System.out.println("ERROR: No test method not found: " + testname);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try
        {
            Object args[] = new Object[]
            { props, checker };
            testmethod.invoke(this,args);
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.out);
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        PropertiesUtil.writePropertiesOutput(resp,props);
    }

    private Method findTestMethod(String testname)
    {
        Class<?>[] parameterTypes = new Class[]
        { Properties.class, Checker.class };
        try
        {
            return this.getClass().getMethod(testname,parameterTypes);
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.out);
            return null;
        }
    }

    public void processFooWebappContextChecks(Properties props, Checker checker)
    {
        String key = "Foo Webapp Context Checks";
        try
        {
            key = "Get ServletContext";
            ServletContext context = getServletContext();
            checker.success(props,key);

            key = "Get /foo Context";
            ServletContext foo = context.getContext("/foo");
            checker.success(props,key);

            key = "Get /foo ServletContextName";
            String name = foo.getServletContextName();
            checker.success(props,key,"Got Name - " + name);
        }
        catch (Throwable t)
        {
            checker.failure(props,key,t);
            return;
        }
    }

    public void processFooWebappRequestDispatcherChecks(Properties props, Checker checker)
    {
        String key = "Foo Webapp Request Dispatcher Checks";
        try
        {
            key = "Get ServletContext";
            ServletContext context = getServletContext();
            checker.success(props,key);

            key = "Get /foo RequestDispatcher";
            @SuppressWarnings("unused")
            RequestDispatcher foo = context.getRequestDispatcher("/foo");
            checker.success(props,key);
        }
        catch (Throwable t)
        {
            checker.failure(props,key,t);
            return;
        }
    }

    public void processClassloaderChecks(Properties props, Checker checker)
    {
        String badurl = "http://not.going.to.work";
        try
        {
            URL url = new URL(badurl);
            @SuppressWarnings("unused")
            URLClassLoader cl = new URLClassLoader(new URL[]
            { url });
            checker.success(props,"Create Classloader: " + badurl);
        }
        catch (MalformedURLException e)
        {
            checker.failure(props,"Process Classloader: " + badurl,e);
        }
    }

    public void processExitChecks(Properties props, Checker checker)
    {
        checker.canExit(props);
    }

    public void processFilesystemChecks(Properties props, Checker checker)
    {
        String jettyHome = System.getProperty("user.dir");
        checker.canRead(props,jettyHome + "/lib/policy/jetty.policy");
        checker.canWrite(props,jettyHome + "/lib/policy/jetty.policy");
        checker.canRead(props,jettyHome + "/lib/");
        checker.canWrite(props,jettyHome + "/lib/");
        checker.canRead(props,jettyHome + "/logs/");
        checker.canWrite(props,jettyHome + "/logs/");
        checker.canRead(props,jettyHome);
        checker.canWrite(props,jettyHome);

        String tmpDir = System.getProperty("java.io.tmpdir");
        checker.canRead(props,tmpDir);
        checker.canWrite(props,tmpDir);

        File webappDir = (File)checker.getServletAttribute(this,props,"javax.servlet.context.tempdir");
        checker.canRead(props,webappDir);
        checker.canWrite(props,webappDir);
    }

    public void processJettyLogChecks(Properties props, Checker checker)
    {
        Calendar c = Calendar.getInstance();
        String jettyHome = System.getProperty("user.dir");
        String logFilename = String.format("%s/logs/%2$tY_%2$tm_%2$td.request.log",jettyHome,c);
        checker.canRead(props,logFilename);
    }

    public void processLibChecks(Properties props, Checker checker)
    {
        checker.canLoadLibrary(props,"foo.so");
    }

    public void processSystemPropertyChecks(Properties props, Checker checker)
    {
        checker.canReadSystemProperty(props,"__ALLOWED_READ_PROPERTY");
        checker.canWriteSystemProperty(props,"__ALLOWED_READ_PROPERTY","SUCCESS");
        checker.canReadSystemProperty(props,"__ALLOWED_WRITE_PROPERTY");
        checker.canWriteSystemProperty(props,"__ALLOWED_WRITE_PROPERTY","SUCCESS");
        checker.canReadSystemProperty(props,"__UNDECLARED_PROPERTY");
        checker.canWriteSystemProperty(props,"__UNDECLARED_PROPERTY","SUCCESS");
    }
}

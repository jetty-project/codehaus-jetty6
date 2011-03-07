package org.mortbay.jetty.tests.webapp.policy;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Calendar;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CheckerServlet extends HttpServlet
{
    private static final long serialVersionUID = -1677050154010585657L;
    private static final Logger LOG = Logger.getLogger(CheckerServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Properties props = new Properties();
        Checker checker = new Checker();

        String pathInfo = req.getPathInfo();
        LOG.info("pathInfo = " + pathInfo);
        if ("filesystem".equals(pathInfo))
        {
            processFilesystemChecks(props,checker);
        }
        else if ("system.properties".equals(pathInfo))
        {
            processSystemPropertyChecks(props,checker);
        }
        else if ("jetty.log".equals(pathInfo))
        {
            processJettyLogChecks(props,checker);
        }
        else if ("classloader".equals(pathInfo))
        {
            processClassloaderChecks(props,checker);
        }
        else if ("libs".equals(pathInfo))
        {
            processLibChecks(props,checker);
        }
        else if ("exit".equals(pathInfo))
        {
            processExitChecks(props,checker);
        }

        PropertiesUtil.writePropertiesOutput(resp,props);
    }

    private void processClassloaderChecks(Properties props, Checker checker)
    {
        String badurl = "http://not.going.to.work";
        try
        {
            URL url = new URL(badurl);
            URLClassLoader cl = new URLClassLoader(new URL[]
            { url });
            checker.success(props,"Create Classloader: " + badurl);
        }
        catch (MalformedURLException e)
        {
            checker.failure(props,"Process Classloader: " + badurl,e);
        }
    }

    private void processExitChecks(Properties props, Checker checker)
    {
        checker.canExit(props);
    }

    private void processFilesystemChecks(Properties props, Checker checker)
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

    private void processJettyLogChecks(Properties props, Checker checker)
    {
        Calendar c = Calendar.getInstance();
        String jettyHome = System.getProperty("user.dir");
        String logFilename = String.format("%s/logs/%2$tY_%2$tm_%2$td.request.log",jettyHome,c);
        checker.canRead(props,logFilename);
    }

    private void processLibChecks(Properties props, Checker checker)
    {
        checker.canLoadLibrary(props,"/lib/foo.so");
    }

    private void processSystemPropertyChecks(Properties props, Checker checker)
    {
        checker.canReadSystemProperty(props,"__ALLOWED_READ_PROPERTY");
        checker.canWriteSystemProperty(props,"__ALLOWED_READ_PROPERTY","SUCCESS");
        checker.canReadSystemProperty(props,"__ALLOWED_WRITE_PROPERTY");
        checker.canWriteSystemProperty(props,"__ALLOWED_WRITE_PROPERTY","SUCCESS");
        checker.canReadSystemProperty(props,"__UNDECLARED_PROPERTY");
        checker.canWriteSystemProperty(props,"__UNDECLARED_PROPERTY","SUCCESS");
    }
}

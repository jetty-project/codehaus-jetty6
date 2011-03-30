package org.mortbay.jetty.tests.webapp.policy.checkers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.mortbay.jetty.tests.webapp.policy.AbstractSecurityCheck;
import org.mortbay.jetty.tests.webapp.policy.SecurityCheckContext;
import org.mortbay.jetty.tests.webapp.policy.SecurityResult;

public class NoSecurityChecker extends AbstractSecurityCheck
{
    public void testFilesystemAccess(SecurityCheckContext check)
    {
        String jettyHome = getJettyHome();

        canRead(check,jettyHome + "/lib/policy/jetty.policy");
        canWrite(check,jettyHome + "/lib/policy/jetty.policy");
        canRead(check,jettyHome + "/lib/");
        canWrite(check,jettyHome + "/lib/");
        canRead(check,jettyHome + "/logs/");
        canWrite(check,jettyHome + "/logs/");

        String tmpDir = System.getProperty("java.io.tmpdir");
        canRead(check,tmpDir);
        canWrite(check,tmpDir);

        ServletContext context = getServletContext(check);
        if (context == null)
        {
            // Can't run other tests below here
            return;
        }

        File webappDir = getServletContextTempDir(check,context);
        if (webappDir != null)
        {
            canRead(check,webappDir.getAbsolutePath());
            canWrite(check,webappDir.getAbsolutePath());
        }
    }

    public void testFooWebappContext(SecurityCheckContext check)
    {
        String fooContext = "/foo";
        SecurityResult result = new SecurityResult(".getContext(%s)",fooContext);
        try
        {
            ServletContext context = getServletContext(check);
            if (context != null)
            {
                ServletContext foo = context.getContext("/foo");
                result.assertNotNull("Get Servlet Context: " + fooContext,foo);
            }
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }

    public void testFooWebappRequestDispatcher(SecurityCheckContext check)
    {
        String fooContext = "/foo";
        SecurityResult result = new SecurityResult("getRequestDispatcher(%s)",fooContext);
        try
        {
            ServletContext context = getServletContext(check);
            if (context != null)
            {
                RequestDispatcher foo = context.getRequestDispatcher("/foo");
                result.assertNotNull("Get Servlet Context: " + fooContext,foo);
            }
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }

    public void testClassloader(SecurityCheckContext check)
    {
        SecurityResult result = new SecurityResult("classloader");
        String badurl = "http://not.going.to.work";
        try
        {
            URL url = new URL(badurl);
            URLClassLoader cl = new URLClassLoader(new URL[]
            { url });
            result.assertNotNull("Create Classloader: " + badurl,cl);
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }

    public void testExit(SecurityCheckContext check)
    {
        SecurityResult result = new SecurityResult("exit");
        try
        {
            System.exit(-99);
            // In this mode, this will likely never be reached.
            result.success("Was able to exit");
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }

    private File getServletContextTempDir(SecurityCheckContext check, ServletContext context)
    {
        SecurityResult result = new SecurityResult("filesystem.get.servlet.tmpdir");
        try
        {
            String key = "javax.servlet.context.tempdir";
            Object obj = context.getAttribute(key);
            result.assertNotNull(".getAttribue('" + key + "')",obj);
            return new File(obj.toString());
        }
        catch (Throwable t)
        {
            result.failure(t);
            return null;
        }
        finally
        {
            check.addResult(result);
        }
    }

    private ServletContext getServletContext(SecurityCheckContext check)
    {
        SecurityResult result = new SecurityResult("filesystem.get.servletcontext");
        try
        {
            ServletContext context = check.getServlet().getServletContext();
            result.assertNotNull("servlet.getServletContext()",context);
            return context;
        }
        catch (Throwable t)
        {
            result.failure(t);
            return null;
        }
        finally
        {
            check.addResult(result);
        }
    }

    private void canWrite(SecurityCheckContext check, String rawpath)
    {
        SecurityResult result = new SecurityResult("filesystem.can.write|%s",rawpath);
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            result.assertEquals("File(" + rawpath + ").canWrite()",true,path.canWrite());
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }

    private void canRead(SecurityCheckContext check, String rawpath)
    {
        SecurityResult result = new SecurityResult("filesystem.can.read|%s",rawpath);
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            result.assertEquals("File(" + rawpath + ").canRead()",true,path.canRead());
        }
        catch (Throwable t)
        {
            result.failure(t);
        }
        finally
        {
            check.addResult(result);
        }
    }
}

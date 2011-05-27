package org.mortbay.jetty.tests.webapp.policy.checkers;

import java.io.File;

import javax.servlet.ServletContext;

import org.mortbay.jetty.tests.webapp.policy.AbstractSecurityCheck;
import org.mortbay.jetty.tests.webapp.policy.SecurityCheckContext;
import org.mortbay.jetty.tests.webapp.policy.SecurityResult;

public class ParanoidSecurityChecker extends AbstractSecurityCheck
{
    public void testFilesystemAccess(SecurityCheckContext check)
    {
        String jettyHome = getJettyHome();

        deniedRead(check,jettyHome + "/lib/policy/jetty.policy");
        deniedWrite(check,jettyHome + "/lib/policy/jetty.policy");
        deniedRead(check,jettyHome + "/lib/");
        deniedWrite(check,jettyHome + "/lib/");
        deniedRead(check,jettyHome + "/logs/");
        deniedWrite(check,jettyHome + "/logs/");

        String tmpDir = System.getProperty("java.io.tmpdir");
        deniedRead(check,tmpDir);
        deniedWrite(check,tmpDir);

        ServletContext context = getServletContext(check);
        if (context == null)
        {
            // Can't run other tests below here
            return;
        }

        File webappDir = getServletContextTempDir(check,context);
        if (webappDir != null)
        {
            deniedRead(check,webappDir.getAbsolutePath());
            deniedWrite(check,webappDir.getAbsolutePath());
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

}

package org.mortbay.jetty.tests.webapp.policy.checkers;

import java.io.File;
import java.security.AccessControlException;

import javax.servlet.ServletContext;

import org.apache.commons.io.FilenameUtils;
import org.mortbay.jetty.tests.webapp.policy.AbstractSecurityCheck;
import org.mortbay.jetty.tests.webapp.policy.SecurityCheckContext;
import org.mortbay.jetty.tests.webapp.policy.SecurityResult;

public class ParanoidSecurityChecker extends AbstractSecurityCheck
{
    public void processFilesystemAccess(SecurityCheckContext check)
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

    private void deniedWrite(SecurityCheckContext check, String rawpath)
    {
        SecurityResult result = new SecurityResult("filesystem.denied.write|%s",rawpath);
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            result.assertEquals("File(" + rawpath + ").canWrite()",false,path.canWrite());
        }
        catch (AccessControlException e)
        {
            result.successExpected(e);
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

    private void deniedRead(SecurityCheckContext check, String rawpath)
    {
        SecurityResult result = new SecurityResult("filesystem.denied.read|%s",rawpath);
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            result.assertEquals("File(" + rawpath + ").canRead()",false,path.canRead());
        }
        catch (AccessControlException e)
        {
            result.successExpected(e);
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

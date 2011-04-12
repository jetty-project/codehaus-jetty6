package org.mortbay.jetty.tests.webapp.policy;

import java.io.File;
import java.security.AccessControlException;

import org.apache.commons.io.FilenameUtils;

public abstract class AbstractSecurityCheck
{
    public String getJettyHome()
    {
        String jettyHome = System.getProperty("jetty.home");
        if (jettyHome == null)
        {
            return System.getProperty("user.dir");
        }
        return jettyHome;
    }

    protected void canWrite(SecurityCheckContext check, String rawpath)
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

    protected void canRead(SecurityCheckContext check, String rawpath)
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

    protected void deniedWrite(SecurityCheckContext check, String rawpath)
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

    protected void deniedRead(SecurityCheckContext check, String rawpath)
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
    
    
    protected void canReadProperty(SecurityCheckContext check, String property)
    {
        SecurityResult result = new SecurityResult("property.can.read|%s",property);
        try
        {
            @SuppressWarnings("unused")
            Object value = System.getProperty(property);
            result.success("Property(" + property + ") can be read");
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
    
    protected void canWriteProperty(SecurityCheckContext check, String property)
    {
        SecurityResult result = new SecurityResult("property.can.write|%s",property);
        try
        {
            System.setProperty(property, "foo");
            result.success("Property(" + property + ") can be written");
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
    
    protected void deniedReadProperty(SecurityCheckContext check, String property)
    {
        SecurityResult result = new SecurityResult("property.denied.read|%s",property);
        try
        {
            @SuppressWarnings("unused")
            Object value = System.getProperty(property);
            result.failure("Property(%s) can be written", property);
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
    
    protected void deniedWriteProperty(SecurityCheckContext check, String property)
    {
        SecurityResult result = new SecurityResult("property.denied.write|%s",property);
        try
        {
            System.setProperty(property, "foo");
            result.failure("Property(%s) can be written", property);
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

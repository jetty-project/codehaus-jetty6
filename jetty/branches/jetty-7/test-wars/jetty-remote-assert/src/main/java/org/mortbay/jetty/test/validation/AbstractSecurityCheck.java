package org.mortbay.jetty.test.validation;

import java.io.File;
import java.security.AccessControlException;

import org.eclipse.jetty.toolchain.test.OS;

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

    protected void canWrite(RemoteAsserts check, String rawpath)
    {
        RemoteAssertResult result = new RemoteAssertResult("filesystem.can.write|%s",rawpath);
        try
        {
            File path = new File(OS.separators(rawpath));
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

    protected void canRead(RemoteAsserts check, String rawpath)
    {
        RemoteAssertResult result = new RemoteAssertResult("filesystem.can.read|%s",rawpath);
        try
        {
            File path = new File(OS.separators(rawpath));
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

    protected void deniedWrite(RemoteAsserts check, String rawpath)
    {
        RemoteAssertResult result = new RemoteAssertResult("filesystem.denied.write|%s",rawpath);
        try
        {
            File path = new File(OS.separators(rawpath));
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

    protected void deniedRead(RemoteAsserts check, String rawpath)
    {
        RemoteAssertResult result = new RemoteAssertResult("filesystem.denied.read|%s",rawpath);
        try
        {
            File path = new File(OS.separators(rawpath));
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
    
    
    protected void canReadProperty(RemoteAsserts check, String property)
    {
        RemoteAssertResult result = new RemoteAssertResult("property.can.read|%s",property);
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
    
    protected void canWriteProperty(RemoteAsserts check, String property)
    {
        RemoteAssertResult result = new RemoteAssertResult("property.can.write|%s",property);
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
    
    protected void deniedReadProperty(RemoteAsserts check, String property)
    {
        RemoteAssertResult result = new RemoteAssertResult("property.denied.read|%s",property);
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
    
    protected void deniedWriteProperty(RemoteAsserts check, String property)
    {
        RemoteAssertResult result = new RemoteAssertResult("property.denied.write|%s",property);
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

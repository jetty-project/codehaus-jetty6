package org.mortbay.jetty.tests.webapp.policy;

import java.io.File;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;

import org.apache.commons.io.FilenameUtils;

public class Checker
{
    protected static final Logger LOG = Logger.getLogger(Checker.class.getName());

    public void canExit(Properties props)
    {
        String prefix = "Can System.exit()";
        try
        {
            System.exit(-2);
            failure(props,prefix,"Exit!?");
        }
        catch (Throwable t)
        {
            success(props,prefix,"Exit was prevented");
        }
    }

    public void canLoadLibrary(Properties props, String libName)
    {
        String prefix = "Can Load Library: " + libName;
        try
        {
            System.loadLibrary(libName);
            success(props,prefix);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canRead(Properties props, File path)
    {
        String prefix = "Can Read: " + path;
        try
        {
            path.canRead();
            success(props,prefix);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canRead(Properties props, String rawpath)
    {
        String prefix = "Can Read: " + rawpath;
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            path.canRead();
            success(props,prefix);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canReadSystemProperty(Properties props, String key)
    {
        String prefix = "Can Read System Property: " + key;
        try
        {
            String value = System.getProperty(key);
            success(props,prefix,"Got: " + value);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canWrite(Properties props, File path)
    {
        String prefix = "Can Write: " + path;
        try
        {
            path.canWrite();
            success(props,prefix);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canWrite(Properties props, String rawpath)
    {
        String prefix = "Can Write: " + rawpath;
        try
        {
            File path = new File(FilenameUtils.separatorsToSystem(rawpath));
            path.canWrite();
            success(props,prefix);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    public void canWriteSystemProperty(Properties props, String key, String value)
    {
        String prefix = "Can Write System Property: " + key;
        try
        {
            System.setProperty(key,value);
            String ret = System.getProperty(key);
            success(props,prefix,"Got: " + ret);
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
        }
    }

    protected void failure(Properties props, String key, String msg)
    {
        props.setProperty(key,"FAILURE - " + msg);
    }

    protected void failure(Properties props, String key, Throwable t)
    {
        LOG.log(Level.WARNING,key + ": " + t.getMessage(),t);
        props.setProperty(key,"FAILURE - " + t.getClass().getSimpleName() + " : " + t.getMessage());
    }

    public Object getServletAttribute(HttpServlet servlet, Properties props, String key)
    {
        String prefix = "Get Servlet Attribute: " + key;

        try
        {
            ServletContext context = servlet.getServletContext();
            if (context == null)
            {
                failure(props,prefix,"Unable to get ServletContext");
                return null;
            }
            Object obj = context.getAttribute(key);
            success(props,prefix);
            return obj;
        }
        catch (Throwable t)
        {
            failure(props,prefix,t);
            return null;
        }
    }

    protected void success(Properties props, String key)
    {
        props.setProperty(key,"Success");
    }

    protected void success(Properties props, String key, String msg)
    {
        props.setProperty(key,"Success - " + msg);
    }
}

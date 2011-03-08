package org.mortbay.jetty.tests.webapp.policy;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

public class PropertiesUtil
{
    public static void writePropertiesOutput(HttpServletResponse resp, Properties props) throws IOException
    {
        resp.setContentType("text/plain");
        OutputStream out = null;
        try
        {
            out = resp.getOutputStream();
            props.store(out,"Checker Results from " + Checker.class.getName());
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
    }
}

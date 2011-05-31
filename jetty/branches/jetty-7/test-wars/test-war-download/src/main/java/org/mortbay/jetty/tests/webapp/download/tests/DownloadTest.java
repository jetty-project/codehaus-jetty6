package org.mortbay.jetty.tests.webapp.download.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.mortbay.jetty.test.validation.junit.ServletRequestContextRule;

public class DownloadTest
{
    @Rule
    public ServletRequestContextRule context = new ServletRequestContextRule();
    
    @Test
    public void testDownloadSmallest()
    {
        String filename ="d.txt";
        String reqUri = context.getRequest().getRequestURL().toString();
        int idx = reqUri.lastIndexOf('/');
        if (idx != reqUri.length())
        {
            reqUri = reqUri.substring(0, ++idx);
        }
        
        SimpleRequest downReq = null;
        
        try
        {
             downReq = new SimpleRequest(new URI(reqUri));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }
        
        try
        {
            String data = downReq.getString(filename);
            File inFile = new File (context.getServlet().getServletContext().getRealPath(filename));

            Assert.assertEquals(data.length(),inFile.length());           
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
    
    @Test
    public void testDownloadSmall()
    {
        String filename = "da.txt";
        String reqUri = context.getRequest().getRequestURL().toString();
        int idx = reqUri.lastIndexOf('/');
        if (idx != reqUri.length())
        {
            reqUri = reqUri.substring(0, ++idx);
        }
        
        SimpleRequest downReq = null;
        
        try
        {
             downReq = new SimpleRequest(new URI(reqUri));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }
        
        try
        {
            String data = downReq.getString(filename);
            File inFile = new File (context.getServlet().getServletContext().getRealPath(filename));

            Assert.assertEquals(data.length(),inFile.length());           
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
    
    @Test
    public void testDownloadLarge()
    {
        String filename="dat.txt";
        String reqUri = context.getRequest().getRequestURL().toString();
        int idx = reqUri.lastIndexOf('/');
        if (idx != reqUri.length())
        {
            reqUri = reqUri.substring(0, ++idx);
        }
        
        SimpleRequest downReq = null;
        
        try
        {
             downReq = new SimpleRequest(new URI(reqUri));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }
        
        try
        {
            String data = downReq.getString(filename);
            File inFile = new File (context.getServlet().getServletContext().getRealPath(filename));

            Assert.assertEquals(data.length(),inFile.length());           
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
    
    @Test
    public void testDownloadLargest()
    {
        String filename = "data.txt";
        String reqUri = context.getRequest().getRequestURL().toString();
        int idx = reqUri.lastIndexOf('/');
        if (idx != reqUri.length())
        {
            reqUri = reqUri.substring(0, ++idx);
        }
        
        SimpleRequest downReq = null;
        
        try
        {
             downReq = new SimpleRequest(new URI(reqUri));
        }
        catch (Exception ex)
        {
            Assert.fail(ex.getMessage());
        }
        
        try
        {
            String data = downReq.getString(filename);
            File inFile = new File (context.getServlet().getServletContext().getRealPath(filename));

            Assert.assertEquals(data.length(),inFile.length());           
        }
        catch (IOException e)
        {
            Assert.fail(e.getMessage());
        }
        
    }
}

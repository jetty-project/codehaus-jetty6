//========================================================================
//Copyright (c) Webtide LLC
//------------------------------------------------------------------------
//All rights reserved. This program and the accompanying materials
//are made available under the terms of the Eclipse Public License v1.0
//and Apache License v2.0 which accompanies this distribution.
//The Eclipse Public License is available at 
//http://www.eclipse.org/legal/epl-v10.html
//The Apache License v2.0 is available at
//http://www.opensource.org/licenses/apache2.0.php
//You may elect to redistribute this code under either of these licenses. 
//========================================================================
package org.mortbay.jetty.tests.webapp.download;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.eclipse.jetty.util.ajax.JSON;

/* ------------------------------------------------------------ */
/**
 * Test File Download
 */
public class DownloadServlet extends HttpServlet
{
    private String[] fileNames = {"d.txt", "da.txt", "dat.txt", "data.txt"};
    
    /* ------------------------------------------------------------ */
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request,response);
    }

    /* ------------------------------------------------------------ */
    @Override
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
    {
        List<String[]> testResults = new ArrayList<String[]>();
        
        String reqUri = request.getRequestURL().toString();
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
        catch (URISyntaxException ex)
        {
            throw new ServletException(ex);
        }

        for (String fname : fileNames)
        {
            String data = downReq.getString(fname);
            File inFile = new File (this.getServletContext().getRealPath(fname));
            testResults.add(new String[] {fname, data.length() == inFile.length()? "PASSED":"FAILED"});
        }

        String jsonResults = JSON.toString(testResults.toArray(new String[2][]));
        
        response.getWriter().write(jsonResults);
    }
}

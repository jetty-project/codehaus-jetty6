//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.demo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.util.ajax.JSON;

/**
 * Servlet implementation class SerialRestServlet
 */
public class SerialRestServlet extends HttpServlet
{
    private final static String __DEFAULT_APPID = "Webtide81-adf4-4f0a-ad58-d91e41bbe85";
    
    final static String ITEMS_PARAM="items";
    final static String APPID_PARAM="appid";
        
    private String _appid;

    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (servletConfig.getInitParameter(APPID_PARAM)==null)
            _appid = __DEFAULT_APPID;
        else
            _appid=servletConfig.getInitParameter(APPID_PARAM);
        
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        long start = System.currentTimeMillis();
        
        String searchParameters = request.getParameter(ITEMS_PARAM);
        List<String> itemTokens = new ArrayList<String>();

        if (searchParameters == null)
        {
            response.setContentType("text/html");
            PrintWriter out = response.getWriter();

            out.println("<HTML><BODY>pass in url with ?items=a,b,c,d<br/>for more dramatic results run multiple times with 10 or more items</BODY></HTML>");
            out.close();

        }
        else
        {
            StringTokenizer strtok = new StringTokenizer( (String)searchParameters, ",");

            while ( strtok.hasMoreTokens() )
            {
                itemTokens.add( URLEncoder.encode(strtok.nextToken(), "UTF-8") );
            }
        }

        
        List list = new ArrayList();        
        // make all requests serially
        for (String itemName : itemTokens)
        {
            URL url = new URL("http://open.api.ebay.com/shopping?MaxEntries=5&appid="+_appid+"&version=573&siteid=0&callname=FindItems&responseencoding=JSON&QueryKeywords=" + itemName);
            
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            
            Map query = (Map)JSON.parse(new BufferedReader(new InputStreamReader(connection.getInputStream())));
            
            Object[] itemsArray = (Object[])query.get("Item");
            
            if (itemsArray==null)
            {
                Map<String, String> m = new HashMap<String, String>();

                m.put("Title", "\"" + itemName + "\" not found!");
                list.add(m);
            }
            else
            {
                List items = new ArrayList();
                for (Object o : (Object[])query.get("Item"))
                    list.add((Map)o);
            }
        }
        

        response.setContentType("text/html");
        PrintWriter out=response.getWriter();
        out.println("<html><head><style type='text/css'>img:hover {height:75px}</style></head><body><small>");

        
        for (Map m : (List<Map>)list)
        {
            out.print("<a href=\""+m.get("ViewItemURLForNaturalSearch")+"\">");
            if (m.containsKey("GalleryURL"))
                out.print("<img border='1px' height='20px' src=\""+m.get("GalleryURL")+"\"/>&nbsp;");
            
            out.print(m.get("Title"));
            out.println("</a><br/>");
        }

        out.println("<hr />");
        out.print( "Total Time: ");
        long duration=System.currentTimeMillis()-start;
        out.print( duration );
        out.println( "ms<br/>");
        out.print( "Thread held: ");
        out.print( duration );
        out.println( "ms");
        
        out.println("</small></body></html>" );
        out.close();   
        
        

    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}

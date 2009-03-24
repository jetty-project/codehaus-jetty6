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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.client.ContentExchange;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.util.ajax.JSON;

/**
 * Servlet implementation class AsyncRESTServlet.
 * Enquires ebay REST service for auctions by key word.
 * May be configured with init parameters: <dl>
 * <dt>appid</dt><dd>The eBay application ID to use</dd>
 * </dl>
 * Each request examines the following request parameters:<dl>
 * <dt>items</dt><dd>The keyword to search for</dd>
 * </dl>
 */
public class AsyncRestServlet extends HttpServlet
{
    private final static String __DEFAULT_APPID = "Webtide81-adf4-4f0a-ad58-d91e41bbe85";

    final static String ITEMS_PARAM = "items";
    final static String APPID_PARAM = "appid";

    final static String CLIENT_ATTR = "org.mortbay.demo.client";
    final static String DURATION_ATTR = "org.mortbay.demo.duration";
    final static String START_ATTR = "org.mortbay.demo.start";

    private HttpClient _client;
    private String _appid;

    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (servletConfig.getInitParameter(APPID_PARAM) == null)
            _appid = __DEFAULT_APPID;
        else
            _appid = servletConfig.getInitParameter(APPID_PARAM);

        _client = new HttpClient();
        _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);

        try
        {
            _client.start();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Long start=System.currentTimeMillis();

        if (!request.isAsyncStarted() || request.getAttribute(CLIENT_ATTR)==null)
        {
            // extract keywords to search for
            String[] keywords=request.getParameter(ITEMS_PARAM).split(",");
            
            // define results data structures
            final List<Map<String, String>> results = 
                Collections.synchronizedList(new ArrayList<Map<String, String>>());
            final AtomicInteger count=new AtomicInteger(keywords.length);
            
            // suspend the request
            final AsyncContext asyncContext = request.startAsync();
            request.setAttribute(CLIENT_ATTR, results);

            // For each keyword
            for (final String item:keywords)
            {
                // create an asynchronous HTTP exchange
                ContentExchange exchange = new ContentExchange()
                {
                    protected void onResponseComplete() throws IOException
                    {
                        // extract auctions from the results
                        Map query = (Map) JSON.parse(this.getResponseContent());
                        Object[] auctions = (Object[]) query.get("Item");
                        if (auctions != null)
                        {
                            for (Object o : auctions)
                                results.add((Map) o);
                        }

                        // if that was all, resume the request
                        if (count.decrementAndGet()<=0)
                        	asyncContext.dispatch();
                    }
                };

                // send the exchange
                exchange.setMethod("GET");
                exchange.setURL("http://open.api.ebay.com/shopping?MaxEntries=5&appid=" + 
                        _appid + 
                        "&version=573&siteid=0&callname=FindItems&responseencoding=JSON&QueryKeywords=" + 
                        URLEncoder.encode(item,"UTF-8"));
                _client.send(exchange);
                
            }

            // save timing info and return
            request.setAttribute(START_ATTR, start);
            request.setAttribute(DURATION_ATTR, new Long(System.currentTimeMillis() - start));
            return;
        }

        // resumed request: either we got all the results, or we timed out
        List<Map<String, String>> results = (List<Map<String, String>>) request.getAttribute(CLIENT_ATTR);
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head><style type='text/css'>img:hover {height:75px}</style></head><body><small>");

        for (Map<String, String> m : results)
        {
            out.print("<a href=\""+m.get("ViewItemURLForNaturalSearch")+"\">");
            if (m.containsKey("GalleryURL"))
                out.print("<img border='1px' height='20px' src=\""+m.get("GalleryURL")+"\"/>&nbsp;");

            out.print(m.get("Title"));
            out.println("</a><br/>");
        }

        out.println("<hr />");
        long duration = (Long) request.getAttribute(DURATION_ATTR);
        long start0 = (Long) request.getAttribute(START_ATTR);

        long now = System.currentTimeMillis();
        out.print("Total Time: ");
        out.print(now - start0);
        out.println("ms<br/>");
        out.print("Thread held: ");

        long duration2 = now - start;
        out.print(duration + duration2);
        out.println("ms (" + duration + " initial + " + duration2 + " resume )");
        out.println("</small></body></html>");
        out.close();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}

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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.util.ajax.JSON;

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
public class AsyncRestServlet extends AbstractRestServlet
{
    final static String RESULTS_ATTR = "org.mortbay.demo.client";
    final static String DURATION_ATTR = "org.mortbay.demo.duration";
    final static String START_ATTR = "org.mortbay.demo.start";

    HttpClient _client;

    public void init(ServletConfig servletConfig) throws ServletException
    {
        super.init(servletConfig);
        
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
        Long start=System.nanoTime();
        
        // resumed request: either we got all the results, or we timed out
        Queue<Map<String, String>> results = (Queue<Map<String, String>>) request.getAttribute(RESULTS_ATTR);
        
        if (results==null)
        {
            // define results data structures
            results = new ConcurrentLinkedQueue<Map<String,String>>();
            request.setAttribute(RESULTS_ATTR, results);
            
            // suspend the request
            // This is done before scheduling async handling to avoid race of 
            // resume before suspend!
            final Continuation continuation = ContinuationSupport.getContinuation(request);
            continuation.setTimeout(10000);
            continuation.suspend();

            // extract keywords to search for
            String[] keywords=request.getParameter(ITEMS_PARAM).split(",");
            final AtomicInteger count=new AtomicInteger(keywords.length);
            
            // Send request each keyword
            for (final String item:keywords)
            {
                sendAsyncRestRequest(item,results,count,continuation/*,debug*/);
            }
            
            // save timing info and return
            request.setAttribute(START_ATTR, start);
            request.setAttribute(DURATION_ATTR, new Long(System.nanoTime() - start));

            return;
        }

        // Generate the response
        String thumbs = generateThumbs(results);
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><head>");
        out.println(STYLE);
        out.println("</head><body><small>");

        long initial = (Long) request.getAttribute(DURATION_ATTR);
        long start0 = (Long) request.getAttribute(START_ATTR);

        long now = System.nanoTime();
        long total=now-start0;
        long generate=now-start;
        long thread=initial+generate;
        
        out.print("<b>Asynchronous: "+request.getParameter(ITEMS_PARAM)+"</b><br/>");
        out.print("Total Time: "+ms(total)+"ms<br/>");

        out.print("Thread held (<span class='red'>red</span>): "+ms(thread)+"ms (" + ms(initial) + " initial + " + ms(generate) + " generate )<br/>");
        out.print("Async wait (<span class='green'>green</span>): "+ms(total-thread)+"ms<br/>");
        
        out.println("<img border='0px' src='red.png'   height='20px' width='"+width(initial)+"px'>"+
                    "<img border='0px' src='green.png' height='20px' width='"+width(total-thread)+"px'>"+
                    "<img border='0px' src='red.png'   height='20px' width='"+width(generate)+"px'>");
        
        out.println("<hr />");
        out.println(thumbs);
        out.println("</small>");
        out.println("</body></html>");
        out.close();
    }

    private void sendAsyncRestRequest(
            final String item, 
            final Queue<Map<String, String>> results, 
            final AtomicInteger count, 
            final Continuation continuation /*,
            final StringBuffer debug*/)
        throws IOException
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

                doCount();
            }

            /* ------------------------------------------------------------ */
            protected void onConnectionFailed(Throwable ex)
            {
                doCount();
            }

            /* ------------------------------------------------------------ */
            protected void onException(Throwable ex)
            {
                doCount();
            }

            /* ------------------------------------------------------------ */
            protected void onExpire()
            {
                doCount();
            }
            
            /* ------------------------------------------------------------ */
            private void doCount()
            {
                if (count.decrementAndGet()<=0)
                {
                    // debug.append("got & resume "+item+"\n");
                    continuation.resume();
                }
                else
                {
                    // debug.append("got "+item+"\n");
                }
            }
            
        };

        // send the exchange
        exchange.setMethod("GET");
        exchange.setURL(restURL(item));
        _client.send(exchange);        
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}

package org.mortbay.demo;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.client.ContentExchange;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.util.ajax.JSON;

/**
 * Servlet implementation class AsyncRESTServlet
 */
public class AsyncRestServlet extends HttpServlet
{
    public static final String ITEMS_PARAM="items";
    HttpClient _client;
    
    
    public void init() throws ServletException
    {
        _client = new HttpClient();
        _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        
        try
        {
            _client.start();
        }
        catch(Exception e)
        {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        final ServletRequest request = (ServletRequest)req; 
        
        if (request.getAttribute("items")==null)
        {
            System.err.println("Search EBAY "+request.getParameter(ITEMS_PARAM));

            // Create an async RESTFUL request to ebay
            ContentExchange exchange = new ContentExchange()
            {
                protected void onResponseComplete() throws IOException
                {
                    super.onResponseComplete();
                    Map query = (Map)JSON.parse(this.getResponseContent());
                    Object[] itemsArray = (Object[])query.get("Item");
                    
                    if (itemsArray==null)
                    {
                        request.setAttribute("message","No Items Found!");
                    }
                    else
                    {
                        List items = new ArrayList();
                        for (Object o : (Object[])query.get("Item"))
                            items.add((Map)o);
                        request.setAttribute("items",items);
                        request.setAttribute("message","&nbsp;");
                    }
                    request.resume();
                }           
            };
            
            // Suspend and send the restful request
            request.suspend();
            request.getServletResponse().disable();
            exchange.setMethod("GET");
            exchange.setURL("http://open.api.ebay.com/shopping?MaxEntries=20&appid=Webtide81-adf4-4f0a-ad58-d91e41bbe85&version=573&siteid=0&callname=FindItems&responseencoding=JSON&QueryKeywords=" +request.getParameter(ITEMS_PARAM));
            request.setAttribute("items",Collections.emptyList());
            _client.send(exchange);
            return;
        }
        
        List list=(List)request.getAttribute("items");
        resp.setContentType("text/html");
        PrintWriter out=resp.getWriter();
        for (Object o : list)
        {
            Map m = (java.util.Map)o;
            out.println("Item: "+m.get("ItemID")+"&nbsp;"+m.get("Title")+"<br/>");
        }
    }

    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}

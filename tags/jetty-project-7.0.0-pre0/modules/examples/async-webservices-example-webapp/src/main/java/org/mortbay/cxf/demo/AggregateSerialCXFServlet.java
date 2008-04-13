package org.mortbay.cxf.demo;
//========================================================================
//Copyright 2007 Mort Bay Consulting Pty. Ltd.
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


import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.Future;

import ebay.apis.eblbasecomponents.*;

public class AggregateSerialCXFServlet extends HttpServlet
{
    ShoppingInterface _shoppingPort;
    public static final String ITEMS_PARAM="items";
    
    

    public void init() throws ServletException
    {
        super.init();

        try
        {
            _shoppingPort = new Shopping().getShopping();
            BindingProvider bp = (BindingProvider) _shoppingPort;

            // retrieve the URL stub from the WSDL
            String ebayURL = (String) bp.getRequestContext().
                    get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            // add eBay-required parameters to the URL
            String endpointURL = ebayURL + "?callname=FindItems&siteid=0" +
                    "&appid=JesseMcC-1aff-4c3c-b0be-e8379d036f56" +
                    "&version=551&requestencoding=SOAP";

            // replace the endpoint address with the new value
            bp.getRequestContext().
                    put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL);
            
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req, resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Object itemsObj = req.getParameter(ITEMS_PARAM);
        List<String> items = new ArrayList<String>();

        if (itemsObj == null)
        {
            resp.setContentType("text/html");
            PrintWriter out = resp.getWriter();

            out.println("<HTML><BODY>pass in url with ?items=a,b,c,d<br/>for more dramatic results run multiple times with 10 or more items</BODY></HTML>");
            out.close();

        }
        else
        {
            StringTokenizer strtok = new StringTokenizer( (String)itemsObj, ",");

            while ( strtok.hasMoreTokens() )
            {
                items.add( strtok.nextToken() );
            }
        }


        long _totalTime = System.currentTimeMillis();
        
        
        FindItemsResponseType[] responses=new FindItemsResponseType[items.size()];
        int r=0;
        
        // make all requests serially
        for (String item: items)
        {
            FindItemsRequestType ebayReq = new FindItemsRequestType();
            ebayReq.setQueryKeywords( item );
            ebayReq.setMaxEntries(4);
            responses[r++] = _shoppingPort.findItems(ebayReq);
        }
            

        resp.setContentType( "text/html" );
        PrintWriter out = resp.getWriter();
        out.println( "<HTML><BODY>");
        

        int i=0;
        for (String item: items)
        {
            FindItemsResponseType response=responses[i];
            if (response==null)
            {
                out.println(items.get(i)+" MISSING RESPONSE!");
            }
            else
            {
                out.print( "<b>" );
                out.print( items.get(i) );
                out.println( "</b>: " );
                String coma=null;
                for (SimpleItemType sit : response.getItem())
                {
                    if (coma==null)
                        coma=", ";
                    else
                        out.print(coma);
                    out.print("<a href=\"");
                    out.print( sit.getViewItemURLForNaturalSearch());
                    out.print("\">");
                    out.print( sit.getTitle());
                    out.print("</a>");
                }
            }
            i++;
            out.println( "<br/>");
        }

        out.print( "Total Time: ");
        long duration=System.currentTimeMillis()-_totalTime;
        out.print( duration );
        out.println( "ms<br/>");
        out.print( "Thread held: ");
        out.print( duration );
        out.println( "ms");
        
        out.println("</BODY></HTML>" );
        out.close();   
        
        

    }
}
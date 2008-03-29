package org.mortbay.cxf.continuation;
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

import ebay.apis.eblbasecomponents.FindItemsRequestType;
import ebay.apis.eblbasecomponents.Shopping;
import ebay.apis.eblbasecomponents.ShoppingInterface;
import ebay.apis.eblbasecomponents.SimpleItemType;

public class AggregateAsyncCXFServlet extends HttpServlet
{
    public static final String CLIENT_ATTR="org.mortbay.cxf.client";
    public static final String ITEMS_ATTR="org.mortbay.cxf.items";
    public static final String DURATION_ATTR="org.mortbay.cxf.duration";
    public static final String ITEMS_PARAM="items";

    ShoppingInterface _shoppingPort;

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
            
            // make a request to initialize everything
            FindItemsRequestType ebayReq = new FindItemsRequestType();
            ebayReq.setQueryKeywords( "shoe" );
            ebayReq.setMaxEntries(1);
            _shoppingPort.findItems(ebayReq);
        }
        catch (Exception e)
        {
            System.out.println("Exception: " + e.getMessage());
            throw new ServletException(e);
        }
    }
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req,resp);
    }
         
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        long start = System.currentTimeMillis();
        if (req.isInitial())
        {
            // first time we have seen this request, so lets start handling it

            // look for key words in query string
            Object itemsObj = req.getParameter( ITEMS_PARAM );
            if ( itemsObj == null )
            {
                resp.setContentType("text/html");
                PrintWriter out = resp.getWriter();

                out.println("<HTML><BODY>pass in url with ?items=a,b,c,d<br/>for more dramatic results run multiple times with 10 or more items</BODY></HTML>");
                out.close();
                return;
            }
                

            ArrayList<String> items = new ArrayList<String>();
            StringTokenizer strtok = new StringTokenizer( (String)itemsObj, ",");
            while ( strtok.hasMoreTokens() )
            {
                items.add( strtok.nextToken() );
            }


            try
            {
                System.out.println("suspending after making initial request");
                req.suspend();

                // Make the webservice requests
                EbayFindItemAsync greets = new EbayFindItemAsync(_shoppingPort,req);
                synchronized (greets)
                {
                    // add as many different searchs here, each will be a seperate async call and ebayAggregate will not be 'done'
                    // until all Future's are returned.
                    for (String keyword : items)
                    {
                        greets.search( keyword );
                    }
                }

                req.setAttribute( CLIENT_ATTR, greets );
                req.setAttribute( ITEMS_ATTR, items );
                req.setAttribute( DURATION_ATTR, new Long(System.currentTimeMillis()-start));
            }
            catch(Exception e)
            {
                // there was a problem sending the requests, so lets resume now
                req.resume();
            }
            
        }
        else
        {
            // This is a resumed request - 
            // so either we have all the results or we timed out
            System.out.println("resumed request "+req.isResumed());

            // Look for an existing client and protect from context restarts
            EbayFindItemAsync ebayAggregate =(EbayFindItemAsync)req.getAttribute(CLIENT_ATTR);
            ArrayList<String> items = (ArrayList<String>)req.getAttribute(ITEMS_ATTR);
            long duration=(Long)(req.getAttribute(DURATION_ATTR));

            List<EbayFindItemAsyncHandler> handlers = ebayAggregate.getPayload();

            resp.setContentType( "text/html" );
            PrintWriter out = resp.getWriter();
            out.println( "<HTML><BODY>");
            
            out.print( "Total Time: ");
            out.print( ebayAggregate.getTotalTime() );
            out.println( "ms<br/>");
            out.print( "Servlet Thread held: ");
            duration+=System.currentTimeMillis()-start;
            out.print( duration );
            out.println( "ms<br/><br/>");

            int i=0;
            for (EbayFindItemAsyncHandler handler : handlers)
            {
                
                if (handler.getResponse()==null)
                {
                    out.println("MISSING RESPONSE!");
                }
                else
                {
                    out.print( "<b>" );
                    out.print( items.get(i) );
                    out.println( "</b>:<br/>" );
                    String coma=null;
                    for (SimpleItemType sit : handler.getResponse().getItem())
                    {
                        if (coma==null)
                            coma=", ";
                        else
                            out.print(coma);
                        out.print("<a href=\"");
                        out.print( sit.getViewItemURLForNaturalSearch());
                        out.print("\">");
                        out.print( sit.getItemID());
                        out.print("</a>");
                    }
                }
                i++;
                out.println( "<br/><br/><br/>");
            }

            out.println("</BODY></HTML>" );
            out.close();

            req.removeAttribute( CLIENT_ATTR );
        }

    }
}

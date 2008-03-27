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
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import ebay.apis.eblbasecomponents.SimpleItemType;

public class AggregateAsyncCXFServlet extends HttpServlet
{
    public static final String CLIENT_ATTR="org.mortbay.cxf.client";
    public static final String ITEMS_ATTR="items";
    
    
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req,resp);
    }
         
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        Object itemsObj = req.getParameter( ITEMS_ATTR );
        ArrayList items = new ArrayList();

        if ( itemsObj == null )
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

        // Look for an existing client and protect from context restarts
        Object clientObj=req.getAttribute(CLIENT_ATTR);

        if ( clientObj != null && clientObj instanceof EbayFindItemAsync)
        {
            System.out.println("found client in req");
            EbayFindItemAsync ebayAggregate = (EbayFindItemAsync)clientObj;

            if ( ebayAggregate.isDone() )
            {
                List handlers = ebayAggregate.getPayload();
                
                StringBuffer sb = new StringBuffer();
                sb.append( "Total Time: ").append( ebayAggregate.getTotalTime() ).append( "ms<br/><br/>");

                for ( Iterator i = handlers.iterator(); i.hasNext(); )
                {
                    EbayFindItemAsyncHandler handler = (EbayFindItemAsyncHandler) i.next();

                    List lsit = handler.getResponse().getItem();

                    ListIterator lsitItr = lsit.listIterator();

                    while ( lsitItr.hasNext() )
                    {
                        SimpleItemType sit = (SimpleItemType) lsitItr.next();
                        sb.append( sit.getDescription() + " " );
                        sb.append( sit.getItemID() + " " );
                        sb.append( sit.getViewItemURLForNaturalSearch() + " " );
                        sb.append( sit.getLocation() + " " );
                        sb.append( "<br/>" );
                    }
                    sb.append( "<br/><br/><br/>");
                }


                resp.setContentType( "text/html" );
                PrintWriter out = resp.getWriter();

                out.println( "<HTML><BODY>" + sb.toString() + "</BODY></HTML>" );
                out.close();

                req.removeAttribute( CLIENT_ATTR );
            }
            else
            {
                System.out.println("suspending for 1000ms since its not done");
                req.suspend( 1000 );
            }
        }
        else
        {

            EbayFindItemAsync greets = new EbayFindItemAsync();
            // add as many different searchs here, each will be a seperate async call and ebayAggregate will not be 'done'
            // until all Future's are returned.
            for ( Iterator i = items.iterator(); i.hasNext(); )
            {
                greets.search( (String) i.next() );
            }

            
            req.setAttribute( CLIENT_ATTR, greets );

            System.out.println("suspending for 1000ms after making initial request");
            req.suspend( 1000 );

        }
    }
}

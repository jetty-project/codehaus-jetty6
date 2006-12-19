// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================

package org.mortbay.cometd;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.TypeUtil;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

/** Cometd Filter
 * Servlet implementing the {@link Bayeux} protocol.
 * 
 * The Servlet can be initialized with a json file mapping channels to {@link DataFilter} definitions.
 * The servlet init parameter "filters" should point to a webapplication resource containing a JSON 
 * array of filter definitions. For example: <pre>
 * [
 *   { 
 *     "channels": "/**",
 *     "class"   : "org.mortbay.cometd.filter.NoMarkupFilter",
 *     "init"    : {}
 *   }
 * ]
 * </pre>
 * 
 * The init parameter "timeout" specifies the poll timeout in milliseconds (default 45000).
 * The init parameter "multiTimeout" specifies the poll timeout if multiple polls are detected from the 
 * same browser (default 0 - disable browser detection).
 * 
 * @author gregw
 * @see {@link Bayeux}
 * @see {@link ChannelPattern}
 */
public class CometdServlet extends HttpServlet
{
    public static final String ORG_MORTBAY_BAYEUX="org.mortbay.bayeux";
    public static final String CLIENT_ATTR="org.mortbay.cometd.client";
    public static final String MESSAGE_PARAM="message";
    public static final String TUNNEL_INIT_PARAM="tunnelInit";
    public static final String BROWSER_ID="bayeuxBID";
    
    private Bayeux _bayeux;
    private long _timeout=45000;
    private long _multiTimeout=0;
    private Object _multiAdvice=null;
    private Map _bidCount=new HashMap();
    private boolean _verbose;

    public void init() throws ServletException
    {
        synchronized (CometdServlet.class)
        {
            _bayeux=(Bayeux)getServletContext().getAttribute(ORG_MORTBAY_BAYEUX);
            if (_bayeux==null)
            {
                _bayeux=new Bayeux(getServletContext());
                getServletContext().setAttribute(ORG_MORTBAY_BAYEUX,_bayeux);
            }
        }
        
        String filters=getInitParameter("filters");
        if (filters!=null)
        {
            try
            {
                Object[] objects = (Object[])JSON.parse(getServletContext().getResourceAsStream(filters));
                for (int i=0;objects!=null && i<objects.length;i++)
                {
                    Map filter_def=(Map)objects[i];
                    
                    Class c = Thread.currentThread().getContextClassLoader().loadClass((String)filter_def.get("class"));
                    DataFilter filter = (DataFilter)c.newInstance();
                    filter.init(filter_def.get("init"));
                    _bayeux.addFilter((String)filter_def.get("channels"),filter);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                getServletContext().log("Could not parse: "+filters,e);
                throw new ServletException(e);
            }
        }
        
        String timeout=getInitParameter("timeout");
        if (timeout!=null)
            _timeout=Long.parseLong(timeout);
        
        String multiTimeout=getInitParameter("multi-timeout");
        if (multiTimeout!=null)
        {
            _multiTimeout=Long.parseLong(multiTimeout);
            _multiAdvice= new JSON.Literal("{\"status\":\"multipleconnections\",\"reconnect\":\"retry\",\"interval\":"+_multiTimeout+",\"transport\":{\"long-polling\":{}}}");
        }
        
        String verbose=getInitParameter("verbose");
        if (verbose!=null)
            _verbose=Boolean.parseBoolean(verbose);
       
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String init=req.getParameter(TUNNEL_INIT_PARAM);
        if ("iframe".equals(init))
        {
            if (_verbose) System.err.println("--> Init Tunnel - IFRAME CURRENTLY BROKEN!!!!!!!");
            Transport transport=new IFrameTransport();
            ((IFrameTransport)transport).initTunnel(resp);
            if (_verbose) System.err.println("<-- Tunnel Over");
        }
        else
        {
            super.service(req,resp);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // Look for an existing client and protect from context restarts
        Object clientObj=req.getAttribute(CLIENT_ATTR);
        Client client=(clientObj instanceof Client)?(Client)clientObj:null;
        Transport transport=null;
        Continuation continuation=null;
        String bid=null;
        
 
        // Have we seen this request before?
        if (client!=null)
        {
            // yes - extract saved properties
            transport =(Transport)req.getAttribute(Bayeux.TRANSPORT_ATTR);
            transport.setResponse(resp);
            bid=(String)req.getAttribute(BROWSER_ID);

            // Reduce browser ID counter
            // TODO protect from exceptions
            if (_multiTimeout>0 && decBID(bid)==0)
            {
                /*
                if (false) // TODO only reset if advised previously
                    _bayeux.advise(client,transport,null);
                    */
            }
        }
        else
        {
            // No - process messages

            // Look for a browser ID
            if (_multiTimeout>0)
            {
                Cookie[] cookies=req.getCookies();
                for (int i=0;cookies!=null && i<cookies.length;i++)
                {
                    if (cookies[i].getName().equals(BROWSER_ID))
                        bid=cookies[i].getValue();
                }
                if (bid==null)
                {
                    long l1=_bayeux._random.nextLong();
                    long l2=_bayeux._random.nextLong();
                    bid=Long.toString(l1<0?-l1:l1,16)+Long.toString(l2<0?-l2:l2,16);
                    Cookie cookie = new Cookie(BROWSER_ID,bid);
                    cookie.setPath("/");
                    resp.addCookie(cookie);
                }
            }


            // variables to loop through batches and messages
            // May have multiple message parameters, each with an array of messages - or just a message
            String[] batches=null;
            int batch_index=0;
            Object batch=null;
            int index=0;
            Map message=null;
            int message_count=0;


            batches=req.getParameterValues(MESSAGE_PARAM);

            // Loop to the first message - it may be handshake without a client
            while (batch_index<batches.length)
            {
                // Do we need to get another batch?
                if (batch==null)
                {
                    if (_verbose) System.err.println("="+batch_index+"=>"+batches[batch_index]);
                    index=0;
                    batch=JSON.parse(batches[batch_index++]);
                }

                if (batch==null)
                    continue;

                if (batch.getClass().isArray())
                {
                    message=(Map)Array.get(batch,index++);
                    if (index>=Array.getLength(batch))
                        batch=null;
                }
                else
                {
                    message=(Map)batch;
                    batch=null;
                }

                message_count++;


                client=_bayeux.getClient((String)message.get(Bayeux.CLIENT_ATTR));


                // If no client, this is a handshake
                if (client==null)
                {
                    // handshake!
                    transport=_bayeux.newTransport(client,message);
                    transport.setResponse(resp);
                    _bayeux.handle(null,transport,message);
                    message=null;
                }

                break;
            }

            // Handle all client messages
            if (client!=null)
            {
                // resolve transport
                transport=_bayeux.newTransport(client,message);
                transport.setResponse(resp);
                if (_verbose && transport instanceof PlainTextJSONTransport)
                    ((PlainTextJSONTransport)transport).setVerbose(_verbose);

                // continue handling messages with a known client and transport!
                try
                {
                    // Tell client to hold messages as a response is likely to be sent.
                    // client.responsePending(); 

                    // handle any message left over from client loop above
                    if (message!=null)
                        _bayeux.handle(client,transport,message);
                    message=null;

                    // handle all other messages
                    while (batch_index<batches.length)
                    {
                        // Do we need to get another batch?
                        if (batch==null)
                        {
                            if (_verbose) System.err.println("="+batch_index+"=>"+batches[batch_index]);
                            index=0;
                            batch=JSON.parse(batches[batch_index++]);
                        }
                        if (batch==null)
                            continue;

                        // get the next message
                        if (batch.getClass().isArray())
                        {
                            message=(Map)Array.get(batch,index++);
                            if (index>=Array.getLength(batch))
                                batch=null;
                        }
                        else
                        {
                            message=(Map)batch;
                            batch=null;
                        }

                        // handle message
                        if (message!=null)
                            _bayeux.handle(client,transport,message);
                        message=null;
                    }
                    
                    
                }
                finally
                {
                    //client.responded();
                }
            }
        }

        // Do we need to wait for messages or are we streaming?
        while (transport.isPolling())
        {   

            long timeout=_timeout;
            continuation=ContinuationSupport.getContinuation(req,client);

            // Get messages or wait
            List messages = null;
            synchronized(client)
            {
                messages = client.takeMessages();

                if (messages==null && !continuation.isPending())
                {
                    //check that only 1 request per browser is waiting
                    if (_multiTimeout>0 && incBID(bid)>1)
                    {
                        // Advise that there are multiple windows waiting
                        // fall back to traditional polling
                        timeout=_multiTimeout;       
                        _bayeux.advise(client,transport,_multiAdvice);
                    }

                    // save state and suspend
                    client.addContinuation(continuation);
                    req.setAttribute(CLIENT_ATTR,client);
                    req.setAttribute(BROWSER_ID,bid);
                    req.setAttribute(Bayeux.TRANSPORT_ATTR,transport);
                    continuation.suspend(timeout);
                    client.removeContinuation(continuation);

                    messages=client.takeMessages();
                }
                continuation.reset();
                client.removeContinuation(continuation);

                if (messages==null) // timeout
                    transport.setPolling(false);
                
            }

            // Send the messages
            if (messages!=null)
            {
                transport.send(messages);
            }

            // Only a simple poll if the transport does not flush
            if (!transport.keepAlive())
                transport.setPolling(false);

        }

        // Send any left over messages.
        /*
            List messages = client.takeMessage();
            if (messages!=null)
                transport.send(messages);
         */ 

        transport.complete();
    }

    private int incBID(String bid)
    {
        synchronized (_bidCount)
        {
            Integer count = (Integer)_bidCount.get(bid);
            count=TypeUtil.newInteger(count==null?1:count.intValue()+1);
            _bidCount.put(bid,count);
            return count.intValue();
        }
    }

    private int decBID(String bid)
    {
        synchronized (_bidCount)
        {
            Integer count = (Integer)_bidCount.get(bid);
            count=(count==null || count.intValue()<=1)?null:TypeUtil.newInteger(count.intValue()-1);
            if (count==null)
            {
                _bidCount.remove(bid);
                return 0;
            }
            _bidCount.put(bid,count);
            return count.intValue();
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req,resp);
    }
}

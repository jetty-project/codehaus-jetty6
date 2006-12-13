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
    private Map _bidCount=new HashMap();

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
            _multiTimeout=Long.parseLong(multiTimeout);
    }

    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String init=req.getParameter(TUNNEL_INIT_PARAM);
        if ("iframe".equals(init))
        {
            Transport transport=new IFrameTransport();
            ((IFrameTransport)transport).initTunnel(resp);
        }
        else
        {
            super.service(req,resp);
        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        String bid=null;
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
        String[] batches=null;
        int batch_index=0;
        Object batch=null;
        int index=0;
        Map message=null;
        int message_count=0;
        
        
        // Look for an existing client and protect from context restarts
        Object clientObj=req.getAttribute(CLIENT_ATTR);
        Client client=(clientObj instanceof Client)?(Client)clientObj:null;
        Transport transport=client==null?null:(Transport)req.getAttribute(Bayeux.TRANSPORT_ATTR);
        Continuation continuation=null;

        try
        {
            // Have we seen this request before?
            if (client==null)
            {
                // This is the first time we have seen this request - so handle all
                // the messages
                // We may see this request again if a continuation retries the
                // request.

                batches=req.getParameterValues(MESSAGE_PARAM);

                // handle batches/messages until we have a client!
                while (batch_index<batches.length)
                {
                    // Do we need to get another batch?
                    if (batch==null)
                    {
                        System.err.println("="+batch_index+"=>"+batches[batch_index]);
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

                    // Get a client if possible
                    if (client==null)
                    {
                        client=_bayeux.getClient((String)message.get(Bayeux.CLIENT_ATTR));
                        req.setAttribute(CLIENT_ATTR,client);
                    }

                    if (transport==null)
                    {
                        transport=_bayeux.newTransport(client,message);
                        req.setAttribute(Bayeux.TRANSPORT_ATTR,transport);
                    }
                    transport.setResponse(resp);

                    if (client!=null)
                        break;

                    _bayeux.handle(null,transport,message);
                    message=null;

                }
                
                if (client==null)
                    return;

                
                // continue handling messages with a known client and transport!
                try
                {
                    // client.responsePending();

                    // handle any message left over from client loop above
                    if (message!=null)
                        _bayeux.handle(client,transport,message);
                    message=null;

                    while (batch_index<batches.length)
                    {


                        // Do we need to get another batch?
                        if (batch==null)
                        {
                            System.err.println("="+batch_index+"=>"+batches[batch_index]);
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
                    // client.responded();
                }
            }

            if (client==null || transport==null)
                return;
            
            // Do we need to wait for messages or are we streaming?
            while (transport.isPolling())
            {   
                long timeout=_timeout;
                continuation=ContinuationSupport.getContinuation(req,client);
                try
                {
                    // Are there multiple windows from the same browser?
                    if (bid!=null && !continuation.isPending())
                    {
                        if (incBID(bid)>1)
                        {
                            // Advise that there are multiple windows
                            // fall back to traditional polling
                            timeout=_multiTimeout;       
                            _bayeux.advise(client,
                                    transport,
                                    new JSON.Literal("{\"status\":\"multipleconnections\",\"reconnect\":\"retry\",\"interval\":"+_multiTimeout+",\"transport\":{\"long-polling\":{}}}"));
                        }
                    }

                    synchronized (client)
                    {
                        client.addContinuation(continuation);
                        if (!client.hasMessages())
                            continuation.suspend(timeout);
                        client.removeContinuation(continuation);
                        continuation.reset();

                        if (client.hasMessages())
                        {
                            List messages=client.takeMessages();
                            System.err.println("responses"+messages);
                            transport.send(messages);
                        }
                        else
                            transport.setPolling(false);

                        // Only a simple poll if the transport does not flush
                        if (!transport.keepAlive())
                        {
                            transport.setPolling(false);
                            return;
                        }
                    } 
                }
                finally
                {
                    // Are we really finished polling?
                    if (bid!=null && (continuation==null || !continuation.isPending()))
                    {
                        if (decBID(bid)==0)
                        {
                            _bayeux.advise(client,transport,null);
                        }
                    }
                }
            }

            // Send any left over messages.
            /*
            synchronized (client)
            {
                if (client.hasMessages())
                    transport.send(client.takeMessages());
            }
            */ 
        }
        finally
        {
            // Are we really finished polling?
            if (continuation==null || !continuation.isPending())
            {
                // complete transport
                if (transport!=null)
                {
                    transport.complete();
                    transport.setPolling(false);
                }
            }

        }
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

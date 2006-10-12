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
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    
    private Bayeux _bayeux;

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
                for (int i=0;i<objects.length;i++)
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
        int message_count=0;
        Client client=(Client)req.getAttribute(CLIENT_ATTR);
        
        Transport transport=(Transport)req.getAttribute(Bayeux.TRANSPORT_ATTR);

        if (client==null)
        {
            // This is the first time we have seen this request - so handle all
            // the messages
            // We may see this request again if a continuation retries the
            // request.

            // handle all messages (before any polling)
            String[] messages=req.getParameterValues(MESSAGE_PARAM);
            
            Object objects=null;
            int index=0;
            for (int m=0; m<messages.length; m++)
            {
                System.err.println("="+m+"=>"+messages[m]);
                if (objects==null)
                {
                    index=0;
                    objects=JSON.parse(messages[m]);
                }

                if (objects==null)
                    continue;

                Object object=objects;
                if (objects.getClass().isArray())
                {
                    if (index>=Array.getLength(objects))
                    {
                        objects=null;
                        continue;
                    }
                    object=Array.get(objects,index++);
                }
                else
                    objects=null;

                message_count++;
                Map message=(Map)object;

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

                _bayeux.handle(client,transport,message);
                
            }
        }

        // handle polling for additinal messages
        while (client!=null&&transport!=null&&transport.isPolling())
        {
            synchronized (client)
            {
                Continuation continuation=ContinuationSupport.getContinuation(req,client);
                client.addContinuation(continuation);
                if (!client.hasMessages())
                    continuation.suspend(25000);
                client.removeContinuation(continuation);
                continuation.reset();

                if (client.hasMessages())
                {
                    List messages=client.takeMessages();
                    transport.send(messages);
                }
                else
                    transport.setPolling(false);

                // Only a simple poll if the transport does not flush
                if (!transport.keepAlive())
                    transport.setPolling(false);
            }
        }

        // complete
        if (transport!=null)
        {
            transport.complete();
            transport.setPolling(false);
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req,resp);
    }
}

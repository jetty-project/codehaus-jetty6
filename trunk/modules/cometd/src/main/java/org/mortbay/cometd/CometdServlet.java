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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.util.MultiPartOutputStream;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

public class CometdServlet extends HttpServlet
{
    private Bayeux _bayeux;
    
    public void init() throws ServletException
    {
        synchronized (CometdServlet.class)
        {
            _bayeux=(Bayeux)getServletContext().getAttribute("org.mortbay.cometd");
            if (_bayeux==null)
            {
                _bayeux=new Bayeux(getServletContext());
                getServletContext().setAttribute("org.mortbay.cometd",_bayeux);
            }
        }
    }
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        int message_count=0;
        Client client=null;
        Transport transport=null;
        
        Continuation continuation=ContinuationSupport.getContinuation(req, client);
        
        if (continuation.isPending())
        {
            // re-establish state
            client=(Client)req.getAttribute("client");
            transport=(Transport)req.getAttribute("transport");
        }
        else
        {
            // handle all messages (before any polling)
            String[] messages=req.getParameterValues("message");
            Object objects=null;
            int index=0;
            for (int m=0;m<messages.length;m++)
            {
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
                
                System.err.println(req.hashCode()+" message ==> "+object);
                message_count++;
                Map message=(Map)object;
                
                // Get a client if possible
                if (client==null)
                {
                    client = _bayeux.getClient(message);
                    req.setAttribute("client",client);
                }

                if (transport==null)
                {
                    transport=_bayeux.newTransport(client,message);
                    req.setAttribute("transport",transport);
                    System.err.println(req.hashCode()+" transport="+transport);
                    
                    if (req.getParameter("tunnelInit")!=null)
                    {
                        transport.initTunnel(resp);
                        return;
                    }
                    
                    transport.preample(resp);
                }
              
                Map reply=_bayeux.handle(client,transport, message);
                System.err.println(req.hashCode()+" reply   <== "+reply);
                
                transport.encode(reply); 
            }
        }
        
        
        // handle polling for additinal messages
        while (client!=null && transport!=null && transport.isPolling())
        {
            synchronized (client)
            {      
                client.addContinuation(continuation);
                if (!client.hasMessages())
                    continuation.suspend(25000);
                client.removeContinuation(continuation);
                continuation.reset();
                
                if (client.hasMessages())
                {
                    List messages=client.getMessages();
                    System.err.println(req.hashCode()+" messages<== "+messages);
                    transport.encode(messages);
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
}



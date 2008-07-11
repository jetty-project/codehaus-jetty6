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

package org.mortbay.cometd.continuation;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cometd.Extension;
import org.cometd.Message;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.AbstractCometdServlet;
import org.mortbay.cometd.ClientImpl;
import org.mortbay.cometd.MessageImpl;
import org.mortbay.cometd.Transport;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;


/* ------------------------------------------------------------ */
/**
 * @author gregw
 * @deprecated use {@link org.mortbay.cometd.SuspendingCometdServlet}
 */
public class ContinuationCometdServlet extends AbstractCometdServlet
{
    /* ------------------------------------------------------------ */
    protected AbstractBayeux newBayeux()
    {
        return new ContinuationBayeux();
    }

    /* ------------------------------------------------------------ */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        doPost(req,resp);
    }

    /* ------------------------------------------------------------ */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // Look for an existing client and protect from context restarts
        Object clientObj=req.getAttribute(CLIENT_ATTR);
        ContinuationClient client=(clientObj instanceof ClientImpl)?(ContinuationClient)clientObj:null;
        Transport transport=null;
        boolean connect=false;
        int num_msgs=-1;
        
        // Have we seen this request before
        if (client!=null)
        {
            // yes - extract saved properties
            transport=(Transport)req.getAttribute(TRANSPORT_ATTR);
            transport.setResponse(resp);
        }
        else
        {
            Message[] messages = getMessages(req);
            num_msgs=messages.length;

            /* check jsonp parameter */
            String jsonpParam=req.getParameter("jsonp");

            // Handle all messages
            try
            {
                for (Message message : messages)
                {
                    if (jsonpParam!=null)
                        message.put("jsonp",jsonpParam);

                    if (client==null)
                    {   
                        client=(ContinuationClient)_bayeux.getClient((String)message.get(AbstractBayeux.CLIENT_FIELD));

                        // If no client,  SHOULD be a handshake, so force a transport and handle
                        if (client==null)
                        {
                            // Setup a browser ID
                            String browser_id=browserId(req);
                            if (browser_id==null)
                                browser_id=newBrowserId(req,resp);

                            if (transport==null)
                            {
                                transport=_bayeux.newTransport(client,message);
                                transport.setResponse(resp);
                            }
                            _bayeux.handle(null,transport,message);
                            message=null;

                            continue;
                        }
                        else
                        {
                            String browser_id=browserId(req);
                            if (browser_id!=null && (client.getBrowserId()==null || !client.getBrowserId().equals(browser_id)))
                                client.setBrowserId(browser_id);

                            // resolve transport
                            if (transport==null)
                            {
                                transport=_bayeux.newTransport(client,message);
                                transport.setResponse(resp);
                            }

                            // Tell client to hold messages as a response is likely to be sent.
                            if (!transport.resumePoll())
                                client.responsePending();
                        }
                    }

                    String channel=_bayeux.handle(client,transport,message);
                    connect|=AbstractBayeux.META_CONNECT.equals(channel);
                }
            }
            finally
            {
                if (transport!=null && client!=null && !transport.resumePoll())
                    client.responded();
                
                for (Message message : messages)
                    ((MessageImpl)message).decRef();
            }
        }

        // Do we need to wait for messages
        if (transport!=null)
        {
            Message pollReply=transport.getPollReply();
            if (pollReply!=null)
            {
                if (_bayeux.isLogDebug())
                    _bayeux.logDebug("doPost: transport is polling");
                long timeout=client.getTimeout();
                if (timeout==0)
                    timeout=_bayeux.getTimeout();

                Continuation continuation=ContinuationSupport.getContinuation(req,client);
                if (!continuation.isPending())
                    client.access();

                // Get messages or wait
                synchronized (client)
                {
                    if (!client.hasMessages() && !continuation.isPending()&& num_msgs<=1)
                    {
                        // save state and suspend
                        ((ContinuationClient)client).setContinuation(continuation);
                        req.setAttribute(CLIENT_ATTR,client);
                        req.setAttribute(TRANSPORT_ATTR,transport);
                        continuation.suspend(timeout);
                    }
                    continuation.reset();
                }

                ((ContinuationClient)client).setContinuation(null);
                transport.setPollReply(null);

                for (Extension e:_bayeux.getExtensions())
                    pollReply=e.sendMeta(pollReply);
                transport.send(pollReply);                 
            }
            else if (client!=null)
            {
                client.access();
            }
        }

        // Send any messages.
        if (client!=null) 
        { 
            List<Message> messages = null; 
            Message message = null; 
            synchronized(client)
            {
                switch (client.getMessages())
                {
                    case 0:
                        break;
                    case 1:
                        message = client.takeMessage(); 
                        break;
                    default:
                        messages = client.takeMessages(); 
                        break;
                }
                
                if (!_asyncDeliver)
                {
                    try
                    {
                        if (message!=null)
                            transport.send(message); 
                        else if (messages!=null)
                            transport.send(messages); 
                        
                        transport.complete();
                        resp.flushBuffer();

                        if (transport.resumePoll())
                            client.resume();
                        
                        return;
                    }
                    catch(Throwable e)
                    {
                        // delivery failed!
                        if (message!=null)
                            client.returnMessage(message);
                        else if (messages!=null)
                            client.returnMessages(messages);
                            
                        if (e instanceof ServletException)
                            throw (ServletException)e;
                        if (e instanceof IOException)
                            throw (IOException)e;
                        if (e instanceof RuntimeException)
                            throw (RuntimeException)e;
                        if (e instanceof Error)
                            throw (Error)e;
                        if (e instanceof ThreadDeath)
                            throw (ThreadDeath)e;
                        throw new ServletException(e);
                    }
                }
            }
            
            if (message!=null)
                transport.send(message); 
            else if (messages!=null)
                transport.send(messages); 
            
            if (transport.resumePoll())
            	client.resume();
        }
        
        if (transport!=null)
            transport.complete();
    }
}

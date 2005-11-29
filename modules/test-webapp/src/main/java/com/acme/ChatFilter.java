//========================================================================
//$Id: ChatFilter.java,v 1.4 2005/11/14 11:00:33 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package com.acme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.util.ajax.AjaxFilter;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

public class ChatFilter extends AjaxFilter
{       
    private final String mutex="mutex";
    private Map chatroom;
    

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.ajax.AjaxFilter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        chatroom=new HashMap();
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.ajax.AjaxFilter#destroy()
     */
    public void destroy()
    {
        super.destroy();
        chatroom=null;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.ajax.AjaxFilter#handle(java.lang.String, javax.servlet.http.HttpServletRequest, org.mortbay.ajax.AjaxFilter.AjaxResponse)
     */
    public void handle(String method, HttpServletRequest request, AjaxResponse response)
    {
        if ("join".equals(method))
            doJoinChat(request,response);
        else if ("chat".equals(method))
            doChat(request,response);
        else if ("getEvents".equals(method))
            doGetEvents(request,response);
        else if ("leave".equals(method))
            doLeaveChat(request,response);
        else
            super.handle(method, request, response);                
    }

    /* ------------------------------------------------------------ */
    private void doJoinChat(HttpServletRequest request, AjaxResponse response)
    {
        HttpSession session = request.getSession(true);
        String id = session.getId();
        String name=request.getParameter("name");
        if (name==null || name.length()==0)
            name="Newbie";
        Member member=null;
        
        synchronized (mutex)
        {
            if (chatroom.containsKey(id))
            {
                // exists already, so just update name
                member=(Member)chatroom.get(id);
                if (!name.equals(member.getName()))
                    member.rename(name);
            }
            else
            {
                member = new Member(session,name);
                chatroom.put(session.getId(),member);
                sendEvent(member,"has joined the chat",true);
            }
            
            sendMembers(response);
            response.objectResponse("joined", "<ok/>");
        }
    }
    

    /* ------------------------------------------------------------ */
    private void doLeaveChat(HttpServletRequest request, AjaxResponse response)
    {
        HttpSession session = request.getSession(true);
        String id = session.getId();

        Member member=null;
        synchronized (mutex)
        {
            member = (Member)chatroom.get(id);
            if (member==null)
                return;
            if ("Elvis".equals(member.getName()))
                sendEvent(member,"has left the building",true);
            else
                sendEvent(member,"has left the chat",true);
            chatroom.remove(id);
        }
        sendMembers(response);
        response.objectResponse("left", "<ok/>");
    }


    /* ------------------------------------------------------------ */
    private void doChat(HttpServletRequest request, AjaxResponse response)
    {
        HttpSession session = request.getSession(true);
        String id = session.getId();
        String text = request.getParameter("text");
        
        Member member=null;
        synchronized (mutex)
        {
            member = (Member)chatroom.get(id);
        }
        
        if (member==null)
            return;
        sendEvent(member, text, false);
        
    }


    /* ------------------------------------------------------------ */
    private void doGetEvents(HttpServletRequest request, AjaxResponse response)
    {
        HttpSession session = request.getSession(true);
        String id = session.getId();
        long timeoutMS = 10000L; // TODO configure.
        
        Member member=null;
        synchronized (mutex)
        {
            member = (Member)chatroom.get(id);
        }

        boolean alerts=false;
        if (member!=null)
        {
            // Get an existing Continuation or create a new one if there are no events.
            if (!member.hasEvents())
            {
                Continuation continuation = ContinuationSupport.getContinuation(request, mutex);
                member.setContinuation(continuation);
                continuation.suspend(timeoutMS);
            }
            
            member.setContinuation(null);
            alerts=member.sendEvents(response);
            
            if (alerts)
                sendMembers(response);
            
            // Signal for a new poll
            response.objectResponse("poll", "<ok/>");
        }
    }

    /* ------------------------------------------------------------ */
    private void sendEvent(Member member, String text, boolean alert)
    {
        Event event=new Event(member.getName(),text,alert);
        
        ArrayList invalids=null;
        synchronized (mutex)
        {
            Iterator iter = chatroom.values().iterator();
            while (iter.hasNext())
            {
                Member m = (Member)iter.next();
                
                try 
                {
                    m.getSession().getAttribute("anything");
                    m.addEvent(event);
                }
                catch(IllegalStateException e)
                {
                    if (invalids==null)
                        invalids=new ArrayList();
                    invalids.add(m);
                    iter.remove();
                }
            }
        }
            
        for (int i=0;invalids!=null && i<invalids.size();i++)
        {
            Member m = (Member)invalids.get(i);
            sendEvent(m,"has timed out of the chat",true);
        }
    }
    
    private void sendMembers(AjaxResponse response)
    {
        StringBuffer buf = new StringBuffer();
        buf.append("<ul>\n");
        synchronized (mutex)
        {
            Iterator iter = chatroom.values().iterator();
            while (iter.hasNext())
            {
                Member m = (Member)iter.next();
                buf.append("<li>");
                buf.append(encodeText(m.getName()));
                buf.append("</li>\n");
            }
        }
        buf.append("</ul>\n");
        response.elementResponse("members", buf.toString());
    }

    
    private static class Event
    {
        private String _from;
        private String _text;
        private boolean _alert;
        
        Event(String from, String text, boolean alert)
        {
            _from=from;
            _text=text;
            _alert=alert;
        }
        
        boolean isAlert()
        {
            return _alert;
        }
        
        public String toString()
        {
            return "<event from=\""+_from+"\" alert=\""+_alert+"\">"+encodeText(_text)+"</event>";
        }
        
        
        
    }

    private class Member
    {
        private HttpSession _session;
        private String _name;
        private List _events = new ArrayList();
        private Continuation _continuation;
        
        Member(HttpSession session, String name)
        {
            _session=session;
            _name=name;
        }
        
        /* ------------------------------------------------------------ */
        /**
         * @return Returns the name.
         */
        public String getName()
        {
            return _name;
        }

        /* ------------------------------------------------------------ */
        /**
         * @param name The name to set.
         */
        public void setName(String name)
        {
            _name = name;
        }

        /* ------------------------------------------------------------ */
        /**
         * @return Returns the session.
         */
        public HttpSession getSession()
        {
            return _session;
        }

        /* ------------------------------------------------------------ */
        /**
         * @return Returns the continuation.
         */
        public Continuation getContinuation()
        {
            return _continuation;
        }

        /* ------------------------------------------------------------ */
        /**
         * @param continuation The continuation to set.
         */
        public void setContinuation(Continuation continuation)
        {
            _continuation = continuation;
        }
        
        /* ------------------------------------------------------------ */
        public void addEvent(Event event)
        {
            synchronized (this)
            {
                _events.add(event);
                if (_continuation!=null)
                    _continuation.resume();
            }
        }

        /* ------------------------------------------------------------ */
        public boolean hasEvents()
        {
            return _events!=null && _events.size()>0;
        }
        
        /* ------------------------------------------------------------ */
        public void rename(String name)
        {
            String oldName = getName();
            setName(name);
            ChatFilter.this.sendEvent(this,oldName+" has been renamed to "+name,true);
        }

        /* ------------------------------------------------------------ */
        public boolean sendEvents(AjaxResponse response)
        {
            synchronized (this)
            {
                boolean alerts=false;
                for (int i=0;i<_events.size();i++)
                {
                    Event event = (Event)_events.get(i);
                    response.objectResponse("event", event.toString());
                    alerts |= event.isAlert();
                }
                _events.clear();
                return alerts;
            }
        }

    }
}

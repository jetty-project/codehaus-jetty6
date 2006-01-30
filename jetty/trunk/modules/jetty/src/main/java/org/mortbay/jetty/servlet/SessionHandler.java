//========================================================================
//$Id: SessionHandler.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
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

package org.mortbay.jetty.servlet;

import java.io.IOException;
import java.util.EventListener;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.jetty.SessionManager;
import org.mortbay.jetty.handler.WrappedHandler;
import org.mortbay.log.Log;

/* ------------------------------------------------------------ */
/** SessionHandler.
 * 
 * @author gregw
 *
 */
public class SessionHandler extends WrappedHandler
{
    /* -------------------------------------------------------------- */
    SessionManager _sessionManager=new HashSessionManager();

    /* ------------------------------------------------------------ */
    /** Constructor.
     * Construct a SessionHandler witha a HashSessionManager with a standard
     * java.util.Random generator is created.
     */
    public SessionHandler()
    {   
        this(new HashSessionManager());
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param manager The session manager
     */
    public SessionHandler(SessionManager manager)
    {
        _sessionManager=manager;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the sessionManager.
     */
    public SessionManager getSessionManager()
    {
        return _sessionManager;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param sessionManager The sessionManager to set.
     */
    public void setSessionManager(SessionManager sessionManager)
    {
        if (isStarted())
            throw new IllegalStateException();
        _sessionManager = sessionManager;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        _sessionManager.start();
        super.doStart();
    }
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        _sessionManager.stop();
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
    {
        boolean result=false;
        Request base_request = (request instanceof Request) ? (Request)request:HttpConnection.getCurrentConnection().getRequest();
        SessionManager old_session_manager=null;
        HttpSession old_session=null;
        
        try
        {
            String requested_session_id=request.getRequestedSessionId();
            
            if (requested_session_id==null)
            {
                boolean requested_session_id_from_cookie=false;
                
                // Look for session id cookie     
                Cookie[] cookies=request.getCookies();
                if (cookies!=null && cookies.length>0)
                {
                    for (int i=0;i<cookies.length;i++)
                    {
                        if (SessionManager.__SessionCookie.equalsIgnoreCase(cookies[i].getName()))
                        {
                            if (requested_session_id!=null)
                            {
                                // Multiple jsessionid cookies. Probably due to
                                // multiple paths and/or domains. Pick the first
                                // known session or the last defined cookie.
                                if (_sessionManager.getHttpSession(requested_session_id)!=null)
                                    break;
                            }
                            
                            requested_session_id=cookies[i].getValue();
                            requested_session_id_from_cookie = true;
                            if(Log.isDebugEnabled())Log.debug("Got Session ID "+requested_session_id+" from cookie");
                        }
                    }
                }
                
                if (requested_session_id==null)
                {
                    String uri = request.getRequestURI();
                    int semi = uri.lastIndexOf(';');
                    if (semi>=0)
                    {	
                        String path_params=uri.substring(semi+1);
                        
                        // check if there is a url encoded session param.
                        if (path_params!=null && path_params.startsWith(SessionManager.__SessionURL))
                        {
                            requested_session_id = path_params.substring(SessionManager.__SessionURL.length()+1);
                            if(Log.isDebugEnabled())Log.debug("Got Session ID "+requested_session_id+" from URL");
                        }
                    }
                }
                
                base_request.setRequestedSessionId(requested_session_id);
                base_request.setRequestedSessionIdFromCookie(requested_session_id!=null && requested_session_id_from_cookie);
            }
            
            old_session_manager = base_request.getSessionManager();
            old_session = base_request.getSession(false);
            
            if (old_session_manager != _sessionManager)
            {
                // new session context
                base_request.setSessionManager(_sessionManager);
                base_request.setSession(null);
            }
            
            // access any existing session
            HttpSession session=request.getSession(false);
            if (session!=null)
                ((SessionManager.Session)session).access();
            else if (_sessionManager!=null)
            {
                session=base_request.recoverNewSession(_sessionManager);
                if (session!=null)
                    base_request.setSession(session);
            }
            
            if(Log.isDebugEnabled())
            {
                Log.debug("sessionManager="+base_request.getSessionManager());
                Log.debug("session="+session);
            }
        
            result=getHandler().handle(target, base_request, response, dispatch);
        }
        catch (RetryRequest r)
        {
            HttpSession session=base_request.getSession(false);
            if (session!=null && session.isNew())
                base_request.saveNewSession(_sessionManager,session);
            throw r;
        }
        finally
        {
            base_request.setSessionManager(old_session_manager);
            base_request.setSession(old_session);
        }
        return result;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param listener
     */
    public void addEventListener(EventListener listener)
    {
        if(_sessionManager!=null)
            _sessionManager.addEventListener(listener);
    }

    /* ------------------------------------------------------------ */
    public void clearEventListeners()
    {
        if(_sessionManager!=null)
            _sessionManager.clearEventListeners();
    }
}

// ========================================================================
// Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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
// ========================================================================

package org.mortbay.terracotta.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.eclipse.jetty.continuation.ContinuationThrowable;
import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.mortbay.jetty.RetryRequest;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.log.Log;

/**
 * A specific subclass of {@link SessionHandler} that sets a contract between
 * this class and {@link TerracottaSessionManager}.
 * The contract requires that a Terracotta named lock will be held for the duration
 * of the request, where the lock name depends on the session id.
 * To achieve this, we call {@link TerracottaSessionManager#enter(Request)} and
 * {@link TerracottaSessionManager#exit(Request)}, in order to be able to obtain
 * and release the Terracotta lock.
 * See the {@link TerracottaSessionManager} javadocs for implementation notes.
 *
 * @version $Revision$ $Date$
 */
public class TerracottaSessionHandler extends SessionHandler
{
    public TerracottaSessionHandler()
    {
    }

    public TerracottaSessionHandler(SessionManager manager)
    {
        super(manager);
    }

    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        setRequestedId(baseRequest,request);

        SessionManager old_session_manager=null;
        HttpSession old_session=null;
        TerracottaSessionManager sessionManager = (TerracottaSessionManager)getSessionManager();
        Request currentRequest = (request instanceof Request) ? (Request)request : HttpConnection.getCurrentConnection().getRequest();
        try
        {
            old_session_manager = baseRequest.getSessionManager();
            old_session = baseRequest.getSession(false);
           
            if (old_session_manager != sessionManager)
            {
                // new session context
                baseRequest.setSessionManager(sessionManager);
                baseRequest.setSession(null);
            }
            // Tell the session manager that the request is entering
            if (sessionManager != null) 
                sessionManager.enter(currentRequest);
            
            // access any existing session
            HttpSession session=null;
            if (sessionManager!=null)
            {
                session=baseRequest.getSession(false);
                if (session!=null)
                {
                    if(session!=old_session)
                    {
                        HttpCookie cookie = sessionManager.access(session,request.isSecure());
                        if (cookie!=null ) // Handle changed ID or max-age refresh
                            baseRequest.getResponse().addCookie(cookie);
                    }
                }
                else
                {
                    session=baseRequest.recoverNewSession(sessionManager);
                    if (session!=null)
                        baseRequest.setSession(session);
                }
            }

            if(Log.isDebugEnabled())
            {
                Log.debug("sessionManager="+sessionManager);
                Log.debug("session="+session);
            }

            // start manual inline of nextScope(target,baseRequest,request,response);
            //noinspection ConstantIfStatement
            if (false)
                nextScope(target,baseRequest,request,response);
            else if (_nextScope!=null)
                _nextScope.doScope(target,baseRequest,request, response);
            else if (_outerScope!=null)
                _outerScope.doHandle(target,baseRequest,request, response);
            else 
                doHandle(target,baseRequest,request, response);
            // end manual inline (pathentic attempt to reduce stack depth)

        }
        finally
        {
            HttpSession session=request.getSession(false);
            if (sessionManager != null)
            {
                // User may have invalidated the session, must get it again
                HttpSession currentSession = currentRequest.getSession(false);
                if (currentSession != null) 
                    sessionManager.complete(currentSession);

                sessionManager.exit(currentRequest);
            }
            
            if (old_session_manager != sessionManager)
            {
                //leaving context, free up the session
                if (session!=null)
                    sessionManager.complete(session);
                baseRequest.setSessionManager(old_session_manager);
                baseRequest.setSession(old_session);
            }
        }
    }
    
}

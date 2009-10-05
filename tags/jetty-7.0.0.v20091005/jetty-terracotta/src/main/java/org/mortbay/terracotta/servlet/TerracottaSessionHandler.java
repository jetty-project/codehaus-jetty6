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

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        setRequestedId(baseRequest, request);

        Request currentRequest = (request instanceof Request) ? (Request)request : HttpConnection.getCurrentConnection().getRequest();

        SessionManager requestSessionManager = currentRequest.getSessionManager();
        HttpSession requestSession = currentRequest.getSession(false);

        TerracottaSessionManager sessionManager = (TerracottaSessionManager)getSessionManager();
        Log.debug("SessionManager = {}", sessionManager);

        // Is it a cross context dispatch or a direct hit to this context ?
        if (sessionManager != requestSessionManager)
        {
            // Setup the request for this context
            currentRequest.setSessionManager(sessionManager);
            currentRequest.setSession(null);
        }

        // Tell the session manager that the request is entering
        if (sessionManager != null) sessionManager.enter(currentRequest);
        try
        {
            HttpSession currentSession = null;
            if (sessionManager != null)
            {
                currentSession = currentRequest.getSession(false);
                if (currentSession != null)
                {
                    if (currentSession != requestSession)
                    {
                        // Access the session only if we did not already
                        HttpCookie cookie = sessionManager.access(currentSession, request.isSecure());
                        if (cookie != null)
                        {
                            // Handle changed session id or cookie max-age refresh
                            baseRequest.getResponse().addCookie(cookie);
                        }
                    }
                    else
                    {
                        // Handle resume of the request
                        currentSession = currentRequest.recoverNewSession(sessionManager);
                        if (currentSession != null) currentRequest.setSession(currentSession);
                    }
                }
            }
            Log.debug("Session = {}", currentSession);
            getHandler().handle(target, baseRequest, request, response);
        }
        catch (ContinuationThrowable x)
        {
            // User may have invalidated the session, must get it again
            HttpSession currentSession = currentRequest.getSession(false);
            if (currentSession != null && currentSession.isNew())
                currentRequest.saveNewSession(sessionManager, currentSession);

            throw x;
        }
        finally
        {
            if (sessionManager != null)
            {
                // User may have invalidated the session, must get it again
                HttpSession currentSession = currentRequest.getSession(false);
                if (currentSession != null) sessionManager.complete(currentSession);

                sessionManager.exit(currentRequest);
            }

            // Restore cross context dispatch
            if (sessionManager != requestSessionManager)
            {
                currentRequest.setSessionManager(requestSessionManager);
                currentRequest.setSession(requestSession);
            }
        }
    }
}

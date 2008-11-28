/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mortbay.jetty.security.authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.ServerAuthentication;
import org.mortbay.jetty.security.SimpleAuthResult;

/**
 * @version $Rev$ $Date$
 */
public class SessionCachingServerAuthentication implements ServerAuthentication
{
    public final static String __J_AUTHENTICATED = "org.mortbay.jetty.Auth";

    private final ServerAuthentication _delegate;

    public SessionCachingServerAuthentication(ServerAuthentication delegate)
    {
        this._delegate = delegate;
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        HttpServletRequest request = messageInfo.getRequestMessage();
        HttpSession session = request.getSession(messageInfo.isAuthMandatory());
        // not mandatory and not authenticated
        if (session == null) 
            return new SimpleAuthResult(ServerAuthStatus.SUCCESS, null, null, (String[]) null, null);

        ServerAuthResult serverAuthResult = (ServerAuthResult) session.getAttribute(__J_AUTHENTICATED);
        if (serverAuthResult != null) 
            return serverAuthResult;

        serverAuthResult = _delegate.validateRequest(messageInfo);
        if (serverAuthResult != null && serverAuthResult.getClientSubject() != null)
        {
            ServerAuthResult newServerAuthResult = new SimpleAuthResult(ServerAuthStatus.SUCCESS, serverAuthResult.getClientSubject(), 
                                                                        serverAuthResult.getUserPrincipal(), serverAuthResult.getGroups(), 
                                                                        serverAuthResult.getAuthMethod());
            session.setAttribute(__J_AUTHENTICATED, newServerAuthResult);
        }
        return serverAuthResult;
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) 
    throws ServerAuthException
    {
        return _delegate.secureResponse(messageInfo, validatedUser);
    }

}

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

import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.ServerAuthException;
import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.ServerAuthStatus;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.security.CrossContextPsuedoSession;

/**
 * Cross-context psuedo-session caching ServerAuthentication
 * 
 * @version $Rev$ $Date$
 */
public class XCPSCachingServerAuthentication implements ServerAuthentication
{
    public final static String __J_AUTHENTICATED = "org.mortbay.jetty.Auth";

    private final ServerAuthentication _delegate;

    private final CrossContextPsuedoSession<ServerAuthResult> _xcps;

    public XCPSCachingServerAuthentication(ServerAuthentication delegate, CrossContextPsuedoSession<ServerAuthResult> xcps)
    {
        this._delegate = delegate;
        this._xcps = xcps;
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        HttpServletRequest request = messageInfo.getRequestMessage();

        ServerAuthResult serverAuthResult = _xcps.fetch(request);
        if (serverAuthResult != null) return serverAuthResult;

        serverAuthResult = _delegate.validateRequest(messageInfo);
        if (serverAuthResult != null) _xcps.store(serverAuthResult, messageInfo.getResponseMessage());

        return serverAuthResult;
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException
    {
        return _delegate.secureResponse(messageInfo, validatedUser);
    }

}
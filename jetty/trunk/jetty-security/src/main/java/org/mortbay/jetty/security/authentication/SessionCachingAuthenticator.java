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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.Authentication;
import org.mortbay.jetty.security.SimpleAuthentication;

/**
 * @version $Rev$ $Date$
 */
public class SessionCachingAuthenticator extends DelegateAuthenticator
{
    public final static String __J_AUTHENTICATED = "org.mortbay.jetty.server.Auth";


    public SessionCachingAuthenticator(Authenticator delegate)
    {
        super(delegate);
    }

    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException
    {
        HttpSession session = ((HttpServletRequest)request).getSession(mandatory);
        // not mandatory and not authenticated
        if (session == null) 
            return SimpleAuthentication.SUCCESS_UNAUTH_RESULTS;

        Authentication serverAuthResult = (Authentication) session.getAttribute(__J_AUTHENTICATED);
        if (serverAuthResult != null) 
            return serverAuthResult;

        serverAuthResult = _delegate.validateRequest(request, response, mandatory);
        if (serverAuthResult != null && serverAuthResult.getClientSubject() != null)
        {
            Authentication newServerAuthResult = new SimpleAuthentication(Authentication.Status.SUCCESS, serverAuthResult.getClientSubject(), 
                                                                        serverAuthResult.getUserPrincipal(), serverAuthResult.getRoles(), 
                                                                        serverAuthResult.getAuthMethod());
            session.setAttribute(__J_AUTHENTICATED, newServerAuthResult);
        }
        return serverAuthResult;
    }

}

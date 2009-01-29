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

import java.io.IOException;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.LoginCallbackImpl;
import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.ServerAuthentication;
import org.mortbay.jetty.security.SimpleAuthResult;
import org.mortbay.util.StringUtil;

/**
 * @version $Rev$ $Date$
 */
public class BasicServerAuthentication implements ServerAuthentication
{

    private final LoginService _loginService;

    private final String _realmName;

    public BasicServerAuthentication(LoginService loginService, String realmName)
    {
        this._loginService = loginService;
        this._realmName = realmName;
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        HttpServletRequest request = messageInfo.getRequestMessage();
        HttpServletResponse response = messageInfo.getResponseMessage();
        String credentials = request.getHeader(HttpHeaders.AUTHORIZATION);

        try
        {
            if (credentials != null)
            {                  
                credentials = credentials.substring(credentials.indexOf(' ')+1);
                credentials = B64Code.decode(credentials,StringUtil.__ISO_8859_1);
                int i = credentials.indexOf(':');
                String username = credentials.substring(0,i);
                String password = credentials.substring(i+1);


                LoginCallbackImpl loginCallback = new LoginCallbackImpl(new Subject(), username, password.toCharArray());
                _loginService.login(loginCallback);
                if (loginCallback.isSuccess())
                { 
                    return new SimpleAuthResult(ServerAuthStatus.SUCCESS, 
                                                loginCallback.getSubject(), 
                                                loginCallback.getUserPrincipal(), 
                                                loginCallback.getGroups(), 
                                                Constraint.__BASIC_AUTH); 
                }
            }

            if (!messageInfo.isAuthMandatory()) 
            {
                return SimpleAuthResult.SUCCESS_UNAUTH_RESULTS;
            }
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "basic realm=\"" + _realmName + '"');
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return SimpleAuthResult.SEND_CONTINUE_RESULTS;
        }
        catch (IOException e)
        {
            throw new ServerAuthException(e);
        }
    }

    // most likely validatedUser is not needed here.

    // corrct?
    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException
    {
        return ServerAuthStatus.SUCCESS;
    }

}

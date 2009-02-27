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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.http.HttpHeaders;
import org.mortbay.jetty.http.security.B64Code;
import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.security.LoginCallbackImpl;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.Authentication;
import org.mortbay.jetty.security.SimpleAuthentication;
import org.mortbay.jetty.util.StringUtil;

/**
 * @version $Rev$ $Date$
 */
public class BasicAuthenticator extends LoginAuthenticator 
{
    /* ------------------------------------------------------------ */
    /**
     * @param loginService
     */
    public BasicAuthenticator(LoginService loginService)
    {
        super(loginService);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.security.Authenticator#getAuthMethod()
     */
    public String getAuthMethod()
    {
        return Constraint.__BASIC_AUTH;
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.security.Authenticator#validateRequest(javax.servlet.ServletRequest, javax.servlet.ServletResponse, boolean)
     */
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
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
                    return new SimpleAuthentication(Authentication.Status.SUCCESS, 
                            loginCallback.getSubject(), 
                            loginCallback.getUserPrincipal(), 
                            loginCallback.getRoles(), 
                            Constraint.__BASIC_AUTH); 
                }
            }

            if (!mandatory) 
            {
                return SimpleAuthentication.SUCCESS_UNAUTH_RESULTS;
            }
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "basic realm=\"" + _loginService.getName() + '"');
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return SimpleAuthentication.SEND_CONTINUE_RESULTS;
        }
        catch (IOException e)
        {
            throw new ServerAuthException(e);
        }
    }

    // most likely validatedUser is not needed here.

    // corrct?
    public Authentication.Status secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication validatedUser) throws ServerAuthException
    {
        return Authentication.Status.SUCCESS;
    }

}

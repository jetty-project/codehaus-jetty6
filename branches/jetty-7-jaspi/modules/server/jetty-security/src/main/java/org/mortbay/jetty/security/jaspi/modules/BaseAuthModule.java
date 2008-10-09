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


package org.mortbay.jetty.security.jaspi.modules;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.LoginService;
import org.mortbay.jetty.ServerAuthException;

/**
 * @version $Rev$ $Date$
 */
public class BaseAuthModule implements ServerAuthModule, ServerAuthContext
{
    private static final Class[] SUPPORTED_MESSAGE_TYPES = new Class[] {HttpServletRequest.class, HttpServletResponse.class};

    protected static final String LOGIN_SERVICE_KEY = "org.mortbay.jetty.security.jaspi.modules.LoginService";

    protected CallbackHandler callbackHandler;
    protected LoginService loginService;

    public Class[] getSupportedMessageTypes()
    {
        return SUPPORTED_MESSAGE_TYPES;
    }

    public BaseAuthModule()
    {
    }

    public BaseAuthModule(CallbackHandler callbackHandler, LoginService loginService)
    {
        this.callbackHandler = callbackHandler;
        this.loginService = loginService;
    }

    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException
    {
        this.callbackHandler = handler;
        loginService = (LoginService) options.get(LOGIN_SERVICE_KEY);
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException
    {
        try {
            loginService.logout(subject);
        } catch (ServerAuthException e) {
            throw new AuthException(e.getMessage());
        }
    }

    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException
    {
        //servlets do not need secured responses
        return AuthStatus.SUCCESS;
    }

    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException
    {
        return AuthStatus.FAILURE;
    }

    /**
     * @param messageInfo message info to examine for mandatory flag
     * @return whether authentication is mandatory or optional
     */
    protected boolean isMandatory(MessageInfo messageInfo)
    {
        String mandatory = (String) messageInfo.getMap().get(JettyMessageInfo.MANDATORY_KEY);
        if (mandatory == null) return false;
        return Boolean.valueOf(mandatory);
    }
}

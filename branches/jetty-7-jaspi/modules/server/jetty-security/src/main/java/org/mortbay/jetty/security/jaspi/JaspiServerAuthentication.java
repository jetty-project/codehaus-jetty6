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


package org.mortbay.jetty.security.jaspi;

import java.util.Map;
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.ServletCallbackHandler;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.AuthResult;
import org.mortbay.jetty.security.SimpleAuthResult;

/**
 * @version $Rev$ $Date$
 */
public class JaspiServerAuthentication implements ServerAuthentication {

    private final String authContextId;
    private final ServerAuthConfig authConfig;
    private final Map authProperties;
    private final ServletCallbackHandler callbackHandler;
    private final Subject serviceSubject;

    public JaspiServerAuthentication(String authContextId, ServerAuthConfig authConfig, Map authProperties, ServletCallbackHandler callbackHandler, Subject serviceSubject) {
        if (callbackHandler == null) throw new NullPointerException("No CallbackHandler");
        if (authConfig == null) throw new NullPointerException("No AuthConfig");
        this.authContextId = authContextId;
        this.authConfig = authConfig;
        this.authProperties = authProperties;
        this.callbackHandler = callbackHandler;
        this.serviceSubject = serviceSubject;
    }

    public AuthResult validateRequest(MessageInfo messageInfo, boolean authRequired) throws AuthException {
        ServerAuthContext authContext = authConfig.getAuthContext(authContextId, serviceSubject, authProperties);
        Subject clientSubject = new Subject();

        AuthStatus authStatus = authContext.validateRequest(messageInfo, clientSubject, serviceSubject);
        String authMethod = (String) messageInfo.getMap().get(JettyMessageInfo.AUTH_METHOD_KEY);
        CallerPrincipalCallback principalCallback = callbackHandler.getThreadCallerPrincipalCallback();
        Principal principal = principalCallback == null? null: principalCallback.getPrincipal();
        GroupPrincipalCallback groupPrincipalCallback = callbackHandler.getThreadGroupPrincipalCallback();
        String[] groups = groupPrincipalCallback == null? null: groupPrincipalCallback.getGroups();
        return new SimpleAuthResult(authStatus,
                clientSubject,
                principal,
                groups,
                authMethod);
    }

    public AuthStatus secureResponse(MessageInfo messageInfo, AuthResult validatedUser) throws AuthException {
        ServerAuthContext authContext = authConfig.getAuthContext(authContextId, serviceSubject, authProperties);
        authContext.cleanSubject(messageInfo, validatedUser.getClientSubject());
        return authContext.secureResponse(messageInfo, serviceSubject);
    }
}

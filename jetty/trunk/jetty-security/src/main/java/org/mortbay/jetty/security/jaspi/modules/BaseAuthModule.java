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

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.http.security.B64Code;
import org.mortbay.jetty.http.security.Credential;
import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.security.LoginCallbackImpl;
import org.mortbay.jetty.security.jaspi.JaspiMessageInfo;
import org.mortbay.jetty.security.jaspi.callback.CredentialValidationCallback;
import org.mortbay.jetty.util.StringUtil;

/**
 * @deprecated use *ServerAuthentication
 * @version $Rev$ $Date$
 */
public class BaseAuthModule implements ServerAuthModule, ServerAuthContext
{
    private static final Class[] SUPPORTED_MESSAGE_TYPES = new Class[] { HttpServletRequest.class, HttpServletResponse.class };

    protected static final String LOGIN_SERVICE_KEY = "org.mortbay.jetty.security.jaspi.modules.LoginService";

    protected CallbackHandler callbackHandler;

    public Class[] getSupportedMessageTypes()
    {
        return SUPPORTED_MESSAGE_TYPES;
    }

    public BaseAuthModule()
    {
    }

    public BaseAuthModule(CallbackHandler callbackHandler)
    {
        this.callbackHandler = callbackHandler;
    }

    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException
    {
        this.callbackHandler = handler;
    }

    public void cleanSubject(MessageInfo messageInfo, Subject subject) throws AuthException
    {
        // TODO apparently we either get the LoginCallback or the LoginService
        // but not both :-(
        // Set<LoginCallback> loginCallbacks =
        // subject.getPrivateCredentials(LoginCallback.class);
        // if (!loginCallbacks.isEmpty()) {
        // LoginCallback loginCallback = loginCallbacks.iterator().next();
        // }
        // try {
        // loginService.logout(subject);
        // } catch (ServerAuthException e) {
        // throw new AuthException(e.getMessage());
        // }
    }

    public AuthStatus secureResponse(MessageInfo messageInfo, Subject serviceSubject) throws AuthException
    {
        // servlets do not need secured responses
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
        String mandatory = (String) messageInfo.getMap().get(JaspiMessageInfo.MANDATORY_KEY);
        if (mandatory == null) return false;
        return Boolean.valueOf(mandatory);
    }

    protected boolean login(Subject clientSubject, String credentials, 
                            String authMethod, MessageInfo messageInfo) 
    throws IOException, UnsupportedCallbackException
    {
        credentials = credentials.substring(credentials.indexOf(' ')+1);
        credentials = B64Code.decode(credentials,StringUtil.__ISO_8859_1);
        int i = credentials.indexOf(':');
        String userName = credentials.substring(0,i);
        String password = credentials.substring(i+1);
        return login(clientSubject, userName, new Password(password), authMethod, messageInfo);
    }

    protected boolean login(Subject clientSubject, String username, 
                            Credential credential, String authMethod, 
                            MessageInfo messageInfo) 
    throws IOException, UnsupportedCallbackException
    {
        CredentialValidationCallback credValidationCallback = new CredentialValidationCallback(clientSubject, username, credential);
        callbackHandler.handle(new Callback[] { credValidationCallback });
        if (credValidationCallback.getResult())
        {
            Set<LoginCallbackImpl> loginCallbacks = clientSubject.getPrivateCredentials(LoginCallbackImpl.class);
            if (!loginCallbacks.isEmpty())
            {
                LoginCallbackImpl loginCallback = loginCallbacks.iterator().next();
                CallerPrincipalCallback callerPrincipalCallback = new CallerPrincipalCallback(clientSubject, loginCallback.getUserPrincipal());
                GroupPrincipalCallback groupPrincipalCallback = new GroupPrincipalCallback(clientSubject, loginCallback.getRoles());
                callbackHandler.handle(new Callback[] { callerPrincipalCallback, groupPrincipalCallback });
            }
            messageInfo.getMap().put(JaspiMessageInfo.AUTH_METHOD_KEY, authMethod);
        }
        return credValidationCallback.getResult();

    }
}

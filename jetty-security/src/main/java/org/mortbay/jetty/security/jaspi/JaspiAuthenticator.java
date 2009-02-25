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

import java.security.Principal;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.ServletCallbackHandler;
import org.mortbay.jetty.security.SimpleAuthResult;
import org.mortbay.jetty.security.LazyAuthResult;

/**
 * @version $Rev$ $Date$
 */
public class JaspiAuthenticator implements Authenticator
{
    private final String _authContextId;
    private final ServerAuthConfig _authConfig;
    private final Map _authProperties;
    private final ServletCallbackHandler _callbackHandler;
    private final Subject _serviceSubject;
    private final boolean _allowLazyAuthentication;

    
    public JaspiAuthenticator(String authContextId, ServerAuthConfig authConfig, Map authProperties, ServletCallbackHandler callbackHandler,
            Subject serviceSubject, boolean allowLazyAuthentication)
    {
        if (callbackHandler == null)
            throw new NullPointerException("No CallbackHandler");
        if (authConfig == null)
            throw new NullPointerException("No AuthConfig");
        this._authContextId = authContextId;
        this._authConfig = authConfig;
        this._authProperties = authProperties;
        this._callbackHandler = callbackHandler;
        this._serviceSubject = serviceSubject;
        this._allowLazyAuthentication = allowLazyAuthentication;
    }

    public String getAuthMethod()
    {
        return "JASPI";
    }

    public ServerAuthResult validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException
    {
        if (_allowLazyAuthentication && !mandatory)
            return new LazyAuthResult(this,request,response);
        
        JettyMessageInfo info = new JettyMessageInfo((HttpServletRequest)request,(HttpServletResponse)response,mandatory);
        request.setAttribute("org.mortbay.jetty.security.jaspi.info",info);
        return validateRequest(info);
    }

    // most likely validatedUser is not needed here.
    public ServerAuthStatus secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, ServerAuthResult validatedUser) throws ServerAuthException
    {
        JettyMessageInfo info = (JettyMessageInfo)req.getAttribute("org.mortbay.jetty.security.jaspi.info");
        if (info==null)info = new JettyMessageInfo((HttpServletRequest)req,(HttpServletResponse)res,mandatory);
        return secureResponse(info,validatedUser);
    }
    
    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        try
        {
            ServerAuthContext authContext = _authConfig.getAuthContext(_authContextId,_serviceSubject,_authProperties);
            Subject clientSubject = new Subject();

            AuthStatus authStatus = authContext.validateRequest(new MessageInfoAdapter(messageInfo),clientSubject,_serviceSubject);
            String authMethod = (String)messageInfo.getMap().get(JettyMessageInfo.AUTH_METHOD_KEY);
            CallerPrincipalCallback principalCallback = _callbackHandler.getThreadCallerPrincipalCallback();
            Principal principal = principalCallback == null?null:principalCallback.getPrincipal();
            GroupPrincipalCallback groupPrincipalCallback = _callbackHandler.getThreadGroupPrincipalCallback();
            String[] groups = groupPrincipalCallback == null?null:groupPrincipalCallback.getGroups();
            return new SimpleAuthResult(toServerAuthStatus(authStatus),clientSubject,principal,groups,authMethod);
        }
        catch (AuthException e)
        {
            throw new ServerAuthException(e);
        }
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException
    {
        try
        {
            ServerAuthContext authContext = _authConfig.getAuthContext(_authContextId,_serviceSubject,_authProperties);
            MessageInfoAdapter adapter = new MessageInfoAdapter(messageInfo);
            authContext.cleanSubject(adapter,validatedUser.getClientSubject());
            return toServerAuthStatus(authContext.secureResponse(adapter,_serviceSubject));
        }
        catch (AuthException e)
        {
            throw new ServerAuthException(e);
        }
    }
    
    ServerAuthStatus toServerAuthStatus(AuthStatus authStatus) throws ServerAuthException
    {
        if (authStatus == AuthStatus.SEND_CONTINUE)
            return ServerAuthStatus.SEND_CONTINUE;
        if (authStatus == AuthStatus.SEND_FAILURE)
            return ServerAuthStatus.SEND_FAILURE;
        if (authStatus == AuthStatus.SEND_SUCCESS)
            return ServerAuthStatus.SEND_SUCCESS;
        if (authStatus == AuthStatus.SUCCESS)
            return ServerAuthStatus.SUCCESS;
        throw new ServerAuthException("Invalid server status: " + authStatus);
    }

    private static class MessageInfoAdapter implements MessageInfo
    {
        private final JettyMessageInfo delegate;

        private MessageInfoAdapter(JettyMessageInfo delegate)
        {
            this.delegate = delegate;
        }

        public Map getMap()
        {
            return delegate.getMap();
        }

        public Object getRequestMessage()
        {
            return delegate.getRequestMessage();
        }

        public Object getResponseMessage()
        {
            return delegate.getResponseMessage();
        }

        public void setRequestMessage(Object request)
        {
            if (!(request instanceof HttpServletRequest))
                throw new IllegalStateException("Not a HttpServletRequest: " + request);
            delegate.setRequestMessage((HttpServletRequest)request);
        }

        public void setResponseMessage(Object response)
        {
            if (!(response instanceof HttpServletResponse))
                throw new IllegalStateException("Not a HttpServletResponse: " + response);
            delegate.setResponseMessage((HttpServletResponse)response);
        }
    }
}

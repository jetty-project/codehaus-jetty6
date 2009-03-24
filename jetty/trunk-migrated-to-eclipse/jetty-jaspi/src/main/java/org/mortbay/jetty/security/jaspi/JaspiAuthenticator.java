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
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.callback.CallerPrincipalCallback;
import javax.security.auth.message.callback.GroupPrincipalCallback;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.security.Authentication;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.DefaultAuthentication;
import org.mortbay.jetty.security.LazyAuthentication;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.server.UserIdentity;

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
        // TODO maybe pass this in via setConfiguration ?
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


    public void setConfiguration(Configuration configuration)
    {
    }
    
    
    public String getAuthMethod()
    {
        return "JASPI";
    }

    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException
    {
        if (_allowLazyAuthentication && !mandatory)
            return new LazyAuthentication(this,request,response);
        
        JaspiMessageInfo info = new JaspiMessageInfo((HttpServletRequest)request,(HttpServletResponse)response,mandatory);
        request.setAttribute("org.mortbay.jetty.security.jaspi.info",info);
        return validateRequest(info);
    }

    // most likely validatedUser is not needed here.
    public Authentication.Status secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication validatedUser) throws ServerAuthException
    {
        JaspiMessageInfo info = (JaspiMessageInfo)req.getAttribute("org.mortbay.jetty.security.jaspi.info");
        if (info==null)info = new JaspiMessageInfo((HttpServletRequest)req,(HttpServletResponse)res,mandatory);
        return secureResponse(info,validatedUser);
    }
    
    public Authentication validateRequest(JaspiMessageInfo messageInfo) throws ServerAuthException
    {
        try
        {
            ServerAuthContext authContext = _authConfig.getAuthContext(_authContextId,_serviceSubject,_authProperties);
            Subject clientSubject = new Subject();

            AuthStatus authStatus = authContext.validateRequest(messageInfo,clientSubject,_serviceSubject);
            String authMethod = (String)messageInfo.getMap().get(JaspiMessageInfo.AUTH_METHOD_KEY);
            CallerPrincipalCallback principalCallback = _callbackHandler.getThreadCallerPrincipalCallback();
            Principal principal = principalCallback == null?null:principalCallback.getPrincipal();
            GroupPrincipalCallback groupPrincipalCallback = _callbackHandler.getThreadGroupPrincipalCallback();
            String[] groups = groupPrincipalCallback == null?null:groupPrincipalCallback.getGroups();
            
            Set<UserIdentity> ids = clientSubject.getPrivateCredentials(UserIdentity.class);
            if (ids.size()>0)
                return new DefaultAuthentication(toServerAuthStatus(authStatus),authMethod,ids.iterator().next());
            return Authentication.SEND_FAILURE_RESULTS;
        }
        catch (AuthException e)
        {
            throw new ServerAuthException(e);
        }
    }

    public Authentication.Status secureResponse(JaspiMessageInfo messageInfo, Authentication validatedUser) throws ServerAuthException
    {
        try
        {
            ServerAuthContext authContext = _authConfig.getAuthContext(_authContextId,_serviceSubject,_authProperties);
            authContext.cleanSubject(messageInfo,validatedUser.getUserIdentity().getSubject());
            return toServerAuthStatus(authContext.secureResponse(messageInfo,_serviceSubject));
        }
        catch (AuthException e)
        {
            throw new ServerAuthException(e);
        }
    }
    
    Authentication.Status toServerAuthStatus(AuthStatus authStatus) throws ServerAuthException
    {
        if (authStatus == AuthStatus.SEND_CONTINUE)
            return Authentication.Status.SEND_CONTINUE;
        if (authStatus == AuthStatus.SEND_FAILURE)
            return Authentication.Status.SEND_FAILURE;
        if (authStatus == AuthStatus.SEND_SUCCESS)
            return Authentication.Status.SEND_SUCCESS;
        if (authStatus == AuthStatus.SUCCESS)
            return Authentication.Status.SUCCESS;
        throw new ServerAuthException("Invalid server status: " + authStatus);
    }

}

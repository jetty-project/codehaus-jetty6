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

package org.mortbay.jetty.security;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.server.Handler;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Response;
import org.mortbay.jetty.server.UserIdentity;
import org.mortbay.jetty.server.handler.ContextHandler;
import org.mortbay.jetty.server.handler.HandlerWrapper;
import org.mortbay.jetty.util.component.LifeCycle;
import org.mortbay.jetty.util.log.Log;

/**
 * Abstract SecurityHandler.
 * Select and apply an {@link Authenticator} to a request.
 * <p>
 * The Authenticator may either be directly set on the handler
 * or will be create during {@link #start()} with a call to
 * either the default or set AuthenticatorFactory.
 */
public abstract class SecurityHandler extends HandlerWrapper implements Authenticator.Configuration
{
    /* ------------------------------------------------------------ */
    private boolean _checkWelcomeFiles = false;
    private Authenticator _authenticator;
    private Authenticator.Factory _authenticatorFactory=new DefaultAuthenticatorFactory();
    private boolean _isLazy=true;
    private String _realmName;
    private String _authMethod;
    private final Map<String,String> _initParameters=new HashMap<String,String>();
    private LoginService _loginService;
    private boolean _loginServiceShared;
    private IdentityService<UserIdentity,?> _identityService;

    /* ------------------------------------------------------------ */
    protected SecurityHandler()
    {
    }
    
    /* ------------------------------------------------------------ */
    /** Get the identityService.
     * @return the identityService
     */
    public IdentityService<UserIdentity,?> getIdentityService()
    {
        return _identityService;
    }

    /* ------------------------------------------------------------ */
    /** Set the identityService.
     * @param identityService the identityService to set
     */
    public void setIdentityService(IdentityService<UserIdentity,?> identityService)
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        _identityService = identityService;
    }

    /* ------------------------------------------------------------ */
    /** Get the loginService.
     * @return the loginService
     */
    public LoginService getLoginService()
    {
        return _loginService;
    }

    /* ------------------------------------------------------------ */
    /** Set the loginService.
     * @param loginService the loginService to set
     */
    public void setLoginService(LoginService loginService)
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        _loginService = loginService;
        _loginServiceShared=false;
    }


    /* ------------------------------------------------------------ */
    public Authenticator getAuthenticator()
    {
        return _authenticator;
    }

    /* ------------------------------------------------------------ */
    /** Set the authenticator.
     * @param authenticator
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setAuthenticator(Authenticator authenticator)
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        _authenticator = authenticator;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the authenticatorFactory
     */
    public Authenticator.Factory getAuthenticatorFactory()
    {
        return _authenticatorFactory;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param authenticatorFactory the authenticatorFactory to set
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setAuthenticatorFactory(Authenticator.Factory authenticatorFactory)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        _authenticatorFactory = authenticatorFactory;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the isLazy
     */
    public boolean isLazy()
    {
        return _isLazy;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param isLazy the isLazy to set
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setLazy(boolean isLazy)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        _isLazy = isLazy;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the realmName
     */
    public String getRealmName()
    {
        return _realmName;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param realmName the realmName to set
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setRealmName(String realmName)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        _realmName = realmName;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return the authMethod
     */
    public String getAuthMethod()
    {
        return _authMethod;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param authMethod the authMethod to set
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setAuthMethod(String authMethod)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        _authMethod = authMethod;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return True if forwards to welcome files are authenticated
     */
    public boolean isCheckWelcomeFiles()
    {
        return _checkWelcomeFiles;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param authenticateWelcomeFiles True if forwards to welcome files are
     *                authenticated
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public void setCheckWelcomeFiles(boolean authenticateWelcomeFiles)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        _checkWelcomeFiles = authenticateWelcomeFiles;
    }

    /* ------------------------------------------------------------ */
    public String getInitParameter(String key)
    {
        return _initParameters.get(key);
    }
    
    /* ------------------------------------------------------------ */
    public Set<String> getInitParameterNames()
    {
        return _initParameters.keySet();
    }
    
    /* ------------------------------------------------------------ */
    /** Set an initialization parameter.
     * @param key
     * @param value
     * @return previous value
     * @throws IllegalStateException if the SecurityHandler is running
     */
    public String setInitParameter(String key, String value)
    {
        if (isRunning())
            throw new IllegalStateException("running");
        return _initParameters.put(key,value);
    }
    

    /* ------------------------------------------------------------ */
    protected LoginService findLoginService()
    {
        List<LoginService> list = getServer().getBeans(LoginService.class);
        
        for (LoginService service : list)
            if (service.getName().equals(getRealmName()))
                return service;
        if (list.size()>0)
            return list.get(0);
        return null;
    }
    
    /* ------------------------------------------------------------ */
    protected IdentityService<UserIdentity,?> findIdentityService()
    {
        List<IdentityService> services = getServer().getBeans(IdentityService.class);
        if (services!=null && services.size()>0)
            return services.get(0);
        return null;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     */
    protected void doStart()
        throws Exception
    {
        // complicated resolution of login and identity service to handle
        // many different ways these can be constructed and injected.
        
        if (_loginService==null)
        {
            _loginService=findLoginService();
            if (_loginService!=null)
                _loginServiceShared=true;
        }
        
        if (_identityService==null)
        {
            if (_loginService!=null)
                _identityService=_loginService.getIdentityService();

            if (_identityService==null)
                _identityService=findIdentityService();
            
            if (_identityService==null)
                _identityService=new DefaultIdentityService();
        }
        
        if (_loginService!=null)
        {
            if (_loginService.getIdentityService()==null)
                _loginService.setIdentityService(_identityService);
            else if (_loginService.getIdentityService()!=_identityService)
                throw new IllegalStateException("LoginService has different IdentityService to "+this);
        }

        if (!_loginServiceShared && _loginService instanceof LifeCycle)
            ((LifeCycle)_loginService).start();        
        
        if (_authenticator==null && _authenticatorFactory!=null)
        {
            _authenticator=_authenticatorFactory.getAuthenticator(getServer(),ContextHandler.getCurrentContext(),this);
            if (_authenticator!=null)
                _authMethod=_authenticator.getAuthMethod();
        }

        if (_authenticator==null)
        {
            Log.warn("No ServerAuthentication for "+this);
            throw new IllegalStateException("No ServerAuthentication");
        }
        
        _authenticator.setConfiguration(this);
        if (_authenticator instanceof LifeCycle)
            ((LifeCycle)_authenticator).start();
        
        
        super.doStart();
    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.server.handler.HandlerWrapper#doStop()
     */
    @Override
    protected void doStop() throws Exception
    {
        super.doStop();
        
        if (!_loginServiceShared && _loginService instanceof LifeCycle)
            ((LifeCycle)_loginService).stop();
        
    }

    protected boolean checkSecurity(Request request)
    {
        switch(request.getDispatcherType())
        {
            case REQUEST:
            case ASYNC:
                return true;
            case FORWARD:
                if (_checkWelcomeFiles && request.getAttribute("org.mortbay.jetty.server.welcome") != null)
                {
                    request.removeAttribute("org.mortbay.jetty.server.welcome");
                    return true;
                }
                return false;
            default:
                return false;
        }
    }
    
    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.server.Handler#handle(java.lang.String,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String pathInContext, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        final Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        final Response base_response = (response instanceof Response) ? (Response) response : HttpConnection.getCurrentConnection().getResponse();
        final Handler handler=getHandler();
        
        if (handler==null)
            return;
        
        if (checkSecurity(base_request))
        {
            Object constraintInfo = prepareConstraintInfo(pathInContext, base_request);
            
            // Check data constraints
            if (!checkUserDataPermissions(pathInContext, base_request, base_response, constraintInfo))
            {
                if (!base_request.isHandled())
                {
                    response.sendError(Response.SC_FORBIDDEN);
                    base_request.setHandled(true);
                }
                return;
            }

            // is Auth mandatory?
            boolean isAuthMandatory = isAuthMandatory(base_request, base_response, constraintInfo);

            // check authentication
            UserIdentity old_user_identity=base_request.getUserIdentity();
            try
            {
                final Authenticator authenticator = _authenticator;
                Authentication authentication = authenticator.validateRequest(request, response, isAuthMandatory);

                if (authentication.getAuthStatus() == Authentication.Status.SUCCESS)
                {
                    final UserIdentity user_identity=authentication.getUserIdentity();
                    base_request.setAuthType(authentication.getAuthMethod());
                    base_request.setUserIdentity(user_identity);

                    if (isAuthMandatory && !checkWebResourcePermissions(pathInContext, base_request, base_response, constraintInfo, user_identity))
                    {
                        response.sendError(Response.SC_FORBIDDEN, "User not in required role");
                        base_request.setHandled(true);
                        return;
                    }
                         
                    handler.handle(pathInContext, request, response);

                    authenticator.secureResponse(request, response, isAuthMandatory, authentication);
                }
                else
                {
                    base_request.setHandled(true);
                }
            }
            catch (ServerAuthException e)
            {
                // jaspi 3.8.3 send HTTP 500 internal server error, with message
                // from AuthException
                response.sendError(Response.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }
            finally
            {
                base_request.setUserIdentity(old_user_identity);   
            }
        }
        else
            handler.handle(pathInContext, request, response);
    }


    /* ------------------------------------------------------------ */
    protected abstract Object prepareConstraintInfo(String pathInContext, Request request);

    /* ------------------------------------------------------------ */
    protected abstract boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException;

    /* ------------------------------------------------------------ */
    protected abstract boolean isAuthMandatory(Request base_request, Response base_response, Object constraintInfo);

    /* ------------------------------------------------------------ */
    protected abstract boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo,
                                                           UserIdentity userIdentity) throws IOException;

    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public class NotChecked implements Principal
    {
        public String getName()
        {
            return null;
        }

        public String toString()
        {
            return "NOT CHECKED";
        }

        public SecurityHandler getSecurityHandler()
        {
            return SecurityHandler.this;
        }
    }

    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static Principal __NO_USER = new Principal()
    {
        public String getName()
        {
            return null;
        }

        public String toString()
        {
            return "No User";
        }
    };
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /**
     * Nobody user. The Nobody UserPrincipal is used to indicate a partial state
     * of authentication. A request with a Nobody UserPrincipal will be allowed
     * past all authentication constraints - but will not be considered an
     * authenticated request. It can be used by Authenticators such as
     * FormAuthenticator to allow access to logon and error pages within an
     * authenticated URI tree.
     */
    public static Principal __NOBODY = new Principal()
    {
        public String getName()
        {
            return "Nobody";
        }

        public String toString()
        {
            return getName();
        }
    };


}

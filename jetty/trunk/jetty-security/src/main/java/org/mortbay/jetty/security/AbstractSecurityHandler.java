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

import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.server.AuthenticationManager;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Response;
import org.mortbay.jetty.server.UserIdentity;
import org.mortbay.jetty.server.UserRealm;
import org.mortbay.jetty.server.handler.HandlerWrapper;
import org.mortbay.jetty.server.handler.SecurityHandler;
import org.mortbay.jetty.server.servlet.FilterMapping;
import org.mortbay.jetty.util.log.Log;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractSecurityHandler extends HandlerWrapper implements SecurityHandler
{
    private UserRealm _realm;

    public UserRealm getUserRealm()
    {
        return _realm;
    }

    public void setUserRealm(UserRealm realm)
    {
        _realm = realm;
    }


    /* ------------------------------------------------------------ */
    // private UserRealm _userRealm;
    // private NotChecked _notChecked = new NotChecked();
    private boolean _checkWelcomeFiles = false;

    // private ServerAuthConfig authConfig;
    // private Subject serviceSubject;
    // private Map authProperties;
    // private ServletCallbackHandler servletCallbackHandler;

    //private ServerAuthentication _serverAuthentication;
    private AuthenticationManager<ServerAuthentication> _authManager = new DefaultAuthenticationManager();

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
     */
    public void setCheckWelcomeFiles(boolean authenticateWelcomeFiles)
    {
        _checkWelcomeFiles = authenticateWelcomeFiles;
    }

    public void setAuthenticationManager (AuthenticationManager authManager)
    {
        _authManager = authManager;
    }
    
    public AuthenticationManager getAuthenticationManager ()
    {
        return _authManager;
    }
    
    // set jaspi components

  /*  public void setServerAuthentication(ServerAuthentication serverAuthentication)
    {
        this._serverAuthentication = serverAuthentication;
    }
*/
    /* ------------------------------------------------------------ */
    public void doStart() throws Exception
    {
        super.doStart();
        if (_authManager == null)
        {
            _authManager = new DefaultAuthenticationManager();
            Log.warn ("Using DefaultAuthenticationManager");
        }
        
        _authManager.setSecurityHandler(this);
        _authManager.start();
        
        //if (_serverAuthentication == null) throw new NullPointerException("No auth configuration configured");
    }/* ------------------------------------------------------------ */

    /*
     * @see org.mortbay.jetty.server.Handler#handle(java.lang.String,
     *      javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String pathInContext, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
    {
        Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        Response base_response = (response instanceof Response) ? (Response) response : HttpConnection.getCurrentConnection().getResponse();
        DispatcherType dispatch=request.getDispatcherType();
        
        try
        {
            boolean checkSecurity = DispatcherType.REQUEST.equals(dispatch);
            if (DispatcherType.FORWARD.equals(dispatch) && _checkWelcomeFiles && request.getAttribute("org.mortbay.jetty.server.welcome") != null)
            {
                request.removeAttribute("org.mortbay.jetty.server.welcome");
                checkSecurity = true;
            }
            if (checkSecurity)
            {
                Object constraintInfo = prepareConstraintInfo(pathInContext, base_request);
                if (!checkUserDataPermissions(pathInContext, base_request, base_response, constraintInfo))
                {
                    if (!base_request.isHandled())
                    {
                        response.sendError(Response.SC_FORBIDDEN);
                        base_request.setHandled(true);
                    }
                    return;
                }
                // JASPI 3.8.1
                boolean isAuthMandatory = isAuthMandatory(base_request, base_response, constraintInfo);
                // TODO check with greg about whether requirement that these be
                // the request/response passed to the resource(servlet) is
                // realistic (i.e. this requires no wrapping between here and
                // invocation)
                JettyMessageInfo messageInfo = new JettyMessageInfo(request, response, isAuthMandatory);
                // String authContextID =
                // authConfig.getAuthContextID(messageInfo);
                // ServerAuthContext authContext =
                // authConfig.getAuthContext(authContextID, serviceSubject,
                // authProperties);

                // JASPI 3.8.2
                // Subject clientSubject = new Subject();
                try
                {
                    ServerAuthentication serverAuthentication = _authManager.getServerAuthentication();
                    ServerAuthResult authResult = serverAuthentication.validateRequest(messageInfo);
                    // AuthStatus authStatus =
                    // authContext.validateRequest(messageInfo, clientSubject,
                    // serviceSubject);
                    
                    if (authResult.getAuthStatus() == ServerAuthStatus.SUCCESS)
                    {
                        // JASPI 3.8. Supply the UserPrincipal and ClientSubject
                        // to the web resource permission check
                        // JASPI 3.8.4 establish request values
                        UserIdentity userIdentity = newUserIdentity(authResult);
                        base_request.setUserIdentity(userIdentity);
                        // isAuthMandatory == false means that request is ok
                        // without any roles assigned.... no need to check now
                        // that we know the roles.
                        if (isAuthMandatory && !checkWebResourcePermissions(pathInContext, base_request, base_response, constraintInfo, userIdentity))
                        {
                            response.sendError(Response.SC_FORBIDDEN, "User not in required role");
                            base_request.setHandled(true);
                            return;
                        }
                        if (getHandler() != null)
                        {
                            // jaspi 3.8.3 auth processing may wrap messages,
                            // use the modified versions
                            //getHandler().handle(pathInContext, messageInfo.getRequestMessage(), messageInfo.getResponseMessage(), dispatch);                           
                            getHandler().handle(pathInContext, messageInfo.getRequestMessage(), messageInfo.getResponseMessage());
                            // TODO set secureResponse = false on error thrown
                            // by servlet to jetty
                            boolean secureResponse = true;
                            if (secureResponse)
                            {
                                serverAuthentication.secureResponse(messageInfo, authResult);
                            }
                        }
                      
                        // TODO is this a sufficient dissociate call?
                        base_request.setUserIdentity(UserIdentity.UNAUTHENTICATED_IDENTITY);
                    }
                    // jaspi otherwise the authContext has cconfigured an
                    // appropriate reply message that does not need to be
                    // secured.
                    else
                    {
                        base_request.setHandled(true);
                    }
                }
                catch (RuntimeException e)
                {
                    throw e;
                }
                catch (IOException e)
                {
                    throw e;
                }
                catch (ServletException e)
                {
                    throw e;
                }
                catch (Error e)
                {
                    throw e;
                }
                finally
                {
                    // jaspi clean up subject
                    // authContext.cleanSubject(messageInfo, clientSubject);
                }
            }
            else
            {
                if (getHandler() != null)
                {
                    //getHandler().handle(pathInContext, request, response, dispatch);
                	getHandler().handle(pathInContext, request, response);
                }
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
            // jaspi this appears to be unnecessary unless there is a major
            // jetty bug
            // if (_userRealm!=null)
            // {
            // if (dispatch==REQUEST)
            // {
            // _userRealm.disassociate(base_request.getUserPrincipal());
            // }
            // }
            // base_request.setUserRealm(old_realm);
        }
    }

    protected abstract UserIdentity newUserIdentity(ServerAuthResult authResult);

    protected abstract UserIdentity newSystemUserIdentity();

    protected abstract Object prepareConstraintInfo(String pathInContext, Request request);

    protected abstract boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException;

    protected abstract boolean isAuthMandatory(Request base_request, Response base_response, Object constraintInfo);

    protected abstract boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo,
                                                           UserIdentity userIdentity) throws IOException;

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
            return AbstractSecurityHandler.this;
        }
    }
}

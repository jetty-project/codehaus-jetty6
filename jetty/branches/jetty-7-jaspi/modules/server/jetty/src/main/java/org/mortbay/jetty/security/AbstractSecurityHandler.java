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

import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.UserIdentity;
import org.mortbay.jetty.AuthResult;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.handler.SecurityHandler;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractSecurityHandler extends HandlerWrapper implements SecurityHandler
{

/* ------------------------------------------------------------ */
//    private UserRealm _userRealm;
//    private NotChecked _notChecked = new NotChecked();
    private boolean _checkWelcomeFiles = false;//jaspi stuff
//    private ServerAuthConfig authConfig;
//    private Subject serviceSubject;
//    private Map authProperties;
//    private ServletCallbackHandler servletCallbackHandler;

    private ServerAuthentication serverAuthentication;

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
     * Nobody user.
     * The Nobody UserPrincipal is used to indicate a partial state of
     * authentication. A request with a Nobody UserPrincipal will be allowed
     * past all authentication constraints - but will not be considered an
     * authenticated request.  It can be used by Authenticators such as
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
     * @param authenticateWelcomeFiles True if forwards to welcome files are authenticated
     */
    public void setCheckWelcomeFiles(boolean authenticateWelcomeFiles)
    {
        _checkWelcomeFiles = authenticateWelcomeFiles;
    }

    //set jaspi components

    public void setServerAuthentication(ServerAuthentication serverAuthentication) {
        this.serverAuthentication = serverAuthentication;
    }

    /* ------------------------------------------------------------ */
    public void doStart()
            throws Exception
    {
        super.doStart();
        if (serverAuthentication == null)
            throw new NullPointerException("No auth configuration configured");
    }/* ------------------------------------------------------------ */

    /*
    * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
    */
    public void handle(String pathInContext, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
        Response base_response = (response instanceof Response) ? (Response) response : HttpConnection.getCurrentConnection().getResponse();
        try
        {
            boolean checkSecurity = dispatch == REQUEST;
            if (dispatch == FORWARD && _checkWelcomeFiles && request.getAttribute("org.mortbay.jetty.welcome") != null)
            {
                request.removeAttribute("org.mortbay.jetty.welcome");
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
                //JASPI 3.8.1
                boolean isAuthMandatory = isAuthMandatory(base_request, base_response, constraintInfo);
                //TODO check with greg about whether requirement that these be the request/response passed to the resource(servlet) is realistic (i.e. this requires no wrapping between here and invocation)
                //TODO we have to get the auth context from the authconfig on each call.
                MessageInfo messageInfo = new JettyMessageInfo(request, response, isAuthMandatory);
//                String authContextID = authConfig.getAuthContextID(messageInfo);
//                ServerAuthContext authContext = authConfig.getAuthContext(authContextID, serviceSubject, authProperties);

                //JASPI 3.8.2
//                Subject clientSubject = new Subject();

                try
                {
                    AuthResult authResult = serverAuthentication.validateRequest(messageInfo, isAuthMandatory);
//                    AuthStatus authStatus = authContext.validateRequest(messageInfo, clientSubject, serviceSubject);
                    if (!isAuthMandatory || authResult.getAuthStatus() == AuthStatus.SUCCESS)
                    {
                        //JASPI 3.8.  Supply the UserPrincipal and ClientSubject to the web resource permission check
                        //JASPI 3.8.4 establish request values
                        UserIdentity userIdentity = newUserIdentity(authResult);
                        base_request.setUserIdentity(userIdentity);
//                        if (userIdentity.getUserPrincipal() == null)
//                        {
//                            base_request.setAuthType(null);
//                        }
//                        else
//                        {
//                            //NOTE! we assume jaspi is configured to always provide correct authMethod values
//                            base_request.setAuthType((String) messageInfo.getMap().get(JettyMessageInfo.AUTH_METHOD_KEY));
//                        }
                        if (!isAuthMandatory && !checkWebResourcePermissions(pathInContext, base_request, base_response, constraintInfo, userIdentity))
                        {
                            response.sendError(Response.SC_FORBIDDEN,"User not in required role");
                            base_request.setHandled(true);
                            return;
                        }
                        if (getHandler() != null)
                        {
                            //jaspi 3.8.3 auth processing may wrap messages, use the modified versions
                            getHandler().handle(pathInContext, (HttpServletRequest) messageInfo.getRequestMessage(), (HttpServletResponse) messageInfo.getResponseMessage(), dispatch);
                            //TODO set secureResponse = false on error thrown by servlet to jetty
                            boolean secureResponse = true;
                            if (secureResponse)
                            {
                                serverAuthentication.secureResponse(messageInfo, authResult);
//                                authContext.secureResponse(messageInfo, serviceSubject);
                            }
                        }
                        //TODO is this a sufficient dissociate call?
                        base_request.setUserIdentity(UserIdentity.UNAUTHENTICATED_IDENTITY);
                    }
                    //jaspi otherwise the authContext has cconfigured an appropriate reply message that does not need to be secured.
                    else
                    {
                        base_request.setHandled(true);
                    }
                }
                catch (RuntimeException e) {
                    throw e;
                }
                catch (IOException e) {
                    throw e;
                }
                catch (ServletException e) {
                    throw e;
                }
                catch (Error e) {
                    throw e;
                }
                finally
                {
                    //jaspi clean up subject
//                    authContext.cleanSubject(messageInfo, clientSubject);
                }

            }
            else
            {
//            if (dispatch==REQUEST && !checkSecurityConstraints(target,base_request,base_response))
//            {
//                base_request.setHandled(true);
//                return;
//            }
//
//            if (dispatch==FORWARD && _checkWelcomeFiles && request.getAttribute("org.mortbay.jetty.welcome")!=null)
//            {
//                request.removeAttribute("org.mortbay.jetty.welcome");
//                if (!checkSecurityConstraints(target,base_request,base_response))
//                {
//                    base_request.setHandled(true);
//                    return;
//                }
//            }

                //jaspi what dispatch does this have???
//            if (_authenticator instanceof FormAuthenticator && target.endsWith(FormAuthenticator.__J_SECURITY_CHECK))
//            {
//                _authenticator.authenticate(getUserRealm(),target,base_request,base_response);
//                base_request.setHandled(true);
//                return;
//            }

                if (getHandler() != null)
                {
                    getHandler().handle(pathInContext, request, response, dispatch);
                }
            }
        }
        catch (AuthException e)
        {
            //jaspi 3.8.3 send HTTP 500 internal server error, with message from AuthException
            response.sendError(Response.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
        finally
        {
            //jaspi this appears to be unnecessary unless there is a major jetty bug
//            if (_userRealm!=null)
//            {
//                if (dispatch==REQUEST)
//                {
//                    _userRealm.disassociate(base_request.getUserPrincipal());
//                }
//            }
//            base_request.setUserRealm(old_realm);
        }
    }

    protected abstract UserIdentity newUserIdentity(AuthResult authResult);

    protected abstract UserIdentity newSystemUserIdentity();

    protected abstract Object prepareConstraintInfo(
            String pathInContext,
            Request request);

    protected abstract boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException;

    protected abstract boolean isAuthMandatory(Request base_request, Response base_response, Object constraintInfo);

    protected abstract boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException;

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

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
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.LoginCallback;
import org.mortbay.jetty.LoginService;
import org.mortbay.jetty.ServerAuthException;
import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.ServerAuthStatus;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.SimpleAuthResult;
import org.mortbay.log.Log;
import org.mortbay.util.StringUtil;
import org.mortbay.util.URIUtil;

/**
 * @version $Rev$ $Date$
 */
public class FormServerAuthentication implements ServerAuthentication {
    public final static String __J_URI = "org.mortbay.jetty.URI";
    public final static String __J_AUTHENTICATED = "org.mortbay.jetty.Auth";
    public final static String __J_SECURITY_CHECK = "/j_security_check";
    public final static String __J_USERNAME = "j_username";
    public final static String __J_PASSWORD = "j_password";


    private final LoginService _loginService;
    private String _formErrorPage;
    private String _formErrorPath;
    private String _formLoginPage;
    private String _formLoginPath;

    public FormServerAuthentication(String loginPage, String errorPage, LoginService loginService) {
        setLoginPage(loginPage);
        setErrorPage(errorPage);
        this._loginService = loginService;
    }

    private void setLoginPage(String path) {
        if (!path.startsWith("/")) {
            Log.warn("form-login-page must start with /");
            path = "/" + path;
        }
        _formLoginPage = path;
        _formLoginPath = path;
        if (_formLoginPath.indexOf('?') > 0)
            _formLoginPath = _formLoginPath.substring(0, _formLoginPath.indexOf('?'));
    }

    /* ------------------------------------------------------------ */
    private void setErrorPage(String path) {
        if (path == null || path.trim().length() == 0) {
            _formErrorPath = null;
            _formErrorPage = null;
        } else {
            if (!path.startsWith("/")) {
                Log.warn("form-error-page must start with /");
                path = "/" + path;
            }
            _formErrorPage = path;
            _formErrorPath = path;

            if (_formErrorPath.indexOf('?') > 0)
                _formErrorPath = _formErrorPath.substring(0, _formErrorPath.indexOf('?'));
        }
    }


    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException {
        HttpServletRequest request = messageInfo.getRequestMessage();
        HttpServletResponse response = messageInfo.getResponseMessage();
        HttpSession session = request.getSession(messageInfo.isAuthMandatory());
        String uri = request.getPathInfo();
        //not mandatory and not authenticated
        if (session == null || isLoginOrErrorPage(uri))
            return new SimpleAuthResult(ServerAuthStatus.SUCCESS);

        try {
            // Handle a request for authentication.
            // TODO perhaps j_securitycheck can be uri suffix?
            if (uri.endsWith(__J_SECURITY_CHECK)) {

                final String username = request.getParameter(__J_USERNAME);
                final char[] password = request.getParameter(__J_PASSWORD).toCharArray();
                LoginCallback loginCallback = new LoginCallback(new Subject(), username, password);
                _loginService.login(loginCallback);
                if (loginCallback.isSuccess()) {
                    // Redirect to original request
                    String nuri = (String) session.getAttribute(__J_URI);
                    if (nuri == null || nuri.length() == 0) {
                        nuri = request.getContextPath();
                        if (nuri.length() == 0)
                            nuri = URIUtil.SLASH;
                    }
                    //TODO shouldn't we forward to original URI instead?
                    session.removeAttribute(__J_URI); // Remove popped return URI.
                    response.setContentLength(0);
                    response.sendRedirect(response.encodeRedirectURL(nuri));

                    return new SimpleAuthResult(ServerAuthStatus.SEND_CONTINUE, loginCallback.getSubject(), loginCallback.getUserPrincipal(), loginCallback.getGroups(), Constraint.__FORM_AUTH);
                }
                //not authenticated
                if (Log.isDebugEnabled()) Log.debug("Form authentication FAILED for " + StringUtil.printable(username));
                if (_formErrorPage == null) {
                    if (response != null)
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                } else {
                    response.setContentLength(0);
                    response.sendRedirect(response.encodeRedirectURL
                            (URIUtil.addPaths(request.getContextPath(),
                                    _formErrorPage)));
                }
                //TODO is this correct response if isMandatory false??? Can that occur?
                return new SimpleAuthResult(ServerAuthStatus.SEND_FAILURE);
            }
            // Check if the session is already authenticated.

            // Don't authenticate authform or errorpage
            if (!messageInfo.isAuthMandatory())
                //TODO verify this is correct action
                return new SimpleAuthResult(ServerAuthStatus.SUCCESS);

            // redirect to login page
            if (request.getQueryString() != null)
                uri += "?" + request.getQueryString();
            session.setAttribute(__J_URI,
                    request.getScheme() +
                            "://" + request.getServerName() +
                            ":" + request.getServerPort() +
                            URIUtil.addPaths(request.getContextPath(), uri));
            response.setContentLength(0);
            response.sendRedirect(response.encodeRedirectURL(URIUtil.addPaths(request.getContextPath(),
                    _formLoginPage)));
            return new SimpleAuthResult(ServerAuthStatus.SEND_CONTINUE);
        }
        catch (IOException e) {
            throw new ServerAuthException(e);
        }

    }

    public boolean isLoginOrErrorPage(String pathInContext) {
        return pathInContext != null &&
                (pathInContext.equals(_formErrorPath) || pathInContext.equals(_formLoginPath));
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException {
        return ServerAuthStatus.SUCCESS;
    }
}

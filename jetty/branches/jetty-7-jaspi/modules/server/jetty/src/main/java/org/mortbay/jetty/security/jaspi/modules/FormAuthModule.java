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
import java.io.Serializable;
import java.security.Principal;
import java.util.Arrays;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.MessagePolicy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.SSORealm;
import org.mortbay.log.Log;
import org.mortbay.util.StringUtil;
import org.mortbay.util.URIUtil;

/**
 * @version $Rev$ $Date$
 */
public class FormAuthModule extends BaseAuthModule
{
    /* ------------------------------------------------------------ */
    public final static String __J_URI = "org.mortbay.jetty.URI";
    public final static String __J_AUTHENTICATED = "org.mortbay.jetty.Auth";
    public final static String __J_SECURITY_CHECK = "/j_security_check";
    public final static String __J_USERNAME = "j_username";
    public final static String __J_PASSWORD = "j_password";

    //    private String realmName;
    private static final String LOGIN_PAGE_KEY = "org.mortbay.jetty.security.jaspi.modules.LoginPage";
    private static final String ERROR_PAGE_KEY = "org.mortbay.jetty.security.jaspi.modules.ErrorPage";

    private String _formErrorPage;
    private String _formErrorPath;
    private String _formLoginPage;
    private String _formLoginPath;

    public FormAuthModule()
    {
    }

    public FormAuthModule(CallbackHandler callbackHandler, LoginService loginService, String loginPage, String errorPage)
    {
        super(callbackHandler, loginService);
        setLoginPage(loginPage);
        setErrorPage(errorPage);
    }

    @Override
    public void initialize(MessagePolicy requestPolicy, MessagePolicy responsePolicy, CallbackHandler handler, Map options) throws AuthException
    {
        super.initialize(requestPolicy, responsePolicy, handler, options);
        setLoginPage((String) options.get(LOGIN_PAGE_KEY));
        setErrorPage((String) options.get(ERROR_PAGE_KEY));
    }

    private void setLoginPage(String path)
    {
        if (!path.startsWith("/"))
        {
            Log.warn("form-login-page must start with /");
            path = "/" + path;
        }
        _formLoginPage = path;
        _formLoginPath = path;
        if (_formLoginPath.indexOf('?') > 0)
            _formLoginPath = _formLoginPath.substring(0, _formLoginPath.indexOf('?'));
    }

    /* ------------------------------------------------------------ */
    private void setErrorPage(String path)
    {
        if (path == null || path.trim().length() == 0)
        {
            _formErrorPath = null;
            _formErrorPage = null;
        }
        else
        {
            if (!path.startsWith("/"))
            {
                Log.warn("form-error-page must start with /");
                path = "/" + path;
            }
            _formErrorPage = path;
            _formErrorPath = path;

            if (_formErrorPath != null && _formErrorPath.indexOf('?') > 0)
                _formErrorPath = _formErrorPath.substring(0, _formErrorPath.indexOf('?'));
        }
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, Subject serviceSubject) throws AuthException
    {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        HttpSession session = request.getSession(isMandatory(messageInfo));
        String uri = request.getPathInfo();
        //not mandatory and not authenticated
        if (session == null || isLoginOrErrorPage(uri))
            return AuthStatus.SUCCESS;

        try
        {
            // Handle a request for authentication.
            // TODO perhaps j_securitycheck can be uri suffix?
            if (uri.endsWith(__J_SECURITY_CHECK))
            {

                final String username = request.getParameter(__J_USERNAME);
                final char[] password = request.getParameter(__J_PASSWORD).toCharArray();
                CallbackHandler loginCallbackHandler = new UserPasswordCallbackHandler(username, password);
                LoginResult loginResult = loginService.login(clientSubject, loginCallbackHandler);
                //TODO what should happen if !isMandatory but credentials exist and are wrong?
                if (loginResult.isSuccess())
                {
                    callbackHandler.handle(new Callback[]{loginResult.getCallerPrincipalCallback(), loginResult.getGroupPrincipalCallback()});
                    messageInfo.getMap().put(JettyMessageInfo.AUTH_METHOD_KEY, Constraint.__FORM_AUTH);

                    String nuri = (String) session.getAttribute(__J_URI);
                    if (nuri == null || nuri.length() == 0)
                    {
                        nuri = request.getContextPath();
                        if (nuri.length() == 0)
                            nuri = URIUtil.SLASH;
                    }
                    session.removeAttribute(__J_URI); // Remove popped return URI.
                    FormCredential form_cred = new FormCredential(username, password, loginResult.getCallerPrincipalCallback().getPrincipal());

                    session.setAttribute(__J_AUTHENTICATED, form_cred);
                    // Sign-on to SSO mechanism
//                    if (realm instanceof SSORealm)
//                        ((SSORealm)realm).setSingleSignOn(request,response,form_cred._userPrincipal,new Password(form_cred._jPassword));

                    // Redirect to original request
                        response.setContentLength(0);
                        response.sendRedirect(response.encodeRedirectURL(nuri));

                    return AuthStatus.SEND_CONTINUE;
                }
                //not authenticated
                if (Log.isDebugEnabled()) Log.debug("Form authentication FAILED for " + StringUtil.printable(username));
                if (_formErrorPage == null)
                {
                    if (response != null)
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
                else
                {
                    if (response != null)
                        response.setContentLength(0);
                    response.sendRedirect(response.encodeRedirectURL
                            (URIUtil.addPaths(request.getContextPath(),
                                    _formErrorPage)));
                }
                //TODO is this correct response if isMandatory false??? Can that occur?
                return AuthStatus.SEND_FAILURE;
            }
            // Check if the session is already authenticated.
            FormCredential form_cred = (FormCredential) session.getAttribute(__J_AUTHENTICATED);

            if (form_cred != null)
            {
                CallbackHandler loginCallbackHandler = new UserPasswordCallbackHandler(form_cred._jUserName, form_cred._jPassword);
                LoginResult loginResult = loginService.login(clientSubject, loginCallbackHandler);
                //TODO what should happen if !isMandatory but credentials exist and are wrong?
                if (loginResult.isSuccess())
                {
                    callbackHandler.handle(new Callback[]{loginResult.getCallerPrincipalCallback(), loginResult.getGroupPrincipalCallback()});
                    messageInfo.getMap().put(JettyMessageInfo.AUTH_METHOD_KEY, Constraint.__FORM_AUTH);

                    form_cred = new FormCredential(form_cred._jUserName, form_cred._jPassword, loginResult.getCallerPrincipalCallback().getPrincipal());

                    session.setAttribute(__J_AUTHENTICATED, form_cred);
                    messageInfo.getMap().put(JettyMessageInfo.AUTH_METHOD_KEY, Constraint.__FORM_AUTH);
                    return AuthStatus.SUCCESS;
                }
//                // We have a form credential. Has it been distributed?
//                if (form_cred._userPrincipal==null)
//                {
//                    // This form_cred appears to have been distributed.  Need to reauth
//                    form_cred.authenticate(realm, request);
//
//                    // Sign-on to SSO mechanism
//                    if (form_cred._userPrincipal!=null && realm instanceof SSORealm)
//                        ((SSORealm)realm).setSingleSignOn(request,response,form_cred._userPrincipal,new Password(form_cred._jPassword));
//
//                }
//                else if (!realm.reauthenticate(form_cred._userPrincipal))
//                    // Else check that it is still authenticated.
//                    form_cred._userPrincipal=null;
//
//                // If this credential is still authenticated
//                if (form_cred._userPrincipal!=null)
//                {
//                    if(Log.isDebugEnabled())Log.debug("FORM Authenticated for "+form_cred._userPrincipal.getName());
//                    request.setAuthType(Constraint.__FORM_AUTH);
//                    //jaspi
////                request.setUserPrincipal(form_cred._userPrincipal);
//                    return form_cred._userPrincipal;
//                }
//                else
//                    session.setAttribute(__J_AUTHENTICATED,null);
//            }
//            else if (realm instanceof SSORealm)
//            {
//                // Try a single sign on.
//                Credential cred = ((SSORealm)realm).getSingleSignOn(request,response);
//
//                if (request.getUserPrincipal()!=null)
//                {
//                    form_cred=new FormCredential();
//                    form_cred._userPrincipal=request.getUserPrincipal();
//                    form_cred._jUserName=form_cred._userPrincipal.getName();
//                    if (cred!=null)
//                        form_cred._jPassword=cred.toString();
//                    if(Log.isDebugEnabled())Log.debug("SSO for "+form_cred._userPrincipal);
//
//                    request.setAuthType(Constraint.__FORM_AUTH);
//                    session.setAttribute(__J_AUTHENTICATED,form_cred);
//                    return form_cred._userPrincipal;
//                }
            }

            // Don't authenticate authform or errorpage
            if (!isMandatory(messageInfo) || isLoginOrErrorPage(uri))
                //TODO verify this is correct action
                return AuthStatus.SUCCESS;

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
            return AuthStatus.SEND_CONTINUE;
        }
        catch (IOException e)
        {
            throw new AuthException(e.getMessage());
        }
        catch (UnsupportedCallbackException e)
        {
            throw new AuthException(e.getMessage());
        }

    }


    public boolean isLoginOrErrorPage(String pathInContext)
    {
        return pathInContext != null &&
                (pathInContext.equals(_formErrorPath) || pathInContext.equals(_formLoginPath));
    }

    /* ------------------------------------------------------------ */
    /**
     * FORM Authentication credential holder.
     */
    private static class FormCredential implements Serializable, HttpSessionBindingListener
    {
        String _jUserName;
        char[] _jPassword;
        transient Principal _userPrincipal;

        private FormCredential(String _jUserName, char[] _jPassword, Principal _userPrincipal)
        {
            this._jUserName = _jUserName;
            this._jPassword = _jPassword;
            this._userPrincipal = _userPrincipal;
        }


        public void valueBound(HttpSessionBindingEvent event)
        {
        }

        public void valueUnbound(HttpSessionBindingEvent event)
        {
            if (Log.isDebugEnabled()) Log.debug("Logout " + _jUserName);

            //TODO jaspi call cleanSubject()
//            if (_realm instanceof SSORealm)
//                ((SSORealm) _realm).clearSingleSignOn(_jUserName);
//
//            if (_realm != null && _userPrincipal != null)
//                _realm.logout(_userPrincipal);
        }

        public int hashCode()
        {
            return _jUserName.hashCode() + _jPassword.hashCode();
        }

        public boolean equals(Object o)
        {
            if (!(o instanceof FormCredential))
                return false;
            FormCredential fc = (FormCredential) o;
            return
                    _jUserName.equals(fc._jUserName) &&
                            Arrays.equals(_jPassword, fc._jPassword);
        }

        public String toString()
        {
            return "Cred[" + _jUserName + "]";
        }

    }

    private static class UserPasswordCallbackHandler implements CallbackHandler
    {
        private final String username;
        private final char[] password;

        public UserPasswordCallbackHandler(String username, char[] password)
        {
            this.username = username;
            this.password = password;
        }

        public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
        {
            for (Callback callback : callbacks)
            {
                if (callback instanceof NameCallback)
                {
                    ((NameCallback) callback).setName(username);
                }
                else if (callback instanceof PasswordCallback)
                {
                    ((PasswordCallback) callback).setPassword(password);
                }
            }
        }
    }
}
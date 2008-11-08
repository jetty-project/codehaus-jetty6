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
import java.security.Principal;

import javax.security.auth.Subject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.LoginCallback;
import org.mortbay.jetty.LoginService;
import org.mortbay.jetty.ServerAuthException;
import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.ServerAuthStatus;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.SimpleAuthResult;
import org.mortbay.util.B64Code;

/**
 * @version $Rev:$ $Date:$
 */
public class ClientCertServerAuthentication implements ServerAuthentication {

    private final LoginService loginService;

    public ClientCertServerAuthentication(LoginService loginService) {
        this.loginService = loginService;
    }

    /**
     * TODO what should happen if an insecure page is accessed without a client cert?  Current code
     * requires a client cert always but allows access to insecure pages if it is not recognized.
     * @param messageInfo
     * @return
     * @throws ServerAuthException
     */
    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException {
        HttpServletRequest request = messageInfo.getRequestMessage();
        HttpServletResponse response = messageInfo.getResponseMessage();
        java.security.cert.X509Certificate[] certs =
            (java.security.cert.X509Certificate[])
            request.getAttribute("javax.servlet.request.X509Certificate");

        try
        {
            // Need certificates.
            if (certs == null || certs.length == 0 || certs[0] == null)
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                        "A client certificate is required for accessing this web application but the server's listener is not configured for mutual authentication (or the client did not provide a certificate).");
                return new SimpleAuthResult(ServerAuthStatus.SEND_FAILURE, null, null, (String[]) null, null);
            }
            Principal principal = certs[0].getSubjectDN();
            if (principal==null)
                principal=certs[0].getIssuerDN();
            final String username=principal==null?"clientcert":principal.getName();
            //TODO no idea if this is correct
            final char[] password = B64Code.encode(certs[0].getSignature());

            //TODO is cert_auth correct?
            LoginCallback loginCallback = new LoginCallback(new Subject(), username, password);
            loginService.login(loginCallback);
            if (loginCallback.isSuccess()) {
                return new SimpleAuthResult(ServerAuthStatus.SUCCESS, loginCallback.getSubject(), loginCallback.getUserPrincipal(), loginCallback.getGroups(), Constraint.__CERT_AUTH2);
            }

            if (!messageInfo.isAuthMandatory())
            {
                return new SimpleAuthResult(ServerAuthStatus.SEND_SUCCESS, null, null, (String[])null, null);
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN,"The provided client certificate does not correspond to a trusted user.");
            return new SimpleAuthResult(ServerAuthStatus.SEND_FAILURE, null, null, (String[])null, null);
        }
        catch (IOException e)
        {
            throw new ServerAuthException(e.getMessage());
        }
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException {
        return ServerAuthStatus.SUCCESS;
    }
}

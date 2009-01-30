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
import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.security.B64Code;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.Password;

/**
 * @deprecated use *ServerAuthentication
 * @version $Rev$ $Date$
 */
public class ClientCertAuthModule extends BaseAuthModule
{

    public ClientCertAuthModule()
    {
    }

    public ClientCertAuthModule(CallbackHandler callbackHandler)
    {
        super(callbackHandler);
    }

    @Override
    public AuthStatus validateRequest(MessageInfo messageInfo, Subject clientSubject, 
                                      Subject serviceSubject) 
    throws AuthException
    {
        HttpServletRequest request = (HttpServletRequest) messageInfo.getRequestMessage();
        HttpServletResponse response = (HttpServletResponse) messageInfo.getResponseMessage();
        java.security.cert.X509Certificate[] certs = (java.security.cert.X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        try
        {
            // Need certificates.
            if (certs == null || certs.length == 0 || certs[0] == null)
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                   "A client certificate is required for accessing this web application but the server's listener is not configured for mutual authentication (or the client did not provide a certificate).");
                return AuthStatus.SEND_FAILURE;
            }
            Principal principal = certs[0].getSubjectDN();
            if (principal == null) principal = certs[0].getIssuerDN();
            final String username = principal == null ? "clientcert" : principal.getName();
            // TODO no idea if this is correct
            final String password = new String(B64Code.encode(certs[0].getSignature()));

            // TODO is cert_auth correct?
            if (login(clientSubject, username, new Password(password), Constraint.__CERT_AUTH, messageInfo)) { return AuthStatus.SUCCESS; }

            if (!isMandatory(messageInfo)) { return AuthStatus.SUCCESS; }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The provided client certificate does not correspond to a trusted user.");
            return AuthStatus.SEND_FAILURE;
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
}
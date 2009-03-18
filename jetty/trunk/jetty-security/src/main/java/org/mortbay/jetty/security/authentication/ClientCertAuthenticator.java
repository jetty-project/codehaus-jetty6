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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.http.security.B64Code;
import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.security.LoginService;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.Authentication;
import org.mortbay.jetty.security.DefaultAuthentication;
import org.mortbay.jetty.server.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public class ClientCertAuthenticator extends LoginAuthenticator
{
    public ClientCertAuthenticator()
    {
        super();
    }

    public String getAuthMethod()
    {
        return Constraint.__CERT_AUTH;
    }
    
    /**
     * TODO what should happen if an insecure page is accessed without a client
     * cert? Current code requires a client cert always but allows access to
     * insecure pages if it is not recognized.
     * 
     * @return
     * @throws ServerAuthException
     */
    public Authentication validateRequest(ServletRequest req, ServletResponse res, boolean mandatory) throws ServerAuthException
    {
        HttpServletRequest request = (HttpServletRequest)req;
        HttpServletResponse response = (HttpServletResponse)res;
        java.security.cert.X509Certificate[] certs = (java.security.cert.X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");

        try
        {
            // Need certificates.
            if (certs == null || certs.length == 0 || certs[0] == null)
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN,
                                   "A client certificate is required for accessing this web application but the server's listener is not configured for mutual authentication (or the client did not provide a certificate).");
                return DefaultAuthentication.SEND_FAILURE_RESULTS;
            }
            
            Principal principal = certs[0].getSubjectDN();
            if (principal == null) principal = certs[0].getIssuerDN();
            final String username = principal == null ? "clientcert" : principal.getName();
            
            // TODO no idea if this is correct
            final char[] credential = B64Code.encode(certs[0].getSignature());

            UserIdentity user = _loginService.login(username,credential);
            if (user!=null)
                return new DefaultAuthentication(Authentication.Status.SUCCESS,Constraint.__CERT_AUTH2,user);

            if (!mandatory) 
            { 
                return DefaultAuthentication.SUCCESS_UNAUTH_RESULTS; 
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The provided client certificate does not correspond to a trusted user.");
            return DefaultAuthentication.SEND_FAILURE_RESULTS;
        }
        catch (IOException e)
        {
            throw new ServerAuthException(e.getMessage());
        }
    }

    public Authentication.Status secureResponse(ServletRequest req, ServletResponse res, boolean mandatory, Authentication validatedUser) throws ServerAuthException
    {
        return Authentication.Status.SUCCESS;
    }
}

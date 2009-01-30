// ========================================================================
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.security;

import org.mortbay.jetty.security.authentication.BasicServerAuthentication;
import org.mortbay.jetty.security.authentication.ClientCertServerAuthentication;
import org.mortbay.jetty.security.authentication.DigestServerAuthentication;
import org.mortbay.jetty.security.authentication.FormServerAuthentication;
import org.mortbay.jetty.security.authentication.LazyServerAuthentication;
import org.mortbay.jetty.security.authentication.SessionCachingServerAuthentication;



public class DefaultAuthenticationManager extends AbstractAuthenticationManager
{
    protected ServerAuthentication _serverAuthentication;
  
    
    public DefaultAuthenticationManager ()
    {        
    }
    
    protected void doStart() throws Exception
    {
        super.doStart();
        
        if (getSecurityHandler() == null)
            throw new IllegalStateException ("No SecurityHandler for AuthenticationManager");
        
       
        if (getAuthMethod() != null && !"".equals(getAuthMethod()))
        {
            LoginService loginService = (LoginService)getSecurityHandler().getUserRealm();
            if (Constraint.__FORM_AUTH.equals(getAuthMethod()))
            {
                _serverAuthentication = new SessionCachingServerAuthentication(new FormServerAuthentication(getLoginPage(), getErrorPage(), loginService));
                /* if (useSSO)
                {
                    CrossContextPsuedoSession<ServerAuthResult> xcps = null;
                    serverAuthentication = new XCPSCachingServerAuthentication(serverAuthentication, xcps);
                }
                 */
            }
            else if (Constraint.__BASIC_AUTH.equals(getAuthMethod()))
            {
                _serverAuthentication = new LazyServerAuthentication(new BasicServerAuthentication(loginService, loginService.getName()));
            }
            else if (Constraint.__DIGEST_AUTH.equals(getAuthMethod()))
            {
                _serverAuthentication = new LazyServerAuthentication(new DigestServerAuthentication(loginService, loginService.getName()));
            }
            else if (Constraint.__CERT_AUTH.equals(getAuthMethod()) || Constraint.__CERT_AUTH2.equals(getAuthMethod()))
            {
                // TODO figure out how to configure max handshake?
                // TODO lazy?
                _serverAuthentication = new LazyServerAuthentication(new ClientCertServerAuthentication(loginService));
            }
            else
                throw new IllegalStateException ("Unrecognized auth method: "+getAuthMethod());
        }
    }

    protected void doStop() throws Exception
    {
        super.doStop();
    }

    public ServerAuthentication getServerAuthentication() {
        return _serverAuthentication;
    }
}

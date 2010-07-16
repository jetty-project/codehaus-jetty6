package org.mortbay.jetty.security;
//========================================================================
//Copyright 1998-2010 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================


import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.security.SpnegoUserRealm.SpnegoUser;
import org.mortbay.log.Log;

public class SpnegoAuthenticator implements Authenticator
{

    public Principal authenticate(UserRealm realm, String pathInContext, Request request, Response response) throws IOException
    {        
        Principal user = null;
        
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (header == null)
        {
            sendChallenge(realm,request,response);
            return user;
        } 
        else if (header != null && header.startsWith(HttpHeaders.NEGOTIATE)) 
        {        	
            Principal principal = request.getUserPrincipal();
            
            String username = header.substring(10);
            
            principal = realm.authenticate(username, null, request);
            
            if (principal != null)
            {
                Log.debug("SpengoAuthenticator: obtained principal: " + principal.getName());

                request.setAuthType(Constraint.__SPNEGO_AUTH);
                request.setUserPrincipal(principal);
                
                response.addHeader(HttpHeaders.WWW_AUTHENTICATE, HttpHeaders.NEGOTIATE + " " + ((SpnegoUser)principal).getToken());
                
                return user;
            }
            else
            {
                Log.debug("SpengoAuthenticator: failed to negotiate principal");

                return user;
            }
        }
        else
        {
            Log.debug("SpengoAuthenticator: unknown authorization header: " + header);
            return null;
        }
       
    }

    public void sendChallenge(UserRealm realm, Request request, Response response) throws IOException
    {
        Log.debug("SpengoAuthenticator: sending challenge");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, HttpHeaders.NEGOTIATE);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public String getAuthMethod()
    {
        return Constraint.__SPNEGO_AUTH;
    }

}

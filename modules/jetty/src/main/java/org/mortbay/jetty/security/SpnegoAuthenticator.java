package org.mortbay.jetty.security;

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

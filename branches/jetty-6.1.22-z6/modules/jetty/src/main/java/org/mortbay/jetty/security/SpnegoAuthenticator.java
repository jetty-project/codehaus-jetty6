package org.mortbay.jetty.security;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpHeaders;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

public class SpnegoAuthenticator implements Authenticator
{

    public Principal authenticate(UserRealm realm, String pathInContext, Request request, Response response) throws IOException
    {
        System.out.println("Trying to authenticate");
        
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
            String password = null;
            
            // doublecheck this password object 'credentials'
            principal = realm.authenticate(username, password, request);
            
            if (principal != null)
            {
                request.setAuthType(Constraint.__SPNEGO_AUTH);
                request.setUserPrincipal(principal);
                return user;
            }
            else
            {
                return user;
                //request.getMimeHeaders().removeHeader("authorization");
            }
        }
        else
        {
            return null;
        }
       
    }

    public void sendChallenge(UserRealm realm, Request request, Response response) throws IOException
    {
        System.out.println("sending challenge");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, HttpHeaders.NEGOTIATE);
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    public String getAuthMethod()
    {
        return Constraint.__SPNEGO_AUTH;
    }

}

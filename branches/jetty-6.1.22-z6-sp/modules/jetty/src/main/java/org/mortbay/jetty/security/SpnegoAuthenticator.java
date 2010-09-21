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
import org.mortbay.util.StringUtil;
import org.mortbay.util.URIUtil;

/**
 * SPNEGO Authentication Authenticator
 * 
 */
public class SpnegoAuthenticator implements Authenticator
{
    private String _errorPage;
    private String _errorPath;
    
    /* ------------------------------------------------------------ */
    public void setErrorPage(String path)
    {
        if (path==null || path.trim().length()==0)
        {
            _errorPath=null;
            _errorPage=null;
        }
        else
        {
            if (!path.startsWith("/"))
            {
                Log.warn("error-page must start with /");
                path="/"+path;
            }
            _errorPage=path;
            _errorPath=path;

            if (_errorPath!=null && _errorPath.indexOf('?')>0)
                _errorPath=_errorPath.substring(0,_errorPath.indexOf('?'));
        }
    }    

    /* ------------------------------------------------------------ */
    public String getErrorPage()
    {
        return _errorPage;
    }
    
    public Principal authenticate(UserRealm realm, String pathInContext, Request request, Response response) throws IOException
    {        
        Principal user = null;
        
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        
        if (header == null)
        {
            sendChallenge(realm,request,response);
            return null;
        } 
        else if (header != null && header.startsWith(HttpHeaders.NEGOTIATE)) 
        {        	
            
            String username = header.substring(10);
            
            user = realm.authenticate(username, null, request);
            
            if (user != null)
            {
                Log.debug("SpengoAuthenticator: obtained principal: " + user.getName());

                request.setAuthType(Constraint.__SPNEGO_AUTH);
                request.setUserPrincipal(user);
                
                response.addHeader(HttpHeaders.WWW_AUTHENTICATE, HttpHeaders.NEGOTIATE + " " + ((SpnegoUser)user).getToken());
                
                return user;
            }
            else
            {
                if(Log.isDebugEnabled())
                {
                    Log.debug("SpengoAuthenticator: authentication failed");
                }
                
                if (_errorPage==null)
                {
                    if (response != null)
                    {
                        response.sendError(HttpServletResponse.SC_FORBIDDEN);
                    }
                }
                else
                {
                    if (response != null)
                    {
                        response.setContentLength(0);
                        
                        response.sendRedirect(response.encodeRedirectURL
                                          (URIUtil.addPaths(request.getContextPath(),
                                                        _errorPage)));
                        
                        return SecurityHandler.__NOBODY;
                    }
                }
                
                return null;
            }
        }
        else
        {
            if(Log.isDebugEnabled())
            {
                Log.debug("SpengoAuthenticator: authentication failed, unknown header (browser is likely misconfigured for SPNEGO)");
            }
            
            if (_errorPage==null)
            {
                if (response != null)
                {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            else
            {
                if (response != null)
                {
                    response.setContentLength(0);
                    
                    response.sendRedirect(response.encodeRedirectURL
                                      (URIUtil.addPaths(request.getContextPath(),
                                                    _errorPage)));
                    
                    return SecurityHandler.__NOBODY;
                }
            }
            
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            
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

// ========================================================================
// $Id: SecurityHandler.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
// Copyright 199-2004 Mort Bay Consulting Pty. Ltd.
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

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.handler.WrappedHandler;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;


/* ------------------------------------------------------------ */
/** Handler to enforce SecurityConstraints.
 *
 * @version $Id: SecurityHandler.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
 * @author Greg Wilkins (gregw)
 */
public class SecurityHandler extends WrappedHandler
{   

    /* ------------------------------------------------------------ */
    private String _authMethod=Constraint.__BASIC_AUTH;
    private UserRealm _userRealm;
    private ConstraintMapping[] _constraintMappings;
    private PathMap _constraintMap=new PathMap();
    private Authenticator _authenticator;
    private NotChecked _notChecked=new NotChecked();
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the authenticator.
     */
    public Authenticator getAuthenticator()
    {
        return _authenticator;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param authenticator The authenticator to set.
     */
    public void setAuthenticator(Authenticator authenticator)
    {
        _authenticator = authenticator;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the userRealm.
     */
    public UserRealm getUserRealm()
    {
        return _userRealm;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param userRealm The userRealm to set.
     */
    public void setUserRealm(UserRealm userRealm)
    {
        _userRealm = userRealm;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the contraintMappings.
     */
    public ConstraintMapping[] getConstraintMappings()
    {
        return _constraintMappings;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param contraintMappings The contraintMappings to set.
     */
    public void setConstraintMappings(ConstraintMapping[] constraintMappings)
    {
        _constraintMappings=constraintMappings;
        if (_constraintMappings!=null)
        {
            this._constraintMappings = constraintMappings;
            _constraintMap.clear();
            
            for (int i=0;i<_constraintMappings.length;i++)
            {
                Object mappings = _constraintMap.get(_constraintMappings[i].getPathSpec());
                mappings=LazyList.add(mappings, _constraintMappings[i]);
                _constraintMap.put(_constraintMappings[i].getPathSpec(),mappings);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public String getAuthMethod()
    {
        return _authMethod;
    }
    
    /* ------------------------------------------------------------ */
    public void setAuthMethod(String method)
    {
        if (isStarted() && _authMethod!=null && !_authMethod.equals(method))
            throw new IllegalStateException("Handler started");
        _authMethod = method;
    }

    /* ------------------------------------------------------------ */
    public void doStart()
        throws Exception
    {
        if (_authenticator==null)
        {
            // Find out the Authenticator.
            if (Constraint.__BASIC_AUTH.equalsIgnoreCase(_authMethod))
                _authenticator=new BasicAuthenticator();
            else if (Constraint.__DIGEST_AUTH.equalsIgnoreCase(_authMethod))
                _authenticator=new DigestAuthenticator();
            else if (Constraint.__CERT_AUTH.equalsIgnoreCase(_authMethod))
               _authenticator=new ClientCertAuthenticator();
            else if (Constraint.__FORM_AUTH.equalsIgnoreCase(_authMethod))
                _authenticator=new FormAuthenticator();
            else
                Log.warn("Unknown Authentication method:"+_authMethod);
        }
        
        super.doStart();
    }
    

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException 
    {
        if (dispatch==REQUEST && !checkSecurityConstraints(target,(Request)request,(Response)response))
            return true;
        if (getHandler()!=null)
            return getHandler().handle(target, request, response, dispatch);
        return false;
    }
    

    /* ------------------------------------------------------------ */
    private boolean checkSecurityConstraints(
        String pathInContext,
        Request request,
        Response response)
        throws IOException
    {
        List mapping_entries= _constraintMap.getMatches(pathInContext);
        String pattern=null;
        if (mapping_entries != null && mapping_entries.size() > 0)
        {
            Object constraints= null;

            // for each path match
            // Add only constraints that have the correct method
            // break if the matching pattern changes.  This allows only
            // constraints with matching pattern and method to be combined.
            loop:
            for (int m= 0; m < mapping_entries.size(); m++)
            {
                Map.Entry entry= (Map.Entry)mapping_entries.get(m);
                Object mappings= entry.getValue();
                String path_spec=(String)entry.getKey();
                
                for (int c=0;c<LazyList.size(mappings);c++)
                {
                	ConstraintMapping mapping=(ConstraintMapping)LazyList.get(mappings,c);
                	if (mapping.getMethod()!=null && !mapping.getMethod().equalsIgnoreCase(request.getMethod()))
                	    continue;
						
					if (pattern!=null && !pattern.equals(path_spec))
						break loop;
					
					pattern=path_spec;	
	                constraints= LazyList.add(constraints, mapping.getConstraint());
                }
            }
            
            return check(constraints,
                _authenticator,
                _userRealm,
                pathInContext,
                request,
                response);
        }
        
        request.setUserPrincipal(_notChecked);
        return true;
    }
    

    /* ------------------------------------------------------------ */
    /** Check security contraints
     * @param constraints 
     * @param authenticator 
     * @param realm 
     * @param pathInContext 
     * @param request 
     * @param response 
     * @return false if the request has failed a security constraint or the authenticator has already sent a response.
     * @exception HttpException 
     * @exception IOException 
     */
    private boolean check(
        Object constraints,
        Authenticator authenticator,
        UserRealm realm,
        String pathInContext,
        Request request,
        Response response)
        throws IOException
    {
        // Combine data and auth constraints
        int dataConstraint= Constraint.DC_NONE;
        Object roles= null;
        boolean unauthenticated= false;
        boolean forbidden= false;

        for (int c= 0; c < LazyList.size(constraints); c++)
        {
            Constraint sc= (Constraint)LazyList.get(constraints,c);

            // Combine data constraints.
            if (dataConstraint > Constraint.DC_UNSET && sc.hasDataConstraint())
            {
                if (sc.getDataConstraint() > dataConstraint)
                    dataConstraint= sc.getDataConstraint();
            }
            else
                dataConstraint= Constraint.DC_UNSET; // ignore all other data constraints

            // Combine auth constraints.
            if (!unauthenticated && !forbidden)
            {
                if (sc.getAuthenticate())
                {
                    if (sc.isAnyRole())
                    {
                        roles= Constraint.ANY_ROLE;
                    }
                    else
                    {
                        List scr= sc.getRoles();
                        if (scr == null || scr.size() == 0)
                        {
                            forbidden= true;
                            break;
                        }
                        else
                        {
                            if (roles != Constraint.ANY_ROLE)
                            {
                                roles= LazyList.addCollection(roles, scr);
                            }
                        }
                    }
                }
                else
                    unauthenticated= true;
            }
        }

        // Does this forbid everything?
        if (forbidden && 
            (!(authenticator instanceof FormAuthenticator) || 
            !((FormAuthenticator)authenticator).isLoginOrErrorPage(pathInContext)))
        {

            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }

        // Handle data constraint
        if (dataConstraint > Constraint.DC_NONE)
        {
            HttpConnection connection = HttpConnection.getCurrentConnection();
            Connector connector = connection.getConnector();
            
            switch (dataConstraint)
            {
                case Constraint.DC_INTEGRAL :
                    if (connector.isIntegral(request))
                        break;
                    if (connector.getConfidentialPort() > 0)
                    {
                        String url=
                            connector.getIntegralScheme()
                                + "://"
                                + request.getServerName()
                                + ":"
                                + connector.getIntegralPort()
                                + request.getRequestURI();
                        if (request.getQueryString() != null)
                            url += "?" + request.getQueryString();
                        response.setContentLength(0);
                        response.sendRedirect(url);
                    }
                    else
                        response.sendError(Response.SC_FORBIDDEN,null);
                    return false;
                case Constraint.DC_CONFIDENTIAL :
                    if (connector.isConfidential(request))
                        break;

                    if (connector.getConfidentialPort() > 0)
                    {
                        String url=
                            connector.getConfidentialScheme()
                                + "://"
                                + request.getServerName()
                                + ":"
                                + connector.getConfidentialPort()
                                + request.getRequestURI();
                        if (request.getQueryString() != null)
                            url += "?" + request.getQueryString();

                        response.setContentLength(0);
                        response.sendRedirect(url);
                    }
                    else
                        response.sendError(Response.SC_FORBIDDEN,null);
                    return false;

                default :
                    response.sendError(Response.SC_FORBIDDEN,null);
                    return false;
            }
        }

        // Does it fail a role check?
        if (!unauthenticated && roles != null)
        {
            if (realm == null)
            {
                response.sendError(Response.SC_INTERNAL_SERVER_ERROR,"No realm");
                return false;
            }

            Principal user= null;

            // Handle pre-authenticated request
            if (request.getAuthType() != null && request.getRemoteUser() != null)
            {
                // TODO - is this still needed???
                user= request.getUserPrincipal();
                if (user == null)
                    user= realm.authenticate(request.getRemoteUser(), null, request);
                if (user == null && authenticator != null)
                    user= authenticator.authenticate(realm, pathInContext, request, response);
            }
            else if (authenticator != null)
            {
                // User authenticator.
                user= authenticator.authenticate(realm, pathInContext, request, response);
            }
            else
            {
                // don't know how authenticate
                Log.warn("Mis-configured Authenticator for " + request.getRequestURI());
                response.sendError(Response.SC_INTERNAL_SERVER_ERROR,"Configuration error");
            }

            // If we still did not get a user
            if (user == null)
                return false; // Auth challenge or redirection already sent
            else if (user == __NOBODY)
                return true; // The Nobody user indicates authentication in transit.

            if (roles != Constraint.ANY_ROLE)
            {
                boolean inRole= false;
                for (int r= LazyList.size(roles); r-- > 0;)
                {
                    if (realm.isUserInRole(user, (String)LazyList.get(roles, r)))
                    {
                        inRole= true;
                        break;
                    }
                }

                if (!inRole)
                {
                    Log.warn("AUTH FAILURE: role for " + user.getName());
                    if ("BASIC".equalsIgnoreCase(authenticator.getAuthMethod()))
                         ((BasicAuthenticator)authenticator).sendChallenge(realm, response);
                    else
                        response.sendError(Response.SC_FORBIDDEN,"User not in required role");
                    return false; // role failed.
                }
            }
        }
        else
        {
            request.setUserPrincipal(_notChecked);
        }

        return true;
    }

    public static Principal __NO_USER = new Principal()
    {
        public String getName()
        {
            return null;
        }
        public String toString()
        {
            return "No User";
        }
    };
    
    public class NotChecked implements Principal
    {
        public String getName()
        {
            return null;
        }
        public String toString()
        {
            return "NOT CHECKED";
        }
        public SecurityHandler getSecurityHandler()
        {
            return SecurityHandler.this;
        }
    };

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** Nobody user.
     * The Nobody UserPrincipal is used to indicate a partial state of
     * authentication. A request with a Nobody UserPrincipal will be allowed
     * past all authentication constraints - but will not be considered an
     * authenticated request.  It can be used by Authenticators such as
     * FormAuthenticator to allow access to logon and error pages within an
     * authenticated URI tree.
     */
    public static Principal __NOBODY = new Principal()
    {
        public String getName()
        {
            return "Nobody";
        }
        
        public String toString()
        {
            return getName();
        }
    };
}


// ========================================================================
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.RunAsToken;
import org.mortbay.jetty.UserIdentity;
import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.jetty.util.LazyList;

/* ------------------------------------------------------------ */
/**
 * Handler to enforce SecurityConstraints. Legacy implementation that is not
 * servlet 2.4 spec compliant.
 * 
 * @author Greg Wilkins (gregw)
 */
public class LegacyConstraintSecurityHandler extends AbstractSecurityHandler implements ConstraintAware
{
    private ConstraintMapping[] _constraintMappings;

    private Set<String> _roles;

    private PathMap _constraintMap = new PathMap();

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the contraintMappings.
     */
    public ConstraintMapping[] getConstraintMappings()
    {
        return _constraintMappings;
    }

    public Set<String> getRoles()
    {
        return _roles;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param constraintMappings The contraintMappings to set.
     */
    public void setConstraintMappings(ConstraintMapping[] constraintMappings, Set<String> roles)
    {
        _constraintMappings = constraintMappings;
        _roles = roles;
        if (_constraintMappings != null)
        {
            this._constraintMappings = constraintMappings;
            _constraintMap.clear();

            for (ConstraintMapping _constraintMapping : _constraintMappings)
            {
                Object mappings = _constraintMap.get(_constraintMapping.getPathSpec());
                mappings = LazyList.add(mappings, _constraintMapping);
                _constraintMap.put(_constraintMapping.getPathSpec(), mappings);
            }
        }
    }

    private static class ConstraintInfo
    {
        private final int _dataConstraint;

        private final List<String> _allowedRoles;

        private ConstraintInfo(int dataConstraint, List<String> allowedRoles)
        {
            this._dataConstraint = dataConstraint;
            this._allowedRoles = allowedRoles;
        }

        public int getDataConstraint()
        {
            return _dataConstraint;
        }

        public List<String> getAllowedRoles()
        {
            return _allowedRoles;
        }
    }

    protected UserIdentity newUserIdentity(ServerAuthResult authResult)
    {
        return new ConstraintUserIdentity(authResult);
    }

    protected UserIdentity newSystemUserIdentity()
    {
        return new ConstraintUserIdentity();
    }

    public RunAsToken newRunAsToken(String runAsRole)
    {
        return new ConstraintRunAsToken(runAsRole);
    }

    protected Object prepareConstraintInfo(String pathInContext, Request request)
    {
        Object mapping_entries = _constraintMap.getLazyMatches(pathInContext);
        String pattern = null;
        Object constraints = null;

        int dataConstraint = Constraint.DC_NONE;
        Object roles = null;
        boolean unchecked = false;
        boolean forbidden = false;
        // for each path match
        // Add only constraints that have the correct method
        // break if the matching pattern changes. This allows only
        // constraints with matching pattern and method to be combined.
        if (mapping_entries != null)
        {
            loop: for (int m = 0; m < LazyList.size(mapping_entries); m++)
            {
                Map.Entry entry = (Map.Entry) LazyList.get(mapping_entries, m);
                Object mappings = entry.getValue();
                String path_spec = (String) entry.getKey();

                for (int c = 0; c < LazyList.size(mappings); c++)
                {
                    ConstraintMapping mapping = (ConstraintMapping) LazyList.get(mappings, c);
                    if (mapping.getMethod() != null && !mapping.getMethod().equalsIgnoreCase(request.getMethod())) continue;

                    if (pattern != null && !pattern.equals(path_spec)) break loop;

                    pattern = path_spec;
                    constraints = LazyList.add(constraints, mapping.getConstraint());
                    Constraint sc = mapping.getConstraint();
                    if (dataConstraint > Constraint.DC_UNSET && sc.hasDataConstraint())
                    {
                        if (sc.getDataConstraint() > dataConstraint) dataConstraint = sc.getDataConstraint();
                    }
                    else
                        dataConstraint = Constraint.DC_UNSET; // ignore all
                    // other data
                    // constraints

                    // Combine auth constraints.
                    if (!unchecked && !forbidden)
                    {
                        if (sc.getAuthenticate())
                        {
                            if (sc.isAnyRole())
                            {
                                roles = Constraint.ANY_ROLE;
                            }
                            else
                            {
                                String[] scr = sc.getRoles();
                                if (scr == null || scr.length == 0)
                                {
                                    forbidden = true;
                                    break;
                                }
                                else
                                {
                                    // TODO - this looks inefficient!
                                    if (roles != Constraint.ANY_ROLE)
                                    {
                                        for (int r = scr.length; r-- > 0;)
                                            roles = LazyList.add(roles, scr[r]);
                                    }
                                }
                            }
                        }
                        else
                            unchecked = true;
                    }
                }
            }
        }
        if (forbidden)
        {
            dataConstraint = Constraint.DC_FORBIDDEN;
            roles = null;
        }
        else if (unchecked)
        {
            roles = null;
        }
        else if (roles instanceof String)
        {
            roles = Collections.singletonList((String) roles);
        }
        return new ConstraintInfo(dataConstraint, (List<String>) roles);
    }

    protected boolean checkUserDataPermissions(String pathInContext, Request request, 
                                               Response response, Object constraintInfo) throws IOException
    {
        int dataConstraint = ((ConstraintInfo) constraintInfo).getDataConstraint();
        if (dataConstraint == Constraint.DC_FORBIDDEN) { return false; }
        if (dataConstraint == Constraint.DC_NONE || dataConstraint == Constraint.DC_UNSET) { return true; }
        HttpConnection connection = HttpConnection.getCurrentConnection();
        Connector connector = connection.getConnector();

        if (dataConstraint == Constraint.DC_INTEGRAL)
        {
            if (connector.isIntegral(request)) return true;
            if (connector.getConfidentialPort() > 0)
            {
                String url = connector.getIntegralScheme() + "://" + request.getServerName() + ":" + connector.getIntegralPort() + request.getRequestURI();
                if (request.getQueryString() != null) url += "?" + request.getQueryString();
                response.setContentLength(0);
                response.sendRedirect(url);
                request.setHandled(true);
            }
            return false;
        }
        else if (dataConstraint == Constraint.DC_CONFIDENTIAL)
        {
            if (connector.isConfidential(request)) return true;

            if (connector.getConfidentialPort() > 0)
            {
                String url = connector.getConfidentialScheme() + "://"
                             + request.getServerName()
                             + ":"
                             + connector.getConfidentialPort()
                             + request.getRequestURI();
                if (request.getQueryString() != null) url += "?" + request.getQueryString();

                response.setContentLength(0);
                response.sendRedirect(url);
                request.setHandled(true);
            }
            return false;
        }
        else
        {
            throw new IllegalArgumentException("Invalid dataConstraint value: " + dataConstraint);
        }

    }

    protected boolean isAuthMandatory(Request base_request, Response base_response, Object constraintInfo)
    {
        // unchecked?
        return ((ConstraintInfo) constraintInfo).getAllowedRoles() != null;
    }

    protected boolean checkWebResourcePermissions(String pathInContext, Request request, 
                                                  Response response, Object constraintInfo, 
                                                  UserIdentity userIdentity) throws IOException
    {
        List<String> roles = ((ConstraintInfo) constraintInfo).getAllowedRoles();
        // unchecked
        if (roles == null) return true;
        for (String role : roles)
        {
            if (role.equals(Constraint.ANY_ROLE)) { return true; }
            if (userIdentity.isUserInRole(role)) { return true; }
        }
        return false;
    }
}
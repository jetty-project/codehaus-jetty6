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
import java.util.Set;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.RunAsToken;
import org.mortbay.jetty.UserIdentity;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.util.LazyList;

/* ------------------------------------------------------------ */
/**
 * Handler to enforce SecurityConstraints. This implementation is servlet spec
 * 2.4 compliant.
 * 
 * @author Greg Wilkins (gregw)
 */
public class SimpleConstraintSecurityHandler extends AbstractSecurityHandler implements ConstraintAware
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
        Object mappings = _constraintMap.match(pathInContext);

        int dataConstraint = Constraint.DC_UNSET;
        Object roles = null;
        boolean unchecked = false;
        // for each constraint in the matched path
        // Add only constraints that have the correct method
        if (mappings != null)
        {
            for (int c = 0; c < LazyList.size(mappings); c++)
            {
                ConstraintMapping mapping = (ConstraintMapping) LazyList.get(mappings, c);
                if (mapping.getMethod() == null || mapping.getMethod().equalsIgnoreCase(request.getMethod()))
                {

                    Constraint sc = mapping.getConstraint();
                    // section 13.7.1, combination of security constraints.
                    // Union of connection types allowed... i.e most permissive
                    // wins.
                    if (sc.hasDataConstraint())
                    {
                        if (sc.getDataConstraint() < dataConstraint) dataConstraint = sc.getDataConstraint();
                    }
                    else
                    {
                        // no constraint implies all connection types allowed.
                        dataConstraint = Constraint.DC_NONE;
                    }
                    // Combine auth constraints.
                    if (sc.getAuthenticate())
                    {
                        String[] scr = sc.getRoles();
                        if (scr == null || scr.length == 0)
                        {
                            dataConstraint = Constraint.DC_FORBIDDEN;
                            roles = null;
                            // once forbidden, no need to look at any other
                            // constraints
                            break;
                        }
                        if (sc.isAnyRole())
                        {
                            // TODO consider using actual list of roles as per
                            // spec.
                            roles = Constraint.ANY_ROLE;
                            continue;
                        }
                        // TODO - this looks inefficient!
                        if (roles != Constraint.ANY_ROLE)
                        {
                            for (int r = scr.length; r-- > 0;)
                                roles = LazyList.add(roles, scr[r]);
                        }
                    }
                    else
                    {
                        unchecked = true;
                    }
                }
            }
        }
        else if (unchecked)
        {
            roles = null;
        }
        if (roles instanceof String)
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
                                                  Response response, Object constraintInfo, UserIdentity userIdentity) throws IOException
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
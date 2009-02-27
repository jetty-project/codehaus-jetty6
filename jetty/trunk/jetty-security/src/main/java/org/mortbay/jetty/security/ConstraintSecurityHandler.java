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
import java.util.Map;
import java.util.Set;

import org.mortbay.jetty.http.security.Constraint;
import org.mortbay.jetty.server.Connector;
import org.mortbay.jetty.server.HttpConnection;
import org.mortbay.jetty.server.PathMap;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Response;
import org.mortbay.jetty.server.RunAsToken;
import org.mortbay.jetty.server.UserIdentity;
import org.mortbay.jetty.util.StringMap;

/* ------------------------------------------------------------ */
/**
 * Handler to enforce SecurityConstraints. This implementation is servlet spec
 * 2.4 compliant and precomputes the constraint combinations for runtime
 * efficiency.
 * 
 */
public class ConstraintSecurityHandler extends SecurityHandler implements ConstraintAware
{
    private ConstraintMapping[] _constraintMappings;
    private Set<String> _roles;
    private PathMap _constraintMap = new PathMap();
    private boolean _strict = true;

    
    /* ------------------------------------------------------------ */
    /** Get the strict mode.
     * @return true if the security handler is running in strict mode.
     */
    public boolean isStrict()
    {
        return _strict;
    }

    /* ------------------------------------------------------------ */
    /** Set the strict mode of the security handler.
     * <p>
     * When in strict mode (the default), the full servlet specification
     * will be implemented.
     * If not in strict mode, some additional flexibility in configuration
     * is allowed:<ul>
     * <li>All users do not need to have a role defined in the deployment descriptor
     * <li>The * role in a constraint applies to ANY role rather than all roles defined in
     * the deployment descriptor.
     * </ul>
     * 
     * @param strict the strict to set
     */
    public void setStrict(boolean strict)
    {
        _strict = strict;
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
    public Set<String> getRoles()
    {
        return _roles;
    }

    /* ------------------------------------------------------------ */
    /**
     * Process the constraints following the combining rules in Servlet 3.0 EA
     * spec section 13.7.1 Note that much of the logic is in the RoleInfo class.
     * 
     * @param constraintMappings
     *            The contraintMappings to set.
     * @param roles
     */
    public void setConstraintMappings(ConstraintMapping[] constraintMappings, Set<String> roles)
    {
        if (isStarted())
            throw new IllegalStateException("Started");
        _constraintMappings = constraintMappings;
        this._roles = roles;

    }

    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.security.SecurityHandler#doStart()
     */
    @Override
    protected void doStart() throws Exception
    {
        _constraintMap.clear();
        if (_constraintMappings != null)
        {
            for (ConstraintMapping mapping : _constraintMappings)
            {
                Map<String, RoleInfo> mappings = (Map<String, RoleInfo>)_constraintMap.get(mapping.getPathSpec());
                if (mappings == null)
                {
                    mappings = new StringMap();
                    _constraintMap.put(mapping.getPathSpec(),mappings);
                }
                RoleInfo allMethodsRoleInfo = mappings.get(null);
                if (allMethodsRoleInfo != null && allMethodsRoleInfo.isForbidden())
                {
                    continue;
                }
                String httpMethod = mapping.getMethod();
                RoleInfo roleInfo = mappings.get(httpMethod);
                if (roleInfo == null)
                {
                    roleInfo = new RoleInfo();
                    mappings.put(httpMethod,roleInfo);
                    if (allMethodsRoleInfo != null)
                    {
                        roleInfo.combine(allMethodsRoleInfo);
                    }
                }
                if (roleInfo.isForbidden())
                {
                    continue;
                }
                Constraint constraint = mapping.getConstraint();
                boolean forbidden = constraint.isForbidden();
                roleInfo.setForbidden(forbidden);
                if (forbidden)
                {
                    if (httpMethod == null)
                    {
                        mappings.clear();
                        mappings.put(null,roleInfo);
                    }
                }
                else
                {
                    UserDataConstraint userDataConstraint = UserDataConstraint.get(constraint.getDataConstraint());
                    roleInfo.setUserDataConstraint(userDataConstraint);

                    boolean unchecked = !constraint.getAuthenticate();
                    roleInfo.setUnchecked(unchecked);
                    if (!roleInfo.isUnchecked())
                    {
                        if (constraint.isAnyRole())
                        {
                            if (_strict)
                            {
                                // * means "all defined roles"
                                for (String role : _roles)
                                    roleInfo.addRole(role);
                            }
                            else
                                // * means any role
                                roleInfo.setAnyRole(true);
                        }
                        else
                        {
                            String[] newRoles = constraint.getRoles();
                            for (String role : newRoles)
                            {
                                if (_strict &&!_roles.contains(role))
                                    throw new IllegalArgumentException("Attempt to use undeclared role: " + role + ", known roles: " + _roles);
                                roleInfo.addRole(role);
                            }
                        }
                    }
                    if (httpMethod == null)
                    {
                        for (Map.Entry<String, RoleInfo> entry : mappings.entrySet())
                        {
                            if (entry.getKey() != null)
                            {
                                RoleInfo specific = entry.getValue();
                                specific.combine(roleInfo);
                            }
                        }
                    }
                }
            }
        }
        super.doStart();
    }

    protected UserIdentity newUserIdentity(ServerAuthResult authResult)
    {
        return new AuthResultUserIdentity(authResult);
    }

    protected UserIdentity newSystemUserIdentity()
    {
        return new AuthResultUserIdentity();
    }

    public RunAsToken newRunAsToken(String runAsRole)
    {
        return new RoleRunAsToken(runAsRole);
    }

    protected Object prepareConstraintInfo(String pathInContext, Request request)
    {
        Map<String, RoleInfo> mappings = (Map<String, RoleInfo>)_constraintMap.match(pathInContext);

        if (mappings != null)
        {
            String httpMethod = request.getMethod();
            RoleInfo roleInfo = mappings.get(httpMethod);
            if (roleInfo == null)
            {
                roleInfo = mappings.get(null);
                if (roleInfo != null)
                {
                    return roleInfo;
                }
            }
        }
        return null;
    }

    protected boolean checkUserDataPermissions(String pathInContext, Request request, Response response, Object constraintInfo) throws IOException
    {
        if (constraintInfo == null)
        {
            return true;
        }
        RoleInfo roleInfo = (RoleInfo)constraintInfo;
        if (roleInfo.isForbidden())
        {
            return false;
        }
        UserDataConstraint dataConstraint = roleInfo.getUserDataConstraint();
        if (dataConstraint == null || dataConstraint == UserDataConstraint.None)
        {
            return true;
        }
        HttpConnection connection = HttpConnection.getCurrentConnection();
        Connector connector = connection.getConnector();

        if (dataConstraint == UserDataConstraint.Integral)
        {
            if (connector.isIntegral(request))
                return true;
            if (connector.getConfidentialPort() > 0)
            {
                String url = connector.getIntegralScheme() + "://" + request.getServerName() + ":" + connector.getIntegralPort() + request.getRequestURI();
                if (request.getQueryString() != null)
                    url += "?" + request.getQueryString();
                response.setContentLength(0);
                response.sendRedirect(url);
                request.setHandled(true);
            }
            return false;
        }
        else if (dataConstraint == UserDataConstraint.Confidential)
        {
            if (connector.isConfidential(request))
                return true;

            if (connector.getConfidentialPort() > 0)
            {
                String url = connector.getConfidentialScheme() + "://" + request.getServerName() + ":" + connector.getConfidentialPort()
                        + request.getRequestURI();
                if (request.getQueryString() != null)
                    url += "?" + request.getQueryString();

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
        if (constraintInfo == null)
        {
            return false;
        }
        return !((RoleInfo)constraintInfo).isUnchecked();
    }

    protected boolean checkWebResourcePermissions(String pathInContext, Request request, Response response, Object constraintInfo, UserIdentity userIdentity)
            throws IOException
    {
        if (constraintInfo == null)
        {
            return true;
        }
        RoleInfo roleInfo = (RoleInfo)constraintInfo;

        if (roleInfo.isUnchecked())
        {
            return true;
        }
        
        if (roleInfo.isAnyRole() && userIdentity.getAuthMethod()!=null)
            return true;
        
        String[] roles = roleInfo.getRoles();
        for (String role : roles)
        {
            if (userIdentity.isUserInRole(role))
                return true;
        }
        return false;
    }
}

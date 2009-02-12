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

package org.mortbay.jetty.security;

import java.security.Principal;
import java.util.Map;

import org.mortbay.jetty.RunAsToken;
import org.mortbay.jetty.servlet.ServletHolder;

/**
 * @version $Rev$ $Date$
 */
public class ConstraintUserIdentity extends AbstractUserIdentity
{

    private ServletHolder _servletHolder;

    private RunAsToken _runAsRole;

    public ConstraintUserIdentity(ServerAuthResult authResult)
    {
        super(authResult);
    }

    public ConstraintUserIdentity()
    {
        super(SimpleAuthResult.SUCCESS_UNAUTH_RESULTS);
    }

    // jaspi called from Request.isUserInRole and
    // ConstraintSecurityHandler.check

    // n.b. run as role does not participate in isUserInRole calculations. It's
    // only for when this component calls something else.
    public boolean isUserInRole(String role)
    {
        if (role == null) { throw new NullPointerException("role"); }
        String actualRole = getRoleRefMap().get(role);
        if (actualRole == null)
        {
            actualRole = role;
        }
        for (String userRole : getAuthResult().getGroups())
        {
            if (userRole.equals(actualRole)) { return true; }
        }
        return false;
    }/* ------------------------------------------------------------ */

    // jaspi called from ServletHolder.handle, initServlet, doStop and tests
    public RunAsToken setRunAsRole(RunAsToken newRunAsRole)
    {
        RunAsToken oldRunAsRole = _runAsRole;
        _runAsRole = newRunAsRole;
        return oldRunAsRole;
    }

    public ServletHolder setServletHolder(ServletHolder newServletHolder)
    {
        ServletHolder oldServletHolder = _servletHolder;
        this._servletHolder = newServletHolder;
        return oldServletHolder;
    }

    // jaspi called from FormAuthenticator.valueUnbound (when session is
    // unbound)
    // TODO usable???
    public void logout(Principal user)
    {
    }

    public Map<String, String> getRoleRefMap()
    {
        if (_servletHolder == null) { return ServletHolder.NO_MAPPED_ROLES; }
        return _servletHolder.getRoleMap();
    }
}

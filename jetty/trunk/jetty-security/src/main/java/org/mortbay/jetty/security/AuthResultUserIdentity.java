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

import org.mortbay.jetty.server.RunAsToken;
import org.mortbay.jetty.server.UserIdentity;

/**
 * {@link UserIdentity} based on a {@link ServerAuthResult}.
 */
public class AuthResultUserIdentity implements UserIdentity
{
    private final ServerAuthResult _authResult;
    private RunAsToken _runAsRole;
    private Map<String, String> _roleRefMap;

    protected AuthResultUserIdentity(ServerAuthResult authResult)
    {
        this._authResult = authResult;
    }

    public AuthResultUserIdentity()
    {
        this._authResult=SimpleAuthResult.SUCCESS_UNAUTH_RESULTS;
    }

    public Principal getUserPrincipal()
    {
        return _authResult.getUserPrincipal();
    }

    public String getAuthMethod()
    {
        return _authResult.getAuthMethod();
    }

    protected ServerAuthResult getAuthResult()
    {
        return _authResult;
    }

    public boolean isUserInRole(String role)
    {
        if (role == null) 
            throw new NullPointerException("role");
        final Map<String,String> roleRefMap=getRoleRefMap();
        String actualRole = roleRefMap==null?role:roleRefMap.get(role);
        if (actualRole == null)
        {
            actualRole = role;
        }
        for (String userRole : getAuthResult().getGroups())
        {
            if (userRole.equals(actualRole)) { return true; }
        }
        return false;
    }

    public Map<String, String> setRoleRefMap(Map<String, String> roleMap)
    {
        Map<String,String> old = _roleRefMap;
        _roleRefMap=roleMap;
        return old;
    }

    public RunAsToken setRunAsRole(RunAsToken newRunAsRole)
    {
        RunAsToken oldRunAsRole = _runAsRole;
        _runAsRole = newRunAsRole;
        return oldRunAsRole;
    }

    public Map<String, String> getRoleRefMap()
    {
        return _roleRefMap;
    }
    
    public String toString()
    {
        return "{"+_authResult+(_runAsRole!=null?_runAsRole.toString():"")+"}";
    }
    
}
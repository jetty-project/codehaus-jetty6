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
import java.util.List;
import java.util.Collections;
import java.util.Arrays;

import javax.security.auth.Subject;


/**
 * @version $Rev$ $Date$
 */
public class SimpleAuthResult implements ServerAuthResult
{

    public static final ServerAuthResult SUCCESS_UNAUTH_RESULTS = new SimpleAuthResult(ServerAuthStatus.SUCCESS, null, null, Collections.<String> emptyList(), null);
    public static final ServerAuthResult SEND_CONTINUE_RESULTS = new SimpleAuthResult(ServerAuthStatus.SEND_CONTINUE, null, null, Collections.<String> emptyList(), null);
    public static final ServerAuthResult SEND_FAILURE_RESULTS = new SimpleAuthResult(ServerAuthStatus.SEND_FAILURE, null, null, Collections.<String> emptyList(), null);

    private final ServerAuthStatus _authStatus;

    private final Subject _clientSubject;

    private final Principal _userPrincipal;

    private final List<String> _groups;

    private final String _authMethod;

    public SimpleAuthResult(ServerAuthStatus authStatus, Subject clientSubject, 
                            Principal userPrincipal, List<String> groups, String authMethod)
    {
        this._authStatus = authStatus;
        this._clientSubject = clientSubject;
        this._userPrincipal = userPrincipal;
        this._groups = groups;
        this._authMethod = userPrincipal == null ? null : authMethod;
    }

    public SimpleAuthResult(ServerAuthStatus authStatus, Subject clientSubject, 
                            Principal userPrincipal, String[] groups, String authMethod)
    {
        this._authStatus = authStatus;
        this._clientSubject = clientSubject;
        this._userPrincipal = userPrincipal;
        this._groups = groups == null ? Collections.<String> emptyList() : Arrays.asList(groups);
        this._authMethod = userPrincipal == null ? null : authMethod;
    }

    public ServerAuthStatus getAuthStatus()
    {
        return _authStatus;
    }

    public Subject getClientSubject()
    {
        return _clientSubject;
    }

    public Principal getUserPrincipal()
    {
        return _userPrincipal;
    }

    public List<String> getGroups()
    {
        return _groups;
    }

    public String getAuthMethod()
    {
        return _authMethod;
    }
}

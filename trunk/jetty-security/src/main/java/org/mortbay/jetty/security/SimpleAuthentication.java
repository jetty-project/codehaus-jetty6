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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;


/**
 * @version $Rev$ $Date$
 */
public class SimpleAuthentication implements Authentication
{

    public static final Authentication SUCCESS_UNAUTH_RESULTS = new SimpleAuthentication(Authentication.Status.SUCCESS, null, null, Authentication.NO_ROLES, null);
    public static final Authentication SEND_CONTINUE_RESULTS = new SimpleAuthentication(Authentication.Status.SEND_CONTINUE, null, null, Authentication.NO_ROLES, null);
    public static final Authentication SEND_FAILURE_RESULTS = new SimpleAuthentication(Authentication.Status.SEND_FAILURE, null, null, Authentication.NO_ROLES, null);

    private final Authentication.Status _authStatus;

    private final Subject _clientSubject;

    private final Principal _userPrincipal;

    private final String[] _roles;

    private final String _authMethod;


    public SimpleAuthentication(Authentication.Status authStatus, Subject clientSubject, 
                            Principal userPrincipal, String[] roles, String authMethod)
    {
        this._authStatus = authStatus;
        this._clientSubject = clientSubject;
        this._userPrincipal = userPrincipal;
        this._roles = roles;
        this._authMethod = userPrincipal == null ? null : authMethod;
    }

    public Authentication.Status getAuthStatus()
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

    public String[] getRoles()
    {
        return _roles;
    }

    public String getAuthMethod()
    {
        return _authMethod;
    }
    
    public String toString()
    {
        return "{Auth,"+_authMethod+","+_authStatus+","+_userPrincipal+","+Arrays.asList(_roles)+"}";
    }
}

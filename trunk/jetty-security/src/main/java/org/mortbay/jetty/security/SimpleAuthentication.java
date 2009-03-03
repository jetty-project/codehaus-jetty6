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

import org.mortbay.jetty.server.UserIdentity;


/**
 * @version $Rev$ $Date$
 */
public class SimpleAuthentication implements Authentication
{
    private final Authentication.Status _authStatus;
    private final String _authMethod;
    private final UserIdentity _userIdentity;

    public SimpleAuthentication(Authentication.Status authStatus, String authMethod, UserIdentity userIdentity)
    {
        _authStatus = authStatus;
        _authMethod = authMethod;
        _userIdentity=userIdentity;
    }

    public String getAuthMethod()
    {
        return _authMethod;
    }
    
    public Authentication.Status getAuthStatus()
    {
        return _authStatus;
    }

    public UserIdentity getUserIdentity()
    {
        return _userIdentity;
    }

    public boolean isSuccess()
    {
        return _authStatus.isSuccess();
    }
    
    public String toString()
    {
        return "{Auth,"+_authMethod+","+_authStatus+","+","+_userIdentity+"}";
    }
}

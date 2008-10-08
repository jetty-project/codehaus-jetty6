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

import javax.security.auth.message.AuthStatus;
import javax.security.auth.Subject;

import org.mortbay.jetty.AuthResult;

/**
 * @version $Rev$ $Date$
 */
public class SimpleAuthResult implements AuthResult {

    private final AuthStatus authStatus;
    private final Subject clientSubject;
    private final Principal userPrincipal;
    private final List<String> groups;
    private final String authMethod;

    public SimpleAuthResult(AuthStatus authStatus, Subject clientSubject, Principal userPrincipal, List<String> groups, String authMethod) {
        this.authStatus = authStatus;
        this.clientSubject = clientSubject;
        this.userPrincipal = userPrincipal;
        this.groups = groups;
        this.authMethod = userPrincipal == null? null: authMethod;
    }
    public SimpleAuthResult(AuthStatus authStatus, Subject clientSubject, Principal userPrincipal, String[] groups, String authMethod) {
        this.authStatus = authStatus;
        this.clientSubject = clientSubject;
        this.userPrincipal = userPrincipal;
        this.groups = groups == null? Collections.<String>emptyList(): Arrays.asList(groups);
        this.authMethod = userPrincipal == null? null: authMethod;
    }

    public AuthStatus getAuthStatus() {
        return authStatus;
    }

    public Subject getClientSubject() {
        return clientSubject;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public List<String> getGroups() {
        return groups;
    }

    public String getAuthMethod() {
        return authMethod;
    }
}

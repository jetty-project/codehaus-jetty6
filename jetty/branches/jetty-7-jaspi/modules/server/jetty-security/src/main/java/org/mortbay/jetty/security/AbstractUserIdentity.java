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

import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.UserIdentity;

/**
 * @version $Rev$ $Date$
 */
public abstract class AbstractUserIdentity implements UserIdentity {
    private final ServerAuthResult authResult;

    protected AbstractUserIdentity(ServerAuthResult authResult) {
        this.authResult = authResult;
    }

    public Principal getUserPrincipal() {
        return authResult.getUserPrincipal();
    }

    public String getAuthMethod() {
        return authResult.getAuthMethod();
    }

    protected ServerAuthResult getAuthResult() {
        return authResult;
    }
}
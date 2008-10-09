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

import javax.security.auth.Subject;

import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.ServerAuthStatus;
import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.ServerAuthException;

/**
 * @version $Rev:$ $Date:$
 */
public class LazyAuthResult implements ServerAuthResult {
    private static final Subject unauthenticatedSubject = new Subject();

    private final ServerAuthentication serverAuthentication;
    private final JettyMessageInfo messageInfo;

    private ServerAuthResult delegate;

    public LazyAuthResult(ServerAuthentication serverAuthentication, JettyMessageInfo messageInfo) {
        if (serverAuthentication == null) throw new NullPointerException("No ServerAuthentication");
        if (messageInfo == null) throw new NullPointerException("No JettyMessageInfo");
        this.serverAuthentication = serverAuthentication;
        this.messageInfo = messageInfo;
    }

    private ServerAuthResult getDelegate() {
        if (delegate == null) {
            try {
                delegate = serverAuthentication.validateRequest(messageInfo);
            } catch (ServerAuthException e) {
                delegate = SimpleAuthResult.NO_AUTH_RESULTS;
            }
        }
        return delegate;
    }

    public ServerAuthStatus getAuthStatus() {
        return getDelegate().getAuthStatus();
    }

    //for cleaning in secureResponse
    public Subject getClientSubject() {
        return delegate == null? unauthenticatedSubject: delegate.getClientSubject();
    }

    public Principal getUserPrincipal() {
        return getDelegate().getUserPrincipal();
    }

    public List<String> getGroups() {
        return getDelegate().getGroups();
    }

    public String getAuthMethod() {
        return getDelegate().getAuthMethod();
    }
}

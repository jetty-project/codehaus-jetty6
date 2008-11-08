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


package org.mortbay.jetty.security.authentication;

import org.mortbay.jetty.ServerAuthentication;
import org.mortbay.jetty.ServerAuthResult;
import org.mortbay.jetty.JettyMessageInfo;
import org.mortbay.jetty.ServerAuthException;
import org.mortbay.jetty.ServerAuthStatus;
import org.mortbay.jetty.security.LazyAuthResult;

/**
 * @version $Rev:$ $Date:$
 */
public class LazyServerAuthentication implements ServerAuthentication {

    private final ServerAuthentication delegate;

    public LazyServerAuthentication(ServerAuthentication delegate) {
        this.delegate = delegate;
    }

    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException {
        if (!messageInfo.isAuthMandatory()) {
            return new LazyAuthResult(delegate, messageInfo);
        }
        return delegate.validateRequest(messageInfo);
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException {
        return delegate.secureResponse(messageInfo, validatedUser);
    }
}

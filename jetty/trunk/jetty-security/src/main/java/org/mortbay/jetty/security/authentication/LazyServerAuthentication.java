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

import org.mortbay.jetty.security.JettyMessageInfo;
import org.mortbay.jetty.security.LazyAuthResult;
import org.mortbay.jetty.security.ServerAuthException;
import org.mortbay.jetty.security.ServerAuthResult;
import org.mortbay.jetty.security.ServerAuthStatus;
import org.mortbay.jetty.security.ServerAuthentication;

/**
 * @version $Rev$ $Date$
 */
public class LazyServerAuthentication implements ServerAuthentication
{

    private final ServerAuthentication _delegate;

    public LazyServerAuthentication(ServerAuthentication delegate)
    {
        this._delegate = delegate;
    }

    /** 
     * @see org.mortbay.jetty.server.server.security.ServerAuthentication#validateRequest(org.mortbay.jetty.server.server.security.JettyMessageInfo)
     */
    public ServerAuthResult validateRequest(JettyMessageInfo messageInfo) throws ServerAuthException
    {
        if (!messageInfo.isAuthMandatory())
        { 
            return new LazyAuthResult(_delegate, messageInfo);
        }
        return _delegate.validateRequest(messageInfo);
    }

    public ServerAuthStatus secureResponse(JettyMessageInfo messageInfo, ServerAuthResult validatedUser) throws ServerAuthException
    {
        return _delegate.secureResponse(messageInfo, validatedUser);
    }
}

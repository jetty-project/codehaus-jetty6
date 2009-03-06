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
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.server.UserIdentity;


/**
 * @version $Rev$ $Date$
 */
public class LazyAuthentication implements Authentication
{
    private static final Subject unauthenticatedSubject = new Subject();

    private final Authenticator _serverAuthentication;
    private final ServletRequest _request;
    private final ServletResponse _response;

    private Authentication _delegate;

    public LazyAuthentication(Authenticator serverAuthentication, ServletRequest request, ServletResponse response)
    {
        if (serverAuthentication == null) throw new NullPointerException("No ServerAuthentication");
        this._serverAuthentication = serverAuthentication;
        this._request=request;
        this._response=response;   
    }

    private Authentication getDelegate()
    {
        if (_delegate == null)
        {
            try
            {
                _delegate = _serverAuthentication.validateRequest(_request, _response, false);
            }
            catch (ServerAuthException e)
            {
                _delegate = DefaultAuthentication.SEND_FAILURE_RESULTS;
            }
        }
        return _delegate;
    }

    public Authentication.Status getAuthStatus()
    {
        return getDelegate().getAuthStatus();
    }

    public boolean isSuccess()
    {
        return getDelegate().isSuccess();
    }
    
    // for cleaning in secureResponse
    public UserIdentity getUserIdentity()
    {
        return _delegate == null ? UserIdentity.UNAUTHENTICATED_IDENTITY: _delegate.getUserIdentity();
    }

    public String getAuthMethod()
    {
        return getDelegate().getAuthMethod();
    }

    public String toString()
    {
        return "{Lazy,"+_delegate+"}";
    }
}

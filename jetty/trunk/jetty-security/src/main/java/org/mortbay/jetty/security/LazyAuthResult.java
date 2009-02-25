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


/**
 * @version $Rev$ $Date$
 */
public class LazyAuthResult implements ServerAuthResult
{
    private static final Subject unauthenticatedSubject = new Subject();

    private final Authenticator _serverAuthentication;
    private final ServletRequest _request;
    private final ServletResponse _response;

    private ServerAuthResult _delegate;

    public LazyAuthResult(Authenticator serverAuthentication, ServletRequest request, ServletResponse response)
    {
        if (serverAuthentication == null) throw new NullPointerException("No ServerAuthentication");
        this._serverAuthentication = serverAuthentication;
        this._request=request;
        this._response=response;   
    }

    private ServerAuthResult getDelegate()
    {
        if (_delegate == null)
        {
            try
            {
                _delegate = _serverAuthentication.validateRequest(_request, _response, false);
            }
            catch (ServerAuthException e)
            {
                _delegate = SimpleAuthResult.SEND_FAILURE_RESULTS;
            }
        }
        return _delegate;
    }

    public ServerAuthStatus getAuthStatus()
    {
        return getDelegate().getAuthStatus();
    }

    // for cleaning in secureResponse
    public Subject getClientSubject()
    {
        return _delegate == null ? unauthenticatedSubject : _delegate.getClientSubject();
    }

    public Principal getUserPrincipal()
    {
        return getDelegate().getUserPrincipal();
    }

    public List<String> getGroups()
    {
        return getDelegate().getGroups();
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

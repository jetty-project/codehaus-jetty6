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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.security.Authentication;
import org.mortbay.jetty.security.Authenticator;
import org.mortbay.jetty.security.LazyAuthentication;
import org.mortbay.jetty.security.ServerAuthException;

/**
 * @version $Rev$ $Date$
 */
public class LazyAuthenticator extends DelegateAuthenticator 
{
    public LazyAuthenticator(Authenticator delegate)
    {
        super(delegate);
    }

    /** 
     * @see org.mortbay.jetty.security.Authenticator#validateRequest(ServletRequest, ServletResponse, boolean)
     */
    public Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException
    {
        if (!mandatory)
        { 
            return new LazyAuthentication(_delegate,request,response);
        }
        return _delegate.validateRequest(request, response, mandatory);
    }
}

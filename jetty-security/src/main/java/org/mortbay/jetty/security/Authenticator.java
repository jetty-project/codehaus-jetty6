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

import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.mortbay.jetty.server.Server;
import org.mortbay.jetty.server.UserIdentity;

/**
 * This is like the JASPI ServerAuthContext but is intended to be easier to use
 * and allow lazy auth.
 * 
 * @version $Rev$ $Date$
 */
public interface Authenticator
{
    void setConfiguration(Configuration configuration);
    String getAuthMethod();
    
    Authentication validateRequest(ServletRequest request, ServletResponse response, boolean mandatory) throws ServerAuthException;
    Authentication.Status secureResponse(ServletRequest request, ServletResponse response, boolean mandatory, Authentication validatedUser) throws ServerAuthException;
    
    interface Configuration
    {
        String getAuthMethod();
        String getRealmName();
        boolean isLazy();
        String getInitParameter(String key);
        Set<String> getInitParameterNames();
        LoginService getLoginService();
        IdentityService getIdentityService();
    }
    
    interface Factory
    {
        Authenticator getAuthenticator(Server server, ServletContext context, Configuration configuration);
    }
}

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

import org.mortbay.jetty.handler.HandlerWrapper;
import org.mortbay.jetty.handler.SecurityHandler;
import org.mortbay.jetty.AuthenticationManager;
import org.mortbay.jetty.RunAsToken;
import org.mortbay.jetty.UserRealm;

/**
 * @version $Rev$ $Date$
 */
public class NoSecurityHandler extends HandlerWrapper implements SecurityHandler
{

    public UserRealm getUserRealm()
    {
        return null;
    }

    public void setUserRealm(UserRealm realm)
    {        
    }

    public RunAsToken newRunAsToken(String runAsRole)
    {
        return null;
    }

    public void setServerAuthentication(ServerAuthentication serverAuthentication)
    {
    }

    public AuthenticationManager getAuthenticationManager()
    {
        return null;
    }

    public void setAuthenticationManager(AuthenticationManager authManager)
    {     
    }

}

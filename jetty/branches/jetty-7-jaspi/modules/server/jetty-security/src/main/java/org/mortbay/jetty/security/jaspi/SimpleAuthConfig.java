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

package org.mortbay.jetty.security.jaspi;

import java.util.Map;

import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.Subject;

/**
 * @version $Rev$ $Date$
 */
public class SimpleAuthConfig implements ServerAuthConfig
{
    public static final String HTTP_SERVLET = "HttpServlet";

    private final String _appContext;

    private final ServerAuthContext _serverAuthContext;

    public SimpleAuthConfig(String appContext, ServerAuthContext serverAuthContext)
    {
        this._appContext = appContext;
        this._serverAuthContext = serverAuthContext;
    }

    public ServerAuthContext getAuthContext(String authContextID, Subject serviceSubject, Map properties) throws AuthException
    {
        return _serverAuthContext;
    }

    // supposed to be of form host-name<space>context-path
    public String getAppContext()
    {
        return _appContext;
    }

    // not used yet
    public String getAuthContextID(MessageInfo messageInfo) throws IllegalArgumentException
    {
        return null;
    }

    public String getMessageLayer()
    {
        return HTTP_SERVLET;
    }

    public boolean isProtected()
    {
        return true;
    }

    public void refresh() throws AuthException, SecurityException
    {
    }
}

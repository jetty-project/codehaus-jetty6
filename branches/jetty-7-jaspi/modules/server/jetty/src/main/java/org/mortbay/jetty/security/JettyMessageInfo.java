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

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

/**
 * @version $Rev:$ $Date:$
 */
public class JettyMessageInfo implements MessageInfo
{
    public static final String MANDATORY_KEY = "javax.security.auth.message.MessagePolicy.isMandatory";
    public static final String AUTH_METHOD_KEY = "javax.servlet.http.authType";
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final Map map = new HashMap();

    public JettyMessageInfo(HttpServletRequest request, HttpServletResponse response, boolean isAuthMandatory)
    {
        this.request = request;
        this.response = response;
        //JASPI 3.8.1
        map.put(MANDATORY_KEY, Boolean.toString(isAuthMandatory));
    }

    public Map getMap()
    {
        return map;
    }

    public Object getRequestMessage()
    {
        return request;
    }

    public Object getResponseMessage()
    {
        return response;
    }

    public void setRequestMessage(Object request)
    {
        if (!(request instanceof HttpServletRequest))
            throw new IllegalArgumentException("request must be an HttpServletRequest, not: " + request);
        this.request = (HttpServletRequest) request;
    }

    public void setResponseMessage(Object response)
    {
        if (!(response instanceof HttpServletResponse))
            throw new IllegalArgumentException("request must be an HttpServletRequest, not: " + response);
        this.response = (HttpServletResponse) response;
    }

    public boolean isMandatory()
    {
        String mandatory = (String) map.get(MANDATORY_KEY);
        if (mandatory == null) throw new IllegalStateException("no mandatory key");
        return Boolean.valueOf(mandatory);

    }

    public String getAuthMethod()
    {
        return (String) map.get(AUTH_METHOD_KEY);
    }
}

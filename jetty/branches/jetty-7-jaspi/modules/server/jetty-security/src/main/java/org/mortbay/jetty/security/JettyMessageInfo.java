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
import java.util.Set;
import java.util.Collection;

import javax.security.auth.message.MessageInfo;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

/**
 * @version $Rev$ $Date$
 */
public class JettyMessageInfo implements MessageInfo
{
    public static final String MANDATORY_KEY = "javax.security.auth.message.MessagePolicy.isMandatory";
    public static final String AUTH_METHOD_KEY = "javax.servlet.http.authType";
    private HttpServletRequest request;
    private HttpServletResponse response;
    private final Map map;

    public JettyMessageInfo(HttpServletRequest request, HttpServletResponse response, boolean isAuthMandatory)
    {
        this.request = request;
        this.response = response;
        //JASPI 3.8.1
        map = new MIMap(isAuthMandatory);
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

    public String getAuthMethod()
    {
        return (String) map.get(AUTH_METHOD_KEY);
    }


    //TODO this has bugs in the view implementations.  Changing them will not affect the hardcoded values.
    private static class MIMap implements Map
    {

        private final boolean isMandatory;
        private String authMethod;
        private Map delegate;

        private MIMap(boolean mandatory)
        {
            isMandatory = mandatory;
        }

        public int size()
        {
            return (isMandatory? 1:0) +
                    (authMethod == null? 0: 1) +
                    (delegate == null? 0: delegate.size());
        }

        public boolean isEmpty()
        {
            return !isMandatory && authMethod == null && (delegate == null || delegate.isEmpty());
        }

        public boolean containsKey(Object key)
        {
            if (MANDATORY_KEY.equals(key)) return true;
            if (AUTH_METHOD_KEY.equals(key)) return true;
            return delegate != null && delegate.containsKey(key);
        }

        public boolean containsValue(Object value)
        {
            if (isMandatory && "true".equals(value)) return true;
            if (authMethod == value || (authMethod != null && authMethod.equals(value))) return true;
            return delegate != null && delegate.containsValue(value);
        }

        public Object get(Object key)
        {
            if (MANDATORY_KEY.equals(key)) return isMandatory? "true": null;
            if (AUTH_METHOD_KEY.equals(key)) return authMethod;
            if (delegate == null) return null;
            return delegate.get(key);
        }

        public Object put(Object key, Object value)
        {
            if (MANDATORY_KEY.equals(key))
            {
                throw new IllegalArgumentException("Mandatory not mutable");
            }
            if (AUTH_METHOD_KEY.equals(key))
            {
                String authMethod = this.authMethod;
                this.authMethod = (String) value;
                if (delegate != null) delegate.put(AUTH_METHOD_KEY, value);
                return authMethod;
            }

            return getDelegate(true).put(key, value);
        }

        public Object remove(Object key)
        {
            if (MANDATORY_KEY.equals(key))
            {
                throw new IllegalArgumentException("Mandatory not mutable");
            }
            if (AUTH_METHOD_KEY.equals(key))
            {
                String authMethod = this.authMethod;
                this.authMethod = null;
                if (delegate != null) delegate.remove(AUTH_METHOD_KEY);
                return authMethod;
            }
            if (delegate == null) return null;
            return delegate.remove(key);
        }

        public void putAll(Map map)
        {
            if (map != null)
            {
                for (Object o: map.entrySet())
                {
                    Map.Entry entry = (Entry) o;
                    put(entry.getKey(), entry.getValue());
                }
            }
        }

        public void clear()
        {
            authMethod = null;
            delegate = null;
        }

        public Set keySet()
        {
            return getDelegate(true).keySet();
        }

        public Collection values()
        {
            return getDelegate(true).values();
        }

        public Set entrySet()
        {
            return getDelegate(true).entrySet();
        }

        private Map getDelegate(boolean create)
        {
            if (!create || delegate != null) return delegate;
            if (create)
            {
                delegate = new HashMap();
                if (isMandatory) delegate.put(MANDATORY_KEY, "true");
                if (authMethod != null) delegate.put(AUTH_METHOD_KEY, authMethod);
            }
            return delegate;
        }
    }
}

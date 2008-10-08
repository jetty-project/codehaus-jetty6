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

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.security.SecureRandom;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

/**
 * @version $Rev$ $Date$
 */
public class HashCrossContextPsuedoSession<T> implements CrossContextPsuedoSession<T>
{
    private final String cookieName;
    private final String cookiePath;
    private final Random _random = new SecureRandom();
    private final Map<String, T> data = new HashMap<String, T>();


    public HashCrossContextPsuedoSession(String cookieName, String cookiePath)
    {
        this.cookieName = cookieName;
        this.cookiePath = cookiePath == null? "/": cookiePath;
    }

    public T fetch(HttpServletRequest request)
    {
        for (Cookie cookie: request.getCookies())
        {
            if (cookieName.equals(cookie.getName()))
            {
                String key = cookie.getValue();
                return data.get(key);
            }
        }
        return null;
    }

    public void store(T datum, HttpServletResponse response)
    {
        String key;

        synchronized(data)
        {
            // Create new ID
            while (true)
            {
                key = Long.toString(Math.abs(_random.nextLong()),
                                      30 + (int)(System.currentTimeMillis() % 7));
                if (!data.containsKey(key))
                    break;
            }

            data.put(key,datum);
        }

        Cookie cookie = new Cookie(cookieName, key);
        cookie.setPath(cookiePath);
        response.addCookie(cookie);
    }

    public void clear(HttpServletRequest request)
    {
        for (Cookie cookie: request.getCookies())
        {
            if (cookieName.equals(cookie.getName()))
            {
                String key = cookie.getValue();
                data.remove(key);
                break;
            }
        }
    }
}

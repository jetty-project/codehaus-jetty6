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
    private final String _cookieName;

    private final String _cookiePath;

    private final Random _random = new SecureRandom();

    private final Map<String, T> _data = new HashMap<String, T>();

    public HashCrossContextPsuedoSession(String cookieName, String cookiePath)
    {
        this._cookieName = cookieName;
        this._cookiePath = cookiePath == null ? "/" : cookiePath;
    }

    public T fetch(HttpServletRequest request)
    {
        for (Cookie cookie : request.getCookies())
        {
            if (_cookieName.equals(cookie.getName()))
            {
                String key = cookie.getValue();
                return _data.get(key);
            }
        }
        return null;
    }

    public void store(T datum, HttpServletResponse response)
    {
        String key;

        synchronized (_data)
        {
            // Create new ID
            while (true)
            {
                key = Long.toString(Math.abs(_random.nextLong()), 30 + (int) (System.currentTimeMillis() % 7));
                if (!_data.containsKey(key)) break;
            }

            _data.put(key, datum);
        }

        Cookie cookie = new Cookie(_cookieName, key);
        cookie.setPath(_cookiePath);
        response.addCookie(cookie);
    }

    public void clear(HttpServletRequest request)
    {
        for (Cookie cookie : request.getCookies())
        {
            if (_cookieName.equals(cookie.getName()))
            {
                String key = cookie.getValue();
                _data.remove(key);
                break;
            }
        }
    }
}

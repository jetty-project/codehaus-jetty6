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


package org.mortbay.jetty.security.jaspi.modules;

import java.util.Arrays;
import java.io.UnsupportedEncodingException;

import org.mortbay.util.B64Code;
import org.mortbay.jetty.LoginCredentials;
import org.mortbay.util.StringUtil;

/**
 * @version $Rev$ $Date$
 */
public class UserPasswordLoginCredentials implements LoginCredentials
{
    private final String username;
    private final char[] password;


    public UserPasswordLoginCredentials(String username, char[] password)
    {
        this.username = username;
        this.password = password;
    }

    public UserPasswordLoginCredentials(String credentials) throws UnsupportedEncodingException
    {
        credentials = credentials.substring(credentials.indexOf(' ') + 1);
        credentials = B64Code.decode(credentials, StringUtil.__ISO_8859_1);
        int i = credentials.indexOf(':');
        username = credentials.substring(0, i);
        password = new char[credentials.length() - (i + 1)];
        credentials.getChars(i + 1, credentials.length(), password, 0);//substring(i+1);
    }

    public String getUsername()
    {
        return username;
    }

    public char[] getPassword()
    {
        return password;
    }

    //erase passwords or other sensitive info
    public void clear()
    {
        Arrays.fill(password, (char)0);
    }
}

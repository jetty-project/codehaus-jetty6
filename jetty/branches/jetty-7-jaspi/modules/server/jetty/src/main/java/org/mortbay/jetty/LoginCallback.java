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


package org.mortbay.jetty;

import java.security.Principal;
import java.util.List;
import java.util.Collections;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;

import javax.security.auth.Subject;

import org.mortbay.util.StringUtil;
import org.mortbay.util.B64Code;

/**
 * This is similar to the jaspi PasswordValidationCallback but includes user principal and group info as well.
 * @version $Rev$ $Date$
 */
public class LoginCallback {

    private final List<String> NO_GROUPS = Collections.emptyList();

    //initial data
    private final Subject subject;
    private final String userName;
    private final char[] password;

    private boolean success;
    private Principal userPrincipal;
    private List<String> groups = NO_GROUPS;

    public LoginCallback(Subject subject, String userName, char[] password) {
        this.subject = subject;
        this.userName = userName;
        this.password = password;
    }

    public LoginCallback(Subject subject, String credentials) throws UnsupportedEncodingException {
        this.subject = subject;
        credentials = credentials.substring(credentials.indexOf(' ') + 1);
        credentials = B64Code.decode(credentials, StringUtil.__ISO_8859_1);
        int i = credentials.indexOf(':');
        this.userName = credentials.substring(0, i);
        this.password = new char[credentials.length() - (i + 1)];
        credentials.getChars(i + 1, credentials.length(), password, 0);//substring(i+1);
    }

    public Subject getSubject() {
        return subject;
    }

    public String getUserName() {
        return userName;
    }

    public char[] getPassword() {
        return password;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups == null? NO_GROUPS: groups;
    }
    public void setGroups(String[] groups) {
        this.groups = groups == null? NO_GROUPS: Arrays.asList(groups);
    }

    public void clearPassword() {
        if (password != null) {
            Arrays.fill(password, (char)0);
        }
    }

}

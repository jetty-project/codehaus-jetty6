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

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;

import org.mortbay.jetty.server.UserIdentity;

/**
 * This is similar to the jaspi PasswordValidationCallback but includes user
 * principal and group info as well.
 * 
 * @version $Rev$ $Date$
 */
public class LoginCallbackImpl implements LoginCallback
{
    // initial data
    private final Subject subject;

    private final String userName;

    private Object credential;

    private boolean success;

    private Principal userPrincipal;

    private String[] roles = UserIdentity.NO_ROLES;

    //TODO could use Credential instance instead of Object if Basic/Form create a Password object
    public LoginCallbackImpl (Subject subject, String userName, Object credential)
    {
        this.subject = subject;
        this.userName = userName;
        this.credential = credential;
    }

    public Subject getSubject()
    {
        return subject;
    }

    public String getUserName()
    {
        return userName;
    }

    public Object getCredential()
    {
        return credential;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }

    public Principal getUserPrincipal()
    {
        return userPrincipal;
    }

    public void setUserPrincipal(Principal userPrincipal)
    {
        this.userPrincipal = userPrincipal;
    }

    public String[] getRoles()
    {
        return roles;
    }

    public void setRoles(String[] groups)
    {
        this.roles = groups;
    }

    public void clearPassword()
    {
        if (credential != null)
        {
            credential = null;
        }
    }

}

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
import java.util.HashSet;

/**
 *
 * Badly named class that holds the role and user data constraint info for a path/http method combination, extracted and combined from
 * security constraints.
 * @version $Rev:$ $Date:$
 */
public class RoleInfo
{
    private boolean unchecked;
    private boolean forbidden;
    private boolean allRoles;
    private UserDataConstraint userDataConstraint;
    private final Set<String> roles = new HashSet<String>();

    public boolean isUnchecked() {
        return unchecked;
    }

    public void setUnchecked(boolean unchecked) {
        if (!forbidden)
        {
            if (unchecked)
            {
                this.unchecked = unchecked;
                this.allRoles = false;
                roles.clear();
            }
        }
    }

    public boolean isForbidden() {
        return forbidden;
    }

    public void setForbidden(boolean forbidden) {
        if (forbidden)
        {
            this.forbidden = forbidden;
            unchecked = false;
            userDataConstraint = null;
            allRoles = false;
            roles.clear();
        }
    }

    public UserDataConstraint getUserDataConstraint() {
        return userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraint userDataConstraint) {
        if (userDataConstraint == null) throw new NullPointerException("Null UserDataConstraint");
        if (this.userDataConstraint == null)
        {
            this.userDataConstraint = userDataConstraint;
        }
        else
        {
            this.userDataConstraint = this.userDataConstraint.combine(userDataConstraint);
        }
    }

    public boolean isAllRoles() {
        return allRoles;
    }

    public void setAllRoles(boolean allRoles) {
        if (allRoles)
        {
            this.allRoles = true;
            roles.clear();
        }
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void addRoles(String[] newRoles)
    {
        if (newRoles != null)
        {
            for (String role: newRoles)
            {
                if (role.equals("*"))
                {
                    setAllRoles(true);
                    return;
                }
                this.roles.add(role);
            }
        }
    }

    public void combine(RoleInfo other)
    {
        if (other.forbidden)
        {
            setForbidden(true);
        }
        if (other.unchecked) setUnchecked(true);
        if (other.allRoles)
        {
            setAllRoles(true);
        }
        else if (!allRoles)
        {
            roles.addAll(other.roles);
        }

        setUserDataConstraint(other.userDataConstraint);
    }
}

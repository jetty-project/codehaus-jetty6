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
 * Badly named class that holds the role and user data constraint info for a
 * path/http method combination, extracted and combined from security
 * constraints.
 * 
 * @version $Rev$ $Date$
 */
public class RoleInfo
{
    private boolean _unchecked;

    private boolean _forbidden;

    private UserDataConstraint _userDataConstraint;

    private final Set<String> _roles = new HashSet<String>();

    public boolean isUnchecked()
    {
        return _unchecked;
    }

    public void setUnchecked(boolean unchecked)
    {
        if (!_forbidden)
        {
            if (unchecked)
            {
                this._unchecked = unchecked;
                _roles.clear();
            }
        }
    }

    public boolean isForbidden()
    {
        return _forbidden;
    }

    public void setForbidden(boolean forbidden)
    {
        if (forbidden)
        {
            this._forbidden = forbidden;
            _unchecked = false;
            _userDataConstraint = null;
            _roles.clear();
        }
    }

    public UserDataConstraint getUserDataConstraint()
    {
        return _userDataConstraint;
    }

    public void setUserDataConstraint(UserDataConstraint userDataConstraint)
    {
        if (userDataConstraint == null) throw new NullPointerException("Null UserDataConstraint");
        if (this._userDataConstraint == null)
        {
            this._userDataConstraint = userDataConstraint;
        }
        else
        {
            this._userDataConstraint = this._userDataConstraint.combine(userDataConstraint);
        }
    }

    public Set<String> getRoles()
    {
        return _roles;
    }

    public void combine(RoleInfo other)
    {
        if (other._forbidden)
        {
            setForbidden(true);
        }
        if (other._unchecked) setUnchecked(true);
        _roles.addAll(other._roles);

        setUserDataConstraint(other._userDataConstraint);
    }
}

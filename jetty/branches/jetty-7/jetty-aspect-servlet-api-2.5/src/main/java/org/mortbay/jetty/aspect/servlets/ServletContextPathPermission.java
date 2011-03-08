package org.mortbay.jetty.aspect.servlets;
//========================================================================
//$Id:$
//Copyright 2011 Webtide, LLC
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Permission;

/**
 * 
 * permission for servlet context paths
 *
 */
public class ServletContextPathPermission extends Permission
{
    URI thisUri;

    public ServletContextPathPermission(String uripath)
    {
        super(uripath);
        
        try
        {
            thisUri = new URI(uripath);
        }
        catch ( URISyntaxException e )
        {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean implies(Permission permission)
    {
        if (!(permission instanceof ServletContextPathPermission ))
        {
            return false;
        }
        
        return thisUri.equals(URI.create(permission.getName()));
    }

    @Override
    public boolean equals(Object permission)
    {
        if (permission == this)
        {
            return true;
        }

        if (!(permission instanceof ServletContextPathPermission))
        {
            return false;
        }
        
        ServletContextPathPermission that = (ServletContextPathPermission)permission;
        
        return this.getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return this.getName().hashCode();
    }

    @Override
    public String getActions()
    {
        return null;
    }

}

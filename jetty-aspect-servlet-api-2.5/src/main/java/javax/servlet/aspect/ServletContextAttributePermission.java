package javax.servlet.aspect;
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

import java.security.BasicPermission;
import java.security.Permission;

/**
 * 
 * Permission object for servlet context attributes
 *
 */
public class ServletContextAttributePermission extends BasicPermission
{

    private static final String __PROPERTY_READ_ACTION = "read";
    private static final String __PROPERTY_WRITE_ACTION = "write";
    private static final String __PROPERTY_RW_ACTION = "all";

    /**
     * Read 
     */
    private final static int __READ = 0x1;

    /**
     * Write
     */
    private final static int __WRITE = 0x2;
    
    /**
     * All
     */
    private final static int __ALL = __READ | __WRITE;
    
    /**
     * None
     */
    private final static int __NONE = 0x0;

    /**
     * mask
     * 
     */
    private transient int _mask;

    private String _actions;

    public ServletContextAttributePermission(String name, String actions)
    {
        super(name);
        init(mask(actions));
    }

    private void init(int mask)
    {

        if ((mask & __ALL) != mask)
        {
            throw new IllegalArgumentException("invalid actions mask");
        }

        if (mask == __NONE)
        {
            throw new IllegalArgumentException("invalid actions mask");
        }

        if (getName() == null)
        {
            throw new NullPointerException("name can't be null");
        }

        this._mask = mask;
    }

    public boolean implies(Permission permission)
    {
        if (!(permission instanceof ServletContextAttributePermission))
        {
            return false;
        }

        ServletContextAttributePermission that = (ServletContextAttributePermission)permission;

        return ((this._mask & that._mask) == that._mask) && super.implies(that);
    }

    public boolean equals(Object permission)
    {
        if (permission == this)
        {
            return true;
        }

        if (!(permission instanceof ServletContextAttributePermission))
        {
            return false;
        }

        ServletContextAttributePermission that = (ServletContextAttributePermission)permission;

        return (this._mask == that._mask) && (this.getName().equals(that.getName()));
    }

    public int hashCode()
    {
        return this.getName().hashCode();
    }

    static String getActions(int mask)
    {
        StringBuilder sb = new StringBuilder();
        
        if ((mask & __READ) == __READ)
        {
            sb.append("read");
        }

        if ((mask & __WRITE) == __WRITE)
        {
            if (sb.length() > 0)
            {
                sb.append(',');
            }
            else
            {
                sb.append("write");
            }
        }
        return sb.toString();
    }

    public String getActions()
    {
        if (_actions == null)
        {
            _actions = getActions(this._mask);
        }

        return _actions;
    }

    private static int mask(String actions)
    {

        int mask = __NONE;

        if (actions == null)
        {
            return mask;
        }

        if (actions.equals(__PROPERTY_READ_ACTION))
        {
            return __READ;
        }
        else if (actions.equals(__PROPERTY_WRITE_ACTION))
        {
            return __WRITE;
        }
        else if (actions.equals(__PROPERTY_RW_ACTION))
        {
            return __READ | __WRITE;
        }
        else
        {
            throw new IllegalArgumentException("unknown actions: " + actions);
        }
    }

}

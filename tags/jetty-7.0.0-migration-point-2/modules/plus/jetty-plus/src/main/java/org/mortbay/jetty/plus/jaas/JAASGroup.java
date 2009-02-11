// ========================================================================
// $Id$
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.plus.jaas;

import java.security.Principal;
import java.security.acl.Group;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;


public class JAASGroup implements Group 
{
    public static final String ROLES = "__roles__";
    
    private String _name = null;
    private HashSet _members = null;
    
    
   
    public JAASGroup(String n)
    {
        this._name = n;
        this._members = new HashSet();
    }
   
    /* ------------------------------------------------------------ */
    /**
     *
     * @param principal <description>
     * @return <description>
     */
    public synchronized boolean addMember(Principal principal)
    {
        return _members.add(principal);
    }

    /**
     *
     * @param principal <description>
     * @return <description>
     */
    public synchronized boolean removeMember(Principal principal)
    {
        return _members.remove(principal);
    }

    /**
     *
     * @param principal <description>
     * @return <description>
     */
    public boolean isMember(Principal principal)
    {
        return _members.contains(principal);
    }


    
    /**
     *
     * @return <description>
     */
    public Enumeration members()
    {

        class MembersEnumeration implements Enumeration
        {
            private Iterator itor;
            
            public MembersEnumeration (Iterator itor)
            {
                this.itor = itor;
            }
            
            public boolean hasMoreElements ()
            {
                return this.itor.hasNext();
            }


            public Object nextElement ()
            {
                return this.itor.next();
            }
            
        }

        return new MembersEnumeration (_members.iterator());
    }


    /**
     *
     * @return <description>
     */
    public int hashCode()
    {
        return getName().hashCode();
    }


    
    /**
     *
     * @param object <description>
          * @return <description>
     */
    public boolean equals(Object object)
    {
        if (! (object instanceof JAASGroup))
            return false;

        return ((JAASGroup)object).getName().equals(getName());
    }

    /**
     *
     * @return <description>
     */
    public String toString()
    {
        return getName();
    }

    /**
     *
     * @return <description>
     */
    public String getName()
    {
        
        return _name;
    }

}

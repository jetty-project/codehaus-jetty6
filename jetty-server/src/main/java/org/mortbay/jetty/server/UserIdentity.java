// ========================================================================
// Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.server;
import java.security.Principal;
import java.util.Map;
import javax.security.auth.Subject;

/* ------------------------------------------------------------ */
/** User object that encapsulates user identity and operations such as run-as-role actions, 
 * checking isUserInRole and getUserPrincipal.
 *
 * Implementations of UserIdentity should be immutable so that they may be
 * cached by Authenticators and LoginServices.
 *
 */
public interface UserIdentity
{
    final static String[] NO_ROLES = new String[]{}; 
    
    /* ------------------------------------------------------------ */
    /**
     * @return The user subject
     */
    Subject getSubject();

    /* ------------------------------------------------------------ */
    /**
     * @return The user principal
     */
    Principal getUserPrincipal();

    /* ------------------------------------------------------------ */
    /**
     * @return The users roles
     */
    String[] getRoles();

    /* ------------------------------------------------------------ */
    /** Check if the user is in a role.
     * This call is used to satisfy authorization calls from 
     * container code which will be using translated role names.
     * @param role A role name.
     * @return True if the user can act in that role.
     */
    boolean isUserInRole(String role);

    interface Source
    {
        UserIdentity getUserIdentity();
    }
    

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /**
     * A UserIdentity Scope.
     * A scope is the environment in which a User Identity is to 
     * be interpreted. Typically it is set by the target servlet of 
     * a request.
     * @see org.mortbay.jetty.servlet.ServletHolder
     */
    interface Scope
    {
        /* ------------------------------------------------------------ */
        /**
         * @return The context path that the identity is being considered within
         */
        String getContextPath();
        
        /* ------------------------------------------------------------ */
        /**
         * @return The name of the identity context. Typically this is the servlet name.
         */
        String getName();
        
        /* ------------------------------------------------------------ */
        /**
         * @return The name of a runAs entity. Typically this is a runAs role applied to a servlet.
         */
        String getRunAsRole();
        
        /* ------------------------------------------------------------ */
        /**
         * @return A map of role reference names that converts from names used by application code
         * to names used by the context deployment.
         */
        Map<String,String> getRoleRefMap();
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static final UserIdentity UNAUTHENTICATED_IDENTITY = new UserIdentity()
    {
        public Subject getSubject()
        {
            return null;
        }
        
        public Principal getUserPrincipal()
        {
            return null;
        }
        public String[] getRoles()
        {
            return NO_ROLES;
        }
        public boolean isUserInRole(String role)
        {
            return false;
        }
        
        public String toString()
        {
            return "UNAUTHENTICATED";
        }
    };
}
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

package org.mortbay.jetty.security;
import java.security.Principal;

import org.mortbay.jetty.Request;

/* ------------------------------------------------------------ */
/** User Realm.
 *
 * This interface should be specialized to provide specific user
 * lookup and authentication using arbitrary methods.
 *
 *
 * 
 * @author Greg Wilkins (gregw)
 */
public interface UserRealm
{
    /* ------------------------------------------------------------ */
    /**
     * used by authenticators in challenges
     * @return realm name
     */
    public String getName();

    /* ------------------------------------------------------------ */
    /** Authenticate a users credentials.
     * Implementations of this method may adorn the calling context to
     * assoicate it with the authenticated principal (eg ThreadLocals). If
     * such context associations are made, they should be considered valid
     * until a UserRealm.deAuthenticate(UserPrincipal) call is made for this
     * UserPrincipal.
     * @param username The username. 
     * @param credentials The user credentials, normally a String password. 
     * @param request The request to be authenticated. Additional
     * parameters may be extracted or set on this request as needed
     * for the authentication mechanism (none required for BASIC and
     * FORM authentication).
     * @return The authenticated UserPrincipal.
     */
    //jaspi called only from authenticators and tests
    public Principal authenticate(String username,Object credentials,Request request);

    /* ------------------------------------------------------------ */
    /** Re Authenticate a Principal.
     * Authenicate a principal that has previously been return from the authenticate method.
     * 
     * Implementations of this method may adorn the calling context to
     * assoicate it with the authenticated principal (eg ThreadLocals). If
     * such context associations are made, they should be considered valid
     * until a UserRealm.deAuthenticate(UserPrincipal) call is made for this
     * UserPrincipal.
     *
     * @return True if this user is still authenticated.
     */
    //jaspi called only from FormAuthenticator and subclasses
    public boolean reauthenticate(Principal user);
    
    /* ------------------------------------------------------------ */
    /** Check if the user is in a role. 
     * @param role A role name.
     * @return True if the user can act in that role.
     */
    //jaspi called from Request.isUserInRole and ConstraintSecurityHandler.check
    public boolean isUserInRole(Principal user, String role);
    
    /* ------------------------------------------------------------ */
    /** Dissassociate the calling context with a Principal.
     * This method is called when the calling context is not longer
     * associated with the Principal.  It should be used by an implementation
     * to remove context associations such as ThreadLocals.
     * The UserPrincipal object remains authenticated, as it may be
     * associated with other contexts.
     * @param user A UserPrincipal allocated from this realm.
     */
    //jaspi this appears to be a bug check method that should be removed.
    public void disassociate(Principal user);
    
    /* ------------------------------------------------------------ */
    /** Push role onto a Principal.
     * This method is used to add a role to an existing principal.
     * @param user An existing UserPrincipal or null for an anonymous user.
     * @param role The role to add.
     * @return A new UserPrincipal object that wraps the passed user, but
     * with the added role.
     */
    //jaspi called from ServletHolder.handle, initServlet, doStop and tests
    public Principal pushRole(Principal user, String role);


    /* ------------------------------------------------------------ */
    /** Pop role from a Principal.
     * @param user A UserPrincipal previously returned from pushRole
     * @return The principal without the role.  Most often this will be the
     * original UserPrincipal passed.
     */
    //jaspi called from ServletHolder.handle, initServlet, doStop and tests
    public Principal popRole(Principal user);

    /* ------------------------------------------------------------ */
    /** logout a user Principal.
     * Called by authentication mechanisms (eg FORM) that can detect logout.
     * @param user A Principal previously returned from this realm
     */
    //jaspi called from FormAuthenticator.valueUnbound (when session is unbound)
    public void logout(Principal user);
    
}

package org.mortbay.jetty.security;

import java.security.Principal;
import javax.security.auth.Subject;
import org.mortbay.jetty.server.UserIdentity;

/* ------------------------------------------------------------ */
/**
 * Associates UserIdentities from with threads and UserIdentity.Contexts.
 * 
 */
public interface IdentityService <T extends UserIdentity.Source, U>
{
    /* ------------------------------------------------------------ */
    /**
     * Associate the {@link UserIdentity} and {@link UserIdentity.Scope}
     * with the current thread.
     * @param user The current user.
     * @param context The new scope.
     * @return A scoped {@link UserIdentity}.
     */
    T associate(UserIdentity user, UserIdentity.Scope context);
    
    /* ------------------------------------------------------------ */
    /**
     * Disassociate the current UserIdentity and reinstate the 
     * previousUser identity.
     * @param previous
     */
    void disassociate(T previous);
    
    /* ------------------------------------------------------------ */
    /**
     * Associate a runas Token with the current thread.
     * @param token The runAsToken to associate.
     * @return The previous runAsToken or null.
     */
    U associateRunAs(RunAsToken token);
    
    /* ------------------------------------------------------------ */
    /**
     * Disassociate the current runAsToken from the thread
     * and reassociate the previous token.
     * @param previousToken
     */
    void disassociateRunAs(U previousToken);

    /* ------------------------------------------------------------ */
    /**
     * Create a new UserIdentity for use with this identity service.
     * The UserIdentity should be immutable and able to be cached.
     * 
     * @param subject
     * @param userPrincipal
     * @param roles
     * @return A new immutable UserIdententity
     */
    UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles);

    /* ------------------------------------------------------------ */
    /**
     * Create a new RunAsToken from a runAsName (normally a role).
     * @param runAsName Normally a role name
     * @return A new immutable RunAsToken
     */
    RunAsToken newRunAsToken(String runAsName);

    UserIdentity newSystemUserIdentity();
}

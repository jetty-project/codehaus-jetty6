package org.mortbay.jetty.security;

import java.security.Principal;

import javax.security.auth.Subject;

import org.mortbay.jetty.server.UserIdentity;

/* ------------------------------------------------------------ */
/**
 * Associates UserIdentities from with threads and UserIdentity.Contexts.
 * 
 */
public interface IdentityService <SCOPED extends UserIdentity, RUNAS>
{

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** A scoped UserIdentity.
     * 
     * An interface used to ob
     *
     */
    interface Scoped
    {
        UserIdentity getScopedUserIdentity();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Associate the {@link UserIdentity} and {@link UserIdentity.Scope}
     * with the current thread.
     * @param user The current user.
     * @param context The new scope.
     * @return A scoped {@link UserIdentity}.
     */
    SCOPED associate(UserIdentity user, UserIdentity.Scope context);
    
    /* ------------------------------------------------------------ */
    /**
     * Disassociate the current UserIdentity and reinstate the 
     * previousUser identity.
     * TODO this might not be necessary.  Both existing implementations are no-ops
     * @param scoped SCOPED returned from previous associate call
     */
    void disassociate(SCOPED scoped);
    
    /* ------------------------------------------------------------ */
    /**
     * Associate a runas Token with the current thread.
     * @param token The runAsToken to associate.
     * @return The previous runAsToken or null.
     */
    RUNAS associateRunAs(RunAsToken token);
    
    /* ------------------------------------------------------------ */
    /**
     * Disassociate the current runAsToken from the thread
     * and reassociate the previous token.
     * @param token RUNAS returned from previous associateRunAs call
     */
    void disassociateRunAs(RUNAS token);

    /* ------------------------------------------------------------ */
    /**
     * Create a new UserIdentity for use with this identity service.
     * The UserIdentity should be immutable and able to be cached.
     * 
     * @param subject Subject to include in UserIdentity
     * @param userPrincipal Principal to include in UserIdentity.  This will be returned from getUserPrincipal calls
     * @param roles set of roles to include in UserIdentity.
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

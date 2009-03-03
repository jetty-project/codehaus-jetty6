package org.mortbay.jetty.security;

import java.security.Principal;
import javax.security.auth.Subject;
import org.mortbay.jetty.server.UserIdentity;

/* ------------------------------------------------------------ */
/**
 * Associates UserIdentities from with threads and UserIdentity.Contexts.
 * 
 */
public interface IdentityService
{
    UserIdentity associate(UserIdentity user, UserIdentity.Context context);
    void disassociate(UserIdentity previousUser);
    RunAsToken associateRunAs(RunAsToken token);
    void disassociateRunAs(RunAsToken previousToken);

    UserIdentity newUserIdentity(Subject subject, Principal userPrincipal, String[] roles);

    RunAsToken newRunAsToken(String runAsName);
}

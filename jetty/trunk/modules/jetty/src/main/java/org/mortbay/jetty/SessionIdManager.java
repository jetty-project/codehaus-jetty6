package org.mortbay.jetty;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.component.LifeCycle;

/** Session ID Manager.
 * Manages session IDs across multiple contexts.
 * @author gregw
 *
 */
public interface SessionIdManager extends LifeCycle
{
    /**
     * @param id
     * @return True if the session ID is in use by at least one context.
     */
    public boolean idInUse(String id);
    
    /**
     * Add a session to the list of known sessions for a given ID.
     * @param session The session
     */
    public void addSession(HttpSession session);
    
    /**
     * Remove session from the list of known sessions for a given ID.
     * @param session
     */
    public void removeSession(HttpSession session);
    
    /**
     * Call {@link HttpSession#invalidate()} on all known sessions for the given id.
     * @param id
     */
    public void invalidateAll(String id);
    
    /**
     * @param request
     * @param created
     * @return
     */
    public String newSessionId(HttpServletRequest request,long created);
    
    
    public String getWorkerName();
}
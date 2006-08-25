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
    public boolean idInUse(String id);
    
    public void addSession(HttpSession session);
    
    public void invalidateAll(String id);
    
    public String newSessionId(HttpServletRequest request,long created);
}
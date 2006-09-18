/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2006 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package org.mortbay.jetty.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.framework.TestCase;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.SessionIdManager;

/**
 * @version $Revision$
 */
public class SessionManagerTest extends TestCase
{
    HashSessionManager sessionManager = new HashSessionManager();
    SessionHandler handler = new SessionHandler(sessionManager);
    Server server = new Server();
    
    protected void setUp() throws Exception
    {
        sessionManager.setMetaManager(new TestSessionIdManager());
        sessionManager.setSessionHandler(handler);
        server.setHandler(handler);
        server.start();
    }

    protected void tearDown() throws Exception
    {
        server.stop();
    }

    public void testSetAttributeToNullIsTheSameAsRemoveAttribute() throws Exception
    {
        HttpSession session = sessionManager.newHttpSession(null);

        assertNull(session.getAttribute("foo"));
        assertFalse(session.getAttributeNames().hasMoreElements());
        session.setAttribute("foo", this);
        assertNotNull(session.getAttribute("foo"));
        assertTrue(session.getAttributeNames().hasMoreElements());
        session.removeAttribute("foo");
        assertNull(session.getAttribute("foo"));
        assertFalse(session.getAttributeNames().hasMoreElements());
        session.setAttribute("foo", this);
        assertNotNull(session.getAttribute("foo"));
        assertTrue(session.getAttributeNames().hasMoreElements());
        session.setAttribute("foo", null);
        assertNull(session.getAttribute("foo"));
        assertFalse(session.getAttributeNames().hasMoreElements());
    }
    
    
    class TestSessionIdManager extends AbstractLifeCycle implements SessionIdManager
    {
        public boolean idInUse(String id)
        {
            return false;
        }

        public void addSession(HttpSession session)
        {
            // Ignore
        }

        public void invalidateAll(String id)
        {
            // Ignore
        }

        public String newSessionId(HttpServletRequest request, long created)
        {
            return "xyzzy";
        }

        public void removeSession(HttpSession session)
        {
            // ignore
        }

    }
}

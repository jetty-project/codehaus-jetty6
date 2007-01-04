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
import org.mortbay.jetty.handler.ContextHandler;

/**
 * @version $Revision$
 */
public class SessionManagerTest extends TestCase
{
    TestSessionIdManager idManager = new TestSessionIdManager();
    HashSessionManager sessionManager = new HashSessionManager();
    SessionHandler handler = new SessionHandler(sessionManager);
    Server server = new Server();
    
    protected void setUp() throws Exception
    {
        sessionManager.setIdManager(idManager);
        ContextHandler context=new ContextHandler();
        sessionManager.setSessionHandler(handler);
        server.setHandler(context);
        context.setHandler(handler);
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
    
    public void testWorker() throws Exception
    {
        try
        {
            idManager._worker="node0";
            HttpSession session = sessionManager.newHttpSession(null);
            
            assertTrue(session.getId().endsWith("node0"));
            String id0=session.getId();
            String clusterId=id0.substring(0,id0.lastIndexOf('.'));
            String id1=clusterId+".node1";
            
            assertTrue(sessionManager.getSessionCookie(session,"/context",false)!=null);
            
            assertEquals(session,sessionManager.getHttpSession(session.getId()));
            assertTrue(sessionManager.access(session,false)==null);
            
            assertEquals(session,sessionManager.getHttpSession(id1));
            assertTrue(sessionManager.access(session,false)!=null);
            
        }
        finally
        {
            idManager._worker=null;
        }
    }
    
    
    class TestSessionIdManager extends AbstractLifeCycle implements SessionIdManager
    {
        String _worker;
        
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

        public String getWorkerName()
        {
            return _worker;
        }

    }
}

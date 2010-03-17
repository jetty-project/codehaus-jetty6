// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.terracotta.servlet;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.SessionIdManager;
import org.eclipse.jetty.server.SessionManager;
import org.eclipse.jetty.server.session.AbstractSessionManager;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.eclipse.jetty.server.session.SessionHandler;

/**
 * @version $Revision$ $Date$
 */
public class TerracottaJettyServer extends AbstractTestServer
{

    public TerracottaJettyServer(int port)
    {
        super(port);
    }

    public TerracottaJettyServer(int port, int maxInactivePeriod, int scavengePeriod)
    {
       super(port,maxInactivePeriod,scavengePeriod);
    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractTestServer#newSessionHandler(org.eclipse.jetty.server.SessionManager)
     */
    @Override
    public SessionHandler newSessionHandler(SessionManager sessionManager)
    {
        return new TerracottaSessionHandler(sessionManager);
    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractTestServer#newSessionIdManager()
     */
    @Override
    public SessionIdManager newSessionIdManager()
    {
        TerracottaSessionIdManager idManager = new TerracottaSessionIdManager(_server);     
        idManager.setWorkerName(String.valueOf(System.currentTimeMillis()));
        return idManager;
    }

    /** 
     * @see org.eclipse.jetty.server.session.AbstractTestServer#newSessionManager()
     */
    @Override
    public AbstractSessionManager newSessionManager()
    {
        TerracottaSessionManager manager =  new TerracottaSessionManager();
        manager.setScavengePeriodMs(TimeUnit.SECONDS.toMillis(_scavengePeriod));
        manager.setMaxInactiveInterval(_maxInactivePeriod);
        return manager;
    }

    
    public static class TestTerracottaSessionManager extends TerracottaSessionManager
    {
        private static final ThreadLocal<Integer> depth = new ThreadLocal<Integer>()
        {
            @Override
            protected Integer initialValue()
            {
                return 0;
            }
        };

        @Override
        public void enter(Request request)
        {
            depth.set(depth.get() + 1);
            super.enter(request);
        }

        @Override
        public void exit(Request request)
        {
            super.exit(request);
            depth.set(depth.get() - 1);
            if (depth.get() == 0)
            {
                assert Lock.getLocks().size() == 0 : Lock.getLocks();
            }
        }
    }

}

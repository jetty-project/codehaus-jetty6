// ========================================================================
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.integration.session;

import org.eclipse.jetty.server.session.JDBCSessionIdManager;
import org.eclipse.jetty.server.session.JDBCSessionManager;
import org.eclipse.jetty.servlet.AbstractSessionTest;
import org.eclipse.jetty.servlet.SessionTestServer;

public class JDBCSessionServerTest extends AbstractSessionTest
{
	
    JDBCSessionServer _serverA;
    JDBCSessionServer _serverB;
    
    
    public class JDBCSessionServer extends SessionTestServer
    {
        public JDBCSessionServer (int port, String workerName)
        {
            super(port, workerName);
        }

        public void configureEnvironment()
        {
           System.setProperty("derby.system.home", System.getProperty("basedir") + "/target/test-db");
        }

        public void configureIdManager()
        {
           JDBCSessionIdManager idMgr = new JDBCSessionIdManager(this);
           idMgr.setWorkerName(_workerName);
           idMgr.setDriverInfo("org.apache.derby.jdbc.EmbeddedDriver", "jdbc:derby:stest;create=true");
           _sessionIdMgr = idMgr;
        }

        public void configureSessionManager1()
        {
           JDBCSessionManager mgr1 = new JDBCSessionManager();
           mgr1.setSaveInterval(10);
           _sessionMgr1 = mgr1;
        }

        public void configureSessionManager2()
        {
            JDBCSessionManager mgr2 = new JDBCSessionManager();
            mgr2.setSaveInterval(10);
            _sessionMgr2 = mgr2;
        }
    }
    
    public SessionTestServer newServer1 ()
    {
        return new JDBCSessionServer (Integer.parseInt(__port1), "duke");
    }
    
    public SessionTestServer newServer2 ()
    {
        return new JDBCSessionServer (Integer.parseInt(__port2), "daisy");
    }
    
}

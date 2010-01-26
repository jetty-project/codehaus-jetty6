// ========================================================================
// Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

import org.eclipse.jetty.server.session.AbstractOrphanedSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.testng.annotations.Test;

/**
 * @version $Revision$ $Date$
 */
public class OrphanedSessionTest extends AbstractOrphanedSessionTest
{
    /**
     * If nodeA creates a session, and just afterwards crashes, it is the only node that knows about the session.
     * Its session data will remain forever in the Terracotta server, but it will never be expired because
     * other nodes are not aware of that session if they never get hit by its session id.
     * We want to test that the session data is gone after scavenging.
     */
    @Test(groups={"tc-all"})
    public void testOrphanedSession() throws Exception
    {
        super.testOrphanedSession();
    }

    @Override
    public AbstractTestServer createServer(int port, int max, int scavenge)
    {
       return new TerracottaJettyServer(port,max,scavenge);
    }

}

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

import org.eclipse.jetty.server.session.AbstractSessionMigrationTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.testng.annotations.Test;

/**
 * @version $Revision$ $Date$
 */
public class SessionMigrationTest extends AbstractSessionMigrationTest
{
    @Test(groups={"tc-all"})
    public void testSessionMigration() throws Exception
    {
      super.testSessionMigration();
    }

    @Override
    public AbstractTestServer createServer(int port)
    {
        return new TerracottaJettyServer(port);
    }
}

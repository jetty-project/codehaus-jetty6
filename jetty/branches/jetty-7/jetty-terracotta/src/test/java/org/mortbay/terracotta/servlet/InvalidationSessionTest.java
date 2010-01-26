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

import org.eclipse.jetty.server.session.AbstractInvalidationSessionTest;
import org.eclipse.jetty.server.session.AbstractTestServer;
import org.testng.annotations.Test;

/**
 * Goal of the test is to be sure that invalidating a session on one node
 * result in the session being unavailable in the other node also.
 * @version $Revision$ $Date$
 */
public class InvalidationSessionTest extends AbstractInvalidationSessionTest
{

    @Override
    public AbstractTestServer createServer(int arg0)
    {
        return new TerracottaJettyServer(arg0);
    }

    
    @Test(groups={"tc-all"})
    public void testInvalidation() throws Exception
    {
       super.testInvalidation();
    }

    @Override
    public void pause()
    {
        
    }

}

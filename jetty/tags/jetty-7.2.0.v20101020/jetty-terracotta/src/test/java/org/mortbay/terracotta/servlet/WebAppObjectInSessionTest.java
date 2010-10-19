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


/**
 * Target of this test is to check that when a webapp on nodeA puts in the session
 * an object of a class loaded from the war (and hence with a WebAppClassLoader),
 * the same webapp on nodeB is able to load that object from the session.
 * This is only possible if the NamedClassLoader mechanism in Terracotta is setup
 * appropriately and it is working correctly, which the scope of this test.
 *
 * @version $Revision$ $Date$
 */
public class WebAppObjectInSessionTest //extends AbstractWebAppObjectInSessionTest
{
  

    // TODO: Restore this test once the new Terracotta TIM module has been updated to work with our implementation
    // TODO: The Terracotta TIM module must be referenced from the tc-config.xml file.
//    @Test(groups={"tc-all"})
    public void testWebappObjectInSession() throws Exception
    {
       //super.testWebappObjectInSession();
    }
}

// ========================================================================
// $Id: IOTest.java,v 1.1 2005/10/05 14:09:25 janb Exp $
// Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import junit.framework.TestSuite;


/* ------------------------------------------------------------ */
/** Util meta Tests.
 * @version $Id: IOTest.java,v 1.1 2005/10/05 14:09:25 janb Exp $
 * @author Greg Wilkins (gregw)
 */
public class IOTest extends junit.framework.TestCase
{
    public IOTest(String name)
    {
      super(name);
    }
    
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(IOTest.class);
        return suite;                  
    }

    /* ------------------------------------------------------------ */
    /** main.
     */
    public static void main(String[] args)
    {
      junit.textui.TestRunner.run(suite());
    }    
    
    

    /* ------------------------------------------------------------ */
    public void testIO() throws InterruptedException
    {
        // Only a little test
        ByteArrayInputStream in = new ByteArrayInputStream
            ("The quick brown fox jumped over the lazy dog".getBytes());
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        IO.copyThread(in,out);
        Thread.sleep(500);

        assertEquals( "copyThread",
                      out.toString(),
                      "The quick brown fox jumped over the lazy dog");
    }

}

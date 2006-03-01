// ========================================================================
// $Id$
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;

import junit.framework.TestCase;

/**
 * TestEnvEntry
 *
 *
 */
public class TestNamingEntries extends TestCase
{
    class SomeObject
    {
        private int value;
        public SomeObject (int value)
        {this.value = value;}
        
        public int getValue ()
        {
            return this.value;
        }
    }
    
    
    public SomeObject someObject;
    
    public void setUp ()
    {
        this.someObject = new SomeObject(4);
    }

    public void testEnvEntry ()
    throws Exception
    {
        InitialContext icontext = new InitialContext();
        
        //override webxml
        EnvEntry ee = new EnvEntry ("nameA", someObject, true);
        assertNotNull(EnvEntry.getEnvEntry("nameA"));
        assertTrue(EnvEntry.getEnvEntry("nameA") instanceof EnvEntry);
        Object x = icontext.lookup("nameA");
        assertNotNull(x);
        assertEquals(x, someObject);
    }
    
    public void testResource ()
    throws Exception
    {
        InitialContext icontext = new InitialContext();
 
        Resource resource = new Resource ("resourceA", someObject);
        assertNotNull(Resource.getResource("resourceA"));
        assertTrue(Resource.getResource("resourceA") instanceof Resource);
        assertEquals(icontext.lookup("resourceA"), someObject);
    }
    
}

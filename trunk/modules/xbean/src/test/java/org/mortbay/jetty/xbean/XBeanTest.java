/**
 *
 * Copyright 2005-2006 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mortbay.jetty.xbean;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.xbean.spring.context.ResourceXmlApplicationContext;
import org.mortbay.jetty.Server;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.UrlResource;

public class XBeanTest extends TestCase {

    protected AbstractApplicationContext context;

    public void testUsingXBeanXmlConfig() throws Exception {
        URL url = getClass().getClassLoader().getResource("org/mortbay/jetty/xbean/xbean.xml");
        assertNotNull("Could not find xbean.xml on the classpath!", url);
        
        context = new ResourceXmlApplicationContext(new UrlResource(url)); 
        String[] names = context.getBeanNamesForType(Server.class);
        assertEquals("Should have the name of a Jetty server", 1, names.length);
        Server server = (Server) context.getBean(names[0]);
        assertNotNull("Should have a Jetty Server", server);
    }

    protected void tearDown() throws Exception {
        if (context != null) {
            context.destroy();
        }
    }

}

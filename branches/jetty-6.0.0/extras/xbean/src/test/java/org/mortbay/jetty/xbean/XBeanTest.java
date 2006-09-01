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
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.webapp.WebAppContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.UrlResource;

public class XBeanTest extends TestCase {

    protected AbstractApplicationContext context;

    public void testUsingXBeanXmlConfig() throws Exception {
        System.setProperty("DEBUG", "false");
        URL url = getClass().getClassLoader().getResource("org/mortbay/jetty/xbean/xbean.xml");
        assertNotNull("Could not find xbean.xml on the classpath!", url);
        
        context = new ResourceXmlApplicationContext(new UrlResource(url)); 
        String[] names = context.getBeanNamesForType(Server.class);
        assertEquals("Should have the name of a Jetty server", 1, names.length);
        Server server = (Server) context.getBean(names[0]);
        assertNotNull("Should have a Jetty Server", server);
        
        HandlerCollection hcollection = (HandlerCollection) server.getChildHandlerByClass(HandlerCollection.class);
        assertNotNull("Should have a HandlerCollection", hcollection);
        assertNotNull("HandlerCollection should contain handlers", hcollection.getHandlers());
        Handler[] handlers = hcollection.getHandlers();
        assertEquals("Should be 3 handlers", 3,handlers.length);
        assertTrue("First handler should be a ContextHandlerCollection", handlers[0] instanceof ContextHandlerCollection);
        Handler[] webapps = ((ContextHandlerCollection)handlers[0]).getChildHandlers();
        assertNotNull("Should be at least one webapp", webapps);
        assertTrue("Should be an instance of WebAppContext", webapps[0] instanceof WebAppContext);
    }

    protected void tearDown() throws Exception {
        if (context != null) {
            context.destroy();
        }
    }

}

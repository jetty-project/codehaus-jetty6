// ========================================================================
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

package org.mortbay.jetty;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.servlet.ServletHandler;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.ServletMapping;
import org.mortbay.util.IO;

/**
 * @version $Revision$
 */
public class ServerTest extends TestCase
{
    /**
     * JETTY-87, adding a handler to a server without any handlers should not
     * throw an exception
     */
    public void testAddHandlerToEmptyServer()
    {
        Server server=new Server();
        DefaultHandler handler=new DefaultHandler();
        try
        {
            server.addHandler(handler);
        }
        catch (Exception e)
        {
            fail("Adding handler "+handler+" to server "+server+" threw exception "+e);
        }
    }

    public void testServerWithPort()
    {
        int port=new Random().nextInt(20000)+10000;
        Server server=new Server(port);
        assertEquals(port,server.getConnectors()[0].getPort());
    }
}

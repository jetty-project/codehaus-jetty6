/*
 * Copyright 2009-2009 Webtide LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mortbay.jetty.rhttp.client;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.log.Log;


/**
 * @version $Revision$ $Date$
 */
public abstract class ClientTest extends TestCase
{
    protected abstract RHTTPClient createClient(int port, String targetId) throws Exception;

    protected abstract void destroyClient(RHTTPClient client) throws Exception;

    public void testConnectNoServer() throws Exception
    {
        RHTTPClient client = createClient(8080, "test1");
        try
        {
            client.connect();
            fail();
        }
        catch (IOException x)
        {
        }
        finally
        {
            destroyClient(client);
        }
    }

    public void testServerExceptionOnHandshake() throws Exception
    {
        final CountDownLatch serverLatch = new CountDownLatch(1);

        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException
            {
                request.setHandled(true);
                if (target.endsWith("/handshake"))
                {
                    serverLatch.countDown();
                    throw new TestException();
                }
            }
        });
        server.start();
        try
        {
            RHTTPClient client = createClient(connector.getLocalPort(), "test2");
            try
            {
                try
                {
                    client.connect();
                    fail();
                }
                catch (IOException x)
                {
                }

                assertTrue(serverLatch.await(1000, TimeUnit.MILLISECONDS));
            }
            finally
            {
                destroyClient(client);
            }
        }
        finally
        {
            server.stop();
        }
    }

    public void testServerExceptionOnConnect() throws Exception
    {
        final CountDownLatch serverLatch = new CountDownLatch(1);

        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException
            {
                request.setHandled(true);
                if (target.endsWith("/connect"))
                {
                    serverLatch.countDown();
                    throw new TestException();
                }
            }
        });
        server.start();
        try
        {
            RHTTPClient client = createClient(connector.getLocalPort(), "test3");
            try
            {
                final CountDownLatch connectLatch = new CountDownLatch(1);
                client.addClientListener(new ClientListener.Adapter()
                {
                    @Override
                    public void connectException()
                    {
                        connectLatch.countDown();
                    }
                });
                client.connect();

                assertTrue(serverLatch.await(1000, TimeUnit.MILLISECONDS));
                assertTrue(connectLatch.await(1000, TimeUnit.MILLISECONDS));
            }
            finally
            {
                destroyClient(client);
            }
        }
        finally
        {
            server.stop();
        }
    }

    public void testServerExceptionOnDeliver() throws Exception
    {
        final CountDownLatch serverLatch = new CountDownLatch(1);

        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException
            {
                request.setHandled(true);
                if (target.endsWith("/connect"))
                {
                    serverLatch.countDown();
                    try
                    {
                        // Simulate a long poll timeout
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException x)
                    {
                        Thread.currentThread().interrupt();
                    }
                }
                else if (target.endsWith("/deliver"))
                {
                    // Throw an exception on deliver
                    throw new TestException();
                }
            }
        });
        server.start();
        try
        {
            RHTTPClient client = createClient(connector.getLocalPort(), "test4");
            try
            {
                final CountDownLatch deliverLatch = new CountDownLatch(1);
                client.addClientListener(new ClientListener.Adapter()
                {
                    @Override
                    public void deliverException(RHTTPResponse response)
                    {
                        deliverLatch.countDown();
                    }
                });
                client.connect();

                assertTrue(serverLatch.await(1000, TimeUnit.MILLISECONDS));

                client.deliver(new RHTTPResponse(1, 200, "OK", new LinkedHashMap<String, String>(), new byte[0]));

                assertTrue(deliverLatch.await(1000, TimeUnit.MILLISECONDS));
            }
            finally
            {
                destroyClient(client);
            }
        }
        finally
        {
            server.stop();
        }
    }

    public void testServerShutdownAfterConnect() throws Exception
    {
        final CountDownLatch connectLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(1);

        Server server = new Server();
        Connector connector = new SocketConnector();
        server.addConnector(connector);
        server.setHandler(new AbstractHandler()
        {
            public void handle(String target, Request request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) throws IOException, ServletException
            {
                request.setHandled(true);
                if (target.endsWith("/connect"))
                {
                    connectLatch.countDown();
                    try
                    {
                        Thread.sleep(10000);
                    }
                    catch (InterruptedException e)
                    {
                        stopLatch.countDown();
                    }
                }
            }
        });
        server.start();
        try
        {
            RHTTPClient client = createClient(connector.getLocalPort(), "test5");
            try
            {
                final CountDownLatch serverLatch = new CountDownLatch(1);
                client.addClientListener(new ClientListener.Adapter()
                {
                    @Override
                    public void connectClosed()
                    {
                        serverLatch.countDown();
                    }
                });
                client.connect();

                assertTrue(connectLatch.await(2000, TimeUnit.MILLISECONDS));

                server.stop();
                assertTrue(stopLatch.await(2000, TimeUnit.MILLISECONDS));

                assertTrue(serverLatch.await(2000, TimeUnit.MILLISECONDS));
            }
            finally
            {
                destroyClient(client);
            }
        }
        finally
        {
            server.stop();
        }
    }
    
    public static class TestException extends NullPointerException
    {
        
    }
}

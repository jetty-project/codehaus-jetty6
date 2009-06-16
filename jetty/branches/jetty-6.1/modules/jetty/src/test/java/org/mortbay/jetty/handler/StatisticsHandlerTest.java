// ========================================================================
// Copyright 2009-2009 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.handler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.util.IO;

/**
 * @version $Revision$ $Date$
 */
public class StatisticsHandlerTest extends TestCase
{
    public void testSynchronizedStatisticsHandler() throws Exception
    {
        SynchronizedStatisticsHandler statisticsHandler = new SynchronizedStatisticsHandler();
        runTestWithStatisticsHandler(statisticsHandler);
    }

    private void runTestWithStatisticsHandler(AbstractStatisticsHandler statisticsHandler) throws Exception
    {
        Server server = new Server();
        Connector connector = new SelectChannelConnector();
        server.setConnectors(new Connector[]{connector});
        server.setHandler(statisticsHandler);
        statisticsHandler.setHandler(new TestHandler());
        server.start();
        int serverPort = connector.getLocalPort();
        try
        {
            int requestCount = 100;
            Worker[] workers = new Worker[50];
            for (int i = 0; i < workers.length; ++i) workers[i] = new Worker(i, serverPort, requestCount);
            for (int i = 0; i < workers.length; ++i) workers[i].start();
            for (int i = 0; i < workers.length; ++i) workers[i].join();

            assertEquals(workers.length * requestCount, statisticsHandler.getRequests());
            assertEquals(workers.length * requestCount, statisticsHandler.getResponses2xx());
            assertTrue(statisticsHandler.getRequestTimeMin() > 0);
            assertTrue(statisticsHandler.getRequestTimeMax() > 0);
            assertTrue(statisticsHandler.getRequestTimeTotal() > 0);
            assertTrue(statisticsHandler.getRequestTimeAverage() > 0);
        }
        finally
        {
            server.stop();
        }
    }

    private static class Worker extends Thread
    {
        private final int id;
        private final int port;
        private final int requestCount;

        private Worker(int id, int port, int requestCount)
        {
            this.id = id;
            this.port = port;
            this.requestCount = requestCount;
        }

        public void run()
        {
            for (int i = 0; i < requestCount; ++i)
            {
                try
                {
                    Socket socket = new Socket(InetAddress.getByName(null), port);
                    String request = "GET /?id=" + id + " HTTP/1.1\r\n" + "Host: localhost\r\nConnection: close\r\n\r\n";
                    OutputStream output = socket.getOutputStream();
                    output.write(request.getBytes("UTF-8"));
                    output.flush();
                    String response = IO.toString(socket.getInputStream());
                    socket.close();
                }
                catch (Exception x)
                {
                    x.printStackTrace();
                    break;
                }
            }
        }
    }

    class TestHandler extends AbstractHandler
    {
        public void handle(String target, HttpServletRequest servletRequest, HttpServletResponse servletResponse, int dispatch) throws IOException, ServletException
        {
            Request request = (Request)servletRequest;
            request.setHandled(true);
            servletResponse.setStatus(HttpServletResponse.SC_OK);
            try
            {
                long sleep = (long) (Math.random() * 100);
                Thread.sleep(sleep);
            }
            catch (InterruptedException x)
            {
                Thread.currentThread().interrupt();
                throw new ServletException(x);
            }
        }
    }
}

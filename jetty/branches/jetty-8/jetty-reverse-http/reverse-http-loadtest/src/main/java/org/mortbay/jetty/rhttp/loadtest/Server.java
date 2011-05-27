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

package org.mortbay.jetty.rhttp.loadtest;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.mortbay.jetty.rhttp.server.GatewayServer;
import org.mortbay.jetty.rhttp.server.StandardTargetIdRetriever;

/**
 * @version $Revision$ $Date$
 */
public class Server
{
    public static void main(String[] args) throws Exception
    {
        int port = 8080;
        if (args.length > 0)
            port = Integer.parseInt(args[0]);

        GatewayServer server = new GatewayServer();
        Connector connector = new SelectChannelConnector();
        connector.setLowResourceMaxIdleTime(connector.getMaxIdleTime());
        connector.setPort(port);
        server.addConnector(connector);
        server.setTargetIdRetriever(new StandardTargetIdRetriever());
        server.start();

        Runtime.getRuntime().addShutdownHook(new Shutdown(server));
    }

    private static class Shutdown extends Thread
    {
        private final GatewayServer server;

        public Shutdown(GatewayServer server)
        {
            this.server = server;
        }

        @Override
        public void run()
        {
            try
            {
                server.stop();
            }
            catch (Exception ignored)
            {
            }
        }
    }
}

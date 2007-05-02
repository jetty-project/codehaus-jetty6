//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.example;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.NCSARequestLog;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandlerCollection;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.handler.RequestLogHandler;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.UserRealm;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.thread.BoundedThreadPool;

public class LikeJettyXml
{
    public static void main(String[] args)
        throws Exception
    {
        Server server = new Server();
        
        BoundedThreadPool threadPool = new BoundedThreadPool();
        threadPool.setMaxThreads(100);
        server.setThreadPool(threadPool);
             
        Connector connector=new SelectChannelConnector();
        connector.setPort(8080);
        server.setConnectors(new Connector[]{connector});
        
        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]{contexts,new DefaultHandler(),requestLogHandler});
        server.setHandler(handlers);
        
        // TODO add javadoc context to contexts
        
        WebAppContext.addWebApplications(server, "./webapps", "org/mortbay/jetty/webapp/webdefault.xml", true, false);
        
        HashUserRealm userRealm = new HashUserRealm();
        userRealm.setName("Test Realm");
        userRealm.setConfig("./etc/realm.properties");
        server.setUserRealms(new UserRealm[]{userRealm});
        
        NCSARequestLog requestLog = new NCSARequestLog("./logs/jetty-yyyy-mm-dd.log");
        requestLog.setExtended(false);
        requestLogHandler.setRequestLog(requestLog);
        
        server.setStopAtShutdown(true);
        server.setSendServerVersion(true);
        
        server.start();
        server.join();
    }
    
}

// ========================================================================
// Copyright (c) 2009-2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
// The Eclipse Public License is available at 
// http://www.eclipse.org/legal/epl-v10.html
// The Apache License v2.0 is available at
// http://www.opensource.org/licenses/apache2.0.php
// You may elect to redistribute this code under either of these licenses. 
// ========================================================================

package org.mortbay.jetty.monitor;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.deploy.ContextDeployer;
import org.eclipse.jetty.deploy.WebAppDeployer;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.jmx.MBeanContainer;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.NCSARequestLog;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/* ------------------------------------------------------------ */
/**
 */
public class XmlConfigTest
{
    private Server _server;
    private String _requestUrl;
    
    @Before
    public void setUp()
        throws Exception
    {
        startServer();

        Resource configRes = Resource.newClassPathResource("/org/mortbay/jetty/monitor/jetty-monitor-test.xml");
        XmlConfiguration xmlConfig = new XmlConfiguration(configRes.getURL());
        xmlConfig.configure();

        int port = _server.getConnectors()[0].getLocalPort();
        _requestUrl = "http://localhost:"+port+ "/d.txt";
    }
    
    @After
    public void tearDown()
        throws Exception
    {
        stopServer();   
    }
       
    @Test
    public void testThreadPoolMXBean()
        throws Exception
    {
        final int threadCount = 100;
        final long requestCount = 100;
        final String requestUrl = _requestUrl;
        final CountDownLatch gate = new CountDownLatch(threadCount);
        ThreadPool worker = new ExecutorThreadPool(threadCount,threadCount,60,TimeUnit.SECONDS);
        for (int idx=0; idx < threadCount; idx++)
        {
            worker.dispatch(new Runnable() {
                        public void run()
                        {
                            runTest(requestUrl, requestCount);
                            gate.countDown();
                        }
                    });
            Thread.sleep(100);
         }
        gate.await();
        assertTrue(true);
    }
     
    protected static void runTest(String requestUrl, long count)
    {
        HttpClient client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        try
        {
            client.start();
        }
        catch (Exception ex)
        {
            Log.debug(ex);
        }
        
        if (client != null)
        {
            Random rnd = new Random();
            for (long cnt=0; cnt < count; cnt++)
            {
                try
                {
                    ContentExchange getExchange = new ContentExchange();
                    getExchange.setURL(requestUrl);
                    getExchange.setMethod(HttpMethods.GET);
                    
                    client.send(getExchange);
                    int state = getExchange.waitForDone();
                    
                    String content = "";
                    int responseStatus = getExchange.getResponseStatus();
                    if (responseStatus == HttpStatus.OK_200)
                    {
                        content = getExchange.getResponseContent();
                    }           
                    
                    Thread.sleep(100);
                }
                catch (InterruptedException ex)
                {
                    break;
                }
                catch (IOException ex)
                {
                    Log.debug(ex);
                }
            }

            try
            {
                client.stop();
            }
            catch (Exception ex)
            {
                Log.debug(ex);
            }
        }
    }

    public void startServer()
        throws Exception
    {
        String jetty_home = System.getProperty("jetty.home","./target/jetty-distribution-7.4.1-SNAPSHOT");
        System.setProperty("jetty.home",jetty_home);

        _server = new Server();
        
        // Setup JMX
        MBeanContainer mbContainer=new MBeanContainer(ManagementFactory.getPlatformMBeanServer());
        _server.getContainer().addEventListener(mbContainer);
        _server.addBean(mbContainer);
        mbContainer.addBean(Log.getLog());
        
        // Setup Threadpool
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setMinThreads(2);
        threadPool.setMaxThreads(10);
        _server.setThreadPool(threadPool);

        // Setup Connectors
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setMaxIdleTime(30000);
        _server.setConnectors(new Connector[]
        { connector });

        HandlerCollection handlers = new HandlerCollection();
        ContextHandlerCollection contexts = new ContextHandlerCollection();
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        handlers.setHandlers(new Handler[]
        { contexts, new DefaultHandler(), requestLogHandler });
        _server.setHandler(handlers);
        
        // Setup deployers       
        ContextDeployer deployer0 = new ContextDeployer();
        deployer0.setContexts(contexts);
        deployer0.setConfigurationDir(jetty_home + "/contexts");
        deployer0.setScanInterval(1);
        _server.addBean(deployer0);

        WebAppDeployer deployer1 = new WebAppDeployer();
        deployer1.setContexts(contexts);
        deployer1.setWebAppDir(jetty_home + "/webapps");
        deployer1.setParentLoaderPriority(false);
        deployer1.setExtract(true);
        deployer1.setAllowDuplicates(false);
        deployer1.setDefaultsDescriptor(jetty_home + "/etc/webdefault.xml");
        _server.addBean(deployer1);

        HashLoginService login = new HashLoginService();
        login.setName("Test Realm");
        login.setConfig(jetty_home + "/etc/realm.properties");
        _server.addBean(login);

        NCSARequestLog requestLog = new NCSARequestLog(jetty_home + "/logs/jetty-yyyy_mm_dd.log");
        requestLog.setExtended(false);
        requestLogHandler.setRequestLog(requestLog);

        _server.setStopAtShutdown(true);
        _server.setSendServerVersion(true);

        _server.start();
    }

    public void stopServer()
        throws Exception
    {
        if (_server != null)
        {
            _server.stop();
            _server = null;
        }       
    }
}

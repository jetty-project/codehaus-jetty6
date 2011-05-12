package org.mortbay.jetty.tests.distribution.jmx;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.eclipse.jetty.toolchain.jmx.JmxServiceConnection;
import org.eclipse.jetty.toolchain.test.SimpleRequest;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.tests.distribution.JettyProcess;

/**
 * Test Jetty with one webapp.
 */
public class JmxIntegrationTest
{
    private static JettyProcess jetty;
    private static JmxServiceConnection jmxConnect; 
    private static MBeanServerConnection mbeanConnect; 

    @BeforeClass
    public static void initJetty() throws Exception
    {
        jetty = new JettyProcess(JmxIntegrationTest.class);

        jetty.delete("contexts/javadoc.xml");
        
        jetty.copyTestWar("test-war-dump.war");

        jetty.overlayConfig("jmx");
        
        jetty.start();

        jmxConnect = new JmxServiceConnection(jetty.getJmxUrl());
        jmxConnect.connect();
        
        mbeanConnect = jmxConnect.getConnection();
    }

    @AfterClass
    public static void shutdownJetty() throws Exception
    {
        if (jmxConnect != null)
        {
            jmxConnect.disconnect();
        }

        if (jetty != null)
        {
            jetty.stop();
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeployMgr() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.deploy:type=deploymentmanager,id=0");
        
        ObjectName[] appProviders = (ObjectName[])mbeanConnect.getAttribute(objName, "appProviders");
        assertTrue(appProviders !=  null && appProviders.length > 0);

        ArrayList apps = (ArrayList)mbeanConnect.getAttribute(objName, "apps");
        assertTrue(apps != null && apps.size() > 0);

        ObjectName[] contexts = (ObjectName[])mbeanConnect.getAttribute(objName, "contexts");
        assertTrue(contexts !=  null && contexts.length > 0);
        
        ArrayList nodes = (ArrayList)mbeanConnect.getAttribute(objName, "nodes");
        assertTrue(nodes != null && nodes.size() > 0);
    }
    
    @Test
    public void testContextProvider() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.deploy.providers:type=contextprovider,id=0");
        
        String monitoredDirName = (String)mbeanConnect.getAttribute(objName, "monitoredDirName");
        assertTrue(monitoredDirName !=  null && monitoredDirName.length() > 0);

        Object recursive = mbeanConnect.getAttribute(objName, "recursive");
        assertTrue(recursive != null && recursive instanceof Boolean);
        
        Object scanInterval = mbeanConnect.getAttribute(objName, "scanInterval");
        assertTrue(scanInterval != null && scanInterval instanceof Integer);        
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testWebappProvider() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.deploy.providers:type=webappprovider,id=0");
        
        String[] configurationClasses = (String[])mbeanConnect.getAttribute(objName, "configurationClasses");
        // Exception would be thrown if there's a problem

        String contextXmlDir = (String)mbeanConnect.getAttribute(objName, "contextXmlDir");
        assertTrue(contextXmlDir !=  null && contextXmlDir.length() > 0);

        String defaultsDescriptor = (String)mbeanConnect.getAttribute(objName, "defaultsDescriptor");
        assertTrue(defaultsDescriptor !=  null && defaultsDescriptor.length() > 0);

        Object extractWars = mbeanConnect.getAttribute(objName, "extractWars");
        assertTrue(extractWars != null && extractWars instanceof Boolean);
        
        String monitoredDirName = (String)mbeanConnect.getAttribute(objName, "monitoredDirName");
        assertTrue(monitoredDirName !=  null && monitoredDirName.length() > 0);

        Object parentLoaderPriority = mbeanConnect.getAttribute(objName, "parentLoaderPriority");
        assertTrue(parentLoaderPriority != null && parentLoaderPriority instanceof Boolean);
        
        Object recursive = mbeanConnect.getAttribute(objName, "recursive");
        assertTrue(recursive != null && recursive instanceof Boolean);
        
        Object scanInterval = mbeanConnect.getAttribute(objName, "scanInterval");
        assertTrue(scanInterval != null && scanInterval instanceof Integer);        

        String tempDir = (String)mbeanConnect.getAttribute(objName, "tempDir");
        // Exception would be thrown if there's a problem
    }
    
    @Test
    public void testServer() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.server:type=server,id=0");
        
        ObjectName[] childHandlers = (ObjectName[])mbeanConnect.getAttribute(objName, "childHandlers");
        assertTrue(childHandlers !=  null && childHandlers.length > 0);

        ObjectName[] connectors = (ObjectName[])mbeanConnect.getAttribute(objName, "connectors");
        assertTrue(connectors !=  null && connectors.length > 0);

        ObjectName[] contexts = (ObjectName[])mbeanConnect.getAttribute(objName, "contexts");
        assertTrue(contexts !=  null && contexts.length > 0);

        Object dumpAfterStart = mbeanConnect.getAttribute(objName, "dumpAfterStart");
        assertTrue(dumpAfterStart != null && dumpAfterStart instanceof Boolean);
        
        Object dumpBeforeStop = mbeanConnect.getAttribute(objName, "dumpBeforeStop");
        assertTrue(dumpBeforeStop != null && dumpBeforeStop instanceof Boolean);
        
        ObjectName handler = (ObjectName)mbeanConnect.getAttribute(objName, "handler");
        assertTrue(handler !=  null);

        ObjectName[] handlers = (ObjectName[])mbeanConnect.getAttribute(objName, "handlers");
        assertTrue(handlers !=  null && handlers.length > 0);

        Object sendServerVersion = mbeanConnect.getAttribute(objName, "sendServerVersion");
        assertTrue(sendServerVersion != null && sendServerVersion instanceof Boolean);
        
        ObjectName server = (ObjectName)mbeanConnect.getAttribute(objName, "server");
        assertTrue(server !=  null);

        Object startupTime = mbeanConnect.getAttribute(objName, "startupTime");
        assertTrue(startupTime != null && startupTime instanceof Long);
        
        ObjectName threadPool = (ObjectName)mbeanConnect.getAttribute(objName, "threadPool");
        assertTrue(threadPool !=  null);

        String version = (String)mbeanConnect.getAttribute(objName, "version");
        assertTrue(version !=  null && version.length() > 0);

        Object starting = mbeanConnect.getAttribute(objName, "starting");
        assertTrue(starting != null && starting instanceof Boolean);
        
        Object started = mbeanConnect.getAttribute(objName, "started");
        assertTrue(started != null && started instanceof Boolean);
        
        Object running = mbeanConnect.getAttribute(objName, "running");
        assertTrue(running != null && running instanceof Boolean);
        
        Object stopping = mbeanConnect.getAttribute(objName, "stopping");
        assertTrue(stopping != null && stopping instanceof Boolean);
        
        Object stopped = mbeanConnect.getAttribute(objName, "stopped");
        assertTrue(stopped != null && stopped instanceof Boolean);
        
        Object failed = mbeanConnect.getAttribute(objName, "failed");
        assertTrue(failed != null && failed instanceof Boolean);
    }

    @Test
    public void testHandlerCollection() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.server.handler:type=contexthandlercollection,id=0");
        
        ObjectName[] childHandlers = (ObjectName[])mbeanConnect.getAttribute(objName, "childHandlers");
        assertTrue(childHandlers !=  null);
        
        ObjectName[] handlers = (ObjectName[])mbeanConnect.getAttribute(objName, "handlers");
        assertTrue(handlers !=  null && handlers.length > 0);

        ObjectName server = (ObjectName)mbeanConnect.getAttribute(objName, "server");
        assertTrue(server !=  null);
    }

    @Test
    @SuppressWarnings("unused")
    public void testConnector() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.server.nio:type=selectchannelconnector,id=0");
        
        Object acceptQueueSize = mbeanConnect.getAttribute(objName, "acceptQueueSize");
        assertTrue(acceptQueueSize != null && acceptQueueSize instanceof Integer);
        
        Object acceptorPriorityOffset = mbeanConnect.getAttribute(objName, "acceptorPriorityOffset");
        assertTrue(acceptorPriorityOffset != null && acceptorPriorityOffset instanceof Integer);
        
        Object acceptors = mbeanConnect.getAttribute(objName, "acceptors");
        assertTrue(acceptors != null && acceptors instanceof Integer);
        
        Object confidentialPort = mbeanConnect.getAttribute(objName, "confidentialPort");
        assertTrue(confidentialPort != null && confidentialPort instanceof Integer);
        
        String confidentialScheme = (String)mbeanConnect.getAttribute(objName, "confidentialScheme");
        assertTrue(confidentialScheme !=  null && confidentialScheme.length() > 0);

        Object connections = mbeanConnect.getAttribute(objName, "connections");
        assertTrue(connections != null && connections instanceof Integer);
        
        Object connectionsDurationMax = mbeanConnect.getAttribute(objName, "connectionsDurationMax");
        assertTrue(connectionsDurationMax != null && connectionsDurationMax instanceof Long);
        
        Object connectionsDurationMean = mbeanConnect.getAttribute(objName, "connectionsDurationMean");
        assertTrue(connectionsDurationMean != null && connectionsDurationMean instanceof Double);
        
        Object connectionsDurationStdDev = mbeanConnect.getAttribute(objName, "connectionsDurationStdDev");
        assertTrue(connectionsDurationStdDev != null && connectionsDurationStdDev instanceof Double);

        Object connectionsDurationTotal = mbeanConnect.getAttribute(objName, "connectionsDurationTotal");
        assertTrue(connectionsDurationTotal != null && connectionsDurationTotal instanceof Long);
        
        Object connectionsOpen = mbeanConnect.getAttribute(objName, "connectionsOpen");
        assertTrue(connectionsOpen != null && connectionsOpen instanceof Integer);
        
        Object connectionsOpenMax = mbeanConnect.getAttribute(objName, "connectionsOpenMax");
        assertTrue(connectionsOpenMax != null && connectionsOpenMax instanceof Integer);
        
        Object connectionsRequestsMax = mbeanConnect.getAttribute(objName, "connectionsRequestsMax");
        assertTrue(connectionsRequestsMax != null && connectionsRequestsMax instanceof Integer);
        
        Object connectionsRequestsMean = mbeanConnect.getAttribute(objName, "connectionsRequestsMean");
        assertTrue(connectionsRequestsMean != null && connectionsRequestsMean instanceof Double);
        
        Object connectionsRequestsStdDev = mbeanConnect.getAttribute(objName, "connectionsRequestsStdDev");
        assertTrue(connectionsRequestsStdDev != null && connectionsRequestsStdDev instanceof Double);

        Object forwarded = mbeanConnect.getAttribute(objName, "forwarded");
        assertTrue(forwarded != null && forwarded instanceof Boolean);
        
        String forwardedForHeader = (String)mbeanConnect.getAttribute(objName, "forwardedForHeader");
        assertTrue(forwardedForHeader !=  null && forwardedForHeader.length() > 0);

        String forwardedHostHeader = (String)mbeanConnect.getAttribute(objName, "forwardedHostHeader");
        assertTrue(forwardedHostHeader !=  null && forwardedHostHeader.length() > 0);

        String forwardedServerHeader = (String)mbeanConnect.getAttribute(objName, "forwardedServerHeader");
        assertTrue(forwardedServerHeader !=  null && forwardedServerHeader.length() > 0);

        String host = (String)mbeanConnect.getAttribute(objName, "host");
        // Exception would be thrown if there's a problem

        String hostHeader = (String)mbeanConnect.getAttribute(objName, "hostHeader");
        // Exception would be thrown if there's a problem

        Object integralPort = mbeanConnect.getAttribute(objName, "integralPort");
        assertTrue(integralPort != null && integralPort instanceof Integer);

        String integralScheme = (String)mbeanConnect.getAttribute(objName, "integralScheme");
        assertTrue(integralScheme !=  null && integralScheme.length() > 0);
        
        Object lowResourcesConnections  = mbeanConnect.getAttribute(objName, "lowResourcesConnections");
        assertTrue(lowResourcesConnections != null && lowResourcesConnections instanceof Integer);

        Object lowResourcesMaxIdleTime = mbeanConnect.getAttribute(objName, "lowResourcesMaxIdleTime");
        assertTrue(lowResourcesMaxIdleTime != null && lowResourcesMaxIdleTime instanceof Integer);
        
        Object maxIdleTime = mbeanConnect.getAttribute(objName, "maxIdleTime");
        assertTrue(maxIdleTime != null && maxIdleTime instanceof Integer);

        String name = (String)mbeanConnect.getAttribute(objName, "name");
        assertTrue(name !=  null && name.length() > 0);

        Object port = mbeanConnect.getAttribute(objName, "port");
        assertTrue(port != null && port instanceof Integer);

        Object requests = mbeanConnect.getAttribute(objName, "requests");
        assertTrue(requests != null && requests instanceof Integer);

        Object resolveNames = mbeanConnect.getAttribute(objName, "resolveNames");
        assertTrue(resolveNames != null && resolveNames instanceof Boolean);

        Object reuseAddress = mbeanConnect.getAttribute(objName, "reuseAddress");
        assertTrue(reuseAddress != null && reuseAddress instanceof Boolean);
        
        ObjectName server = (ObjectName)mbeanConnect.getAttribute(objName, "server");
        assertTrue(server != null);

        Object soLingerTime = mbeanConnect.getAttribute(objName, "soLingerTime");
        assertTrue(soLingerTime != null && soLingerTime instanceof Integer);

        Object statsOn = mbeanConnect.getAttribute(objName, "statsOn");
        assertTrue(statsOn != null && statsOn instanceof Boolean);
        
        Object statsOnMs = mbeanConnect.getAttribute(objName, "statsOnMs");
        assertTrue(statsOnMs != null && statsOnMs instanceof Long);
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testSessionManager() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.server.session:type=hashsessionmanager,id=0");
        
        Object httpOnly = mbeanConnect.getAttribute(objName, "httpOnly");
        assertTrue(httpOnly != null && httpOnly instanceof Boolean);
        
        ObjectName idManager = (ObjectName)mbeanConnect.getAttribute(objName, "idManager");
        assertTrue(idManager != null);

        Object maxCookieAge = mbeanConnect.getAttribute(objName, "maxCookieAge");
        assertTrue(maxCookieAge != null && maxCookieAge instanceof Integer);
     
        Object maxInactiveInterval = mbeanConnect.getAttribute(objName, "maxInactiveInterval");
        assertTrue(maxInactiveInterval != null && maxInactiveInterval instanceof Integer);
     
        Object refreshCookieAge = mbeanConnect.getAttribute(objName, "refreshCookieAge");
        assertTrue(refreshCookieAge != null && refreshCookieAge instanceof Integer);
     
        Object secureCookies = mbeanConnect.getAttribute(objName, "secureCookies");
        assertTrue(secureCookies != null && secureCookies instanceof Boolean);
        
        String sessionCookie = (String)mbeanConnect.getAttribute(objName, "sessionCookie");
        assertTrue(sessionCookie !=  null && sessionCookie.length() > 0);

        String sessionDomain = (String)mbeanConnect.getAttribute(objName, "sessionDomain");
        // Exception would be thrown if there's a problem

        String sessionIdPathParameterName = (String)mbeanConnect.getAttribute(objName, "sessionIdPathParameterName");
        assertTrue(sessionIdPathParameterName !=  null && sessionIdPathParameterName.length() > 0);

        String sessionPath = (String)mbeanConnect.getAttribute(objName, "sessionPath");
        // Exception would be thrown if there's a problem
        
        Object sessionTimeMax = mbeanConnect.getAttribute(objName, "sessionTimeMax");
        assertTrue(sessionTimeMax != null && sessionTimeMax instanceof Long);
        
        Object sessionTimeMean = mbeanConnect.getAttribute(objName, "sessionTimeMean");
        assertTrue(sessionTimeMean != null && sessionTimeMean instanceof Double);
        
        Object sessionTimeStdDev = mbeanConnect.getAttribute(objName, "sessionTimeStdDev");
        assertTrue(sessionTimeStdDev != null && sessionTimeStdDev instanceof Double);

        Object sessionTimeTotal = mbeanConnect.getAttribute(objName, "sessionTimeTotal");
        assertTrue(sessionTimeTotal != null && sessionTimeTotal instanceof Long);
        
        Object sessions = mbeanConnect.getAttribute(objName, "sessions");
        assertTrue(sessions != null && sessions instanceof Integer);
        
        Object sessionsMax = mbeanConnect.getAttribute(objName, "sessionsMax");
        assertTrue(sessionsMax != null && sessionsMax instanceof Integer);
        
        Object sessionsTotal = mbeanConnect.getAttribute(objName, "sessionsTotal");
        assertTrue(sessionsTotal != null && sessionsTotal instanceof Integer);
    }
    
    @Test
    @SuppressWarnings({ "unused", "unchecked" })
    public void testFilterHolder() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlet:type=filterholder,name=GzipFilter,id=0");
        
        String className = (String)mbeanConnect.getAttribute(objName, "className");
        assertTrue(className !=  null && className.length() > 0);

        String displayName = (String)mbeanConnect.getAttribute(objName, "displayName");
        // Exception would be thrown if there's a problem

        HashMap initParameters = (HashMap)mbeanConnect.getAttribute(objName, "initParameters");
        assertTrue(initParameters != null && initParameters.size() > 0);
        
        String name = (String)mbeanConnect.getAttribute(objName, "name");
        assertTrue(name !=  null && name.length() > 0);
    }

    @Test
    public void testFilterMapping() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlet:type=filtermapping,id=0");
        
        String filterName = (String)mbeanConnect.getAttribute(objName, "filterName");
        assertTrue(filterName !=  null && filterName.length() > 0);
        
        String[] pathSpecs = (String[])mbeanConnect.getAttribute(objName, "pathSpecs");
        assertTrue(pathSpecs !=  null && pathSpecs.length > 0);
        
        String[] servletNames = (String[])mbeanConnect.getAttribute(objName, "servletNames");
        assertTrue(servletNames !=  null);
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testServletHandler() throws Exception
    {
        // Tests ServerHandler and Handler attributes
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlet:type=servlethandler,id=0");
        
        ObjectName[] childHandlers = (ObjectName[])mbeanConnect.getAttribute(objName, "childHandlers");
        assertTrue(childHandlers !=  null);
        
        Object[] filterMappings = (Object[])mbeanConnect.getAttribute(objName, "filterMappings");
        assertTrue(filterMappings !=  null && filterMappings.length > 0);

        ObjectName[] filters = (ObjectName[])mbeanConnect.getAttribute(objName, "filters");
        assertTrue(filters !=  null && filters.length > 0);

        ObjectName handler = (ObjectName)mbeanConnect.getAttribute(objName, "handler");
        // Exception would be thrown if there's a problem

        ObjectName[] handlers = (ObjectName[])mbeanConnect.getAttribute(objName, "handlers");
        assertTrue(handlers !=  null);

        ObjectName server = (ObjectName)mbeanConnect.getAttribute(objName, "server");
        assertTrue(server != null);
               
        Object[] servletMappings = (Object[])mbeanConnect.getAttribute(objName, "servletMappings");
        assertTrue(servletMappings !=  null && servletMappings.length > 0);

        ObjectName[] servlets = (ObjectName[])mbeanConnect.getAttribute(objName, "servlets");
        assertTrue(servlets !=  null && servlets.length > 0);
    }

    @Test
    @SuppressWarnings({ "unused", "unchecked" })
    public void testServletHolder() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlet:type=servletholder,name=default,id=0");
        
        String className = (String)mbeanConnect.getAttribute(objName, "className");
        assertTrue(className !=  null && className.length() > 0);

        String displayName = (String)mbeanConnect.getAttribute(objName, "displayName");
        // Exception would be thrown if there's a problem

        String forcedPath = (String)mbeanConnect.getAttribute(objName, "forcedPath");
        // Exception would be thrown if there's a problem
        
        Object initOrder = mbeanConnect.getAttribute(objName, "initOrder");
        assertTrue(initOrder != null && initOrder instanceof Integer);

        HashMap initParameters = (HashMap)mbeanConnect.getAttribute(objName, "initParameters");
        assertTrue(initParameters != null && initParameters.size() > 0);
        
        String name = (String)mbeanConnect.getAttribute(objName, "name");
        assertTrue(name !=  null && name.length() > 0);
        
        String runAsRole = (String)mbeanConnect.getAttribute(objName, "runAsRole");
        // Exception would be thrown if there's a problem
    }

    @Test
    public void testServletMapping() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlet:type=servletmapping,id=0");
        
        String[] pathSpecs = (String[])mbeanConnect.getAttribute(objName, "pathSpecs");
        assertTrue(pathSpecs !=  null && pathSpecs.length > 0);
        
        String servletName = (String)mbeanConnect.getAttribute(objName, "servletName");
        assertTrue(servletName !=  null && servletName.length() > 0);
    }
    
    @Test
    public void testQoSFilter() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlets:type=qosfilter,id=0");
        
        Object maxRequests = mbeanConnect.getAttribute(objName, "maxRequests");
        assertTrue(maxRequests != null && maxRequests instanceof Integer);

        Object suspendMs = mbeanConnect.getAttribute(objName, "suspendMs");
        assertTrue(suspendMs != null && suspendMs instanceof Long);

        Object waitMs = mbeanConnect.getAttribute(objName, "waitMs");
        assertTrue(waitMs != null && waitMs instanceof Long);
    }
    
    @Test
    public void testDoSFilter() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.servlets:type=dosfilter,id=0");
        
        Object delayMs = mbeanConnect.getAttribute(objName, "delayMs");
        assertTrue(delayMs != null && delayMs instanceof Long);

        Object insertHeaders = mbeanConnect.getAttribute(objName, "insertHeaders");
        assertTrue(insertHeaders != null && insertHeaders instanceof Boolean);
        
        Object maxIdleTrackerMs = mbeanConnect.getAttribute(objName, "maxIdleTrackerMs");
        assertTrue(maxIdleTrackerMs != null && maxIdleTrackerMs instanceof Long);
        
        Object maxRequestMs = mbeanConnect.getAttribute(objName, "maxRequestMs");
        assertTrue(maxRequestMs != null && maxRequestMs instanceof Long);
        
        Object maxRequestsPerSec = mbeanConnect.getAttribute(objName, "maxRequestsPerSec");
        assertTrue(maxRequestsPerSec != null && maxRequestsPerSec instanceof Integer);

        Object maxWaitMs = mbeanConnect.getAttribute(objName, "maxWaitMs");
        assertTrue(maxWaitMs != null && maxWaitMs instanceof Long);
        
        Object remotePort = mbeanConnect.getAttribute(objName, "remotePort");
        assertTrue(remotePort != null && remotePort instanceof Boolean);
        
        Object throttleMs = mbeanConnect.getAttribute(objName, "throttleMs");
        assertTrue(throttleMs != null && throttleMs instanceof Long);

        Object throttledRequests = mbeanConnect.getAttribute(objName, "throttledRequests");
        assertTrue(throttledRequests != null && throttledRequests instanceof Integer);

        Object trackSessions = mbeanConnect.getAttribute(objName, "trackSessions");
        assertTrue(trackSessions != null && trackSessions instanceof Boolean);
    }
    
    @Test
    @SuppressWarnings("unused")
    public void testLogger() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.util.log:type=stderrlog,id=0");

        Object debugEnabled = mbeanConnect.getAttribute(objName, "debugEnabled");
        assertTrue(debugEnabled != null && debugEnabled instanceof Boolean);
        
        String name = (String)mbeanConnect.getAttribute(objName, "name");
        // Exception would be thrown if there's a problem
    }
    
    @Test
    public void testThreadPool() throws Exception
    {
        ObjectName objName = new ObjectName("org.eclipse.jetty.util.thread:type=queuedthreadpool,id=0");
        
        Object daemon = mbeanConnect.getAttribute(objName, "daemon");
        assertTrue(daemon != null && daemon instanceof Boolean);
        
        Object idleThreads = mbeanConnect.getAttribute(objName, "idleThreads");
        assertTrue(idleThreads != null && idleThreads instanceof Integer);

        Object lowOnThreads = mbeanConnect.getAttribute(objName, "lowOnThreads");
        assertTrue(lowOnThreads != null && lowOnThreads instanceof Boolean);
        
        Object maxIdleTimeMs = mbeanConnect.getAttribute(objName, "maxIdleTimeMs");
        assertTrue(maxIdleTimeMs != null && maxIdleTimeMs instanceof Integer);

        Object maxThreads = mbeanConnect.getAttribute(objName, "maxThreads");
        assertTrue(maxThreads != null && maxThreads instanceof Integer);

        Object minThreads = mbeanConnect.getAttribute(objName, "minThreads");
        assertTrue(minThreads != null && minThreads instanceof Integer);

        String name = (String)mbeanConnect.getAttribute(objName, "name");
        assertTrue(name != null && name.length() > 0);

        Object threads = mbeanConnect.getAttribute(objName, "threads");
        assertTrue(threads != null && threads instanceof Integer);

        Object threadsPriority = mbeanConnect.getAttribute(objName, "threadsPriority");
        assertTrue(threadsPriority != null && threadsPriority instanceof Integer);
    }

    @Test
    @SuppressWarnings({ "unused", "unchecked" })
    public void testWebAppContext() throws Exception
    {   	
        // Tests WebAppContext, ServletContextHandler, ContextHandler, 
        // ScopedHandler, HandlerWrapper, AbstractHandler attributes
        ObjectName objName = new ObjectName("org.eclipse.jetty.webapp:type=webappcontext,name=test,id=0");
        
        Object aliases = mbeanConnect.getAttribute(objName, "aliases");
        assertTrue(aliases != null && aliases instanceof Boolean);

        Object allowNullPathInfo = mbeanConnect.getAttribute(objName, "allowNullPathInfo");
        assertTrue(allowNullPathInfo != null && allowNullPathInfo instanceof Boolean);

        ObjectName[] childHandlers = (ObjectName[])mbeanConnect.getAttribute(objName, "childHandlers");
        assertTrue(childHandlers !=  null && childHandlers.length > 0);

        String classPath = (String)mbeanConnect.getAttribute(objName, "classPath");
        assertTrue(classPath != null && classPath.length() > 0);

        Object compactPath = mbeanConnect.getAttribute(objName, "compactPath");
        assertTrue(compactPath != null && compactPath instanceof Boolean);

        String[] configClasses = (String[])mbeanConnect.getAttribute(objName, "configurationClasses");
        assertTrue(configClasses !=  null && configClasses.length > 0);

        String[] connectorNames = (String[])mbeanConnect.getAttribute(objName, "connectorNames");
        // Exception would be thrown if there's a problem

        HashMap contextAttributes = (HashMap)mbeanConnect.getAttribute(objName, "contextAttributes");
        assertTrue(contextAttributes != null && contextAttributes.size() > 0);

        String contextPath = (String)mbeanConnect.getAttribute(objName, "contextPath");
        assertTrue(contextPath != null && contextPath.length() > 0);
        
        Object copyWebDir = mbeanConnect.getAttribute(objName, "copyWebDir");
        assertTrue(copyWebDir != null && copyWebDir instanceof Boolean);

        String defaultsDescriptor = (String)mbeanConnect.getAttribute(objName, "defaultsDescriptor");
        assertTrue(defaultsDescriptor != null && defaultsDescriptor.length() > 0);

        String descriptor = (String)mbeanConnect.getAttribute(objName, "descriptor");
        // Exception would be thrown if there's a problem

        String displayName = (String)mbeanConnect.getAttribute(objName, "displayName");
        assertTrue(displayName != null && displayName.length() > 0);

        Object distributable = mbeanConnect.getAttribute(objName, "distributable");
        assertTrue(distributable != null && distributable instanceof Boolean);

        ObjectName errorHandler = (ObjectName)mbeanConnect.getAttribute(objName, "errorHandler");
        assertTrue(errorHandler != null);

        String extraClasspath = (String)mbeanConnect.getAttribute(objName, "extraClasspath");
        // Exception would be thrown if there's a problem

        Object extractWAR = mbeanConnect.getAttribute(objName, "extractWAR");
        assertTrue(extractWAR != null && extractWAR instanceof Boolean);

        ObjectName handler = (ObjectName)mbeanConnect.getAttribute(objName, "handler");
        assertTrue(handler != null);
               
        ObjectName[] handlers = (ObjectName[])mbeanConnect.getAttribute(objName, "handlers");
        assertTrue(handlers !=  null && handlers.length > 0);

        HashMap initParams = (HashMap)mbeanConnect.getAttribute(objName, "initParams");
        assertTrue(initParams != null && initParams.size() > 0);
        
        Object maxFormContentSize = mbeanConnect.getAttribute(objName, "maxFormContentSize");
        assertTrue(maxFormContentSize != null && maxFormContentSize instanceof Integer);

        String overrideDescriptor = (String)mbeanConnect.getAttribute(objName, "overrideDescriptor");
        assertTrue(overrideDescriptor != null && overrideDescriptor.length() > 0);
        
        Object parentLoaderPriority = mbeanConnect.getAttribute(objName, "parentLoaderPriority");
        assertTrue(parentLoaderPriority != null && parentLoaderPriority instanceof Boolean);

        String resourceBase = (String)mbeanConnect.getAttribute(objName, "resourceBase");
        assertTrue(resourceBase != null && resourceBase.length() > 0);

        ObjectName securityHandler = (ObjectName)mbeanConnect.getAttribute(objName, "securityHandler");
        assertTrue(securityHandler != null);
               
        ObjectName server = (ObjectName)mbeanConnect.getAttribute(objName, "server");
        assertTrue(server != null);
        
        String[] serverClasses = (String[])mbeanConnect.getAttribute(objName, "serverClasses");
        assertTrue(serverClasses != null && serverClasses.length > 0);
        
        ObjectName servletHandler = (ObjectName)mbeanConnect.getAttribute(objName, "servletHandler");
        assertTrue(servletHandler != null);
               
        ObjectName sessionHandler = (ObjectName)mbeanConnect.getAttribute(objName, "sessionHandler");
        assertTrue(sessionHandler != null);
               
        String[] systemClasses = (String[])mbeanConnect.getAttribute(objName, "systemClasses");
        assertTrue(systemClasses != null && systemClasses.length > 0);
        
        File tempDirectory = (File)mbeanConnect.getAttribute(objName, "tempDirectory");
        assertTrue(tempDirectory != null);

        String[] virtualHosts = (String[])mbeanConnect.getAttribute(objName, "virtualHosts");
        // Exception would be thrown if there's a problem

        String war = (String)mbeanConnect.getAttribute(objName, "war");
        assertTrue(war != null && war.length() > 0);
        
        String[] welcomeFiles = (String[])mbeanConnect.getAttribute(objName, "welcomeFiles");
        assertTrue(welcomeFiles != null && welcomeFiles.length > 0);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testAppLifecycle() throws Exception
    {           
        SimpleRequest request = new SimpleRequest(jetty.getBaseUri());
        String result = request.getString("/test-war-dump/");
        assertTrue("Application didn't respond",result.length() > 0);
        
        JmxServiceConnection jmxConnection = new JmxServiceConnection(jetty.getJmxUrl());
        jmxConnection.connect();
        
        MBeanServerConnection mbsConnection = jmxConnection.getConnection();
        ObjectName dmObjName = new ObjectName("org.eclipse.jetty.deploy:type=deploymentmanager,id=0");
        ArrayList<String> apps = (ArrayList<String>)mbsConnection.getAttribute(dmObjName, "apps");
        
        String[] params = new String[] {apps.get(1), "undeployed"};
        String[] signature = new String[] {"java.lang.String", "java.lang.String"};
        mbsConnection.invoke(dmObjName, "requestAppGoal", params, signature);

        try
        {
                result = request.getString("/test-war-dump/");
        }
        catch (IOException ex)
        {
                assertTrue(ex.getMessage().contains("404"));
        }
        
        params = new String[] {apps.get(1), "started"};
        signature = new String[] {"java.lang.String", "java.lang.String"};
        mbsConnection.invoke(dmObjName, "requestAppGoal", params, signature);

        request = new SimpleRequest(jetty.getBaseUri());
        result = request.getString("/test-war-dump/");
        assertTrue("Application didn't respond",result.length() > 0);
    }
}

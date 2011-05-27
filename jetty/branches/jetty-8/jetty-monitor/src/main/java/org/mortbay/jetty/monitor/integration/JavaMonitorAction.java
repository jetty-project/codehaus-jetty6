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


package org.mortbay.jetty.monitor.integration;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.eclipse.jetty.client.ContentExchange;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethods;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.io.ByteArrayBuffer;
import org.eclipse.jetty.util.log.Log;
import org.mortbay.jetty.monitor.EventNotifier;
import org.mortbay.jetty.monitor.EventState;
import org.mortbay.jetty.monitor.EventState.TriggerState;
import org.mortbay.jetty.monitor.EventTrigger;
import org.mortbay.jetty.monitor.JMXMonitor;
import org.mortbay.jetty.monitor.MonitorAction;
import org.mortbay.jetty.monitor.triggers.AggregateEventTrigger;


/* ------------------------------------------------------------ */
/**
 */
public class JavaMonitorAction extends MonitorAction
{
    private final HttpClient _client;
    
    private final String _url;
    private final String _uuid;
    private final String _appid;
    
    private String _srvip;
    private String _session;
    
    /* ------------------------------------------------------------ */
    /**
     * @param notifier
     * @param pollInterval
     * @throws Exception 
     * @throws MalformedObjectNameException 
     */
    public JavaMonitorAction(EventNotifier notifier, String url, String uuid, String appid, long pollInterval)
        throws Exception
    {
        super(new AggregateEventTrigger(),notifier,pollInterval);
        
        _url = url;
        _uuid = uuid;
        _appid = appid;
        
        _client = new HttpClient();
        _client.setTimeout(60000);
        _client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        
        try
        {
            _client.start();
            _srvip = getServerIP();
        }
        catch (Exception ex)
        {
            Log.debug(ex);
        }
        
        sendData(new Properties());
     }

    /* ------------------------------------------------------------ */
    /**
     * @see org.mortbay.jetty.monitor.MonitorAction#execute(org.mortbay.jetty.monitor.EventTrigger, org.mortbay.jetty.monitor.EventState, long)
     */
    @Override
    public void execute(EventTrigger trigger, EventState<?> state, long timestamp)
    {
        exec(trigger, state, timestamp);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param trigger
     * @param state
     * @param timestamp
     */
    private <T> void exec(EventTrigger trigger, EventState<T> state, long timestamp)
    {
        Collection<TriggerState<T>> trs = state.values();
        
        Properties data = new Properties();
        for (TriggerState<T> ts :  trs)
        {
            Object value = ts.getValue();

            StringBuffer buffer = new StringBuffer();
            buffer.append(value == null ? "" : value.toString());
            buffer.append("|");
            buffer.append(getClassID(value));
            buffer.append("||");
            buffer.append(ts.getDescription());
            
            data.setProperty(ts.getID(), buffer.toString());
            
            try
            {
                sendData(data);
            }
            catch (Exception ex)
            {
                Log.debug(ex);
            }
        }
     }
    
    /* ------------------------------------------------------------ */
    /**
     * @param data
     * @throws Exception 
     */
    private void sendData(Properties data)
        throws Exception
    {
        data.put("account", _uuid);
        data.put("appserver", "Jetty");
        data.put("localIp", _srvip);
        if (_appid == null)
            data.put("lowestPort", getHttpPort());
        else
            data.put("lowestPort", _appid);
        if (_session != null)
            data.put("session", _session);
        
        Properties response = sendRequest(data);
        
        parseResponse(response);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param request
     * @return
     * @throws Exception 
     */
    private Properties sendRequest(Properties request)
        throws Exception
    {
        ByteArrayOutputStream reqStream = null;
        ByteArrayInputStream resStream = null;
        Properties response = null;
    
        try {
            ContentExchange reqEx = new ContentExchange();
            reqEx.setURL(_url);
            reqEx.setMethod(HttpMethods.POST);
            reqEx.addRequestHeader("Connection","close");
            
            reqStream = new ByteArrayOutputStream();
            request.storeToXML(reqStream,null);
            ByteArrayBuffer reqBuff = new ByteArrayBuffer(reqStream.toByteArray());

            reqEx.setRequestContent(reqBuff);
            _client.send(reqEx);
        
            reqEx.waitForDone();
            
            if (reqEx.getResponseStatus() == HttpStatus.OK_200)
            {
                response = new Properties();
                resStream = new ByteArrayInputStream(reqEx.getResponseContentBytes());
                response.loadFromXML(resStream);               
            }
        }
        finally
        {
            try
            {
                if (reqStream != null)
                    reqStream.close();
            }
            catch (IOException ex)
            {
                Log.ignore(ex);
            }
            
            try
            {
                if (resStream != null)
                    resStream.close();
            }
            catch (IOException ex)
            {
                Log.ignore(ex);
            }
        }
        
        return response;    
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param response
     * @return
     */
    private void parseResponse(Properties response)
    {
        if (response.get("onhold") != null)
            throw new Error("Suspended");
        

        if (response.get("session") != null)
        {
            _session = (String) response.remove("session");

            AggregateEventTrigger trigger = (AggregateEventTrigger)getTrigger();

            String queryString;
            ObjectName[] queryResults;
            for (Map.Entry<Object, Object> entry : response.entrySet())
            {
                String[] values = ((String) entry.getValue()).split("\\|");

                queryString = values[0];
                if (queryString.startsWith("com.javamonitor.openfire"))
                    continue;
                
                if (queryString.startsWith("com.javamonitor"))
                {
                    queryString = "org.mortbay.jetty.monitor.integration:type=javamonitortools,id=0";
                }
                
                queryResults = null;
                try
                {
                    queryResults = queryNames(queryString);
                }
                catch (IOException e)
                {
                    Log.debug(e);
                }
                catch (MalformedObjectNameException e)
                {
                    Log.debug(e);
                }
                
                if (queryResults != null)
                {
                    int idx = 0;
                    for(ObjectName objName : queryResults)
                    {
                        String id = entry.getKey().toString()+(idx == 0 ? "" : ":"+idx);
                        String name = queryString.equals(objName.toString()) ? "" : objName.toString();
                        boolean repeat = Boolean.parseBoolean(values[2]);
                        trigger.add(new JavaMonitorTrigger(objName, values[1], id, name, repeat));
                    }   
                }
           }
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param value
     * @return
     */
    private int getClassID(final Object value)
    {
        if (value == null)
            return 0;
        
        if (value instanceof Byte || 
            value instanceof Short ||
            value instanceof Integer ||
            value instanceof Long)
            return 1;
            
        if (value instanceof Float ||
            value instanceof Double)
            return 2;
        
        if (value instanceof Boolean)
            return 3;

        return 4; // String
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @throws Exception 
     */
    private String getServerIP()
        throws Exception
    {
        Socket s = null;
        try {
            if (getProperty("http.proxyHost") != null)
            {
                s = new Socket(getProperty("http.proxyHost"),
                               parseInt(getProperty("http.proxyPort", "80")));
            } 
            else
            {
                int port = 80;
                
                URL url = new URL(_url);
                if (url.getPort() != -1) {
                    port = url.getPort();
                }
                s = new Socket(url.getHost(), port);
            }
            return s.getLocalAddress().getHostAddress();
        }
        finally
        {
            try
            {
                if (s != null)
                    s.close();
            } 
            catch (IOException ex)
            {
                Log.ignore(ex);
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     * @throws Exception
     */
    public Integer getHttpPort() 
    {       
        Collection<ObjectName> connectors = null;
        MBeanServerConnection service;
        try
        {
            service = JMXMonitor.getServiceConnection();

            connectors = service.queryNames(new ObjectName("org.mortbay.jetty.nio:type=selectchannelconnector,*"), null);
            if (connectors != null && connectors.size() > 0)
            {
                Integer lowest = Integer.MAX_VALUE;
                for (final ObjectName connector : connectors) {
                    lowest = (Integer)service.getAttribute(connector, "port");
                }
        
                if (lowest < Integer.MAX_VALUE)
                    return lowest;
            }
        }
        catch (Exception ex)
        {
            Log.debug(ex);
        }
        
        return 0;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param param
     * @return
     * @throws IOException
     * @throws NullPointerException 
     * @throws MalformedObjectNameException 
     */
    private ObjectName[] queryNames(ObjectName param) 
        throws IOException, MalformedObjectNameException
    {
        ObjectName[] result = null;
        
        MBeanServerConnection connection = JMXMonitor.getServiceConnection();
        Set names = connection.queryNames(param, null);
        if (names != null && names.size() > 0)
        {
            result = new ObjectName[names.size()];
            
            int idx = 0;
            for(Object name : names)
            {
                if (name instanceof ObjectName)
                    result[idx++] = (ObjectName)name;
                else
                    result[idx++] = new ObjectName(name.toString());
            }
        }
        
        return result;
    }
    
    private ObjectName[] queryNames(String param) 
        throws IOException, MalformedObjectNameException
    {
        return queryNames(new ObjectName(param));
    }

 }

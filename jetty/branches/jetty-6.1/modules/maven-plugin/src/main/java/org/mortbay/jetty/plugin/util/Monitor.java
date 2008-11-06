//========================================================================
//$Id$
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.plugin.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.mortbay.jetty.Server;




/**
 * Monitor
 *
 * Listens for stop commands eg via mvn jetty:stop and
 * causes jetty to stop either by exiting the jvm, or
 * by stopping the Server instances. The choice of
 * behaviour is controlled by either passing true
 * (exit jvm) or false (stop Servers) in the constructor.
 * 
 */
public class Monitor extends Thread
{
    private int _port;
    private String _key;
    private Server[] _servers;

    ServerSocket _socket;
    boolean _kill;

    public Monitor(int port, String key, Server[] servers, boolean kill) 
    throws UnknownHostException, IOException
    {
        if(port <= 0)
            throw new IllegalStateException ("Bad stop port");
        if (key==null)
            throw new IllegalStateException("Bad stop key");
        _port = port;
        _key = key;
        _servers = servers;
        _kill = kill;
        setDaemon(true);
        setName("StopJettyPluginMonitor");
        _socket=new ServerSocket(port,1,InetAddress.getByName("127.0.0.1"));   
    }
    
    public void run()
    {
        while (true)
        {
            Socket socket = null;
            try
            {
                socket = _socket.accept();
                LineNumberReader lin = new LineNumberReader(new InputStreamReader(socket.getInputStream()));
                
                String key = lin.readLine();
                if (!_key.equals(key)) continue;
                String cmd = lin.readLine();
                if ("stop".equals(cmd))
                {
                    try{socket.close();}catch (Exception e){e.printStackTrace();}
                    try{socket.close();}catch (Exception e){e.printStackTrace();}
                    if (_kill)
                    {
                        PluginLog.getLog().info("Killing Jetty");
                        System.exit(0);     
                    }
                    else
                    {
                        for (int i=0; _servers != null && i < _servers.length; i++)
                        {
                            try
                            {
                                PluginLog.getLog().info("Stopping server "+i);                             
                                _servers[i].stop();
                            }
                            catch (Exception e)
                            {
                                PluginLog.getLog().error(e);
                            }
                        }
                    }
                }
            }
            catch (Exception e)
            {
                PluginLog.getLog().error(e);
            }
            finally
            {
                if (socket != null)
                {
                    try
                    {
                        socket.close();
                    }
                    catch (Exception e)
                    {
                        PluginLog.getLog().debug(e);
                    }
                }
                socket = null;
            }
        }
    }
}

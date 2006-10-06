//========================================================================
//$Id$
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


package org.mortbay.jetty.deployer;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.xml.XmlConfiguration;
import org.mortbay.util.Scanner;




/**
 * HotFileDeployer
 *
 * This deployer watches a designated directory for the
 * appearance/disappearance or changes to xml
 * configuration files.
 * 
 * These xml configuration files are in the format of jetty-web.xml
 * files, which means that they configure a single webapp.
 * 
 * When one of these files is detected in the designated
 * directory, a new WebAppContext is created and the xml
 * file applied to it to configure and thus deploy the 
 * corresponding webapp.
 * 
 * Similarly, when one of these existing files is removed,
 * the corresponding webapp is undeployed; when one of these
 * files is changed, the corresponding webapp is undeployed,
 * the (changed) xml config file reapplied to it, and then
 * (re)deployed.
 * 
 * Note that the webapp itself is NOT copied into the hot
 * deploy directory. The webapp directory or war file can
 * exist anywhere. It is the xml config file that points to
 * it's location and deploys it from there. 
 * 
 * This means that the hot deploy system is very powerful.
 * It means, for example, that you can keep a "read-only" copy of
 * your webapp somewhere, and apply different configurations
 * to it simply by dropping different xml configuration files into
 * the hot deploy directory.
 * 
 * Because the xml config file is in the format of a jetty-web.xml
 * file, this means that you can configure anything about the
 * webapp in the same way that you could by adding an explicit
 * <New class="org.mortbay.jetty.webapp.WebAppContext"> statement
 * to a jetty.xml file.
 * 
 * NOTE: that the default directory to scan is $jetty.home/webapps.
 * This means that you can have a single directory that represents
 * all of your currently deployed webapps:
 *   + the exploded webapps and wars which are deployed only on startup
 *   + xml config files describing a webapp that is hot deployed
 *   
 *   You can of course change the hot deploy directory if you wish.
 */
public class HotFileDeployer extends AbstractDeployer
       implements Scanner.FileAddedListener, Scanner.FileChangedListener, Scanner.FileRemovedListener
{
    private int _scanInterval = 10;
    private Scanner _scanner = new Scanner();
    private Resource _hotDeployDir;
    private Map _currentDeployments = new HashMap();
    private Server _server;

    
    
    /**
     * Constructor
     * @throws Exception
     */
    public HotFileDeployer ()
    throws Exception
    {
        //set up the default scan location to be $jetty.home/webapps
        String jettyHome = System.getProperty("jetty.home");
        Log.debug("jetty.home="+jettyHome);
        setHotDeployDir(Resource.newResource(jettyHome).addPath("webapps"));
        Log.debug("hot deploy dir="+_hotDeployDir.getFile().getCanonicalPath());
    }

    /**
     * @return the server
     */
    public Server getServer()
    {
        return _server;
    }


    /**
     * @param server the server to set
     */
    public void setServer(Server server)
    {
        _server = server;
    }
    
    public void setScanInterval (int seconds)
    {
        _scanInterval = seconds;
    }
    
    public int getScanInterval ()
    {
        return _scanInterval;
    }
    
    public void setHotDeployDir (String dir)
    throws Exception
    {
        setHotDeployDir(Resource.newResource(dir));
    }
    
    public void setHotDeployDir (File file)
    throws Exception
    {
        setHotDeployDir(Resource.newResource(file.toURL()));
    }
    
    public void setHotDeployDir (Resource resource)
    {
        _hotDeployDir=resource;
    }

    public Resource getHotDeployDir ()
    {
        return _hotDeployDir;
    }
    
    
    /** 
     * Start the hot deployer looking for webapps to deploy/undeploy
     * 
     * @see org.mortbay.component.AbstractLifeCycle#doStart()
     */
    public void doStart() throws Exception
    {
        if (_hotDeployDir==null)
            throw new IllegalStateException("No hot deploy dir specified");
        
        if (_server==null)
            throw new IllegalStateException("No server specified for deployer");
        
        _scanner.setScanDir(_hotDeployDir.getFile());
        _scanner.setScanInterval(getScanInterval());
        //Accept changes only in files that could be the equivalent of jetty-web.xml files.
        //That is, files that configure a single webapp.
        _scanner.setFilenameFilter (new FilenameFilter ()
        {
            public boolean accept (File dir, String name)
            {
                try
                {
                    if (name.endsWith(".xml") && dir.equals(getHotDeployDir().getFile()))
                        return true;
                    return false;
                }
                catch (IOException e)
                {
                    Log.warn(e);
                    return false;
                }
            }
        });
        _scanner.addListener(this);
        _scanner.start();
    }
    
    

    /** Stop the hot deployer.
     * 
     * @see org.mortbay.component.AbstractLifeCycle#doStop()
     */
    public void doStop() throws Exception
    {
        _scanner.stop();    
    }


    /** 
     * Handle a new deployment
     * @see org.mortbay.util.Scanner.FileAddedListener#fileAdded(java.lang.String)
     */
    public void fileAdded(String filename)
    throws Exception
    {
        WebAppContext webapp = createWebApp (filename);
        deploy(_server, webapp);
        _currentDeployments.put(filename, webapp);
    }

    /** 
     * Handle a change to an existing deployment.
     * Undeploy then redeploy.
     * @see org.mortbay.util.Scanner.FileChangedListener#fileChanged(java.lang.String)
     */
    public void fileChanged(String filename)
    throws Exception
    {
       fileRemoved(filename);
       fileAdded(filename);  
    }

    /** 
     * Handle an undeploy.
     * @see org.mortbay.util.Scanner.FileRemovedListener#fileRemoved(java.lang.String)
     */
    public void fileRemoved(String filename)
    throws Exception
    {
        WebAppContext webapp = (WebAppContext)_currentDeployments.get(filename);
        undeploy(_server, webapp);
        _currentDeployments.remove(filename);
    }
    
    
    /**
     * Create a WebAppContext for the webapp being hot deployed,
     * then apply the xml config file to it to configure it.
     * 
     * @param filename the config file found in the hot deploy directory
     * @return
     * @throws Exception
     */
    private WebAppContext createWebApp (String filename)
    throws Exception
    {
        // The config file can call any method on WebAppContext to configure
        // the webapp being deployed.
        File hotDeployXmlFile = new File (filename);
        if (!hotDeployXmlFile.exists())
            return null;
        
        WebAppContext context = new WebAppContext();      
        XmlConfiguration xmlConfiguration = new XmlConfiguration(hotDeployXmlFile.toURL());
        xmlConfiguration.configure(context); 
        return context;
    }

}

//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Scanner
 * 
 * Scans a list of files and directories on a periodic basis to detect changes.
 * If a change in any of the watched files is found, then the target LifeCycle
 * objects are stopped and restarted.
 * 
 *  This is used by the Jetty Maven plugin to watch the classes, dependencies 
 *  and web.xml file of a web application and to restart the webapp if any
 *  of the above changes.
 *  
 * @author janb
 *
 */
public class Scanner extends Thread
{
	
	private int scanInterval;
	
	private List roots;

	private Map scanInfo = Collections.EMPTY_MAP;
	
	private List listeners;
	
	public interface Listener
	{
		public void changesDetected(Scanner scanner, List changes);
	}
	
	
	public Scanner ()
	{	
		setDaemon(true);
	}
	
	
	/**
	 * The files and directory roots to watch. Directories will be
	 * recursively scanned.
	 * 
	 * @return Returns the roots.
	 */
	public List getRoots()
	{
		return this.roots;
	}

	/**
	 * @param roots The roots to set.
	 */
	public void setRoots(List roots)
	{
		this.roots = roots;
		//do an initializing scan
		scanInfo = scan();
	}

	/**
	 * 
	 * @return Returns the scanInterval.
	 */
	public int getScanInterval()
	{
		return this.scanInterval;
	}

	/**
	 * 
	 * @param scanInterval The scanInterval in seconds to set.
	 */
	public void setScanInterval(int scanInterval)
	{
		this.scanInterval = scanInterval;
	}
	

	/**
	 * List of Scanner.Listener implementations.
	 * @return Returns the listeners.
	 */
	public List getListeners()
	{
		return this.listeners;
	}


	/**
	 * @param listeners The listeners to set.
	 */
	public void setListeners(List listeners)
	{
		this.listeners = listeners;
	}
	
    
	
	/**
	 * Loop every scanInterval seconds until interrupted, checking to see if
	 * any of the watched files have changed. If they have, stop and restart
	 * the LifeCycle targets.
	 *  
	 * @see java.lang.Runnable#run()
	 */
	public void run ()
	{	
	    // set the sleep interval
	    long sleepMillis = getScanInterval()*1000L;
	    boolean running = true;
	    while (running)
	    {
	        try
	        {
	            //wake up and scan the files
	            Thread.sleep(sleepMillis);
	            Map latestScanInfo = scan();
	            
	            List filesWithDifferences = getDifferences(latestScanInfo, scanInfo);
	            
	            if (!filesWithDifferences.isEmpty())
	            {
	                if ((getListeners() != null) && (!getListeners().isEmpty()))
	                {
	                    try
	                    {
	                        PluginLog.getLog().debug("Calling scanner listeners ...");
	                        
	                        for (int i=0; i<getListeners().size();i++)
	                            ((Scanner.Listener)getListeners().get(i)).changesDetected(this, filesWithDifferences);
	                        
	                        PluginLog.getLog().debug("Listeners completed.");
	                    }
	                    catch (Exception e)
	                    {
	                        PluginLog.getLog().warn("Error doing stop/start", e);
	                    }
	                }
	            }				
	            scanInfo = latestScanInfo;
	        }
	        catch (InterruptedException e)
	        {
	            running = false;	
	        }
	    }
	}
	
	
	/**
	 * Scan the files and directories.
	 * 
	 * @return
	 */
	private Map scan ()
	{
	    PluginLog.getLog().debug("Scanning ...");
	    List roots = getRoots();
	    if ((roots == null) || (roots.isEmpty()))
	        return Collections.EMPTY_MAP;
	    
	    LinkedHashMap scanInfoMap = new LinkedHashMap();	
	    Iterator itor = roots.iterator();
	    while (itor.hasNext())
	    {
	        File f = (File)itor.next();
	        scan (f, scanInfoMap);
	    }
	    
	    if  (PluginLog.getLog().isDebugEnabled())
	    {
	        itor = scanInfo.entrySet().iterator();
	        while (itor.hasNext())
	        {
	            Map.Entry e = (Map.Entry)itor.next();
	            PluginLog.getLog().debug("Scanned "+e.getKey()+" : "+e.getValue());
	        }
	    }
	    
	    PluginLog.getLog().debug("Scan complete at "+new Date().toString());
	    return scanInfoMap;
	}
	
	
	/**
	 * Scan the file, or recurse into it if it is a directory.
	 * @param f
	 * @param scanInfoMap
	 */
	private void scan (File f, Map scanInfoMap)
	{
	    try
	    {
            if (!f.exists())
            {
                return;
            }
            
	        if (f.isFile())
	        {
	            String name = f.getCanonicalPath();
	            long lastModified = f.lastModified();
	            scanInfoMap.put(name, new Long(lastModified));
	        }
	        else if (f.isDirectory())
	        {
	            File[] files = f.listFiles();
	            for (int i=0;i<files.length;i++)
	                scan(files[i], scanInfoMap);
	        }
	        else
	            PluginLog.getLog().error ("Skipping file of unacceptable type: "+f.getName());
	    }
	    catch (IOException e)
	    {
	        PluginLog.getLog().error("Error scanning watched files", e);
	    }
	}
	
	
	private List getDifferences (Map newScan, Map oldScan)
	{
	    ArrayList fileNames = new ArrayList();
	    Set oldScanKeys = new HashSet(oldScan.keySet());
	    Iterator itor = newScan.entrySet().iterator();
	    while (itor.hasNext())
	    {
	        Map.Entry entry = (Map.Entry)itor.next();
	        if (!oldScanKeys.contains(entry.getKey()))
	        {
	            PluginLog.getLog().debug("File added: "+entry.getKey());
	            fileNames.add(entry.getKey());
	        }
	        else if (!oldScan.get(entry.getKey()).equals(entry.getValue()))
	        {
	            PluginLog.getLog().debug("File changed: "+entry.getKey());
	            fileNames.add(entry.getKey());
	            oldScanKeys.remove(entry.getKey());
	        }
	        else
	            oldScanKeys.remove(entry.getKey());
	    }
	    
	    if (!oldScanKeys.isEmpty())
	    {
	        fileNames.addAll(oldScanKeys);
	        if (PluginLog.getLog().isDebugEnabled())
	        {
	            Iterator keyItor = oldScanKeys.iterator();
	            while (keyItor.hasNext())
	            {
	                PluginLog.getLog().debug("File removed: "+keyItor.next());
	            }
	            
	        }
	    }
	    
	    return fileNames;
	}
	
}

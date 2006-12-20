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


package org.mortbay.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mortbay.log.Log;


/**
 * Scanner
 * 
 * Utility for scanning a directory for added, removed and changed
 * files and reporting these events via registered Listeners.
 *
 * TODO AbstractLifeCycle
 */
public class Scanner implements Runnable
{
    private int _scanInterval;

    
    private List _listeners = Collections.synchronizedList(new ArrayList());
    private Map _prevScan = Collections.EMPTY_MAP;
    private FilenameFilter _filter;
    private File _scanDir;
    private Thread _thread;
    private volatile boolean _running = false;
    private boolean _reportExisting = true;


    /**
     * Listener
     * 
     * Signature of notifications re file changes.
     */
    public interface Listener
    {
        public void fileChanged (String filename) throws Exception;
        public void fileAdded (String filename) throws Exception;
        public void fileRemoved (String filename) throws Exception;
    }

    


    /**
     * 
     */
    public Scanner ()
    {       
    }

    /**
     * Get the scan interval
     * @return interval between scans in seconds
     */
    public int getScanInterval()
    {
        return _scanInterval;
    }

    /**
     * Set the scan interval
     * @param scanInterval pause between scans in seconds
     */
    public void setScanInterval(int scanInterval)
    {
        this._scanInterval = scanInterval;
    }

    /**
     * Set the location of the directory to scan.
     * @param dir
     */
    public void setScanDir (File dir)
    {
        _scanDir = dir;
    }

    /**
     * Get the location of the directory to scan
     * @return
     */
    public File getScanDir ()
    {
        return _scanDir;
    }

    /**
     * Apply a filter to files found in the scan directory.
     * Only files matching the filter will be reported as added/changed/removed.
     * @param filter
     */
    public void setFilenameFilter (FilenameFilter filter)
    {
        this._filter = filter;
    }

    /**
     * Get any filter applied to files in the scan dir.
     * @return
     */
    public FilenameFilter getFilenameFilter ()
    {
        return _filter;
    }

    /**
     * Whether or not an initial scan will report all files as being
     * added.
     * @param reportExisting if true, all files found on initial scan will be 
     * reported as being added, otherwise not
     */
    public void setReportExistingFilesOnStartup (boolean reportExisting)
    {
        this._reportExisting = reportExisting;
    }

    /**
     * Add an added/removed/changed listener
     * @param listener
     */
    public synchronized void addListener (Listener listener)
    {
        if (listener == null)
            return;

        _listeners.add(listener);   
    }



    /**
     * Remove a registered listener
     * @param listener the Listener to be removed
     */
    public synchronized void removeListener (Listener listener)
    {
        if (listener == null)
            return;
        _listeners.remove(listener);    
    }


    /** 
     * Scan the configured directory, sleeping for the
     * configured scanInterval (in seconds) between each pass.
     * 
     * @see java.lang.Runnable#run()
     */
    public void run ()
    {   
        // set the sleep interval
        long sleepMillis = getScanInterval()*1000L;


        if (_reportExisting)
        {
            // if files exist at startup, report them
            scan();
        }
        else
        {
            //just register the list of existing files and only report changes
            _prevScan = scanFiles();
        }

        _running = true;
        while (_running)
        {
            try
            {
                //wake up and scan the files
                Thread.sleep(sleepMillis);
                scan();
            }
            catch (InterruptedException e)
            {
                _running = false;    
            }
        }
    }


    /**
     * Start the scanning action.
     */
    public void start ()
    {
        if (_running)
            throw new IllegalStateException("Already running");

        _thread = new Thread(this, "scanner");
        _thread.setDaemon(true);
        _thread.start();
    }


    /**
     * Stop the scanning.
     */
    public void stop ()
    {
        if (_running)
            _thread.interrupt ();
    }


    /**
     * Perform a pass of the scanner and report changes
     */
    public void scan ()
    {
        Map currentScan = scanFiles();
        reportDifferences(currentScan, _prevScan);
        _prevScan = currentScan;     
    }



    /**
     * Recursively scan all files in the designated directory.
     * @return
     */
    public Map scanFiles ()
    {
        File dir = getScanDir();

        Log.debug("Scanning directory "+getScanDir());
        HashMap scanInfo = new HashMap();

        if ((dir != null) && (dir.exists()))
            scanFile(dir, scanInfo);

        Log.debug("Scan complete at "+new Date());
        return scanInfo;
    }


    /**
     * Report the adds/changes/removes to the registered listeners
     * 
     * @param currentScan the info from the most recent pass
     * @param oldScan info from the previous pass
     */
    public void reportDifferences (Map currentScan, Map oldScan) 
    {
        Set oldScanKeys = new HashSet(oldScan.keySet());
        Iterator itor = currentScan.entrySet().iterator();
        while (itor.hasNext())
        {
            Map.Entry entry = (Map.Entry)itor.next();
            if (!oldScanKeys.contains(entry.getKey()))
            {
                Log.debug("File added: "+entry.getKey());
                reportAddition ((String)entry.getKey());
            }
            else if (!oldScan.get(entry.getKey()).equals(entry.getValue()))
            {
                Log.debug("File changed: "+entry.getKey());
                reportChange((String)entry.getKey());
                oldScanKeys.remove(entry.getKey());
            }
            else
                oldScanKeys.remove(entry.getKey());
        }

        if (!oldScanKeys.isEmpty())
        {

            Iterator keyItor = oldScanKeys.iterator();
            while (keyItor.hasNext())
            {
                String filename = (String)keyItor.next();
                Log.debug("File removed: "+filename);
                reportRemoval(filename);
            }
        }
    }





    /**
     * Get last modified time on a single file or recurse if
     * the file is a directory. 
     * @param f file or directory
     * @param scanInfoMap map of filenames to last modified times
     */
    private void scanFile (File f, Map scanInfoMap)
    {
        try
        {
            if (!f.exists())
                return;

            if (f.isFile())
            {
                Log.debug("Checking file "+f.getName());
                if ((_filter == null) ||
                        ((_filter != null) && _filter.accept(f.getParentFile(), f.getName())))
                {
                    Log.debug("File accepted");
                    String name = f.getCanonicalPath();
                    long lastModified = f.lastModified();
                    scanInfoMap.put(name, new Long(lastModified));
                }
            }
            else if (f.isDirectory())
            {
                File[] files = f.listFiles();
                for (int i=0;i<files.length;i++)
                    scanFile(files[i], scanInfoMap);
            }
        }
        catch (IOException e)
        {
            Log.warn("Error scanning watched files", e);
        }
    }


    /**
     * Report a file addition to the registered FileAddedListeners
     * @param filename
     */
    private void reportAddition (String filename)
    {
        Iterator itor = _listeners.iterator();
        while (itor.hasNext())
        {
            try
            {
                ((Listener)itor.next()).fileAdded(filename);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
    }


    /**
     * Report a file removal to the FileRemovedListeners
     * @param filename
     */
    private void reportRemoval (String filename)
    {
        Iterator itor = _listeners.iterator();
        while (itor.hasNext())
        {
            try
            {
                ((Listener)itor.next()).fileRemoved(filename);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
    }


    /**
     * Report a file change to the FileChangedListeners
     * @param filename
     */
    private void reportChange (String filename)
    {
        Iterator itor = _listeners.iterator();
        while (itor.hasNext())
        {
            try
            {
                ((Listener)itor.next()).fileChanged(filename);
            }
            catch (Exception e)
            {
                Log.warn(e);
            }
        }
    }

}

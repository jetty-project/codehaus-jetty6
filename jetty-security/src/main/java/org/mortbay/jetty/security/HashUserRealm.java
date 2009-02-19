// ========================================================================
// Copyright 1996-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.security;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.Response;
import org.mortbay.jetty.server.UserRealm;
import org.mortbay.jetty.util.Scanner;
import org.mortbay.jetty.util.Scanner.BulkListener;
import org.mortbay.jetty.util.component.AbstractLifeCycle;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/** HashMapped User Realm.
 *
 * An implementation of UserRealm that stores users and roles in-memory in
 * HashMaps.
 * <P>
 * Typically these maps are populated by calling the load() method or passing
 * a properties resource to the constructor. The format of the properties
 * file is: <PRE>
 *  username: password [,rolename ...]
 * </PRE>
 * Passwords may be clear text, obfuscated or checksummed.  The class 
 * com.mortbay.Util.Password should be used to generate obfuscated
 * passwords or password checksums.
 * 
 * If DIGEST Authentication is used, the password must be in a recoverable
 * format, either plain text or OBF:.
 *
 * The HashUserRealm also implements SSORealm but provides no implementation
 * of SSORealm. Instead setSSORealm may be used to provide a delegate
 * SSORealm implementation. 
 *
 * @see Password
 * @author Greg Wilkins (gregw)
 */
public class HashUserRealm extends AbstractUserRealm
{
    /* ------------------------------------------------------------ */

    private String _config;
    private Resource _configResource;
    private Scanner _scanner;
    private int _refreshInterval=0;//default is not to reload
    

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public HashUserRealm()
    {
        super();
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm Name
     */
    public HashUserRealm(String name)
    {
        super(name);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm name
     * @param config Filename or url of user properties file.
     */
    public HashUserRealm(String name, String config)
        throws IOException
    {
        _realmName=name;
        setConfig(config);
    }
    
    public String getConfig()
    {
        return _config;
    }
    
    public Resource getConfigResource()
    {
        return _configResource;
    }

    /* ------------------------------------------------------------ */
    /** Load realm users from properties file.
     * The property file maps usernames to password specs followed by
     * an optional comma separated list of role names.
     *
     * @param config Filename or url of user properties file.
     * @exception IOException 
     */
    public void setConfig(String config)
        throws IOException
    {
        _config=config;
        _configResource=Resource.newResource(_config);
       loadConfig();
 
    }

    public void setRefreshInterval (int msec)
    {
        _refreshInterval=msec;
    }
    
    public int getRefreshInterval()
    {
        return _refreshInterval;
    }
    
    protected void loadConfig () 
    throws IOException
    {
        synchronized (this)
        {
            _users.clear();
            _roles.clear();
            
            if(Log.isDebugEnabled())Log.debug("Load "+this+" from "+_config);
            Properties properties = new Properties();
            properties.load(_configResource.getInputStream());

            Iterator iter = properties.entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();

                String username=entry.getKey().toString().trim();
                String credentials=entry.getValue().toString().trim();
                String roles=null;
                int c=credentials.indexOf(',');
                if (c>0)
                {
                    roles=credentials.substring(c+1).trim();
                    credentials=credentials.substring(0,c).trim();
                }

                if (username!=null && username.length()>0 &&
                        credentials!=null && credentials.length()>0)
                {
                    putUser(username,credentials);
                    if(roles!=null && roles.length()>0)
                    {
                        StringTokenizer tok = new StringTokenizer(roles,", ");
                        while (tok.hasMoreTokens())
                            putUserRole(username,tok.nextToken());
                    }
                }
            }
        }
    }

   

    /**
     * Warning - deprecated! This method was originally exposed as public,
     * and added a user to the in-memory security information. This method
     * should not be callable other than by the realm implementation and will
     * be removed in future releases.
     * 
     * @param name
     * @param credentials
     * @return
     * @deprecated 
     */
    public synchronized Object put(Object name, Object credentials)
    {
        return putUser(name, credentials);
    }
 

    /**
     * Warning - deprecated! This method was originally exposed as public, and
     * added role information to the in-memory security information. This method
     * should not be callable other than by the implementation and will be removed
     * in a future release.
     * 
     * @param userName
     * @param roleName     
     * @deprecated 
     */
    public synchronized void addUserToRole(String userName, String roleName)
    {
        putUserRole(userName, roleName);
    }
    
    /** 
     * @see org.mortbay.jetty.util.component.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        super.doStart();
        if (_scanner!=null)
            _scanner.stop(); 

        if (getRefreshInterval() > 0)
        {
            _scanner = new Scanner();
            _scanner.setScanInterval(getRefreshInterval());
            List dirList = new ArrayList(1);
            dirList.add(_configResource.getFile());
            _scanner.setScanDirs(dirList);
            _scanner.setFilenameFilter(new FilenameFilter ()
            {
                public boolean accept(File dir, String name)
                {
                    File f = new File(dir,name);
                    try
                    {
                        if (f.compareTo(_configResource.getFile())==0)
                            return true;
                    }
                    catch (IOException e)
                    {
                        return false;
                    }

                    return false;
                }

            });
            _scanner.addListener(new BulkListener()
            {
                public void filesChanged(List filenames) throws Exception
                {
                    if (filenames==null)
                        return;
                    if (filenames.isEmpty())
                        return;
                    if (filenames.size()==1 && filenames.get(0).equals(_config))
                        loadConfig();
                }
                public String toString()
                {
                    return "HashUserRealm$Scanner";
                }

            });
            _scanner.setReportExistingFilesOnStartup(false);
            _scanner.setRecursive(false);
            _scanner.start();
        }
    }

    /** 
     * @see org.mortbay.jetty.util.component.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        if (_scanner!=null)
            _scanner.stop();
        _scanner=null;
    }



}

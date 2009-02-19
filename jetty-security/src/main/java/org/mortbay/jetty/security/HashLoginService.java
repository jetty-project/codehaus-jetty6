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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.util.Scanner;
import org.mortbay.jetty.util.Scanner.BulkListener;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/**
 * HashMapped User Realm.
 * 
 * An implementation of UserRealm that stores users and roles in-memory in
 * HashMaps.
 * <P>
 * Typically these maps are populated by calling the load() method or passing a
 * properties resource to the constructor. The format of the properties file is:
 * 
 * <PRE>
 *  username: password [,rolename ...]
 * </PRE>
 * 
 * Passwords may be clear text, obfuscated or checksummed. The class
 * com.mortbay.Util.Password should be used to generate obfuscated passwords or
 * password checksums.
 * 
 * If DIGEST Authentication is used, the password must be in a recoverable
 * format, either plain text or OBF:.
 * 
 * @see org.mortbay.jetty.server.server.http.security.Password
 * @author Greg Wilkins (gregw)
 */
public class HashLoginService extends AbstractLoginService implements LoginService
{
    private String _config;

    private Resource _configResource;

    private Scanner _scanner;

    private int _refreshInterval = 0;// default is not to reload

    /* ------------------------------------------------------------ */
    /**
     * Constructor.
     */
    public HashLoginService()
    {
    }

    /* ------------------------------------------------------------ */
    /**
     * Constructor.
     * 
     * @param name Realm Name
     */
    public HashLoginService(String name)
    {
        super(name);
    }

    public HashLoginService(String realmName, Map<String, User> _users)
    {
        super(realmName);

        this._users = _users;
    }/* ------------------------------------------------------------ */

    /**
     * Constructor.
     * 
     * @param name Realm name
     * @param config Filename or url of user properties file.
     * @throws java.io.IOException if user properties file could not be loaded
     */
    public HashLoginService(String name, String config) throws IOException
    {
        super(name);
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
    /**
     * Load realm users from properties file. The property file maps usernames
     * to password specs followed by an optional comma separated list of role
     * names.
     * 
     * @param config Filename or url of user properties file.
     * @exception java.io.IOException if user properties file could not be
     *                    loaded
     */
    public void setConfig(String config) throws IOException
    {
        _config = config;
        _configResource = Resource.newResource(_config);
        loadConfig();

    }

    public void setRefreshInterval(int msec)
    {
        _refreshInterval = msec;
    }

    public int getRefreshInterval()
    {
        return _refreshInterval;
    }

    public void loadConfig() throws IOException
    {
        synchronized (this)
        {
            _users.clear();

            if (Log.isDebugEnabled()) Log.debug("Load " + this + " from " + _config);
            Properties properties = new Properties();
            properties.load(_configResource.getInputStream());

            for (Map.Entry<Object, Object> entry : properties.entrySet())
            {
                String username = ((String) entry.getKey()).trim();
                String credentials = ((String) entry.getValue()).trim();
                String roles = null;
                int c = credentials.indexOf(',');
                if (c > 0)
                {
                    roles = credentials.substring(c + 1).trim();
                    credentials = credentials.substring(0, c).trim();
                }

                if (username != null && username.length() > 0 && credentials != null && credentials.length() > 0)
                {

                    String[] roleArray = NO_ROLES;
                    if (roles != null && roles.length() > 0)
                    {
                        roleArray = roles.split(",");
                    }
                    putUser(username, new KnownUser(username, new Password(credentials), roleArray));
                }
            }
        }
    }


    /**
     * @see org.mortbay.jetty.server.server.util.component.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        super.doStart();
        if (_scanner != null) _scanner.stop();

        if (getRefreshInterval() > 0)
        {
            _scanner = new Scanner();
            _scanner.setScanInterval(getRefreshInterval());
            List<File> dirList = new ArrayList<File>(1);
            dirList.add(_configResource.getFile());
            _scanner.setScanDirs(dirList);
            _scanner.setFilenameFilter(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    File f = new File(dir, name);
                    try
                    {
                        if (f.compareTo(_configResource.getFile()) == 0) return true;
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
                    if (filenames == null) return;
                    if (filenames.isEmpty()) return;
                    if (filenames.size() == 1 && filenames.get(0).equals(_config)) loadConfig();
                }

                public String toString()
                {
                    return "HashLoginService$Scanner";
                }

            });
            _scanner.setReportExistingFilesOnStartup(false);
            _scanner.setRecursive(false);
            _scanner.start();
        }
    }

    /**
     * @see org.mortbay.jetty.server.server.util.component.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        if (_scanner != null) _scanner.stop();
        _scanner = null;
    }
}
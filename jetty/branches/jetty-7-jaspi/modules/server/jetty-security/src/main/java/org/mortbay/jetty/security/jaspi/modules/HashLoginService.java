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

package org.mortbay.jetty.security.jaspi.modules;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.security.auth.Subject;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.LoginCallback;
import org.mortbay.jetty.LoginService;
import org.mortbay.jetty.ServerAuthException;
import org.mortbay.jetty.security.Credential;
import org.mortbay.jetty.security.Password;
import org.mortbay.log.Log;
import org.mortbay.resource.Resource;
import org.mortbay.util.Scanner;
import org.mortbay.util.Scanner.BulkListener;

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
 * @see org.mortbay.jetty.security.Password
 * @author Greg Wilkins (gregw)
 */
public class HashLoginService extends AbstractLifeCycle implements LoginService
{
    private static final String[] NO_ROLES = new String[0];

    /**
     * HttpContext Attribute to set to activate SSO.
     */
    public static final String __SSO = "org.mortbay.http.SSO";

    /* ------------------------------------------------------------ */
    private String _realmName;

    private String _config;

    private Resource _configResource;

    protected Map<String, User> _users = new HashMap<String, User>();

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
        _realmName = name;
    }

    public HashLoginService(String _realmName, Map<String, User> _users)
    {
        this._realmName = _realmName;
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
        _realmName = name;
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
                    put(username, new KnownUser(username, new Password(credentials), roleArray));
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @param name The realm name
     */
    public void setName(String name)
    {
        _realmName = name;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return The realm name.
     */
    public String getName()
    {
        return _realmName;
    }

    /* ------------------------------------------------------------ */
    /**
     * Put user into realm.
     * 
     * @param name User name
     * @param credentials String password, Password or UserPrinciple instance.
     * @return Old UserPrinciple value or null
     */
    public synchronized Object put(String name, Object credentials)
    {
        if (credentials instanceof User) return _users.put(name, (User) credentials);

        if (credentials instanceof Password) return _users.put(name, new KnownUser(name, (Password) credentials));
        if (credentials != null) return _users.put(name, new KnownUser(name, Credential.getCredential(credentials.toString())));
        return null;
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Realm[" + _realmName + "]==" + _users.keySet();
    }

    /* ------------------------------------------------------------ */
    public void dump(PrintStream out)
    {
        out.println(this + ":");
        out.println(super.toString());
    }

    /**
     * @see org.mortbay.component.AbstractLifeCycle#doStart()
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
     * @see org.mortbay.component.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        if (_scanner != null) _scanner.stop();
        _scanner = null;
    }

    /* ------------------------------------------------------------ */
    public void login(LoginCallback loginCallback) throws ServerAuthException
    {
        KnownUser user;
        synchronized (this)
        {
            user = getKnownUser(loginCallback.getUserName());
        }
        if (user != null && user.authenticate(loginCallback.getPassword()))
        {
            loginCallback.getSubject().getPrincipals().add(user);
            loginCallback.setUserPrincipal(user);
            loginCallback.setGroups(user.roles);
            loginCallback.setSuccess(true);
        }
    }

    protected KnownUser getKnownUser(String userName)
    {
        return (KnownUser) _users.get(userName);
    }

    public void logout(Subject subject) throws ServerAuthException
    {
        subject.getPrincipals(KnownUser.class).clear();
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class User implements Principal
    {
        String[] roles = NO_ROLES;

        public String getName()
        {
            return "Anonymous";
        }

        public boolean isAuthenticated()
        {
            return false;
        }

        public String toString()
        {
            return getName();
        }
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public static class KnownUser extends User
    {
        private String _userName;

        private Credential _cred;

        /* -------------------------------------------------------- */
        KnownUser(String name, Credential credential)
        {
            _userName = name;
            _cred = credential;
        }

        public KnownUser(String name, Credential credential, String[] roles)
        {
            this(name, credential);
            this.roles = roles;
        }

        /* -------------------------------------------------------- */
        boolean authenticate(Object credentials)
        {
            return _cred != null && _cred.check(credentials);
        }

        /* ------------------------------------------------------------ */
        public String getName()
        {
            return _userName;
        }

        /* -------------------------------------------------------- */
        public boolean isAuthenticated()
        {
            return true;
        }
    }

}
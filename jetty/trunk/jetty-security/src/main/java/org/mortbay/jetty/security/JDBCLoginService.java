// ========================================================================
// Copyright 2003-2004 Mort Bay Consulting Pty. Ltd.
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

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.mortbay.jetty.http.security.Password;
import org.mortbay.jetty.server.LoginCallback;
import org.mortbay.jetty.util.Loader;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/**
 * HashMapped User Realm with JDBC as data source. JDBCLoginService extends
 * HashULoginService and adds a method to fetch user information from database.
 * The login() method checks the inherited Map for the user. If the user is not
 * found, it will fetch details from the database and populate the inherited
 * Map. It then calls the superclass login() method to perform the actual
 * authentication. Periodically (controlled by configuration parameter),
 * internal hashes are cleared. Caching can be disabled by setting cache refresh
 * interval to zero. Uses one database connection that is initialized at
 * startup. Reconnect on failures. authenticate() is 'synchronized'.
 * 
 * An example properties file for configuration is in
 * $JETTY_HOME/etc/jdbcRealm.properties
 * 
 * @version $Id$
 * @author Arkadi Shishlov (arkadi)
 * @author Fredrik Borgh
 * @author Greg Wilkins (gregw)
 * @author Ben Alex
 */

public class JDBCLoginService extends AbstractLoginService
{
    private String _config;
    private Resource _configResource;
    private String _jdbcDriver;
    private String _url;
    private String _userName;
    private String _password;
    private String _userTableKey;
    private String _userTablePasswordField;
    private String _roleTableRoleField;
    private int _cacheTime;
    private long _lastHashPurge;
    private Connection _con;
    private String _userSql;
    private String _roleSql;

    /* ------------------------------------------------------------ */
    /**
     * Constructor.
     */
    public JDBCLoginService()
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /**
     * Constructor.
     * 
     * @param name name of login service
     */
    public JDBCLoginService(String name)
    {
        super(name);
    }

    /* ------------------------------------------------------------ */
    /**
     * Constructor.
     * 
     * @param name Realm name
     * @param config Filename or url of JDBC connection properties file.
     * @exception java.io.IOException problem loading configuration
     * @exception ClassNotFoundException problem loading driver
     * @throws IllegalAccessException problem using driver
     * @throws InstantiationException problem creating driver
     */
    public JDBCLoginService(String name, String config) 
    throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException
    {
        super(name);
        setConfig(config);
        Loader.loadClass(this.getClass(), _jdbcDriver).newInstance();
        connectDatabase();
    }


    public String getConfig()
    {
        return _config;
    }

    /* ------------------------------------------------------------ */
    /**
     * Load JDBC connection configuration from properties file.
     * 
     * @param config Filename or url of user properties file.
     * @exception java.io.IOException
     */
    public void setConfig(String config) throws IOException
    {        
        _config=config;
        _configResource=Resource.newResource(_config);

        Properties properties = new Properties();
        Resource resource = Resource.newResource(config);
        properties.load(resource.getInputStream());

        _jdbcDriver = properties.getProperty("jdbcdriver");
        _url = properties.getProperty("url");
        _userName = properties.getProperty("username");
        _password = properties.getProperty("password");
        String _userTable = properties.getProperty("usertable");
        _userTableKey = properties.getProperty("usertablekey");
        String _userTableUserField = properties.getProperty("usertableuserfield");
        _userTablePasswordField = properties.getProperty("usertablepasswordfield");
        String _roleTable = properties.getProperty("roletable");
        String _roleTableKey = properties.getProperty("roletablekey");
        _roleTableRoleField = properties.getProperty("roletablerolefield");
        String _userRoleTable = properties.getProperty("userroletable");
        String _userRoleTableUserKey = properties.getProperty("userroletableuserkey");
        String _userRoleTableRoleKey = properties.getProperty("userroletablerolekey");
        _cacheTime = new Integer(properties.getProperty("cachetime"));

        if (_jdbcDriver == null || _jdbcDriver.equals("")
            || _url == null
            || _url.equals("")
            || _userName == null
            || _userName.equals("")
            || _password == null
            || _cacheTime < 0)
        {
            if (Log.isDebugEnabled()) Log.debug("UserRealm " + getName() + " has not been properly configured");
        }
        _cacheTime *= 1000;
        _lastHashPurge = 0;
        _userSql = "select " + _userTableKey + "," + _userTablePasswordField + " from " + _userTable + " where " + _userTableUserField + " = ?";
        _roleSql = "select r." + _roleTableRoleField
                   + " from "
                   + _roleTable
                   + " r, "
                   + _userRoleTable
                   + " u where u."
                   + _userRoleTableUserKey
                   + " = ?"
                   + " and r."
                   + _roleTableKey
                   + " = u."
                   + _userRoleTableRoleKey;
    }

    /* ------------------------------------------------------------ */
    /**
     * (re)Connect to database with parameters setup by loadConfig()
     */
    public void connectDatabase()
    {
        try
        {
            Class.forName(_jdbcDriver);
            _con = DriverManager.getConnection(_url, _userName, _password);
        }
        catch (SQLException e)
        {
            Log.warn("UserRealm " + getName() + " could not connect to database; will try later", e);
        }
        catch (ClassNotFoundException e)
        {
            Log.warn("UserRealm " + getName() + " could not connect to database; will try later", e);
        }
    }

    /* ------------------------------------------------------------ */
    @Override
    public void login(LoginCallback loginCallback) throws ServerAuthException
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (now - _lastHashPurge > _cacheTime || _cacheTime == 0)
            {
                _users.clear();
                _lastHashPurge = now;
            }
            // TODO JASPI not sure if this should be in sync block. Was not in
            // JDBCUserRealm
            super.login(loginCallback);
        }
    }

    @Override
    protected KnownUser getKnownUser(String userName)
    {
        KnownUser user = super.getKnownUser(userName);
        if (user == null)
        {
            user = loadUser(userName);
        }
        return user;
    }

    /* ------------------------------------------------------------ */
    private KnownUser loadUser(String username)
    {
        try
        {
            if (null == _con) connectDatabase();

            if (null == _con) throw new SQLException("Can't connect to database");

            PreparedStatement stat = _con.prepareStatement(_userSql);
            stat.setObject(1, username);
            ResultSet rs = stat.executeQuery();

            if (rs.next())
            {
                int key = rs.getInt(_userTableKey);
                String credentials = rs.getString(_userTablePasswordField);
                stat.close();

                stat = _con.prepareStatement(_roleSql);
                stat.setInt(1, key);
                rs = stat.executeQuery();
                List<String> roles = new ArrayList<String>();
                while (rs.next())
                    roles.add(rs.getString(_roleTableRoleField));

                stat.close();
                KnownUser user = new KnownUser(username, new Password(credentials), roles.toArray(new String[roles.size()]));
                putUser(username, user);
                return user;
            }
        }
        catch (SQLException e)
        {
            Log.warn("UserRealm " + getName() + " could not load user information from database", e);
            connectDatabase();
        }
        return null;
    }
}
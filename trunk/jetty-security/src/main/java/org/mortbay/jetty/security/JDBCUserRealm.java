// ========================================================================
// $Id$
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
import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.mortbay.jetty.server.Request;
import org.mortbay.jetty.server.UserRealm;
import org.mortbay.jetty.util.Loader;
import org.mortbay.jetty.util.log.Log;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/** HashMapped User Realm with JDBC as data source.
 * JDBCUserRealm extends HashUserRealm and adds a method to fetch user
 * information from database.
 * The authenticate() method checks the inherited HashMap for the user.
 * If the user is not found, it will fetch details from the database
 * and populate the inherited HashMap. It then calls the HashUserRealm
 * authenticate() method to perform the actual authentication.
 * Periodically (controlled by configuration parameter), internal
 * hashes are cleared. Caching can be disabled by setting cache
 * refresh interval to zero.
 * Uses one database connection that is initialized at startup. Reconnect
 * on failures. authenticate() is 'synchronized'.
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

public class JDBCUserRealm extends AbstractUserRealm
{
    private String _config;
    private Resource _configResource;
    
    private String _jdbcDriver;
    private String _url;
    private String _userName;
    private String _password;
    private String _userTable;
    private String _userTableKey;
    private String _userTableUserField;
    private String _userTablePasswordField;
    private String _roleTable;
    private String _roleTableKey;
    private String _roleTableRoleField;
    private String _userRoleTable;
    private String _userRoleTableUserKey;
    private String _userRoleTableRoleKey;
    private int _cacheTime;
    
    private long _lastHashPurge;
    private Connection _con;
    private String _userSql;
    private String _roleSql;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public JDBCUserRealm()
    {
        super();
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name 
     */
    public JDBCUserRealm(String name)
    {
        super(name);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm name
     * @param config Filename or url of JDBC connection properties file.
     * @exception IOException 
     * @exception ClassNotFoundException 
     */
    public JDBCUserRealm(String name, String config)
        throws IOException,
               ClassNotFoundException,
               InstantiationException,
               IllegalAccessException
    {
        super(name);
        setConfig(config);
        Loader.loadClass(this.getClass(),_jdbcDriver).newInstance();
        connectDatabase();
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
    
    
    /* ------------------------------------------------------------ */
    /** Load JDBC connection configuration from properties file.
     *     
     * @exception IOException 
     */
    protected void loadConfig()
        throws IOException
    {        
        Properties properties = new Properties();
        
        properties.load(getConfigResource().getInputStream());
        
        _jdbcDriver = properties.getProperty("jdbcdriver");
        _url = properties.getProperty("url");
        _userName = properties.getProperty("username");
        _password = properties.getProperty("password");
        _userTable = properties.getProperty("usertable");
        _userTableKey = properties.getProperty("usertablekey");
        _userTableUserField = properties.getProperty("usertableuserfield");
        _userTablePasswordField = properties.getProperty("usertablepasswordfield");
        _roleTable = properties.getProperty("roletable");
        _roleTableKey = properties.getProperty("roletablekey");
        _roleTableRoleField = properties.getProperty("roletablerolefield");
        _userRoleTable = properties.getProperty("userroletable");
        _userRoleTableUserKey = properties.getProperty("userroletableuserkey");
        _userRoleTableRoleKey = properties.getProperty("userroletablerolekey");
        // default cachetime = 30s
        String cachetime = properties.getProperty("cachetime");
        _cacheTime = cachetime!=null ? new Integer(cachetime).intValue() : 30;
        
        if (_jdbcDriver == null || _jdbcDriver.equals("")
            || _url == null || _url.equals("")
            || _userName == null || _userName.equals("")
            || _password == null
            || _cacheTime < 0)
        {
            if(Log.isDebugEnabled())Log.debug("UserRealm " + getName()
                        + " has not been properly configured");
        }
        _cacheTime *= 1000;
        _lastHashPurge = 0;
        _userSql = "select " + _userTableKey + ","
            + _userTablePasswordField + " from "
            + _userTable + " where "
            + _userTableUserField + " = ?";
        _roleSql = "select r." + _roleTableRoleField
            + " from " + _roleTable + " r, "
            + _userRoleTable + " u where u."
            + _userRoleTableUserKey + " = ?"
            + " and r." + _roleTableKey + " = u."
            + _userRoleTableRoleKey;
    }

    
    /* ------------------------------------------------------------ */
    /** (re)Connect to database with parameters setup by loadConfig()
     */
    public void connectDatabase()
    {
        try 
        {
             Class.forName(_jdbcDriver);
            _con = DriverManager.getConnection(_url, _userName, _password);
        }
        catch(SQLException e)
        {
            Log.warn("UserRealm " + getName()
                      + " could not connect to database; will try later", e);
        }
        catch(ClassNotFoundException e)
        {
            Log.warn("UserRealm " + getName()
                      + " could not connect to database; will try later", e);
        }
    }
    
    /* ------------------------------------------------------------ */
    public Principal authenticate(String username,
                                  Object credentials,
                                  Request request)
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (now - _lastHashPurge > _cacheTime || _cacheTime == 0)
            {
                _users.clear();
                _roles.clear();
                _lastHashPurge = now;
            }
            Principal user = super.getPrincipal(username);
            if (user == null)
            {
                loadUser(username);
                user = super.getPrincipal(username);
            }
        }
        return super.authenticate(username, credentials, request);
    }
    
    /* ------------------------------------------------------------ */
    /** Check if a user is in a role.
     * @param user The user, which must be from this realm 
     * @param roleName 
     * @return True if the user can act in the role.
     */
    public synchronized boolean isUserInRole(Principal user, String roleName)
    {
        if(super.getPrincipal(user.getName())==null)
            loadUser(user.getName());
        return super.isUserInRole(user, roleName);
    }
    
    
    /* ------------------------------------------------------------ */
    private void loadUser(String username)
    {
        try
        {
            if (null==_con)
                connectDatabase();
            
            if (null==_con)
                throw new SQLException("Can't connect to database");
            
            PreparedStatement stat = _con.prepareStatement(_userSql);
            stat.setObject(1, username);
            ResultSet rs = stat.executeQuery();
    
            if (rs.next())
            {
                int key = rs.getInt(_userTableKey);
                putUser(username, rs.getString(_userTablePasswordField));
                stat.close();
                
                stat = _con.prepareStatement(_roleSql);
                stat.setInt(1, key);
                rs = stat.executeQuery();

                while (rs.next())
                    putUserRole(username, rs.getString(_roleTableRoleField));
                
                stat.close();
            }
        }
        catch (SQLException e)
        {
            Log.warn("UserRealm " + getName()
                      + " could not load user information from database", e);
            connectDatabase();
        }
    }
}

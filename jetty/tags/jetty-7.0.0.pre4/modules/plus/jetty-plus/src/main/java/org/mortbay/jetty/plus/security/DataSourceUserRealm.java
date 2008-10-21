// ========================================================================
// $Id$
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.plus.security;

import java.io.IOException;
import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.plus.naming.NamingEntryUtil;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.log.Log;



/**
 * DataSourceUserRealm
 *
 * Obtain user/password/role information from a database
 * via jndi DataSource.
 */
public class DataSourceUserRealm extends HashUserRealm
{
    private String _jndiName = "javax.sql.DataSource/default";
    private DataSource _datasource;
    private Server _server;
    private String _userTableName = "users";
    private String _userTableKey = "id";
    private String _userTableUserField = "username";
    private String _userTablePasswordField = "pwd";
    private String _roleTableName = "roles";
    private String _roleTableKey = "id";
    private String _roleTableRoleField = "role";
    private String _userRoleTableName = "user_roles";
    private String _userRoleTableUserKey = "user_id";
    private String _userRoleTableRoleKey = "role_id";
    private int _cacheMs = 30000;
    private long _lastHashPurge = 0;
    private String _userSql;
    private String _roleSql;
    


    public DataSourceUserRealm (String jndiName)
    {
        _jndiName=jndiName;
    }
    
    
    public DataSourceUserRealm()
    {
    }
    
    
    public void setServer (Server server)
    {
        _server=server;
    }
    
    public Server getServer()
    {
        return _server;
    }
    
    public void setUserTableName (String name)
    {
        _userTableName=name;
    }
    
    public String getUserTableName()
    {
        return _userTableName;
    }
    
    public String getUserTableKey()
    {
        return _userTableKey;
    }


    public void setUserTableKey(String tableKey)
    {
        _userTableKey = tableKey;
    }


    public String getUserTableUserField()
    {
        return _userTableUserField;
    }


    public void setUserTableUserField(String tableUserField)
    {
        _userTableUserField = tableUserField;
    }


    public String getUserTablePasswordField()
    {
        return _userTablePasswordField;
    }


    public void setUserTablePasswordField(String tablePasswordField)
    {
        _userTablePasswordField = tablePasswordField;
    }


    public String getRoleTableName()
    {
        return _roleTableName;
    }


    public void setRoleTableName(String tableName)
    {
        _roleTableName = tableName;
    }


    public String getRoleTableKey()
    {
        return _roleTableKey;
    }


    public void setRoleTableKey(String tableKey)
    {
        _roleTableKey = tableKey;
    }


    public String getRoleTableRoleField()
    {
        return _roleTableRoleField;
    }


    public void setRoleTableRoleField(String tableRoleField)
    {
        _roleTableRoleField = tableRoleField;
    }


    public String getUserRoleTableName()
    {
        return _userRoleTableName;
    }


    public void setUserRoleTableName(String roleTableName)
    {
        _userRoleTableName = roleTableName;
    }


    public String getUserRoleTableUserKey()
    {
        return _userRoleTableUserKey;
    }


    public void setUserRoleTableUserKey(String roleTableUserKey)
    {
        _userRoleTableUserKey = roleTableUserKey;
    }


    public String getUserRoleTableRoleKey()
    {
        return _userRoleTableRoleKey;
    }


    public void setUserRoleTableRoleKey(String roleTableRoleKey)
    {
        _userRoleTableRoleKey = roleTableRoleKey;
    }

    public void setCacheMs (int ms)
    {
        _cacheMs=ms;
    }
    
    public int getCacheMs ()
    {
        return _cacheMs;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * Check if user is authentic
     * 
     * @see org.mortbay.jetty.security.HashUserRealm#authenticate(java.lang.String, java.lang.Object, org.mortbay.jetty.Request)
     */
    public Principal authenticate(String username,
            Object credentials,
            Request request)
    {
        synchronized (this)
        {
            long now = System.currentTimeMillis();
            if (now - _lastHashPurge > _cacheMs || _cacheMs == 0)
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
    /** Load database configuration from properties file.
     * This is really here to satisfy the HashUserRealm interface.
     * Setters should be used instead.
     *     
     * @exception IOException 
     */
    protected void loadConfig()
    throws IOException
    {        
        Properties properties = new Properties();

        properties.load(getConfigResource().getInputStream());

        _jndiName = properties.getProperty("jndiname");
        setUserTableName(properties.getProperty("usertable"));
        setUserTableKey(properties.getProperty("usertablekey"));
        setUserTableUserField(properties.getProperty("usertableuserfield"));
        setUserTablePasswordField(properties.getProperty("usertablepasswordfield"));
        setRoleTableName(properties.getProperty("roletable"));
        setRoleTableKey(properties.getProperty("roletablekey"));
        setRoleTableRoleField(properties.getProperty("roletablerolefield"));
        setUserRoleTableName(properties.getProperty("userroletable"));
        setUserRoleTableUserKey(properties.getProperty("userroletableuserkey"));
        setUserRoleTableRoleKey(properties.getProperty("userroletablerolekey"));
        // default cachetime = 30s
        String cacheSec = properties.getProperty("cachetime");
        if (cacheSec != null)
            setCacheMs(new Integer(cacheSec).intValue() * 1000);

        if (_jndiName == null || _cacheMs < 0)
        {
            if(Log.isDebugEnabled())Log.debug("UserRealm " + getName()
                    + " has not been properly configured");
        }
    }

    /* ------------------------------------------------------------ */
    /** Load user's info from database.
     * 
     * @param user
     */
    private void loadUser (String user)
    {
        Connection connection = null;
        try
        {        
            initDb();
            connection = getConnection();
            
            PreparedStatement statement = connection.prepareStatement(_userSql);
            statement.setObject(1, user);
            ResultSet rs = statement.executeQuery();
    
            if (rs.next())
            {
                int key = rs.getInt(_userTableKey);
                put(user, rs.getString(_userTablePasswordField));
                statement.close();
                
                statement = connection.prepareStatement(_roleSql);
                statement.setInt(1, key);
                rs = statement.executeQuery();

                while (rs.next())
                    addUserToRole(user, rs.getString(_roleTableRoleField));
                
                statement.close();
            }
        }
        catch (NamingException e)
        {
            Log.warn("No datasource for "+_jndiName, e);
        }
        catch (SQLException e)
        {
            Log.warn("Problem loading user info for "+user, e);
        }
        finally
        {
            if (connection != null)
            {
                try
                {
                    connection.close();
                }
                catch (SQLException x)
                {
                    Log.warn("Problem closing connection", x);
                }
                finally
                {
                    connection = null;
                }
            }
        }
    }
   
    /* ------------------------------------------------------------ */
    /**
     * Lookup the datasource for the jndiName and formulate the
     * necessary sql query strings based on the configured table
     * and column names.
     * 
     * @throws NamingException
     */
    private void initDb() throws NamingException
    {
        if (_datasource != null)
            return;
        
        InitialContext ic = new InitialContext();
        //try finding the datasource in the Server scope
        try
        {
           _datasource = (DataSource)NamingEntryUtil.lookup(_server, _jndiName);
        }
        catch (NameNotFoundException e)
        {
            //next try the jvm scope
        }

        //try finding the datasource in the jvm scope
        if (_datasource==null)
        {
            _datasource = (DataSource)NamingEntryUtil.lookup(null, _jndiName);
        }

        _userSql = "select " + _userTableKey + "," + _userTablePasswordField 
                  + " from " + _userTableName 
                  + " where "+ _userTableUserField + " = ?";
        
        _roleSql = "select r." + _roleTableRoleField
                  + " from " + _roleTableName + " r, " + _userRoleTableName 
                  + " u where u."+ _userRoleTableUserKey + " = ?"
                  + " and r." + _roleTableKey + " = u." + _userRoleTableRoleKey;
    }
    
    private Connection getConnection () 
    throws NamingException, SQLException
    {
        initDb();
        return _datasource.getConnection();
    }

}

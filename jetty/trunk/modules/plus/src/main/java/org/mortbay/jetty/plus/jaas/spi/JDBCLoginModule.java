// ========================================================================
// $Id$
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas.spi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import org.mortbay.jetty.plus.jaas.JAASRole;
import org.mortbay.jetty.security.Credential;
import org.mortbay.log.Log;
import org.mortbay.util.Loader;



/* ---------------------------------------------------- */
/** JDBCLoginModule
 * <p>JAAS LoginModule to retrieve user information from
 *  a database and authenticate the user.
 *
 * <p><h4>Notes</h4>
 * <p>This version uses plain old JDBC connections NOT
 * Datasources.
 *
 * <p><h4>Usage</h4>
 * <pre>
 */
/*
 * </pre>
 *
 * @see 
 * @version 1.0 Tue Apr 15 2003
 * @author Jan Bartel (janb)
 */
public class JDBCLoginModule extends AbstractLoginModule
{
    private String dbDriver;
    private String dbUrl;
    private String dbUserName;
    private String dbPassword;
    private String userQuery;
    private String rolesQuery;
    

  

    
    /* ------------------------------------------------ */
    /** Logout authenticated user
     * @return 
     * @exception LoginException 
     */
    public boolean logout()
        throws LoginException
    {
        boolean result = super.logout();
        return result;
    }

    
    /* ------------------------------------------------ */
    /** Init LoginModule.
     * Called once by JAAS after new instance created.
     * @param subject 
     * @param callbackHandler 
     * @param sharedState 
     * @param options 
     */
    public void initialize(Subject subject,
                           CallbackHandler callbackHandler,
                           Map sharedState,
                           Map options)
    {
        try
        {
            super.initialize(subject, callbackHandler, sharedState, options);
            
            //get the jdbc  username/password, jdbc url out of the options
            dbDriver = (String)options.get("dbDriver");
            dbUrl = (String)options.get("dbUrl");
            dbUserName = (String)options.get("dbUserName");
            dbPassword = (String)options.get("dbPassword");

            if (dbUserName == null)
                dbUserName = "";

            if (dbPassword == null)
                dbPassword = "";
            
            if (dbDriver != null)
                Loader.loadClass(this.getClass(), dbDriver).newInstance();
            
            //get the user credential query out of the options
            String dbUserTable = (String)options.get("userTable");
            String dbUserTableUserField = (String)options.get("userField");
            String dbUserTableCredentialField = (String)options.get("credentialField");
            
            userQuery = "select "+dbUserTableCredentialField+" from "+dbUserTable+" where "+dbUserTableUserField+"=?";
            
            
            //get the user roles query out of the options
            String dbUserRoleTable = (String)options.get("userRoleTable");
            String dbUserRoleTableUserField = (String)options.get("userRoleUserField");
            String dbUserRoleTableRoleField = (String)options.get("userRoleRoleField");
            
            rolesQuery = "select "+dbUserRoleTableRoleField+" from "+dbUserRoleTable+" where "+dbUserRoleTableUserField+"=?";
            
            if(Log.isDebugEnabled())Log.debug("userQuery = "+userQuery);
            if(Log.isDebugEnabled())Log.debug("rolesQuery = "+rolesQuery);
            
        }
        catch (ClassNotFoundException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (InstantiationException e)
        {
            throw new IllegalStateException (e.toString());
        }
        catch (IllegalAccessException e)
        {
            throw new IllegalStateException (e.toString());
        }
    }


    

    /* ------------------------------------------------ */
    /** Load info from database
     * @param userName user info to load
     * @exception SQLException 
     */
    public UserInfo getUserInfo (String userName)
        throws SQLException
    {
        //connect to database
        Connection connection = null;
        
        try
        {
            if (!((dbDriver != null)
                  &&
                  (dbUrl != null)))
                throw new IllegalStateException ("Database connection information not configured");

            if(Log.isDebugEnabled())Log.debug("Connecting using dbDriver="+dbDriver+"+ dbUserName="+dbUserName+", dbPassword="+dbUrl);
            
            connection = DriverManager.getConnection (dbUrl,
                                                      dbUserName,
                                                      dbPassword);
            
            //query for credential
            PreparedStatement statement = connection.prepareStatement (userQuery);
            statement.setString (1, userName);
            ResultSet results = statement.executeQuery();
            String dbCredential = null;
            if (results.next())
            {
                dbCredential = results.getString(1);
            }
            results.close();
            statement.close();
            
            //query for role names
            statement = connection.prepareStatement (rolesQuery);
            statement.setString (1, userName);
            results = statement.executeQuery();
            List roles = new ArrayList();
            
            while (results.next())
            {
                String roleName = results.getString (1);
                roles.add (new JAASRole(roleName));
            }
            
            results.close();
            statement.close();
            return new UserInfo (userName, Credential.getCredential(dbCredential), roles);
        }
        finally
        {
            connection.close();
        }
    }
    
    
    
}

package org.mortbay.jetty.servlet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.JDBCSessionManager.Session;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.log.Log;


/**
 * JDBCSessionIdManager
 *
 * SessionIdManager implementation that uses a database to store in-use session ids, 
 * to support distributed sessions.
 * 
 */
public class JDBCSessionIdManager extends AbstractSessionIdManager
{    
    private static final String __createString = "create table JettySessionIds (id varchar(60), primary key(id))";
    private static final String __insertString = "insert into JettySessionIds (id)  values (?)";
    private static final String __deleteString = "delete from JettySessionIds where id = ?";
    private static final String __queryString = "select * from JettySessionIds where id = ?";
    protected HashSet<String> _sessionIds = new HashSet();
    protected String _driverClassName;
    protected String _connectionUrl;

    public JDBCSessionIdManager(Server server)
    {
        super(server);
    }
    
    public JDBCSessionIdManager(Server server, Random random)
    {
       super(server, random);
    }

    /**
     * Configure jdbc connection information
     * 
     * @param driverClassName
     * @param connectionUrl
     */
    public void setDriverInfo (String driverClassName, String connectionUrl)
    {
        _driverClassName=driverClassName;
        _connectionUrl=connectionUrl;
    }
    
    public String getDriverClassName()
    {
        return _driverClassName;
    }
    
    public String getConnectionUrl ()
    {
        return _connectionUrl;
    }
   
    public void addSession(HttpSession session)
    {
        if (session == null)
            return;
        
        synchronized (_sessionIds)
        {
            String id = ((JDBCSessionManager.Session)session).getClusterId();            
            try
            {
                insert(id);
                _sessionIds.add(id);
            }
            catch (Exception e)
            {
                Log.warn("Problem storing session id="+id, e);
            }
        }
    }
    
    public void removeSession(HttpSession session)
    {
        if (session == null)
            return;
        
        removeSession(((JDBCSessionManager.Session)session).getClusterId());
    }
    
    
    
    public void removeSession (String id)
    {

        if (id == null)
            return;
        
        synchronized (_sessionIds)
        {  
            if (Log.isDebugEnabled())
                Log.debug("Removing session id="+id);
            try
            {               
                _sessionIds.remove(id);
                delete(id);
            }
            catch (Exception e)
            {
                Log.warn("Problem removing session id="+id, e);
            }
        }
        
    }
    

    /** 
     * Get the session id without any node identifier suffix.
     * 
     * @see org.mortbay.jetty.SessionIdManager#getClusterId(java.lang.String)
     */
    public String getClusterId(String nodeId)
    {
        int dot=nodeId.lastIndexOf('.');
        return (dot>0)?nodeId.substring(0,dot):nodeId;
    }
    

    /** 
     * Get the session id, including this node's id as a suffix.
     * 
     * @see org.mortbay.jetty.SessionIdManager#getNodeId(java.lang.String, javax.servlet.http.HttpServletRequest)
     */
    public String getNodeId(String clusterId, HttpServletRequest request)
    {
        if (_workerName!=null)
            return clusterId+'.'+_workerName;

        return clusterId;
    }


    public boolean idInUse(String id)
    {
        if (id == null)
            return false;
        
        String clusterId = getClusterId(id);
        
        synchronized (_sessionIds)
        {
            if (_sessionIds.contains(clusterId))
                return true; //optimisation - if this session is one we've been managing, we can check locally
            
            //otherwise, we need to go to the database to check
            try
            {
                return exists(clusterId);
            }
            catch (Exception e)
            {
                Log.warn("Problem checking inUse for id="+clusterId, e);
                return false;
            }
        }
    }

    /** 
     * Invalidate the session matching the id on all contexts.
     * 
     * @see org.mortbay.jetty.SessionIdManager#invalidateAll(java.lang.String)
     */
    public void invalidateAll(String id)
    {            
        //take the id out of the list of known sessionids for this node
        removeSession(id);
        
        synchronized (_sessionIds)
        {
            //tell all contexts that may have a session object with this id to
            //get rid of them
            Handler[] contexts = _server.getChildHandlersByClass(WebAppContext.class);
            for (int i=0; contexts!=null && i<contexts.length; i++)
            {
                AbstractSessionManager.Session session = ((AbstractSessionManager)((WebAppContext)contexts[i]).getSessionHandler().getSessionManager()).getSession(id);
                if (session !=null)
                    session.invalidate();
            }
        }
    }


    public void doStart()
    {
        try
        {
            prepareTables();        
            super.doStart();
        }
        catch (Exception e)
        {
            Log.warn("Problem initialising JettySessionIds table", e);
        }
    }
    /**
     * Set up the table in the database
     * @throws SQLException
     */
    private void prepareTables()
    throws Exception
    {
        Class.forName(_driverClassName);
        Connection connection = null;
        try
        {
            connection = getConnection();
            String tableName = "JettySessionIds";
            DatabaseMetaData metaData = connection.getMetaData();
            if (metaData.storesLowerCaseIdentifiers())
                tableName = tableName.toLowerCase();
            if (metaData.storesUpperCaseIdentifiers())
                tableName = tableName.toUpperCase();
            
            ResultSet result = metaData.getTables(null, null, tableName, null);
            if (!result.next())
            {
                //table does not exist, so create it
                connection.createStatement().executeUpdate(__createString);
            }
        }
        finally
        {
            if (connection != null)
                connection.close();
        }
    }
    
    private void insert (String id)
    throws SQLException 
    {
        Connection connection = null;
        try
        {
            connection = getConnection();
            connection.setAutoCommit(true);            
            PreparedStatement query = connection.prepareStatement(__queryString);
            query.setString(1, id);
            ResultSet result = query.executeQuery();
            //only insert the id if it isn't in the db already 
            if (!result.next())
            {
                PreparedStatement statement = connection.prepareStatement(__insertString);
                statement.setString(1, id);
                statement.executeUpdate();
            }
        }
        finally
        {
            if (connection != null)
                connection.close();
        }
    }
    
    private void delete (String id)
    throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = getConnection();
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement(__deleteString);
            statement.setString(1, id);
            statement.executeUpdate();
        }
        finally
        {
            if (connection != null)
                connection.close();
        }
    }
    
    
    private boolean exists (String id)
    throws SQLException
    {
        Connection connection = null;
        try
        {
            connection = getConnection();
            connection.setAutoCommit(true);
            PreparedStatement statement = connection.prepareStatement(__queryString);
            statement.setString(1, id);
            ResultSet result = statement.executeQuery();
            if (result.next())
                return true;
            else
                return false;
        }
        finally
        {
            if (connection != null)
                connection.close();
        }
    }
    
    /**
     * Get a connection from the driver.
     * @return
     * @throws SQLException
     */
    private Connection getConnection ()
    throws SQLException
    {
        return DriverManager.getConnection(_connectionUrl);
    }
}

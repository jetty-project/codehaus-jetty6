package org.mortbay.jetty.servlet;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

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
    private static final String __createSessionIdTable = "create table JettySessionIds (id varchar(60), primary key(id))";
    private static final String __createSessionTable = "create table JettySessions (rowId varchar(60), sessionId varchar(60), "+
                                                       " contextPath varchar(60), lastNode varchar(60), accessTime bigint, "+
                                                       " lastAccessTime bigint, createTime bigint, cookieTime bigint, "+
                                                       " lastSavedTime bigint, expiryTime bigint, map blob, primary key(rowId))";
    
    private static final String __selectExpiredSessions = "select * from JettySessions where expiryTime >= ? and expiryTime <= ?";
    private static final String __deleteOldExpiredSessions = "delete from JettySessions where expiryTime >0 and expiryTime <= ?";

    private static final String __insertId = "insert into JettySessionIds (id)  values (?)";
    private static final String __deleteId = "delete from JettySessionIds where id = ?";
    private static final String __queryId = "select * from JettySessionIds where id = ?";
    
    
    protected HashSet<String> _sessionIds = new HashSet();
    protected String _driverClassName;
    protected String _connectionUrl;
    protected Timer _timer; //scavenge timer
    protected TimerTask _task; //scavenge task
    protected long _lastScavengeTime;
    protected long _scavengeIntervalSec = 60 * 10; //10mins
    
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
   
    
    public void setScavengeInterval (long sec)
    {
        _scavengeIntervalSec = sec;
    }
    
    public long getScavengeInterval ()
    {
        return _scavengeIntervalSec;
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
                AbstractSessionManager manager = ((AbstractSessionManager)((WebAppContext)contexts[i]).getSessionHandler().getSessionManager());
                if (manager instanceof JDBCSessionManager)
                {
                    ((JDBCSessionManager)manager).invalidateSession(id);
                }
            }
        }
    }


    /** 
     * Start up the id manager.
     * 
     * Makes necessary database tables and starts a Session
     * scavenger thread.
     * 
     * @see org.mortbay.jetty.servlet.AbstractSessionIdManager#doStart()
     */
    public void doStart()
    {
        try
        {
            Random rand = new Random();
            int variability = rand.nextInt(60); //add variability of up to 1 min
            _scavengeIntervalSec += variability;
            prepareTables();        
            super.doStart();
            if (Log.isDebugEnabled()) Log.debug("Scavenging interval = "+_scavengeIntervalSec+" sec");
            _timer=new Timer("JDBCSessionScavenger", true);

            synchronized (this)
            {
                if (_task!=null)
                    _task.cancel();
                _task = new TimerTask()
                {
                    public void run()
                    {
                        scavenge();
                    }   
                };
                long scavengeIntervalMs = _scavengeIntervalSec * 1000;
                _timer.schedule(_task, scavengeIntervalMs, scavengeIntervalMs);
            }

        }
        catch (Exception e)
        {
            Log.warn("Problem initialising JettySessionIds table", e);
        }
    }
    
    /** 
     * Stop the scavenger.
     * 
     * @see org.mortbay.component.AbstractLifeCycle#doStop()
     */
    public void doStop () 
    throws Exception
    {
        synchronized(this)
        {
            if (_task!=null)
                _task.cancel();
            if (_timer!=null)
                _timer.cancel();
            _timer=null;
        }
        super.doStop();
    }
    
    /**
     * Get a connection from the driver.
     * @return
     * @throws SQLException
     */
    protected Connection getConnection ()
    throws SQLException
    {
        return DriverManager.getConnection(_connectionUrl);
    }
    
    /**
     * Set up the tables in the database
     * @throws SQLException
     */
    private void prepareTables()
    throws Exception
    {
        Class.forName(_driverClassName);
        Connection connection = null;
        try
        {
            //make the id table
            connection = getConnection();
            connection.setAutoCommit(true);
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
                connection.createStatement().executeUpdate(__createSessionIdTable);
            }
            
            //make the session table
            tableName = "JettySessions";   
            if (metaData.storesLowerCaseIdentifiers())
                tableName = tableName.toLowerCase();
            if (metaData.storesUpperCaseIdentifiers())
                tableName = tableName.toUpperCase();
            result = metaData.getTables(null, null, tableName, null);
            if (!result.next())
            {
                //table does not exist, so create it
                connection.createStatement().executeUpdate(__createSessionTable);
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
            PreparedStatement query = connection.prepareStatement(__queryId);
            query.setString(1, id);
            ResultSet result = query.executeQuery();
            //only insert the id if it isn't in the db already 
            if (!result.next())
            {
                PreparedStatement statement = connection.prepareStatement(__insertId);
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
            PreparedStatement statement = connection.prepareStatement(__deleteId);
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
            PreparedStatement statement = connection.prepareStatement(__queryId);
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
    
    private void scavenge ()
    {
        
        Connection connection = null;
        List expiredSessionIds = new ArrayList();
        try
        {            
            if (Log.isDebugEnabled()) Log.debug("Scavenge sweep started at "+System.currentTimeMillis());
            if (_lastScavengeTime > 0)
            {
                connection = getConnection();
                connection.setAutoCommit(true);
                //"select sessionId from JettySessions where expiryTime > (lastScavengeTime - scanInterval) and expiryTime < lastScavengeTime";
                PreparedStatement statement = connection.prepareStatement(__selectExpiredSessions);
                long lowerBound = (_lastScavengeTime - (_scavengeIntervalSec * 1000));
                long upperBound = _lastScavengeTime;
                if (Log.isDebugEnabled()) Log.debug("Searching for sessions expired between "+lowerBound + " and "+upperBound);
                statement.setLong(1, lowerBound);
                statement.setLong(2, upperBound);
                ResultSet result = statement.executeQuery();
                while (result.next())
                {
                    String sessionId = result.getString("sessionId");
                    expiredSessionIds.add(sessionId);
                    if (Log.isDebugEnabled()) Log.debug("Found expired sessionId="+sessionId);
                }


                //tell the SessionManagers to expire any sessions with a matching sessionId in memory
                Handler[] contexts = _server.getChildHandlersByClass(WebAppContext.class);
                for (int i=0; contexts!=null && i<contexts.length; i++)
                {
                    AbstractSessionManager manager = ((AbstractSessionManager)((WebAppContext)contexts[i]).getSessionHandler().getSessionManager());
                    if (manager instanceof JDBCSessionManager)
                    {
                        ((JDBCSessionManager)manager).expire(expiredSessionIds);
                    }
                }

                //find all sessions that have expired at least a couple of scanIntervals ago and just delete them
                upperBound = _lastScavengeTime - (2 * (_scavengeIntervalSec * 1000));
                if (upperBound > 0)
                {
                    if (Log.isDebugEnabled()) Log.debug("Deleting old expired sessions expired before "+upperBound);
                    statement = connection.prepareStatement(__deleteOldExpiredSessions);
                    statement.setLong(1, upperBound);
                    statement.executeUpdate();
                }
            }
        }
        catch (SQLException e)
        {
            Log.warn("Problem selecting expired sessions", e);
        }
        finally
        {           
            _lastScavengeTime=System.currentTimeMillis();
            if (Log.isDebugEnabled()) Log.debug("Scavenge sweep ended at "+_lastScavengeTime);
            if (connection != null)
            {
                try
                {
                connection.close();
                }
                catch (SQLException e)
                {
                    Log.warn(e);
                }
            }
        }
    }
}

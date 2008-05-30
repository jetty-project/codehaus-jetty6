package org.mortbay.jetty.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.mortbay.log.Log;

/**
 * JDBCSessionManager
 *
 * SessionManager that persists sessions to a database to enable clustering.
 * 
 * Session data is persisted to the JettySessions table:
 * 
 * rowId (unique in cluster: webapp name/path + virtualhost + sessionId)
 * sessionId (unique in a context)
 * lastNode (name of node last handled session)
 * accessTime (time in ms session was accessed)
 * lastAccessTime (previous time in ms session was accessed)
 * createTime (time in ms session created)
 * cookieTime (time in ms session cookie created)
 * map (attribute map)
 */
public class JDBCSessionManager extends AbstractSessionManager
{
    //TODO do we need to persist both the previous access time and the current access time????
    private static final String __createString = "create table JettySessions (rowId varchar(60), sessionId varchar(60), contextPath varchar(60), lastNode varchar(60), accessTime bigint, "+
                                                " lastAccessTime bigint, createTime bigint, cookieTime bigint, map blob, primary key(rowId))";
    
    private static final String __insertString = "insert into JettySessions (rowId, sessionId, contextPath, lastNode, accessTime, lastAccessTime, createTime, cookieTime, map) "+
                                                 " values (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    
    private static final String __deleteString = "delete from JettySessions where rowId = ?";
    
    private static final String __selectString = "select * from JettySessions where sessionId = ? and contextPath = ?";
    
    private static final String __updateString = "update JettySessions set lastNode = ?, accessTime = ?, lastAccessTime = ?, map = ? where rowId = ?";
    
    private static final String __updateNodeString = "update JettySessions set lastNode = ? where rowId = ?";
    
    
    private String _driverClassName;
    private String _connectionUrl;
    private ConcurrentHashMap _sessions;
    
    /**
     * SessionData
     *
     * Persistable data about a session.
     */
    public class SessionData
    {
        private String _id;
        private String _rowId;
        private long _accessed;
        private long _lastAccessed;
        private long _maxIdleMs;
        private long _cookieSet;
        private long _created;
        private Map _attributes;
        private String _lastNode;
        private String _canonicalContext;

        public SessionData (String sessionId)
        {
            _id=sessionId;
            _created=System.currentTimeMillis();
            _accessed = _created;
            _attributes = new ConcurrentHashMap();
        }

        public synchronized String getId ()
        {
            return _id;
        }

        public synchronized long getCreated ()
        {
            return _created;
        }
        
        protected synchronized void setCreated (long ms)
        {
            _created = ms;
        }
        
        public synchronized long getAccessed ()
        {
            return _accessed;
        }
        
        protected synchronized void setAccessed (long ms)
        {
            _accessed = ms;
        }
        
        
        public synchronized void setMaxIdleMs (long ms)
        {
            _maxIdleMs = ms;
        }

        public synchronized long getMaxIdleMs()
        {
            return _maxIdleMs;
        }

        public synchronized void setLastAccessed (long ms)
        {
            _lastAccessed = ms;
        }

        public synchronized long getLastAccessed()
        {
            return _lastAccessed;
        }

        public void setCookieSet (long ms)
        {
            _cookieSet = ms;
        }

        public synchronized long getCookieSet ()
        {
            return _cookieSet;
        }
        
        public synchronized void setRowId (String rowId)
        {
            _rowId=rowId;
        }
        
        protected synchronized String getRowId()
        {
            return _rowId;
        }
        
        protected synchronized Map getAttributeMap ()
        {
            return _attributes;
        }
        
        protected synchronized void setAttributeMap (ConcurrentHashMap map)
        {
            _attributes = map;
        } 
        
        public synchronized void setLastNode (String node)
        {
            _lastNode=node;
        }
        
        public synchronized String getLastNode ()
        {
            return _lastNode;
        }
        
        public synchronized void setCanonicalContext(String str)
        {
            _canonicalContext=str;
        }
        
        public synchronized String getCanonicalContext ()
        {
            return _canonicalContext;
        }
        
        public String toString ()
        {
            return "Session rowId="+_rowId+",id="+_id+",lastNode="+_lastNode+",created="+_created+",accessed="+_accessed+",lastAccessed="+_lastAccessed+",cookieSet="+_cookieSet;
        }
    }

    
    
    /**
     * Session
     *
     * Session instance in memory of this node.
     */
    public class Session extends AbstractSessionManager.Session
    {
        private SessionData _data;
        private boolean _dirty=false;

        /**
         * Session from a request.
         * 
         * @param request
         */
        protected Session (HttpServletRequest request)
        {
         
            super(request);   
            _data = new SessionData(_clusterId);
            _data.setMaxIdleMs(_dftMaxIdleSecs*1000);
            _data.setCanonicalContext(canonicalize(_context.getContextPath()));
        }

        /**
          * Session restored in database.
          * @param row
          */
         protected Session (SessionData data)
         {
             super(data.getCreated(), data.getId());
             _data=data;
             _values=data.getAttributeMap();
         }
        
         protected Map newAttributeMap()
         {
             return _data.getAttributeMap();
         }
         
         public void setAttribute (String name, Object value)
         {
             super.setAttribute(name, value);
             _dirty=true;
         }

         public void removeAttribute (String name)
         {
             super.removeAttribute(name); 
             _dirty=true;
         }

        /** 
         * Entry to session.
         * Called by SessionHandler on inbound request and the session already exists in this node's memory.
         * 
         * @see org.mortbay.jetty.servlet.AbstractSessionManager.Session#access(long)
         */
        protected void access(long time)
        {
            super.access(time);
            _data.setLastAccessed(_data.getAccessed());
            _data.setAccessed(time);
            _dirty=true;
        }

        /** 
         * Exit from session
         * @see org.mortbay.jetty.servlet.AbstractSessionManager.Session#complete()
         */
        protected void complete()
        {
            super.complete();
            if (_dirty)
            {
                try
                {
                    updateSession(_data);
                }
                catch (Exception e)
                {
                    Log.warn("Problem persisting changed session data id="+getId(), e);
                }
                finally
                {
                    _dirty=false;
                }
            }
        }
    }
    
    
    
    
    /**
     * ClassLoadingObjectInputStream
     *
     *
     */
    protected class ClassLoadingObjectInputStream extends ObjectInputStream
    {
        public ClassLoadingObjectInputStream(java.io.InputStream in) throws IOException
        {
            super(in);
        }

        public ClassLoadingObjectInputStream () throws IOException
        {
            super();
        }

        public Class resolveClass (java.io.ObjectStreamClass cl) throws IOException, ClassNotFoundException
        {
            try
            {
                return Class.forName(cl.getName(), false, Thread.currentThread().getContextClassLoader());
            }
            catch (ClassNotFoundException e)
            {
                return super.resolveClass(cl);
            }
        }
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
   

  
   
    /** 
     * A session has been requested by it's id on this node.
     * 
     * Load the session by id AND context path from the database.
     * Multiple contexts may share the same session id (due to dispatching)
     * but they CANNOT share the same contents.
     * 
     * Check if last node id is my node id, if so, then the session we have
     * in memory cannot be stale. If another node used the session last, then
     * we need to refresh from the db.
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#getSession(java.lang.String)
     */
    public Session getSession(String idInCluster)
    {
        Session session = (Session)_sessions.get(idInCluster);
        
        synchronized (this)
        {        
            try
            {
                SessionData data = loadSession(idInCluster, canonicalize(_context.getContextPath()));
                if (data != null)
                {
                    if (!data.getLastNode().equals(getIdManager().getWorkerName()) || session==null)
                    {
                        //session last used on a different node, or we don't have it in memory
                        session = new Session(data);
                        _sessions.put(idInCluster, session);

                        //TODO is this the best way to do this? Or do this on the way out using
                        //the _dirty flag?
                        updateSessionNode(data);
                    }
                    else
                        if (Log.isDebugEnabled()) Log.debug("Session not stale "+session._data);
                    //session in db shares same id, but is not for this context
                }
                else
                {
                    //No session in db with matching id and context path.
                    session=null;
                    if (Log.isDebugEnabled()) Log.debug("No session in database matching id="+idInCluster);
                }
                
                return session;
            }
            catch (Exception e)
            {
                Log.warn("Unable to load session from database", e);
                return null;
            }
        }
    }

   
    public Map getSessionMap()
    {
       return Collections.unmodifiableMap(_sessions);
    }

    
    public int getSessions()
    {
        int size = 0;
        synchronized (this)
        {
            size = _sessions.size();
        }
        return size;
    }


    public void doStart() throws Exception
    {
        if (_sessionIdManager==null)
            throw new IllegalStateException("No session id manager defined");
        
        if (_driverClassName==null)
            _driverClassName = ((JDBCSessionIdManager)_sessionIdManager).getDriverClassName();
        
        if (_connectionUrl==null)
            _connectionUrl = ((JDBCSessionIdManager)_sessionIdManager).getConnectionUrl();
        
        _sessions = new ConcurrentHashMap();
        prepareTables();
        super.doStart();
    }
    
    protected void invalidateSessions()
    {
        //Do nothing - we don't want to remove and
        //invalidate all the sessions because this
        //method is called from doStop(), and just
        //because this context is stopping does not
        //mean that we should remove the session from
        //any other nodes
    }

   
    protected void removeSession(String idInCluster)
    {
        synchronized (this)
        {
           try
           {
               Session session = (Session)_sessions.remove(idInCluster);
               deleteSession(session._data);
           }
           catch (Exception e)
           {
               Log.warn("Problem deleting session id="+idInCluster, e);
           }
        }
    }


    /** 
     * Add a newly created session to our in-memory list for this node and persist it.
     * 
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#addSession(org.mortbay.jetty.servlet.AbstractSessionManager.Session)
     */
    protected void addSession(AbstractSessionManager.Session session)
    {
        if (session==null)
            return;
        
        synchronized (this)
        {
            _sessions.put(session.getClusterId(), session);
            //TODO or delay the store until exit out of session? If we crash before we store it
            //then session data will be lost.
            try
            {
                storeSession(((JDBCSessionManager.Session)session)._data);
            }
            catch (Exception e)
            {
                Log.warn("Unable to store new session id="+session.getId() , e);
            }
        }
    }


    /** 
     * Make a new Session.
     * 
     * @see org.mortbay.jetty.servlet.AbstractSessionManager#newSession(javax.servlet.http.HttpServletRequest)
     */
    protected AbstractSessionManager.Session newSession(HttpServletRequest request)
    {
        return new Session(request);
    }
    
    
    
    /**
     * Load a session from the database
     * @param id
     * @return
     * @throws Exception
     */
    protected SessionData loadSession (String id, String canonicalContextPath)
    throws Exception
    {
        SessionData data = null;
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try
        {
            statement = connection.prepareStatement(__selectString);
            statement.setString(1, id);
            statement.setString(2, canonicalContextPath);
            ResultSet result = statement.executeQuery();
            if (result.next())
            {
               data = new SessionData(id);
               data.setRowId(result.getString("rowId"));
               data.setCookieSet(result.getLong("cookieTime"));
               data.setLastAccessed(result.getLong("lastAccessTime")); //TODO is this necessary?
               data.setAccessed (result.getLong("accessTime"));
               data.setCreated(result.getLong("createTime"));
               data.setLastNode(result.getString("lastNode"));
               data.setCanonicalContext(result.getString("contextPath"));
               Blob blob = result.getBlob("map");
               ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(blob.getBinaryStream());
               Object o = ois.readObject();
               data.setAttributeMap((ConcurrentHashMap)o);
               ois.close();
               
               if (Log.isDebugEnabled())
                   Log.debug("LOADED session "+data);
            }
            return data;
        }   
        finally
        {
            if (connection!=null)
                connection.close();
        }
    }
    
    /**
     * Insert a session into the database.
     * 
     * @param data
     * @throws Exception
     */
    protected void storeSession (SessionData data)
    throws Exception
    {
        if (data==null)
            return;
        
        //put into the database      
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try
        {   
            String rowId = calculateRowId(data);
            data.setRowId(rowId); //set it on the in-memory data as well as in db
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(__insertString);
            statement.setString(1, rowId); //rowId
            statement.setString(2, data.getId()); //session id
            statement.setString(3, data.getCanonicalContext()); //context path
            statement.setString(4, getIdManager().getWorkerName());//my node id
            statement.setLong(5, data.getAccessed());//accessTime
            statement.setLong(6, data.getLastAccessed()); //lastAccessTime
            statement.setLong(7, data.getCreated()); //time created
            statement.setLong(8, data.getCookieSet());//time cookie was set
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data.getAttributeMap());
            byte[] bytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            statement.setBinaryStream(9, bais, bytes.length);//attribute map as blob
            
            statement.executeUpdate();
            
            if (Log.isDebugEnabled())
                Log.debug("Stored session "+data);
        }   
        finally
        {
            if (connection!=null)
                connection.close();
        }
    }
    
    
    /**
     * Update data on an existing persisted session.
     * 
     * @param data
     * @throws Exception
     */
    protected void updateSession (SessionData data)
    throws Exception
    {
        if (data==null)
            return;
        
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try
        {              
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(__updateString);     
            statement.setString(1, getIdManager().getWorkerName());//my node id
            statement.setLong(2, data.getAccessed());//accessTime
            statement.setLong(3, data.getLastAccessed()); //lastAccessTime
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(data.getAttributeMap());
            byte[] bytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            
            statement.setBinaryStream(4, bais, bytes.length);//attribute map as blob 
            statement.setString(5, data.getRowId()); //rowId
            statement.executeUpdate();
            
            if (Log.isDebugEnabled())
                Log.debug("Updated session "+data);
        }
        finally
        {
            if (connection!=null)
                connection.close();
        }
    }
    
    
    /**
     * Update the node on which the session was last seen to be my node.
     * 
     * @param data
     * @throws Exception
     */
    protected void updateSessionNode (SessionData data)
    throws Exception
    {
        String nodeId = getIdManager().getWorkerName();
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try
        {            
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(__updateNodeString);
            statement.setString(1, nodeId);
            statement.setString(2, data.getRowId());
            statement.executeUpdate();
            statement.close();
            if (Log.isDebugEnabled())
                Log.debug("Updated last node for session id="+data.getId()+", lastNode = "+nodeId);
        }
        finally
        {
            if (connection!=null)
                connection.close();
        }
    }

    
    /**
     * Delete a session from the database. Should only be called
     * when the session has been invalidated.
     * 
     * @param data
     * @throws Exception
     */
    protected void deleteSession (SessionData data)
    throws Exception
    {
        Connection connection = getConnection();
        PreparedStatement statement = null;
        try
        {
            connection.setAutoCommit(true);
            statement = connection.prepareStatement(__deleteString);
            statement.setString(1, data.getRowId());
            statement.executeUpdate();
            if (Log.isDebugEnabled())
                Log.debug("Deleted Session "+data);
        }
        finally
        {
            if (connection!=null)
                connection.close();
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
            String tableName = "JettySessions";
            connection = getConnection();
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

    /**
     * Calculate a unique id for this session across the cluster.
     * 
     * Unique id is composed of: contextpath_virtualhost0_sessionid
     * @param data
     * @return
     */
    private String calculateRowId (SessionData data)
    {
        String rowId = canonicalize(_context.getContextPath());
        String[] vhosts = _context.getContextHandler().getVirtualHosts();
        rowId = rowId + "_" + ((vhosts==null||vhosts[0]==null?"":vhosts[0]));
        rowId = rowId+"_"+data.getId();
        return rowId;
    }
    
    private String canonicalize (String path)
    {
        if (path==null)
            return "";
        
        return path.replace('/', '_').replace('.','_').replace('\\','_');
    }
}

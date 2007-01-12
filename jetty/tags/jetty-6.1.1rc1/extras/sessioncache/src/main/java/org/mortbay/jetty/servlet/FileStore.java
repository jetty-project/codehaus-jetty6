package org.mortbay.jetty.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.mortbay.jetty.servlet.CacheSessionManager.Store;
import org.mortbay.log.Log;

public class FileStore implements Store
{
    private String _sessionsDir;
    private File _sessions;

    public FileStore(String cacheDirectory)
    {
        _sessionsDir = cacheDirectory;
    }

    public FileStore()
    {
        this(System.getProperty("java.io.tmp",".")+"/org.mortbay.jetty.sessions");
    }

    public void setContext(String contextName)
    {
        createSessionDir(_sessionsDir+"/"+contextName);
    }
    
    private void createSessionDir(String path)
    {
        _sessions = new File(path);
        if (_sessions.exists()) _sessions.delete();
        _sessions.mkdir();        
    }

    public Object get(String id)
    {
        File sessionFile = new File(_sessions.getAbsolutePath()+"/"+id);
        Object session;
        if (sessionFile.exists())
        {
            try
            {
                FileInputStream out = new FileInputStream(sessionFile);
                ObjectInputStream in = new ObjectInputStream(out);
                session = in.readObject();
                in.close();
                return session;
            }
            catch (IOException e)
            {
                Log.debug(e);
            }
            catch (ClassNotFoundException e)
            {
                Log.debug(e);
            }
        }
        return null;
    }

    public void add(String id, Serializable session)
    {
        FileOutputStream out = null;
        ObjectOutputStream oos = null;
        
        try
        {
            out = new FileOutputStream(_sessions.getAbsolutePath()+"/"+((HttpSession)session).getId());
            oos = new ObjectOutputStream(out);
            oos.writeObject(session);
            oos.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void remove(String id)
    {
        File sessionFile = new File(_sessions.getAbsolutePath()+id);
        if (sessionFile.exists())
        {
            sessionFile.delete();
        }
    }

    public List getKeys()
    {
        String[] sessionArray = _sessions.list();
        if (sessionArray != null)
        {
            return Arrays.asList(sessionArray);
        }
        return null;
    }

}

package org.mortbay.jetty.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.mortbay.jetty.servlet.CacheSessionManager.AttributesFactory;
import org.mortbay.log.Log;

public class FileAttributesFactory implements AttributesFactory
{
    // TODO replace this with ehcache disk store.
    
    private File _sessionAttributeDir;

    public FileAttributesFactory(String dir)
    {
        _sessionAttributeDir = new File(dir);
        if (_sessionAttributeDir.exists()) _sessionAttributeDir.delete();
        _sessionAttributeDir.mkdir();        
    }

    
    public FileAttributesFactory()
    {
        this(System.getProperty("java.io.tmpdir","."));
    }


    public Map create(String id, String context)
    {
        // TODO Auto-generated method stub
        return new FileSessionAttributeMap(id, context);
    }

    class FileSessionAttributeMap implements Map
    {
        private HashMap _sessionAttributes;
        private String _clusterId;
        private String _context;
        private File _contextSubDir;

        public FileSessionAttributeMap(String clusterId, String context)
        {
            _clusterId = clusterId;
            _context = context;
            _sessionAttributes = new HashMap();
            _contextSubDir = new File(_sessionAttributeDir+"/"+_context);
            
            if (!_contextSubDir.exists())
                _contextSubDir.mkdir();
            else if(!_contextSubDir.isDirectory())
                throw new IllegalStateException("context sub directory is not a directory");
            
            writeMap();
        }

        public void clear()
        {
            if (_sessionAttributes != null)
            _sessionAttributes.clear();
            writeMap();

        }

        public boolean containsKey(Object obj)
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.containsKey(obj);
            return false;
        }

        public boolean containsValue(Object obj)
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.containsValue(obj);
            return false;
        }

        public Set entrySet()
        {
            readMap();
            if (_sessionAttributes != null)
                _sessionAttributes.entrySet();
            return null;
        }

        public Object get(Object obj)
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.get(obj);
            return null;
        }

        public boolean isEmpty()
        {
            readMap();
            if (_sessionAttributes != null)
                _sessionAttributes.isEmpty();
            return true;
        }

        public Set keySet()
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.keySet();
            return null;
        }

        public Object put(Object key, Object value)
        {
            readMap();
            Object obj = null;
            if (_sessionAttributes != null)
            {
                obj = _sessionAttributes.put(key, value);
                writeMap();
            }
            return obj;
        }

        public void putAll(Map map)
        {
            readMap();
            if (_sessionAttributes != null)
            {
                _sessionAttributes.putAll(map);
                writeMap();
            }
        }

        public Object remove(Object obj)
        {
            readMap();
            Object object = null;
            if (_sessionAttributes != null)
            {
                object = _sessionAttributes.remove(obj);
                writeMap();
            }
            return object;
        }

        public int size()
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.size();
            return 0;
        }

        public Collection values()
        {
            readMap();
            if (_sessionAttributes != null)
                return _sessionAttributes.values();
            return null;
        }
        
        private void writeMap()
        {
            FileOutputStream out = null;
            ObjectOutputStream oos = null;
            
            try
            {
                out = new FileOutputStream(_contextSubDir.getAbsolutePath()+"/"+_clusterId);
                oos = new ObjectOutputStream(out);
                oos.writeObject(_sessionAttributes);
                oos.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        
        public void readMap()
        {
            File sessionAttributeFile = new File(_contextSubDir.getAbsolutePath()+"/"+_clusterId);
            if (sessionAttributeFile.exists())
            {
                try
                {
                    FileInputStream fileIn = new FileInputStream(sessionAttributeFile);
                    ObjectInputStream in = new ObjectInputStream(fileIn);
                    _sessionAttributes = (HashMap)in.readObject();
                    in.close();
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
        }

    }
}
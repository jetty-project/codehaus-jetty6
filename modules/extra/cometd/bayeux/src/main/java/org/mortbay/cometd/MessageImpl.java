//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.cometd;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.cometd.Bayeux;
import org.cometd.Message;
import org.mortbay.util.ajax.JSON;


public class MessageImpl extends HashMap<String, Object> implements Message, org.mortbay.util.ajax.JSON.Generator
{
    MessagePool _pool;
    String _clientId;
    String _json;
    String _channel;
    String _id;
    Object _data;
    Message _associated;
    AtomicInteger _refs=new AtomicInteger();

    /* ------------------------------------------------------------ */
    public MessageImpl()
    {
        super(8);
    }
    
    /* ------------------------------------------------------------ */
    public MessageImpl(MessagePool bayeux)
    {
        super(8);
        _pool=bayeux;
    }
    
    /* ------------------------------------------------------------ */
    public int getRefs()
    {
        return _refs.get();
    }
    
    /* ------------------------------------------------------------ */
    public void incRef()
    {
        _refs.getAndIncrement();
    }

    /* ------------------------------------------------------------ */
    public void decRef()
    {
        int r= _refs.decrementAndGet();
        if (r==0 && _pool!=null)
            _pool.recycleMessage(this);
        else if (r<0)
            throw new IllegalStateException();
    }

    /* ------------------------------------------------------------ */
    public String getChannel()
    {
        return _channel;
    }
    
    /* ------------------------------------------------------------ */
    public String getClientId()
    {
        if (_clientId==null)
            _clientId=(String)get(Bayeux.CLIENT_FIELD);
        return _clientId;
    }

    /* ------------------------------------------------------------ */
    public String getId()
    {
        return _id;
    }

    /* ------------------------------------------------------------ */
    public Object getData()
    {
        return _data;
    }
    
    /* ------------------------------------------------------------ */
    public void addJSON(StringBuffer buffer)
    {
        buffer.append(getJSON());
    }

    /* ------------------------------------------------------------ */
    public String getJSON()
    {
        if (_json==null)
        {
            JSON json=_pool==null?JSON.getDefault():_pool.getMsgJSON();
            StringBuffer buf = new StringBuffer(json.getStringBufferSize());
            synchronized(buf)
            {
                json.appendMap(buf,this);
                _json=buf.toString();
            }
        }
        return _json;
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#clear()
     */
    @Override
    public void clear()
    {
        _json=null;
        _id=null;
        _channel=null;
        _clientId=null;
        setAssociated(null);
        _refs.set(0);
        Iterator<Map.Entry<String,Object>> iterator=super.entrySet().iterator();
        while(iterator.hasNext())
        {
            Map.Entry<String, Object> entry=iterator.next();
            String key=entry.getKey();
            if (Bayeux.CHANNEL_FIELD.equals(key))
                entry.setValue(null);
            else if (Bayeux.ID_FIELD.equals(key))
                entry.setValue(null);
            else if (Bayeux.TIMESTAMP_FIELD.equals(key))
                entry.setValue(null);
            else if (Bayeux.DATA_FIELD.equals(key))
                entry.setValue(null);
            else
                iterator.remove();
        }
        super.clear();
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(String key, Object value)
    {
        _json=null;
        if (Bayeux.CHANNEL_FIELD.equals(key))
            _channel=(String)value;
        else if (Bayeux.ID_FIELD.equals(key))
            _id=value.toString();
        else if (Bayeux.CLIENT_FIELD.equals(key))
            _clientId=(String)value;
        else if (Bayeux.DATA_FIELD.equals(key))
            _data=value;
        return super.put(key,value);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#putAll(java.util.Map)
     */
    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        _json=null;
        super.putAll(m);
        _channel=(String)get(Bayeux.CHANNEL_FIELD);
        Object id=get(Bayeux.ID_FIELD);
        _id=id==null?null:id.toString();
        _data=get(Bayeux.DATA_FIELD);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#remove(java.lang.Object)
     */
    @Override
    public Object remove(Object key)
    {
        _json=null;
        if (Bayeux.CHANNEL_FIELD.equals(key))
            _channel=null;
        else if (Bayeux.ID_FIELD.equals(key))
            _id=null;
        else if (Bayeux.DATA_FIELD.equals(key))
            _data=null;
        return super.remove(key);
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#entrySet()
     */
    @Override
    public Set<java.util.Map.Entry<String, Object>> entrySet()
    {
        return Collections.unmodifiableSet(super.entrySet());
    }

    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see java.util.HashMap#keySet()
     */
    @Override
    public Set<String> keySet()
    {
        return Collections.unmodifiableSet(super.keySet());
    }

    /* ------------------------------------------------------------ */
    public Message getAssociated()
    {
        return _associated;
    }

    /* ------------------------------------------------------------ */
    public void setAssociated(Message associated)
    {
        if (_associated!=associated)
        {
            if (_associated!=null)
                ((MessageImpl)_associated).decRef();
            _associated=associated;
            if (_associated!=null)
                ((MessageImpl)_associated).incRef();
        }
    }
    

}
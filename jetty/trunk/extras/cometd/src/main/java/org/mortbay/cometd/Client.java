// ========================================================================
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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
//========================================================================

package org.mortbay.cometd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;
import org.mortbay.util.ajax.Continuation;


/* ------------------------------------------------------------ */
/**
 * @author gregw
 *
 */
public class Client
{
    private String _id;
    private String _type;
    private ArrayList _messages=new ArrayList();
    private Object _continuations;
    private int _responsesPending;
    private Object _dataFilters=null;
    
    /* ------------------------------------------------------------ */
    Client()
    {
        _id=Long.toString(System.identityHashCode(this)*System.currentTimeMillis(),36);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param filter
     */
    public void addDataFilter(DataFilter filter)
    {
        _dataFilters=LazyList.add(_dataFilters,filter);
    }
    
    /* ------------------------------------------------------------ */
    public String getConnectionType()
    {
        return _type;
    }
    
    /* ------------------------------------------------------------ */
    public String getId()
    {
        return _id;
    }

   
    /* ------------------------------------------------------------ */
    public boolean hasMessages()
    {
        synchronized (this)
        {
            return _messages!=null && _messages.size()>0;
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param filter
     */
    public void removeDataFilter(DataFilter filter)
    {
        _dataFilters=LazyList.remove(_dataFilters,filter);
    }
    
    /* ------------------------------------------------------------ */
    public List takeMessages()
    {
        synchronized (this)
        {
            if (_messages==null || _messages.size()==0)
                return null;
            List list = _messages;
            _messages=new ArrayList();
            return list;
        }
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _id;
    }
    
    
    /* ------------------------------------------------------------ */
    void addContinuation(Continuation continuation)
    {
        synchronized (this)
        {
            _continuations=LazyList.add(_continuations,continuation);
        }
    }

    /* ------------------------------------------------------------ */
    void deliver(Map message)
    {
        synchronized (this)
        {
            _messages.add(message);
            
            if (_responsesPending<1)
            {
                if (_continuations!=null)
                {
                    for (int i=0;i<LazyList.size(_continuations);i++)
                    {
                        Continuation continuation=(Continuation)LazyList.get(_continuations,i);
                        continuation.resume();
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    Object filterData(Object data,Client from)
    {
        synchronized (this)
        {
            try
            {
                for (int f=0;f<LazyList.size(_dataFilters);f++)
                {
                    data=((DataFilter)LazyList.get(_dataFilters,f)).filter(data, from);
                    if (data==null)
                        return null;
                }
            }
            catch (IllegalStateException e)
            {
                Log.debug(e);
                return null;
            }
        }
        return data;
    }
    
    /* ------------------------------------------------------------ */
    void removeContinuation(Continuation continuation)
    {
        synchronized (this)
        {
            _continuations=LazyList.remove(_continuations,continuation);
        }
    }

    /* ------------------------------------------------------------ */
    int responded()
    {
        synchronized (this)
        {
            return _responsesPending--;
        }
    }

    /* ------------------------------------------------------------ */
    int responsePending()
    {
        synchronized (this)
        {
            return ++_responsesPending;
        }
    }

    /* ------------------------------------------------------------ */
    void setConnectionType(String type)
    {
        _type=type;
    }
    
    /* ------------------------------------------------------------ */
    void setId(String _id)
    {
        this._id=_id;
    }
}
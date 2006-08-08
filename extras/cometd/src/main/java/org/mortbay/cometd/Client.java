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
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.mortbay.util.LazyList;
import org.mortbay.util.ajax.Continuation;


/* ------------------------------------------------------------ */
/**
 * @author gregw
 *
 */
public class Client 
{
    private String _id=System.identityHashCode(this)+Long.toString(System.currentTimeMillis(),36);
    private String _type;
    private ArrayList _messages=new ArrayList();
    private Object _continuations;

    /* ------------------------------------------------------------ */
    public Client()
    {
    }

    /* ------------------------------------------------------------ */
    public String getConnectionType()
    {
        return _type;
    }
    
    /* ------------------------------------------------------------ */
    public void setConnectionType(String type)
    {
        _type=type;
    }
    
    /* ------------------------------------------------------------ */
    public String getId()
    {
        return _id;
    }
    
    /* ------------------------------------------------------------ */
    public void setId(String _id)
    {
        this._id=_id;
    }

    /* ------------------------------------------------------------ */
    public void removeContinuation(Continuation continuation)
    {
        synchronized (this)
        {
            _continuations=LazyList.remove(_continuations,continuation);
        }
    }

    /* ------------------------------------------------------------ */
    public void addContinuation(Continuation continuation)
    {
        synchronized (this)
        {
            _continuations=LazyList.add(_continuations,continuation);
        }
    }

    /* ------------------------------------------------------------ */
    public void send(Map message)
    {
        synchronized (this)
        {
            _messages.add(message);
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

    /* ------------------------------------------------------------ */
    public List getMessages()
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
    public boolean hasMessages()
    {
        synchronized (this)
        {
            return _messages!=null && _messages.size()>0;
        }
    }
}
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
// ========================================================================

package org.mortbay.cometd;

import java.util.HashMap;

import org.mortbay.util.LazyList;

public class Channel
{
    private Bayeux _bayeux;
    private String _id;
    private Object _subscribers=null;
    private long _nextMsgId;
    
    public Channel(String id,Bayeux bayeux)
    {
        _id=id;
        _bayeux=bayeux;
    }
    
    public String getId()
    {
        return _id;
    }
    
    public void addSubscriber(Client client)
    {
        synchronized (this)
        {
            _subscribers=LazyList.add(_subscribers,client);
        }
    }

    /* ------------------------------------------------------------ */
    public void removeSubscriber(Client client)
    {
        synchronized (this)
        {
            _subscribers=LazyList.remove(_subscribers,client);
        }    
    }
    
    /* ------------------------------------------------------------ */
    public void send(Object data)
    {
        HashMap msg = new HashMap();
        msg.put("channel",_id);
        msg.put("data",data);
        msg.put("timestamp",_bayeux.getTimeOnServer());
        
        synchronized (this)
        {
            long id=this.hashCode()*msg.hashCode();
            id=id<0?-id:id;
            msg.put("id",Long.toString(id,36)+Long.toString(_nextMsgId++,36));
            int subscribers=LazyList.size(_subscribers);
            for (int i=0;i<subscribers;i++)
                ((Client)LazyList.get(_subscribers,i)).send(msg);
        }
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _id+"("+_subscribers+")";
    }
}

    


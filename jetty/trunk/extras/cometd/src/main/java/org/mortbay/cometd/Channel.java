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

import org.mortbay.log.Log;
import org.mortbay.util.LazyList;

public class Channel
{
    private Bayeux _bayeux;
    private String _id;
    private long _nextMsgId;
    private Object _subscribers=null;
    private Object _dataFilters=null;
    
    Channel(String id,Bayeux bayeux)
    {
        _id=id;
        _bayeux=bayeux;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param client
     */
    public void addSubscriber(Client client)
    {
        synchronized (this)
        {
            _subscribers=LazyList.add(_subscribers,client);
        }
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
    /**
     * @param filter
     */
    public void removeDataFilter(DataFilter filter)
    {
        _dataFilters=LazyList.remove(_dataFilters,filter);
    }
    
    
    /* ------------------------------------------------------------ */
    /**
     * @return
     */
    public String getId()
    {
        return _id;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param client
     */
    public void removeSubscriber(Client client)
    {
        synchronized (this)
        {
            _subscribers=LazyList.remove(_subscribers,client);
        }    
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param data
     */
    public void publish(Object data, Client from)
    {
        try
        {
            for (int f=0;f<LazyList.size(_dataFilters);f++)
            {
                data=((DataFilter)LazyList.get(_dataFilters,f)).filter(data, from);
                if (data==null)
                    return;
            }
        }
        catch (IllegalStateException e)
        {
            Log.debug(e);
            return;
        }
        
        HashMap msg = new HashMap();
        msg.put(Bayeux.CHANNEL_ATTR,_id);
        msg.put(Bayeux.TIMESTAMP_ATTR,_bayeux.getTimeOnServer());
        
        synchronized (this)
        {
            long id=this.hashCode()*msg.hashCode();
            id=id<0?-id:id;
            msg.put("id",Long.toString(id,36)+Long.toString(_nextMsgId++,36));
            int subscribers=LazyList.size(_subscribers);
            for (int i=0;i<subscribers;i++)
            {
                Client client= (Client)LazyList.get(_subscribers,i);
                Object client_data=client.filterData(data,from);
                if (client_data!=null)
                {
                    msg.put(Bayeux.DATA_ATTR,client_data);
                    ((Client)LazyList.get(_subscribers,i)).deliver(msg);
                }
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param client The client for which this token will be valid
     * @param subscribe True if this token may be used for subscriptions
     * @param send True if this token may be used for send
     * @param oneTime True if this token may only be used in one request batch.
     * @return A new token that can be used for subcriptions and or sending.
     */
    public String getToken(Client client, boolean subscribe, boolean send, boolean oneTime)
    {
        String token=Long.toString(_bayeux.getRandom(client.hashCode()),36);
        // TODO register somewhere ?
        return token;
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _id+"("+_subscribers+")";
    }
}

    


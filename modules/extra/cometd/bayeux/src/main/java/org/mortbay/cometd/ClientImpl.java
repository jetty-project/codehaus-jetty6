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
import java.util.Collections;
import java.util.EventListener;
import java.util.List;

import org.mortbay.util.LazyList;

import dojox.cometd.Bayeux;
import dojox.cometd.Client;
import dojox.cometd.Extension;
import dojox.cometd.Listener;
import dojox.cometd.Message;
import dojox.cometd.MessageListener;
import dojox.cometd.RemoveListener;


/* ------------------------------------------------------------ */
/**
 * 
 * @author gregw
 */
public class ClientImpl implements Client
{
    private String _id;
    private String _type;
    private Object _messageQ=null;
    private int _responsesPending;
    private ChannelImpl[] _subscriptions=new ChannelImpl[0]; // copy of write
    private boolean _JSONCommented;
    private Listener _listener;
    private List<RemoveListener> _rListeners;
    private List<MessageListener> _mListeners;
    protected AbstractBayeux _bayeux;
    private String _browserId;
    private int _adviseVersion;
    private int _batch;
    private int _maxQueue;
    private long _timeout;

    /* ------------------------------------------------------------ */
    protected ClientImpl(AbstractBayeux bayeux)
    {
        _bayeux=bayeux;
        _maxQueue=-1;
        _bayeux.addClient(this,null);
        if (_bayeux.isLogInfo())
            _bayeux.logInfo("newClient: "+this);
    }
    
    /* ------------------------------------------------------------ */
    protected ClientImpl(AbstractBayeux bayeux, String idPrefix)
    {
        _bayeux=bayeux;
        _maxQueue=0;
        
        _bayeux.addClient(this,idPrefix);
        
        if (_bayeux.isLogInfo())
            _bayeux.logInfo("newClient: "+this);
        
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @deprecated
     */
    protected ClientImpl(AbstractBayeux bayeux, String idPrefix, Listener listener)
    {
        this(bayeux,idPrefix);
        _listener=listener;
    }
    

    /* ------------------------------------------------------------ */
    /**
     * @deprecated use {@link Channel#publish(Client, Object, String)}
     * @see dojox.cometd.Client#publish(java.lang.String, java.lang.Object, java.lang.String)
     */
    public void publish(String toChannel, Object data, String msgId)
    {
        _bayeux.getChannel(toChannel).publish(this,data,msgId);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @deprecated use {@link Channel#subscribe(Client)}
     * @see dojox.cometd.Client#subscribe(java.lang.String)
     */
    public void subscribe(String toChannel)
    {
        _bayeux.subscribe(toChannel,this);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @deprecated use {@link Channel#unsubscribe(Client)}
     * @see dojox.cometd.Client#unsubscribe(java.lang.String)
     */
    public void unsubscribe(String toChannel)
    {
        _bayeux.unsubscribe(toChannel,this);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated use {@link #deliver(Client, String, Object, String)}
     */
    public void deliver(Client from, Message message)
    {
        for (Extension e:_bayeux._extensions)
            message=e.send(message);
        doDelivery(from,message);
    }

    /* ------------------------------------------------------------ */
    public void deliver(Client from, String toChannel, Object data, String id)
    {
        // TODO recycle maps
        Message message=_bayeux.newMessage();
        message.put(Bayeux.CHANNEL_FIELD,toChannel);
        message.put(Bayeux.DATA_FIELD,data);
        if (id!=null)   
            message.put(Bayeux.ID_FIELD,id);

        for (Extension e:_bayeux._extensions)
            message=e.send(message);
        doDelivery(from,message);
        
        ((MessageImpl)message).decRef();
    }
    
    /* ------------------------------------------------------------ */
    protected void doDelivery(Client from, Message message)
    {
        synchronized(this)
        {
            ((MessageImpl)message).incRef();
            
            if (_maxQueue<0)
                _messageQ=LazyList.add(_messageQ,message);
            else if (_maxQueue>0)
            { 
                if (LazyList.size(_messageQ)>=_maxQueue)
                    _messageQ=LazyList.remove(_messageQ,0);
                _messageQ=LazyList.add(_messageQ,message);
            }
            
            if (_batch==0 &&  _responsesPending<1)
                resume();
            
            if (_mListeners!=null)
                for (MessageListener l:_mListeners)
                    l.deliver(from,this,message);
        }
    }


    /* ------------------------------------------------------------ */
    public void startBatch()
    {
        synchronized(this)
        {
            _batch++;
        }
    }
    
    /* ------------------------------------------------------------ */
    public void endBatch()
    {
        synchronized(this)
        {
            if (--_batch==0 && LazyList.size(_messageQ)>0 && _responsesPending<1)
                resume();
        }
    }
    
    /* ------------------------------------------------------------ */
    public String getConnectionType()
    {
        return _type;
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.C#getId()
     */
    public String getId()
    {
        return _id;
    }
   
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.C#hasMessages()
     */
    public boolean hasMessages()
    {
        synchronized(this)
        {
            return LazyList.size(_messageQ)>0;
        }
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return the commented
     */
    public boolean isJSONCommented()
    {
        synchronized(this)
        {
            return _JSONCommented;
        }
    }

    /* ------------------------------------------------------------ */
    public boolean isLocal()
    {
        return true;
    }
       
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see dojox.cometd.Client#remove(boolean)
     */
    public void remove(boolean timeout)
    {
        synchronized(this)
        {
            Client client=_bayeux.removeClient(_id);   
            if (_bayeux.isLogInfo())
                _bayeux.logInfo("Remove client "+client+" timeout="+timeout); 
            if (_rListeners!=null)
                for (RemoveListener l:_rListeners)
                    l.removed(_id, timeout);
            if (_browserId!=null)
                _bayeux.clientOffBrowser(getBrowserId(),_id);
            _browserId=null;
        }
        resume();
        
    }
    
    /* ------------------------------------------------------------ */
    public int responded()
    {
        synchronized(this)
        {
            return _responsesPending--;
        }
    }

    /* ------------------------------------------------------------ */
    public int responsePending()
    {
        synchronized(this)
        {
            return ++_responsesPending;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Called by deliver to resume anything waiting on this client.
     */
    public void resume()
    {
    }

    /* ------------------------------------------------------------ */
    /**
     * @param commented the commented to set
     */
    public void setJSONCommented(boolean commented)
    {
        synchronized(this)
        {
            _JSONCommented=commented;
        }
    }

    /* ------------------------------------------------------------ */
    public void setListener(Listener listener)
    {
        synchronized(this)
        {
            if (_listener!=null)
                removeListener(_listener);
            _listener=listener;
            if (_listener!=null)
                addListener(_listener);
        }
    }

    /* ------------------------------------------------------------ */
    public Listener getListener()
    {
        return _listener;
    }

    /* ------------------------------------------------------------ */
    /*
     * @return the number of messages queued
     */
    public int getMessages()
    {
        synchronized(this)
        {
            return LazyList.size(_messageQ);
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.C#takeMessages()
     */
    public List<Message> takeMessages()
    {
        synchronized(this)
        {
            switch (LazyList.size(_messageQ))
            {
                case 0: return null;
                case 1: 
                    Message message = (Message)LazyList.get(_messageQ,0);
                    _messageQ=null;
                    return Collections.singletonList(message);
                default:
                    List<Message> messages = LazyList.getList(_messageQ);
                    _messageQ=null;
                    return messages;
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /* (non-Javadoc)
     * @see org.mortbay.cometd.C#takeMessages()
     */
    public Message takeMessage()
    {
        synchronized(this)
        {
            switch (LazyList.size(_messageQ))
            {
                case 0: return null;
                case 1: 
                {
                    Message message = (Message)LazyList.get(_messageQ,0);
                    _messageQ=null;
                    return message;
                }
                default:
                {
                    Message message = (Message)LazyList.get(_messageQ,0);
                    _messageQ=LazyList.remove(_messageQ,0);
                    return message;
                }
            }
        }
    }

    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _id;
    }

    /* ------------------------------------------------------------ */
    protected void addSubscription(ChannelImpl channel)
    {
        synchronized (this)
        {
            _subscriptions=(ChannelImpl[])LazyList.addToArray(_subscriptions,channel,null);
        }
    }

    /* ------------------------------------------------------------ */
    protected void removeSubscription(ChannelImpl channel)
    {
        synchronized (this)
        {
            _subscriptions=(ChannelImpl[])LazyList.removeFromArray(_subscriptions,channel);
        }
    }

    /* ------------------------------------------------------------ */
    protected void setConnectionType(String type)
    {
        synchronized (this)
        {
            _type=type;
        }
    }

    /* ------------------------------------------------------------ */
    protected void setId(String _id)
    {
        synchronized (this)
        {
            this._id=_id;
        }
    }

    /* ------------------------------------------------------------ */
    protected void unsubscribeAll()
    {
        ChannelImpl[] subscriptions;
        synchronized(this)
        {
            _messageQ=null;
            subscriptions=_subscriptions;
            _subscriptions=new ChannelImpl[0];
        }
        for (ChannelImpl channel : subscriptions)
            channel.unsubscribe(this);
        
    }

    /* ------------------------------------------------------------ */
    public void setBrowserId(String id)
    {
        if (_browserId!=null && !_browserId.equals(id))
            _bayeux.clientOffBrowser(_browserId,_id);
        _browserId=id;
        if (_browserId!=null)
            _bayeux.clientOnBrowser(_browserId,_id);
    }

    /* ------------------------------------------------------------ */
    public String getBrowserId()
    {
        return _browserId;
    }
    
    /* ------------------------------------------------------------ */
   public boolean equals(Object o)
   {
       if (!(o instanceof Client))
           return false;
       return getId().equals(((Client)o).getId());
   }

   /* ------------------------------------------------------------ */
   /**
    * @return the advised
    */
   public int getAdviceVersion()
   {
       return _adviseVersion;
   }

   /* ------------------------------------------------------------ */
   /**
    * @param advised the advised to set
    */
   public void setAdviceVersion(int version)
   {
       _adviseVersion=version;
   }


   /* ------------------------------------------------------------ */
   public void addListener(EventListener listener)
   {
       synchronized(this)
       {
           if (listener instanceof MessageListener)
           {
               if (_mListeners==null)
                   _mListeners=new ArrayList<MessageListener>();
               _mListeners.add((MessageListener)listener);
           }
           if (listener instanceof RemoveListener)
           {
               if (_rListeners==null)
                   _rListeners=new ArrayList<RemoveListener>();
               _rListeners.add((RemoveListener)listener);
           }
       }
   }

   /* ------------------------------------------------------------ */
   public void removeListener(EventListener listener)
   {
       synchronized(this)
       {
           if (listener instanceof MessageListener)
           {
               if (_mListeners!=null)
                   _mListeners.remove((MessageListener)listener);
           }
           if (listener instanceof RemoveListener)
           {
               if (_rListeners!=null)
                   _rListeners.remove((RemoveListener)listener);
           }
       }
   }

   /* ------------------------------------------------------------ */
   public long getTimeout() 
   {
       return _timeout;
   }
   
   /* ------------------------------------------------------------ */
   public void setTimeout(long timeoutMS) 
   {
       _timeout=timeoutMS;
   }

}
//========================================================================
//$Id: AbstractConnector.java,v 1.9 2005/11/14 11:00:31 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.component.LifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.WaitingContinuation;


/** Abstract Connector implementation.
 * This abstract implemenation of the Connector interface provides:<ul>
 * <li>AbstractLifeCycle implementation</li>
 * <li>Implementations for connector getters and setters</li>
 * <li>Buffer management</li>
 * <li>Socket configuration</li>
 * <li>Base acceptor thread</li>
 * </ul>
 * 
 * @author gregw
 *
 * TODO - allow multiple Acceptor threads
 */
public abstract class AbstractConnector extends AbstractLifeCycle implements Connector
{
    private int _headerBufferSize=8*1024;
    private int _requestBufferSize=32*1024;
    private int _responseBufferSize=64*1024;

    private String _name;
    private Server _server;
    private ThreadPool _threadPool;
    private String _host;
    private int _port=0;
    private String _integralScheme=HttpSchemes.HTTPS;
    private int _integralPort=0;
    private String _confidentialScheme=HttpSchemes.HTTPS;
    private int _confidentialPort=0;
    private int _acceptQueueSize=0;
    private int _acceptors=1;
    private boolean _useDNS;
    
    protected int _maxIdleTime=30000; 
    protected int _lowResourceMaxIdleTime=-1; 
    protected int _soLingerTime=1000; 
    
    private transient ArrayList _headerBuffers;
    private transient ArrayList _requestBuffers;
    private transient ArrayList _responseBuffers;
    private transient Thread[] _acceptorThread;
    
    Object _statsLock = new Object();
    transient long _statsStartedAt=-1;
    transient int _requests;
    transient int _connections;                  // total number of connections made to server
    
    transient int _connectionsOpen;              // number of connections currently open
    transient int _connectionsOpenMin;           // min number of connections open simultaneously
    transient int _connectionsOpenMax;           // max number of connections open simultaneously
    
    transient long _connectionsDurationMin;      // min duration of a connection
    transient long _connectionsDurationMax;      // max duration of a connection
    transient long _connectionsDurationTotal;    // total duration of all coneection
    
    transient int _connectionsRequestsMin;       // min requests per connection
    transient int _connectionsRequestsMax;       // max requests per connection

    
    /* ------------------------------------------------------------------------------- */
    /** 
     */
    public AbstractConnector()
    {
    }

    /* ------------------------------------------------------------------------------- */
    public abstract void open() throws IOException;

    /* ------------------------------------------------------------------------------- */
    /*
     */
    public Server getServer()
    {
        return _server;
    }

    /* ------------------------------------------------------------------------------- */
    public void setServer(Server server)
    {
        _server=server;
    }
    
    /* ------------------------------------------------------------------------------- */
    /*
     * @see org.mortbay.jetty.HttpListener#getHttpServer()
     */
    public ThreadPool getThreadPool()
    {
        return _threadPool;
    }

    /* ------------------------------------------------------------------------------- */
    public void setThreadPool(ThreadPool pool)
    {
        _threadPool=pool;
    }
    
    /* ------------------------------------------------------------------------------- */
    /**
     */
    public void setHost(String host) 
    {
        _host=host;
    }

    /* ------------------------------------------------------------------------------- */
    /*
     */
    public String getHost()
    {
        return _host;
    }

    /* ------------------------------------------------------------------------------- */
    /*
     * @see org.mortbay.jetty.HttpListener#setPort(int)
     */
    public void setPort(int port)
    {
        _port=port;
    }

    /* ------------------------------------------------------------------------------- */
    /*
     * @see org.mortbay.jetty.HttpListener#getPort()
     */
    public int getPort()
    {
        return _port;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the headerBufferSize.
     */
    public int getHeaderBufferSize()
    {
        return _headerBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param headerBufferSize The headerBufferSize to set.
     */
    public void setHeaderBufferSize(int headerBufferSize)
    {
        _headerBufferSize = headerBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the maxIdleTime.
     */
    public int getMaxIdleTime()
    {
        return _maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param maxIdleTime The maxIdleTime to set.
     */
    public void setMaxIdleTime(int maxIdleTime)
    {
        _maxIdleTime = maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the maxIdleTime.
     */
    public int getLowResourceMaxIdleTime()
    {
        return _lowResourceMaxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param maxIdleTime The maxIdleTime to set.
     */
    public void setLowResourceMaxIdleTime(int maxIdleTime)
    {
        _lowResourceMaxIdleTime = maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestBufferSize.
     */
    public int getRequestBufferSize()
    {
        return _requestBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param requestBufferSize The requestBufferSize to set.
     */
    public void setRequestBufferSize(int requestBufferSize)
    {
        _requestBufferSize = requestBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the responseBufferSize.
     */
    public int getResponseBufferSize()
    {
        return _responseBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param responseBufferSize The responseBufferSize to set.
     */
    public void setResponseBufferSize(int responseBufferSize)
    {
        _responseBufferSize = responseBufferSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the soLingerTime.
     */
    public long getSoLingerTime()
    {
        return _soLingerTime;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the acceptQueueSize.
     */
    public int getAcceptQueueSize()
    {
        return _acceptQueueSize;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param acceptQueueSize The acceptQueueSize to set.
     */
    public void setAcceptQueueSize(int acceptQueueSize)
    {
        _acceptQueueSize = acceptQueueSize;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @return Returns the number of acceptor threads.
     */
    public int getAcceptors()
    {
        return _acceptors;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param acceptors The number of acceptor threads to set.
     */
    public void setAcceptors(int acceptors)
    {
        _acceptors = acceptors;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param soLingerTime The soLingerTime to set.
     */
    public void setSoLingerTime(int soLingerTime)
    {
        _soLingerTime = soLingerTime;
    }
    
    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        super.doStart();
        
        // open listener port
        open();
        
        if (_headerBuffers!=null)
            _headerBuffers.clear();
        else
            _headerBuffers=new ArrayList();
        if (_requestBuffers!=null)
            _requestBuffers.clear();
        else
            _requestBuffers=new ArrayList();
        if (_responseBuffers!=null)
            _responseBuffers.clear();
        else
            _responseBuffers=new ArrayList(); 
        
        if (_threadPool==null)
            _threadPool=_server.getThreadPool();
        if (_threadPool!=_server.getThreadPool() && (_threadPool instanceof LifeCycle))
            ((LifeCycle)_threadPool).start();
        
        // Start selector thread
        _acceptorThread=new Thread[getAcceptors()];
        for (int i=0;i<_acceptorThread.length;i++)
        {
            if (!_threadPool.dispatch(new Acceptor(i)))
            {
                Log.warn("insufficient maxThreads configured for {}",this);
                break;
            }
        }
        
        Log.info("Started {}",this);
    }
    
    /* ------------------------------------------------------------ */
    protected void doStop() throws Exception
    {
        if (_threadPool!=_server.getThreadPool() && _threadPool instanceof LifeCycle)
            ((LifeCycle)_threadPool).stop();
        
        Thread[] acceptors=_acceptorThread;
        _acceptorThread=null;
        if (acceptors != null)
        {
            for (int i=0;i<acceptors.length;i++)
            {
                Thread thread=acceptors[i];
                if (thread!=null)
                    thread.interrupt();
            }
        }
        
        try{close();} catch(IOException e) {Log.warn(e);}

        super.doStop();
    }

    /* ------------------------------------------------------------ */
    public void join() throws InterruptedException
    {
        Thread[] threads=_acceptorThread;
        if (threads!=null)
            for (int i=0;i<threads.length;i++)
                if (threads[i]!=null)
                    threads[i].join();
    }

    /* ------------------------------------------------------------ */
    protected void configure(Socket socket)
        throws IOException
    {   
        try
        {
            socket.setTcpNoDelay(true);
            if (_maxIdleTime >= 0)
                socket.setSoTimeout(_maxIdleTime);
            if (_soLingerTime >= 0)
                socket.setSoLinger(true, _soLingerTime/1000);
            else
                socket.setSoLinger(false, 0);
        }
        catch (Exception e)
        {
            Log.ignore(e);
        }
    }


    /* ------------------------------------------------------------ */
    public void customize(EndPoint endpoint, Request request)
        throws IOException
    {      
    }
    
    /* ------------------------------------------------------------ */
    protected abstract Buffer newBuffer(int size);

    
    /* ------------------------------------------------------------ */
    public Buffer getBuffer(int size)
    {
        if (size==_headerBufferSize)
        {
            synchronized(_headerBuffers)
            {
                if (_headerBuffers.size()==0)
                    return newBuffer(size);
                return (Buffer) _headerBuffers.remove(_headerBuffers.size()-1);
            }
        }
        else if (size==_responseBufferSize)
        {
            synchronized(_responseBuffers)
            {
                if (_responseBuffers.size()==0)
                    return newBuffer(size);
                return (Buffer) _responseBuffers.remove(_responseBuffers.size()-1);
            }
        }
        else if (size==_requestBufferSize)
        {
            synchronized(_requestBuffers)
            {
                if (_requestBuffers.size()==0)
                    return newBuffer(size);
                return (Buffer) _requestBuffers.remove(_requestBuffers.size()-1);
            }   
        }
        
        return newBuffer(size);    
    }
    

    /* ------------------------------------------------------------ */
    public void returnBuffer(Buffer buffer)
    {
        buffer.clear();
        if (!buffer.isVolatile() && !buffer.isImmutable())
        {
            int c=buffer.capacity();
            if (c==_headerBufferSize)
            {
                synchronized(_headerBuffers)
                {
                    _headerBuffers.add(buffer);
                }
            }
            else if (c==_responseBufferSize)
            {
                synchronized(_responseBuffers)
                {
                    _responseBuffers.add(buffer);
                }
            }
            else if (c==_requestBufferSize)
            {
                synchronized(_requestBuffers)
                {
                    _requestBuffers.add(buffer);
                }
            }
        }
    }

    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#getConfidentialPort()
     */
    public int getConfidentialPort()
    {
        return _confidentialPort;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#getConfidentialScheme()
     */
    public String getConfidentialScheme()
    {
        return _confidentialScheme;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#isConfidential(org.mortbay.jetty.Request)
     */
    public boolean isIntegral(Request request)
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#getConfidentialPort()
     */
    public int getIntegralPort()
    {
        return _integralPort;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#getIntegralScheme()
     */
    public String getIntegralScheme()
    {
        return _integralScheme;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Connector#isConfidential(org.mortbay.jetty.Request)
     */
    public boolean isConfidential(Request request)
    {
        return false;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param confidentialPort The confidentialPort to set.
     */
    public void setConfidentialPort(int confidentialPort)
    {
        _confidentialPort = confidentialPort;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param confidentialScheme The confidentialScheme to set.
     */
    public void setConfidentialScheme(String confidentialScheme)
    {
        _confidentialScheme = confidentialScheme;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param integralPort The integralPort to set.
     */
    public void setIntegralPort(int integralPort)
    {
        _integralPort = integralPort;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param integralScheme The integralScheme to set.
     */
    public void setIntegralScheme(String integralScheme)
    {
        _integralScheme = integralScheme;
    }

    /* ------------------------------------------------------------ */
    public Continuation newContinuation()
    {
        return new WaitingContinuation();
    }
    
    /* ------------------------------------------------------------ */
    protected abstract void accept(int acceptorID) throws IOException, InterruptedException;


    /* ------------------------------------------------------------ */
    public boolean getResolveNames()
    {
        return _useDNS;
    }
    
    /* ------------------------------------------------------------ */
    public void setResolveNames(boolean resolve)
    {
        _useDNS=resolve;
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        String name = this.getClass().getName();
        int dot = name.lastIndexOf('.');
        if (dot>0)
            name=name.substring(dot+1);
        
        return name+" @ "+(getHost()==null?"0.0.0.0":getHost())+":"+(getLocalPort()<=0?getPort():getLocalPort());
    }
    
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class Acceptor implements Runnable
    {
        int _acceptor=0;
        
        Acceptor(int id)
        {
            _acceptor=id;
        }
        
        /* ------------------------------------------------------------ */
        public void run()
        {   
            Thread current = Thread.currentThread();
            _acceptorThread[_acceptor]=current;
            String name =_acceptorThread[_acceptor].getName();
            current.setName(name+" - Acceptor"+_acceptor+" "+AbstractConnector.this);
            Log.debug("Starting " + this);
            try
            {
                current.setPriority(current.getPriority()-1);
                while (isRunning() && 
                       (!(getThreadPool() instanceof LifeCycle) || ((LifeCycle)getThreadPool()).isRunning()))
                {
                    try
                    {
                        accept(_acceptor); 
                    }
                    catch(IOException e)
                    {
                        Log.ignore(e);
                    }
                    catch(Exception e)
                    {
                        Log.warn(e);
                    }
                }
            }
            finally
            {   
                Log.debug("Stopping " + this);
                current.setPriority(current.getPriority()+1);
                current.setName(name);
                try
                {
                    if (_acceptor==0)
                        close();
                }
                catch (IOException e)
                {
                    Log.warn(e);
                }
            }
        }
    }

    public String getName()
    {
        if (_name==null)
            _name= (getHost()==null?"0.0.0.0":getHost())+":"+(getLocalPort()<=0?getPort():getLocalPort());
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
    
    

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of requests handled by this context
     * since last call of statsReset(). If setStatsOn(false) then this
     * is undefined.
     */
    public int getRequests() {return _requests;}

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectionsDurationMin.
     */
    public long getConnectionsDurationMin()
    {
        return _connectionsDurationMin;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectionsDurationTotal.
     */
    public long getConnectionsDurationTotal()
    {
        return _connectionsDurationTotal;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectionsOpenMin.
     */
    public int getConnectionsOpenMin()
    {
        return _connectionsOpenMin;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the connectionsRequestsMin.
     */
    public int getConnectionsRequestsMin()
    {
        return _connectionsRequestsMin;
    }


    /* ------------------------------------------------------------ */
    /** 
     * @return Number of connections accepted by the server since
     * statsReset() called. Undefined if setStatsOn(false).
     */
    public int getConnections() {return _connections;}

    /* ------------------------------------------------------------ */
    /** 
     * @return Number of connections currently open that were opened
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public int getConnectionsOpen() {return _connectionsOpen;}

    /* ------------------------------------------------------------ */
    /** 
     * @return Maximum number of connections opened simultaneously
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public int getConnectionsOpenMax() {return _connectionsOpenMax;}

    /* ------------------------------------------------------------ */
    /** 
     * @return Average duration in milliseconds of open connections
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public long getConnectionsDurationAve() {return _connections==0?0:(_connectionsDurationTotal/_connections);}

    /* ------------------------------------------------------------ */
    /** 
     * @return Maximum duration in milliseconds of an open connection
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public long getConnectionsDurationMax() {return _connectionsDurationMax;}

    /* ------------------------------------------------------------ */
    /** 
     * @return Average number of requests per connection
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public int getConnectionsRequestsAve() {return _connections==0?0:(_requests/_connections);}

    /* ------------------------------------------------------------ */
    /** 
     * @return Maximum number of requests per connection
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public int getConnectionsRequestsMax() {return _connectionsRequestsMax;}


    
    /* ------------------------------------------------------------ */
    /** Reset statistics.
     */
    public void statsReset()
    {
        _statsStartedAt=_statsStartedAt==-1?-1:System.currentTimeMillis();

        _connections=0;
        
        _connectionsOpenMin=_connectionsOpen;
        _connectionsOpenMax=_connectionsOpen;
        _connectionsOpen=0;
        
        _connectionsDurationMin=0;
        _connectionsDurationMax=0;
        _connectionsDurationTotal=0;

        _requests=0;

        _connectionsRequestsMin=0;
        _connectionsRequestsMax=0;
    }
    
    /* ------------------------------------------------------------ */
    public void setStatsOn(boolean on)
    {
        if (on && _statsStartedAt!=-1)
            return;
        Log.info("Statistics on = "+on+" for "+this);
        statsReset();
        _statsStartedAt=on?System.currentTimeMillis():-1;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if statistics collection is turned on.
     */
    public boolean getStatsOn()
    {
        return _statsStartedAt!=-1;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return Timestamp stats were started at.
     */
    public long getStatsOnMs()
    {
        return (_statsStartedAt!=-1)?(System.currentTimeMillis()-_statsStartedAt):0;
    }
    

    /* ------------------------------------------------------------ */
    protected void connectionOpened(HttpConnection connection)
    {
        if (_statsStartedAt==-1)
            return;
        synchronized(_statsLock)
        {
            _connectionsOpen++;
            if (_connectionsOpen > _connectionsOpenMax)
                _connectionsOpenMax=_connectionsOpen;
        }
    }
    
    /* ------------------------------------------------------------ */
    protected void connectionClosed(HttpConnection connection)
    {
        if (_statsStartedAt>=0)
        {
            synchronized(_statsLock)
            {
                int requests=connection.getRequests();
                _requests+=requests;
                long duration=System.currentTimeMillis()-connection.getTimeStamp();
                _connections++;
                _connectionsOpen--;
                _connectionsDurationTotal+=duration;
                if (_connectionsOpen<0)
                    _connectionsOpen=0;
                if (_connectionsOpen<_connectionsOpenMin)
                    _connectionsOpenMin=_connectionsOpen;
                if (_connectionsDurationMin==0 || duration<_connectionsDurationMin)
                    _connectionsDurationMin=duration;
                if (duration>_connectionsDurationMax)
                    _connectionsDurationMax=duration;
                if (_connectionsRequestsMin==0 || requests<_connectionsRequestsMin)
                    _connectionsRequestsMin=requests;
                if (requests>_connectionsRequestsMax)
                    _connectionsRequestsMax=requests;
            }
        }
        
        connection.destroy();
    }

}

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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.EndPoint;
import org.mortbay.log.Log;
import org.mortbay.thread.ThreadPool;
import org.mortbay.util.ajax.Continuation;


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
    private int _headerBufferSize=2*1024;
    private int _requestBufferSize=16*1024;
    private int _responseBufferSize=48*1024;

    private Server _server;
    private ThreadPool _threadPool;
    private String _host;
    private int _port=8080;
    private String _integralScheme=HttpSchemes.HTTPS;
    private int _integralPort=0;
    private String _confidentialScheme=HttpSchemes.HTTPS;
    private int _confidentialPort=0;
    private int _acceptQueueSize=0;
    private int _acceptors=1;
    
    protected long _maxIdleTime=30000; 
    protected long _soLingerTime=1000; 
    
    private transient SocketAddress _address;
    
    private transient ArrayList _headerBuffers;
    private transient ArrayList _requestBuffers;
    private transient ArrayList _responseBuffers;
    private transient Thread[] _acceptorThread;
   
    
    
    /* ------------------------------------------------------------------------------- */
    /** 
     */
    public AbstractConnector()
    {
    }

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

    /* ------------------------------------------------------------------------------- */
    public SocketAddress getAddress()
    {
        if (_address==null)
            _address=(_host==null)?new InetSocketAddress(_port):new InetSocketAddress(_host,_port);
       
        return _address;
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
    public long getMaxIdleTime()
    {
        return _maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param maxIdleTime The maxIdleTime to set.
     */
    public void setMaxIdleTime(long maxIdleTime)
    {
        _maxIdleTime = maxIdleTime;
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
     * @param acceptQueueSize The number of acceptor threads to set.
     */
    public void setAcceptors(int acceptors)
    {
        _acceptors = acceptors;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param soLingerTime The soLingerTime to set.
     */
    public void setSoLingerTime(long soLingerTime)
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
        if (_threadPool!=_server.getThreadPool())
            _threadPool.start();
        
        // Start selector thread
        _acceptorThread=new Thread[getAcceptors()];
        for (int i=0;i<_acceptorThread.length;i++)
            _threadPool.dispatch(new Acceptor(i));
    }
    
    /* ------------------------------------------------------------ */
    protected void doStop() throws Exception
    {
        if (_threadPool!=_server.getThreadPool())
            _threadPool.stop();
        
        if (_acceptorThread != null)
            for (int i=0;i<_acceptorThread.length;i++)
                if (_acceptorThread[i]!=null)
                    _acceptorThread[i].interrupt();
        _acceptorThread=null;
        _address=null;
        
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
                socket.setSoTimeout((int)_maxIdleTime);
            if (_soLingerTime >= 0)
                socket.setSoLinger(true, (int)_soLingerTime/1000);
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
        return null;
    }
    
    /* ------------------------------------------------------------ */
    protected abstract void accept(int acceptorID) throws IOException, InterruptedException;

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Connector "+getHost()+":"+getPort();
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
            _acceptorThread[_acceptor]=Thread.currentThread();
            String name =_acceptorThread[_acceptor].getName();
            _acceptorThread[_acceptor].setName(name+" - Acceptor"+_acceptor+" "+AbstractConnector.this);
            Log.info("Starting " + this);
            try
            {
                while (isRunning() && getThreadPool().isRunning())
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
                Log.info("Stopping " + this);
                Thread.currentThread().setName(name);
                try
                {
                    close();
                }
                catch (IOException e)
                {
                    Log.warn(e);
                }
            }
        }
    }
}

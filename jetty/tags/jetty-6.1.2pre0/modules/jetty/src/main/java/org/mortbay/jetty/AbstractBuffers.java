package org.mortbay.jetty;

import java.util.ArrayList;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;

/* ------------------------------------------------------------ */
/** Abstract Buffer pool.
 * simple unbounded pool of buffers.
 * @author gregw
 *
 */
public abstract class AbstractBuffers extends AbstractLifeCycle implements Buffers
{
    private static int BUFFER_POOLS=4;
    private static int BUFFER_LOSS_RATE=256; // Leak buffers to shrink pools
    
    private int _headerBufferSize=8*1024;
    private int _requestBufferSize=32*1024;
    private int _responseBufferSize=64*1024;

    // Use and array of buffers to avoid contention
    private transient ArrayList[] _headerBuffers=new ArrayList[BUFFER_POOLS];
    private transient int _header;
    private transient int _loss;
    private transient ArrayList _requestBuffers;
    private transient ArrayList _responseBuffers;

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
    protected abstract Buffer newBuffer(int size);

    
    /* ------------------------------------------------------------ */
    public Buffer getBuffer(int size)
    {
        if (size==_headerBufferSize)
        {   
            int h=_header++ % BUFFER_POOLS;
            
            synchronized(_headerBuffers[h])
            {
                if (_headerBuffers[h].size()==0)
                    return newBuffer(size);
                return (Buffer) _headerBuffers[h].remove(_headerBuffers[h].size()-1);
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
        int h=_header;
        if (h>=BUFFER_LOSS_RATE)
        {
            int l=_loss++;
            if (l>BUFFER_POOLS)
                _loss=0;
            if (h>=BUFFER_LOSS_RATE+l)
            {
                _header=0;
                return;
            }
        }
        h=h%BUFFER_POOLS;

        buffer.clear();
        if (!buffer.isVolatile() && !buffer.isImmutable())
        {
            int c=buffer.capacity();
            if (c==_headerBufferSize)
            {
                synchronized(_headerBuffers[h])
                {
                    _headerBuffers[h].add(buffer);
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
    protected void doStart() throws Exception
    {
        super.doStart();

        for (int i=0;i<_headerBuffers.length;i++)
        {
            if (_headerBuffers[i]!=null)
                _headerBuffers[i].clear();
            else
                _headerBuffers[i]=new ArrayList();
        }
        if (_requestBuffers!=null)
            _requestBuffers.clear();
        else
            _requestBuffers=new ArrayList();
        if (_responseBuffers!=null)
            _responseBuffers.clear();
        else
            _responseBuffers=new ArrayList(); 
    }
    
    
}

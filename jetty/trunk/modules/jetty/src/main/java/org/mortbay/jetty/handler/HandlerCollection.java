//========================================================================
//$Id: HandlerCollection.java,v 1.5 2005/11/11 22:55:39 gregwilkins Exp $
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

package org.mortbay.jetty.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.Server;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiException;

/* ------------------------------------------------------------ */
/** A collection of handlers.  
 * For each request, all handler are called, regardless of 
 * the response status or exceptions.
 *  
 * @author gregw
 *
 */
public class HandlerCollection extends AbstractHandler implements Handler
{
    private Handler[] _handlers;

    /* ------------------------------------------------------------ */
    public HandlerCollection()
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the handlers.
     */
    public Handler[] getHandlers()
    {
        return _handlers;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * 
     * @param handlers The handlers to set.
     */
    public void setHandlers(Handler[] handlers)
    {
        Handler [] old_handlers = _handlers==null?null:(Handler[])_handlers.clone();
        
        if (getServer()!=null)
            getServer().getContainer().update(this, old_handlers, handlers, "handler");
        
        Server server = getServer();
        MultiException mex = new MultiException();
        for (int i=0;handlers!=null && i<handlers.length;i++)
        {
            if (handlers[i].getServer()!=server)
                handlers[i].setServer(server);
            try
            {
                if (isStarted())
                    handlers[i].start();
            }
            catch (Throwable e)
            {
                mex.add(e);
            }
        }

        // quasi atomic.... so don't go doing this under load on a SMP system.
        _handlers = handlers;

        for (int i=0;old_handlers!=null && i<old_handlers.length;i++)
        {
            if (old_handlers[i]!=null)
            {
                try
                {
                    if (old_handlers[i].isStarted())
                        old_handlers[i].stop();
                }
                catch (Throwable e)
                {
                    mex.add(e);
                }
                
                old_handlers[i].setServer(null);
            }
        }
                
        mex.ifExceptionThrowRuntime();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.EventHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) 
        throws IOException, ServletException
    {
        if (_handlers!=null && isStarted())
        {
            MultiException mex=null;
            
            for (int i=0;i<_handlers.length;i++)
            {
                try
                {
                    _handlers[i].handle(target,request, response, dispatch);
                }
                catch(Throwable e)
                {
                    if (mex==null)
                        mex=new MultiException();
                    mex.add(e);
                }
            }
            if (mex!=null)
                throw new ServletException(mex);
            
        }    
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.handler.AbstractHandler#doStart()
     */
    protected void doStart() throws Exception
    {
        MultiException mex=new MultiException();
        if (_handlers!=null)
        {
            for (int i=0;i<_handlers.length;i++)
                try{_handlers[i].start();}catch(Throwable e){mex.add(e);}
        }
        super.doStart();
        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.handler.AbstractHandler#doStop()
     */
    protected void doStop() throws Exception
    {
        MultiException mex=new MultiException();
        try { super.doStop(); } catch(Throwable e){mex.add(e);}
        if (_handlers!=null)
        {
            for (int i=_handlers.length;i-->0;)
                try{_handlers[i].stop();}catch(Throwable e){mex.add(e);}
        }
        mex.ifExceptionThrow();
    }
    

    /* ------------------------------------------------------------ */
    /**
     * Conveniance method to set a single handler
     * @return  the handler.
     */
    public Handler getHandler()
    {
        if (_handlers==null || _handlers.length==0)
            return null;
        if (_handlers.length>1)
            throw new IllegalStateException("Multiple Handlers");
        return _handlers[0];
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Conveniance method to set a single handler
     * @param handler The handler to set.
     */
    public void setHandler(Handler handler)
    {
        setHandlers(new Handler[]{handler});
    }    

    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        if (getServer()!=null && getServer()!=server)
            getServer().getContainer().update(this, _handlers, null, "handler");
        if (server!=null && getServer()!=server)
            server.getContainer().update(this, null,_handlers, "handler");
            
        super.setServer(server);
        Handler[] h=getHandlers();
        for (int i=0;h!=null && i<h.length;i++)
            h[i].setServer(server);
    }

    /* ------------------------------------------------------------ */
    public void addHandler(Handler handler)
    {
        setHandlers((Handler[])LazyList.addToArray(getHandlers(), handler, Handler.class));
    }
    
    /* ------------------------------------------------------------ */
    public void removeHandler(Handler handler)
        throws Exception
    {
        Handler[] handlers = getHandlers();
        
        if (handlers!=null && handlers.length>0 )
            setHandlers((Handler[])LazyList.removeFromArray(handlers, handler));
    }

    /* ------------------------------------------------------------ */
    protected Object expandChildren(Object list, Class byClass)
    {
        Handler[] handlers = getHandlers();
        for (int i=0;handlers!=null && i<handlers.length;i++)
            list=expandHandler(handlers[i], list, byClass);
        return list;
    }


}

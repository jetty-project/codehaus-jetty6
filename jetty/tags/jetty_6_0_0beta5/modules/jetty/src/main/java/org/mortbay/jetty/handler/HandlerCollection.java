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

import org.mortbay.component.Container;
import org.mortbay.jetty.Handler;
import org.mortbay.util.MultiException;

/* ------------------------------------------------------------ */
/** HandlerCollection.
 * @author gregw
 *
 */
public class HandlerCollection extends AbstractHandler implements Handler
{
    private Handler[] _handlers;

    /* ------------------------------------------------------------ */
    /**
     * 
     */
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
     * @param handlers The handlers to set.
     */
    public void setHandlers(Handler[] handlers)
    {
        Container.update(this, _handlers, handlers, "handler");
        _handlers = handlers;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.EventHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (_handlers!=null && isStarted())
        {
            for (int i=0;i<_handlers.length;i++)
            {
                if (_handlers[i].handle(target,request, response, dispatch))
                    return true;
            }
        }    
        return false;
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
        _handlers = new Handler[]{handler};
    }
}

/* ------------------------------------------------------------------------
 * $Id$
 * Copyright 2006 Tim Vernum
 * ------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ------------------------------------------------------------------------
 */

package org.mortbay.jetty.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.util.LazyList;
import org.mortbay.util.MultiException;

/**
 * @version $Revision$
 */
public class PrePostHandler extends HandlerWrapper
{
    private Handler _preHandler;
    private Handler _postHandler;

    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
    {
        if (!isStarted())
            return false;

        boolean handled=false;
        try
        {
            if (_preHandler!=null)
                handled=_preHandler.handle(target, request, response, dispatch);
            if (!handled)
                handled = super.handle(target, request, response, dispatch);
        }
        finally
        {
            if (_postHandler!=null)
                handled|=_postHandler.handle(target, request, response, dispatch);
        }
        return handled;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param handlers The handlers to set.
     */
    public void setPreHandler(Handler handler)
    {
        try
        {
            Handler old_handler = _preHandler;
            
            if (getServer()!=null)
                getServer().getContainer().update(this, old_handler, handler, "preHandler");
            
            if (handler!=null)
            {
                handler.setServer(getServer());
                if (isStarted() && !handler.isStarted())
                    handler.start();
            }
            
            _preHandler = handler;
            
            if (old_handler!=null)
            {
                if (old_handler.isStarted())
                    old_handler.stop();
                old_handler.setServer(null);
            }
        }
        catch(Exception e)
        {
            IllegalStateException ise= new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }
    }    
    
    /* ------------------------------------------------------------ */
    /**
     * @param handlers The handlers to set.
     */
    public void setPostHandler(Handler handler)
    {
        try
        {
            Handler old_handler = _postHandler;
            
            if (getServer()!=null)
                getServer().getContainer().update(this, old_handler, handler, "postHandler");
            
            if (handler!=null)
            {
                handler.setServer(getServer());
                if (isStarted() && !handler.isStarted())
                    handler.start();
            }
            
            _postHandler = handler;
            
            if (old_handler!=null)
            {
                if (old_handler.isStarted())
                    old_handler.stop();
                old_handler.setServer(null);
            }
        }
        catch(Exception e)
        {
            IllegalStateException ise= new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        MultiException mex=new MultiException();
        try
        {
            if (_preHandler!=null)
                _preHandler.start();
        }
        catch(Exception e)
        {
            mex.add(e);
        }

        try
        {
            super.doStart();
        }
        catch(Exception e)
        {
            mex.add(e);
        }
        
        try
        {
            if (_postHandler!=null)
                _postHandler.start();
        }
        catch(Exception e)
        {
            mex.add(e);
        }
        
        mex.ifExceptionThrow();
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        MultiException mex=new MultiException();
        try
        {
            if (_preHandler!=null)
                _preHandler.stop();
        }
        catch(Exception e)
        {
            mex.add(e);
        }

        try
        {
            super.doStop();
        }
        catch(Exception e)
        {
            mex.add(e);
        }
        
        try
        {
            if (_postHandler!=null)
                _postHandler.stop();
        }
        catch(Exception e)
        {
            mex.add(e);
        }
        
        mex.ifExceptionThrow();
    }

    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        if (getServer()!=null && getServer()!=server)
        {
            getServer().getContainer().update(this, _preHandler, null, "preHandler");
            getServer().getContainer().update(this, _postHandler, null, "postHandler");
        }

        super.setServer(server);
        
        if (_preHandler!=null)
            _preHandler.setServer(server);
        if (_postHandler!=null)
            _postHandler.setServer(server);
        
        if (server!=null && getServer()!=server)
        {
            server.getContainer().update(this, null,_preHandler, "preHandler");
            server.getContainer().update(this, null,_postHandler, "postHandler");
        }
            
    }

    /* ------------------------------------------------------------ */
    protected Object expandChildren(Object list, Class byClass)
    {
        list=expandHandler(_preHandler,list,byClass);
        list=super.expandChildren(list, byClass);
        list=expandHandler(_postHandler,list,byClass);
        return list;
    }
}

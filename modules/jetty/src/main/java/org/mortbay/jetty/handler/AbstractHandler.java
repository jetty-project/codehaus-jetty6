//========================================================================
//$Id: AbstractHandler.java,v 1.4 2005/11/11 22:55:39 gregwilkins Exp $
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


import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.log.Log;
import org.mortbay.util.LazyList;


/* ------------------------------------------------------------ */
/** AbstractHandler.
 * @author gregw
 *
 */
public abstract class AbstractHandler extends AbstractLifeCycle implements Handler
{
    protected String _string;
    private Server _server;
    
    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public AbstractHandler()
    {
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.LifeCycle#start()
     */
    protected void doStart() throws Exception
    {
        Log.debug("starting {}",this);
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.LifeCycle#stop()
     */
    protected void doStop() throws Exception
    {
        Log.debug("stopping {}",this);
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        if (_string==null)
        {
            _string=super.toString();
            _string=_string.substring(_string.lastIndexOf('.')+1);
        }
        return _string;
    }

    /* ------------------------------------------------------------ */
    public void setServer(Server server)
    {
        Server old_server=_server;
        if (old_server!=null && old_server!=server)
            old_server.getContainer().removeBean(this);
        _server=server;
        if (_server!=null && _server!=old_server)
            _server.getContainer().addBean(this);
    }

    /* ------------------------------------------------------------ */
    public Server getServer()
    {
        return _server;
    }

    /* ------------------------------------------------------------ */
    public Handler[] getChildHandlers()
    {
        Object list = expandChildren(null,null);
        return (Handler[])LazyList.toArray(list, Handler.class);
    }
        
    /* ------------------------------------------------------------ */
    public Handler[] getChildHandlersByClass(Class byclass)
    {
        Object list = expandChildren(null,byclass);
        return (Handler[])LazyList.toArray(list, Handler.class);
    }
    
    /* ------------------------------------------------------------ */
    public Handler getChildHandlerByClass(Class byclass)
    {
        // TODO this can be more efficient?
        Object list = expandChildren(null,byclass);
        if (list==null)
            return null;
        return (Handler)LazyList.get(list, 0);
    }
    
    /* ------------------------------------------------------------ */
    protected Object expandChildren(Object list, Class byClass)
    {
        return list;
    }

    /* ------------------------------------------------------------ */
    protected Object expandHandler(Handler handler, Object list, Class byClass)
    {
        if (handler==null)
            return list;
        
        if (handler!=null && (byClass==null || byClass.isAssignableFrom(handler.getClass())))
            list=LazyList.add(list, handler);

        if (handler instanceof AbstractHandler)
            list=((AbstractHandler)handler).expandChildren(list, byClass);
        else
        {
            Handler[] handlers=byClass==null?handler.getChildHandlers():handler.getChildHandlersByClass(byClass);
            list=LazyList.addArray(list, handlers);
        }
        
        return list;
    }
    
    
}

//========================================================================
//$Id: WrappedHandler.java,v 1.2 2005/11/11 22:55:39 gregwilkins Exp $
//Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

/* ------------------------------------------------------------ */
/** HandlerCollection.
 * @author gregw
 *
 */
public class WrappedHandler extends AbstractHandler
{
    private Handler _handler;

    /* ------------------------------------------------------------ */
    /**
     * 
     */
    public WrappedHandler()
    {
        super();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the handlers.
     */
    public Handler getHandler()
    {
        return _handler;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param handlers The handlers to set.
     */
    public void setHandler(Handler handler)
    {
        _handler = handler;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStart()
     */
    protected void doStart() throws Exception
    {
        if (_handler!=null)
            _handler.start();
        super.doStart();
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.thread.AbstractLifeCycle#doStop()
     */
    protected void doStop() throws Exception
    {
        super.doStop();
        if (_handler!=null)
            _handler.stop();
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.EventHandler#handle(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        if (_handler==null || !isStarted())
            return false;
        return _handler.handle(target,request, response, dispatch);
    }
}

//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.servlet.PathMap;
import org.mortbay.util.LazyList;

public class ContextHandlerCollection extends HandlerCollection
{
    private PathMap _contextMap;
    
    /* ------------------------------------------------------------ */
    /**
     * Remap the context paths.
     * TODO make this private
     */
    public void mapContexts()
    {
        _contextMap=null;
        Handler[] handlers=getHandlers();
        if (handlers!=null && handlers.length>0)
        {
            PathMap contextMap = new PathMap();
            List list = new ArrayList();
            for (int i=0;i<handlers.length;i++)
            {
                list.clear();
                
                expandHandler(handlers[i], list,ContextHandler.class);
                
                Iterator iter = list.iterator();
                while(iter.hasNext())
                {
                    Handler handler = (Handler)iter.next();
                    
                    String contextPath=((ContextHandler)handler).getContextPath();
                    
                    if (contextPath==null ||
                                    contextPath.indexOf(',')>=0 ||
                                    contextPath.startsWith("*"))
                        throw new IllegalArgumentException ("Illegal context spec:"+contextPath);
                    
                    if(!contextPath.startsWith("/"))
                        contextPath='/'+contextPath;
                    
                    if (contextPath.length()>1)
                    {
                        if (contextPath.endsWith("/"))
                            contextPath+="*";
                        else if (!contextPath.endsWith("/*"))
                            contextPath+="/*";
                    }
                    
                    Object contexts=contextMap.get(contextPath);
                    contexts = LazyList.add(contexts, handlers[i]);
                    contextMap.put(contextPath, contexts);
                }
            }
            _contextMap=contextMap;
        }
    }
    

    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.handler.HandlerCollection#setHandlers(org.mortbay.jetty.Handler[])
     */
    public void setHandlers(Handler[] handlers)
    {
        _contextMap=null;
        super.setHandlers(handlers);
        if (isStarted())
            mapContexts();
    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        mapContexts();
        super.doStart();
    }
    

    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        Handler[] handlers = getHandlers();
        if (handlers==null || handlers.length==0)
        {
            response.sendError(500);
        }
        else
        {
            Response base_response = HttpConnection.getCurrentConnection().getResponse();
            PathMap map = _contextMap;
            if (map!=null && target!=null && target.startsWith("/"))
            {
                Object contexts = map.getLazyMatches(target);
                for (int i=0; i<LazyList.size(contexts); i++)
                {
                    Map.Entry entry = (Map.Entry)LazyList.get(contexts, i);
                    Object list = entry.getValue();
                    for (int j=0; j<LazyList.size(list); j++)
                    {
                        Handler handler = (Handler)LazyList.get(list,j);
                        handler.handle(target,request, response, dispatch);
                        if (response.isCommitted() || base_response.getStatus()!=-1)
                            return;
                    }
                }
            }
            
            for (int i=0;i<handlers.length;i++)
            {
                handlers[i].handle(target,request, response, dispatch);
                if (response.isCommitted() || base_response.getStatus()!=-1)
                    return;
            }
            
        }    
    }

    
}

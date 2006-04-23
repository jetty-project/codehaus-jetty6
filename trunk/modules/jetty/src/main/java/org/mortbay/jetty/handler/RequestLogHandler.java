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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;
import org.mortbay.jetty.RequestLog;
import org.mortbay.log.Log;


/** 
 * RequestLogHandler.
 * This handler can be used to wrap an individual context for context logging.
 * 
 * @author Nigel Canonizado
 * 
 */
public class RequestLogHandler extends HandlerWrapper
{

    private RequestLog _requestLog;
    
    /* ------------------------------------------------------------ */
    /* 
     * @see org.mortbay.jetty.Handler#handle(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, int)
     */
    public boolean handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException
    {
        boolean result = super.handle(target, request, response, dispatch);
        _requestLog.log((Request)request, (Response)response);
        
        return result;
    }
    
    public void setRequestLog(RequestLog requestLog)
    {
        _requestLog = requestLog;
    }
    
    public RequestLog getRequestLog() {
        
        return _requestLog;
    }

}

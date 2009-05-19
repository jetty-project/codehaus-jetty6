// ========================================================================
// Copyright 2009 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//========================================================================

package org.mortbay.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.log.Log;

/* ------------------------------------------------------------ */
/** Includable DoS Filter.
 * This is an extension to the {@link DoSFilter} that uses Jetty APIs to allow
 * connections to be closed cleanly. 
 */

public class IncludableDoSFilter extends DoSFilter
{
    protected void closeConnection(HttpServletRequest request, HttpServletResponse response, Thread thread)
    {
        try
        {
            Request base_request=(request instanceof Request)?(Request)request:HttpConnection.getCurrentConnection().getRequest();
            base_request.getConnection().getEndPoint().close();
        }
        catch(IOException e)
        {
            Log.warn(e);
        }
    }
}

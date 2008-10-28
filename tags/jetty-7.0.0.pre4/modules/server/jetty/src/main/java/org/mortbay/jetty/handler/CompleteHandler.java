//========================================================================
//$Id: Request.java,v 1.15 2005/11/16 22:02:40 gregwilkins Exp $
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

import java.util.List;

import javax.servlet.ServletRequest;

import org.mortbay.jetty.Request;

/**
 * An interface for handlers that wish to be notified of request completion.
 * 
 * If the request attribute COMPLETE_HANDLER_ATTR is set as either a single 
 * CompleteHandler instance or a {@link List} of CompleteHandler instances,
 * then when the {@link ServletRequest#complete()} method is called, then 
 * the {@link #complete(Request)} method is called for each CompleteHandler.
 * 
 * @author ayao
 *
 */
public interface CompleteHandler
{
    public final static String COMPLETE_HANDLER_ATTR = "org.mortbay.jetty.handler.CompleteHandlers";
    void complete(Request request);
}

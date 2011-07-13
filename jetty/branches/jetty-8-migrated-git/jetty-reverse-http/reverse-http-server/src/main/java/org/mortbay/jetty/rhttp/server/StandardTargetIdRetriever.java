/*
 * Copyright 2009-2009 Webtide LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mortbay.jetty.rhttp.server;

import javax.servlet.http.HttpServletRequest;

/**
 * <p>This implementation retrieves the targetId from the request URI following this pattern:</p>
 * <pre>
 * /contextPath/servletPath/&lt;targetId&gt;/other/paths
 * </pre>
 * @version $Revision$ $Date$
 */
public class StandardTargetIdRetriever implements TargetIdRetriever
{
    public String retrieveTargetId(HttpServletRequest httpRequest)
    {
        String uri = httpRequest.getRequestURI();
        String path = uri.substring(httpRequest.getServletPath().length());
        String[] segments = path.split("/");
        if (segments.length < 2) return null;
        return segments[1];
    }
}

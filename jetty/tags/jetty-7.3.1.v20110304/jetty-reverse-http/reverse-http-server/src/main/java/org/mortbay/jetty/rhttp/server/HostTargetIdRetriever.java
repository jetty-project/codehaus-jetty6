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
 * @version $Revision$ $Date$
 */
public class HostTargetIdRetriever implements TargetIdRetriever
{
    private final String suffix;

    public HostTargetIdRetriever(String suffix)
    {
        this.suffix = suffix;
    }

    public String retrieveTargetId(HttpServletRequest httpRequest)
    {
        String host = httpRequest.getHeader("Host");
        if (host != null)
        {
            // Strip the port
            int colon = host.indexOf(':');
            if (colon > 0)
            {
                host = host.substring(0, colon);
            }

            if (suffix != null && host.endsWith(suffix))
            {
                return host.substring(0, host.length() - suffix.length());
            }
        }
        return host;
    }
}

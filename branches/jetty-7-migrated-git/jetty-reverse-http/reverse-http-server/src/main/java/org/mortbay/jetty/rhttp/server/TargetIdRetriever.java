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
 * <p>Implementations should retrieve a <em>targetId</em> from an external request.</p>
 * <p>Implementations of this class may return a fixed value, or inspect the request
 * looking for URL patterns (e.g. "/&lt;targetId&gt;/resource.jsp"), or looking for request
 * parameters (e.g. "/resource.jsp?targetId=&lt;targetId&gt;), or looking for virtual host
 * naming patterns (e.g. "http://&lt;targetId&gt;.host.com/resource.jsp"), etc.</p>
 *
 * @version $Revision$ $Date$
 */
public interface TargetIdRetriever
{
    /**
     * Extracts and returns the targetId.
     * @param httpRequest the external request from where the targetId could be extracted
     * @return the extracted targetId
     */
    public String retrieveTargetId(HttpServletRequest httpRequest);
}

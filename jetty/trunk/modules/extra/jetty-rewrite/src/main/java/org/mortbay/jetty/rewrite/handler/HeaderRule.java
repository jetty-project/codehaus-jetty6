//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.rewrite.handler;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract rule that matches against request headers.
 */

public abstract class HeaderRule extends Rule
{
    private String _header;
    private String _headerValue;

    /* ------------------------------------------------------------ */
    public String getHeader()
    {
        return _header;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param header
     *                the header name to check for
     */
    public void setHeader(String header)
    {
        _header = header;
    }

    /* ------------------------------------------------------------ */
    public String getHeaderValue()
    {
        return _headerValue;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param headerValue
     *                the header value to match against. If null, then the
     *                presence of the header is enough to match
     */
    public void setHeaderValue(String headerValue)
    {
        _headerValue = headerValue;
    }

    /* ------------------------------------------------------------ */
    @Override
    public String matchAndApply(String target, HttpServletRequest request,
            HttpServletResponse response) throws IOException
    {
        String requestHeaderValue = request.getHeader(_header);
        
        if (requestHeaderValue != null)
            if (_headerValue == null || _headerValue.equals(requestHeaderValue))
                apply(target, requestHeaderValue, request, response);
        
        return null;
    }

    /* ------------------------------------------------------------ */
    /**
     * Apply the rule to the request
     * 
     * @param target
     *                field to attempt match
     * @param value 
     *                header value found
     * @param request
     *                request object
     * @param response
     *                response object
     * @return The target (possible updated)
     * @throws IOException
     *                 exceptions dealing with operating on request or response
     *                 objects
     */
    protected abstract String apply(String target, String value, HttpServletRequest request, HttpServletResponse response) throws IOException;

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return super.toString() + "[" + _header + ":" + _headerValue + "]";
    }

}

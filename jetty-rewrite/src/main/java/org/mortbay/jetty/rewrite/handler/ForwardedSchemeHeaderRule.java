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


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.rewrite.handler.PatternRule;
import org.mortbay.jetty.server.Request;


/**
 * Set the scheme for the request 
 *
 * @author Ervin Varga
 * @author Athena Yao
 */
public class ForwardedSchemeHeaderRule extends HeaderRule {
    private String _scheme="https";

    /* ------------------------------------------------------------ */
    public String getScheme() 
    {
        return _scheme;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param scheme the scheme to set on the request. Defaults to "https"
     */
    public void setScheme(String scheme)
    {
        _scheme = scheme;
    }
    
    /* ------------------------------------------------------------ */
    protected String apply(String target, String value, HttpServletRequest request, HttpServletResponse response) 
    {
        ((Request) request).setScheme(_scheme);
        return target;
    }    
}

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

import org.mortbay.jetty.http.HttpFields;
import org.mortbay.jetty.rewrite.handler.ForwardedSchemeHeaderRule;

public class ForwardedSchemeHeaderRuleTest extends AbstractRuleTestCase
{
    private ForwardedSchemeHeaderRule _rule;
    private HttpFields _requestHeaderFields;

    public void setUp() throws Exception
    {
        super.setUp();
        _rule = new ForwardedSchemeHeaderRule();
        _requestHeaderFields = _connection.getRequestFields();
        _request.setScheme(null);
    }
    
    public void testDefaultScheme() throws Exception
    {
        setRequestHeader("X-Forwarded-Scheme", "https");
        _rule.setHeader("X-Forwarded-Scheme");
        _rule.setHeaderValue("https");
        
        _rule.matchAndApply("/", _request, _response);
        assertEquals("https", _request.getScheme());
    }

    public void testScheme() throws Exception
    {
        setRequestHeader("X-Forwarded-Scheme", "https");
        _rule.setHeader("X-Forwarded-Scheme");
        _rule.setHeaderValue("https");
        _rule.setScheme("https");

        _rule.matchAndApply("/", _request, _response);
        assertEquals("https", _request.getScheme());
        
        
        _rule.setScheme("http");
        _rule.matchAndApply("/", _request, _response);
        assertEquals("http", _request.getScheme());
    }
    
    public void testHeaderValue() throws Exception
    {
        setRequestHeader("Front-End-Https", "on");
        _rule.setHeader("Front-End-Https");
        _rule.setHeaderValue("on");
        _rule.setScheme("https");
        
        _rule.matchAndApply("/",_request,_response);
        assertEquals("https",_request.getScheme());
        _request.setScheme(null);
        
        
        // header value doesn't match rule's value
        setRequestHeader("Front-End-Https", "off");
        
        _rule.matchAndApply("/",_request,_response);
        assertEquals(null,_request.getScheme());
        _request.setScheme(null);

        
        // header value can be any value
        setRequestHeader("Front-End-Https", "any");
        _rule.setHeaderValue(null);
        
        _rule.matchAndApply("/",_request,_response);
        assertEquals("https",_request.getScheme());
    }
    
    private void setRequestHeader(String header, String headerValue)
    {
        _requestHeaderFields.put(header, headerValue);
    }
}

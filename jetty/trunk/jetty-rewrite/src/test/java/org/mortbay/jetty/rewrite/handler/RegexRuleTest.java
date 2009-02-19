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
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.rewrite.handler.RegexRule;
import org.mortbay.jetty.server.Request;

import junit.framework.TestCase;

public class RegexRuleTest extends TestCase
{
    private RegexRule _rule;
    
    public void setUp()
    {
        _rule = new TestRegexRule();
        
    }
    
    public void tearDown()
    {
        _rule = null;
    }
    
    public void testTrueMatch() throws IOException
    {
        String[][] matchCases = {
                // regex: *.jsp
                {"/.*.jsp", "/hello.jsp"},
                {"/.*.jsp", "/abc/hello.jsp"},
                
                // regex: /abc or /def
                {"/abc|/def", "/abc"},
                {"/abc|/def", "/def"},
                
                // regex: *.do or *.jsp
                {".*\\.do|.*\\.jsp", "/hello.do"},
                {".*\\.do|.*\\.jsp", "/hello.jsp"},
                {".*\\.do|.*\\.jsp", "/abc/hello.do"},
                {".*\\.do|.*\\.jsp", "/abc/hello.jsp"},
                
                {"/abc/.*.htm|/def/.*.htm", "/abc/hello.htm"},
                {"/abc/.*.htm|/def/.*.htm", "/abc/def/hello.htm"},
                
                // regex: /abc/*.jsp
                {"/abc/.*.jsp", "/abc/hello.jsp"},
                {"/abc/.*.jsp", "/abc/def/hello.jsp"}
        };
        
        for (int i = 0; i < matchCases.length; i++)
        {
            String[] matchCase = matchCases[i];
            assertMatch(true, matchCase);
        }
    }
    
    public void testFalseMatch() throws IOException
    {
        String[][] matchCases = {
                {"/abc/.*.jsp", "/hello.jsp"}
        };
        
        for (int i = 0; i < matchCases.length; i++)
        {
            String[] matchCase = matchCases[i];
            assertMatch(false, matchCase);
        }
    }
    
    private void assertMatch(boolean flag, String[] matchCase) throws IOException
    {
        _rule.setRegex(matchCase[0]);
        final String uri=matchCase[1];
        String result = _rule.matchAndApply(uri,
        new Request()
        {
            public String getRequestURI()
            {
                return uri;
            }
        }, null
        );
        
        assertEquals("regex: " + matchCase[0] + " uri: " + matchCase[1], flag, result!=null);
    }
    
    private class TestRegexRule extends RegexRule
    {
        public String apply(String target,HttpServletRequest request,HttpServletResponse response, Matcher matcher) throws IOException
        {
            return target;
        }
    }
}

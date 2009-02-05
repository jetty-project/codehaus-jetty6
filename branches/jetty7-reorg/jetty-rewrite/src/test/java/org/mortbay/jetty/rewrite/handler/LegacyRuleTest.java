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

import org.mortbay.jetty.rewrite.handler.LegacyRule;

public class LegacyRuleTest extends AbstractRuleTestCase
{
    private LegacyRule _rule;
    
    String[][] _tests=
    {
            {"/foo/bar","/*","/replace/foo/bar"},
            {"/foo/bar","/foo/*","/replace/bar"},
            {"/foo/bar","/foo/bar","/replace"}
    };
    
    public void setUp() throws Exception
    {
        super.setUp();
        _rule = new LegacyRule();
    }
    
    public void tearDown()
    {
        _rule = null;
    }
    
    public void testMatchAndApply() throws Exception
    {
        for (int i=0;i<_tests.length;i++)
        {
            _rule.addRewriteRule(_tests[i][1], "/replace");
            
            String result = _rule.matchAndApply(_tests[i][0], _request, _response);
        
            assertEquals(_tests[i][1], _tests[i][2], result);
        }
    }
    
    public void testAddRewrite()
    {
        try
        {
            _rule.addRewriteRule("*.txt", "/replace");
            fail();
        } 
        catch (IllegalArgumentException e)
        {
        }
    }
}

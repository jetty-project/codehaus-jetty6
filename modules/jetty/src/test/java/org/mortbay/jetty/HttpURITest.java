//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty;

import junit.framework.TestCase;

public class HttpURITest extends TestCase
{
    String[][] tests=
    { 
        {"/path/info",null,null,null,null,"/path/info",null,null,null}, 
        {"/path/info#fragment",null,null,null,null,"/path/info",null,null,"fragment"}, 
        {"/path/info?query",null,null,null,null,"/path/info",null,"query",null}, 
        {"/path/info?query#fragment",null,null,null,null,"/path/info",null,"query","fragment"}, 
        {"/path/info;param",null,null,null,null,"/path/info","param",null,null},    
        {"/path/info;param#fragment",null,null,null,null,"/path/info","param",null,"fragment"}, 
        {"/path/info;param?query",null,null,null,null,"/path/info","param","query",null}, 
        {"/path/info;param?query#fragment",null,null,null,null,"/path/info","param","query","fragment"}, 
        {"//host/path/info",null,"//host","host",null,"/path/info",null,null,null}, 
        {"//user@host/path/info",null,"//user@host","host",null,"/path/info",null,null,null}, 
        {"//user@host:8080/path/info",null,"//user@host:8080","host","8080","/path/info",null,null,null}, 
        {"//host:8080/path/info",null,"//host:8080","host","8080","/path/info",null,null,null}, 
        {"http:/path/info","http",null,null,null,"/path/info",null,null,null},    
        {"http:/path/info#fragment","http",null,null,null,"/path/info",null,null,"fragment"}, 
        {"http:/path/info?query","http",null,null,null,"/path/info",null,"query",null}, 
        {"http:/path/info?query#fragment","http",null,null,null,"/path/info",null,"query","fragment"}, 
        {"http:/path/info;param","http",null,null,null,"/path/info","param",null,null},    
        {"http:/path/info;param#fragment","http",null,null,null,"/path/info","param",null,"fragment"}, 
        {"http:/path/info;param?query","http",null,null,null,"/path/info","param","query",null}, 
        {"http:/path/info;param?query#fragment","http",null,null,null,"/path/info","param","query","fragment"},                
        {"http://user@host:8080/path/info;param?query#fragment","http","//user@host:8080","host","8080","/path/info","param","query","fragment"}, 
        {"xxxxx://user@host:8080/path/info;param?query#fragment","xxxxx","//user@host:8080","host","8080","/path/info","param","query","fragment"}, 
        {"http:///;?#","http","//",null,null,"/","","",""}, 
        {"/path/info?a=?query",null,null,null,null,"/path/info",null,"a=?query",null}, 
        {"/path/info?a=;query",null,null,null,null,"/path/info",null,"a=;query",null}, 
    };
    
    public void testURIs()
        throws Exception
    {
        HttpURI uri = new HttpURI();
        
        for (int t=0;t<tests.length;t++)
        {
            uri.parse(tests[t][0]);
            assertEquals(t+" "+tests[t][0],tests[t][1],uri.getScheme());
            assertEquals(t+" "+tests[t][0],tests[t][2],uri.getAuthority());
            assertEquals(t+" "+tests[t][0],tests[t][3],uri.getHost());
            assertEquals(t+" "+tests[t][0],tests[t][4]==null?-1:Integer.parseInt(tests[t][4]),uri.getPort());
            assertEquals(t+" "+tests[t][0],tests[t][5],uri.getPath());
            assertEquals(t+" "+tests[t][0],tests[t][6],uri.getParam());
            assertEquals(t+" "+tests[t][0],tests[t][7],uri.getQuery());
            assertEquals(t+" "+tests[t][0],tests[t][8],uri.getFragment());
        }
        
    }

}

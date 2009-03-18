//========================================================================
//Copyright 2009 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.server;

import java.io.IOException;

import javax.servlet.ServletInputStream;

import org.mortbay.jetty.http.HttpParser;
import org.mortbay.jetty.io.Buffer;

public class HttpInput extends ServletInputStream
{
    protected final HttpParser _parser;
    protected final long _maxIdleTime;
    
    /* ------------------------------------------------------------ */
    public HttpInput(HttpParser parser, long maxIdleTime)
    {
        _parser=parser;
        _maxIdleTime=maxIdleTime;
    }
    
    /* ------------------------------------------------------------ */
    /*
     * @see java.io.InputStream#read()
     */
    public int read() throws IOException
    {
        int c=-1;
        Buffer content=_parser.blockForContent(_maxIdleTime);
        if (content!=null)
            c= 0xff & content.get();
        return c;
    }
    
    /* ------------------------------------------------------------ */
    /* 
     * @see java.io.InputStream#read(byte[], int, int)
     */
    public int read(byte[] b, int off, int len) throws IOException
    {
        int l=-1;
        Buffer content=_parser.blockForContent(_maxIdleTime);
        if (content!=null)
            l= content.get(b, off, len);
        return l;
    }
    

}
// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty;

import org.mortbay.io.Buffer;
import org.mortbay.io.BufferCache;

/**
 * 
 * @author gregw
 */
public class HttpHeaderValues extends BufferCache
{
    public final static String
        CLOSE="close",
        CHUNKED="chunked",
        GZIP="gzip",
        IDENTITY="identity",
        KEEP_ALIVE="keep-alive",
        CONTINUE="100-continue",
        PROCESSING="102-processing",
        TE="TE";

    public final static int
        CLOSE_ORDINAL=1,
        CHUNKED_ORDINAL=2,
        GZIP_ORDINAL=3,
        IDENTITY_ORDINAL=4,
        KEEP_ALIVE_ORDINAL=5,
        CONTINUE_ORDINAL=6,
        PROCESSING_ORDINAL=7,
        TE_ORDINAL=8;
    
    public final static HttpHeaderValues CACHE= new HttpHeaderValues();

    public final static Buffer 
        CLOSE_BUFFER=CACHE.add(CLOSE,CLOSE_ORDINAL),
        CHUNKED_BUFFER=CACHE.add(CHUNKED,CHUNKED_ORDINAL),
        GZIP_BUFFER=CACHE.add(GZIP,GZIP_ORDINAL),
        IDENTITY_BUFFER=CACHE.add(IDENTITY,IDENTITY_ORDINAL),
        KEEP_ALIVE_BUFFER=CACHE.add(KEEP_ALIVE,KEEP_ALIVE_ORDINAL),
        CONTINUE_BUFFER=CACHE.add(CONTINUE, CONTINUE_ORDINAL),
        PROCESSING_BUFFER=CACHE.add(PROCESSING, PROCESSING_ORDINAL),
        TE_BUFFER=CACHE.add(TE,TE_ORDINAL);
        
    static
    {  
        int index=100;
        CACHE.add("gzip",index++);
        CACHE.add("gzip,deflate",index++);
        CACHE.add("deflate",index++);
        CACHE.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1)",index++);
        CACHE.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322)",index++);
        CACHE.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.0.7) Gecko/20060909 Firefox/1.5.0.7",index++);
        CACHE.add("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)",index++);
        CACHE.add("Mozilla/5.0 (compatible; Yahoo! Slurp; http://help.yahoo.com/help/us/ysearch/slurp)",index++);
        CACHE.add("Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1)",index++);
        CACHE.add("Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0",index++);
        CACHE.add("msnbot/1.0 (+http://search.msn.com/msnbot.htm)",index++);
    }
}

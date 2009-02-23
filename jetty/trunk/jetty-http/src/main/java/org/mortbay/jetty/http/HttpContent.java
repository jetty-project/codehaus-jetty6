//========================================================================
//$Id: HttpContent.java,v 1.1 2005/10/05 14:09:21 janb Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.http;

import java.io.IOException;
import java.io.InputStream;

import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.util.resource.Resource;

/* ------------------------------------------------------------ */
/** HttpContent.
 * @author gregw
 *
 */
public interface HttpContent
{
    Buffer getContentType();
    Buffer getLastModified();
    Buffer getBuffer();
    Resource getResource();
    long getContentLength();
    InputStream getInputStream() throws IOException;
    void release();
}
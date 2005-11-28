// ========================================================================
// $Id: InBuffer.java,v 1.1 2005/10/05 14:09:25 janb Exp $
// Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.io;

import java.io.IOException;

/* ------------------------------------------------------------------------------- */
/**
 * @deprecated USE BufferIO
 */
public interface InBuffer extends Buffer
{
    boolean isClosed();
    
    /**
     * Close any backing stream associated with the buffer
     */
    void close() throws IOException;

    /**
     * Fill the buffer from the current putIndex to it's capacity from whatever 
     * byte source is backing the buffer. The putIndex is increased if bytes filled.
     * The buffer may chose to do a compact before filling.
     * @return an <code>int</code> value indicating the number of bytes 
     * filled or -1 if EOF is reached.
     */
    int fill() throws IOException;

}

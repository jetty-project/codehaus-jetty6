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

package org.mortbay.io;

import java.io.IOException;

/* ------------------------------------------------------------------------------- */
/**
 * 
 * @deprecated USE BufferIO
 */
public interface OutBuffer extends Buffer
{
    boolean isClosed();
    
    /**
     * Close any backing stream associated with the buffer
     */
    void close() throws IOException;

    /**
     * Flush the buffer from the current getIndex to it's putIndex using whatever byte
     * sink is backing the buffer. The getIndex is updated with the number of bytes flushed.
     * Any mark set is cleared.
     * If the entire contents of the buffer are flushed, then an implicit empty() is done.
     * 
     * @return  the number of bytes written
     */
    int flush() throws IOException;

    /**
     * Flush the buffer from the current getIndex to it's putIndex using whatever byte
     * sink is backing the buffer. The getIndex is updated with the number of bytes flushed.
     * Any mark set is cleared.
     * If the entire contents of the buffer are flushed, then an implicit empty() is done.
     * The passed header/trailer buffers are written before/after the contents of this buffer. This may be done 
     * either as gather writes, as a poke into this buffer or as several writes. The implementation is free to
     * select the optimal mechanism.
     * @param header A buffer to write before flushing this buffer. This buffers getIndex is updated.
     * @param trailer A buffer to write after flushing this buffer. This buffers getIndex is updated.
     * @return the total number of bytes written.
     */
    int flush(Buffer header, Buffer trailer) throws IOException;

}

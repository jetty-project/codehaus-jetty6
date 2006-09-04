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

package org.mortbay.jetty.ajp;

import java.io.IOException;

import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;

/**
 * @author Markus Kobler
 */
public class Ajp13Generator {

    public Ajp13Generator(Buffers buffers, EndPoint io, int bufferSize) {
        
    }


    public void sendError(int code, String reason, String content, boolean close) throws IOException {

    }


    // XXX Implement
    public boolean isComplete() {
        return true;
    }

    // XXX Implement
    public boolean isCommited() {
        return true;
    }

    public void reset(boolean returnBuffers) {
        
    }
    
    
}

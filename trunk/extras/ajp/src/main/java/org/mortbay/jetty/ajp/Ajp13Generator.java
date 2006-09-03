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

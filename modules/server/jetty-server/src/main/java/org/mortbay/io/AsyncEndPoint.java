package org.mortbay.io;

public interface AsyncEndPoint extends EndPoint
{
    /* ------------------------------------------------------------ */
    /**
     * Dispatch the endpoint to a thread to attend to it.
     * 
     * @return True If the dispatched succeeded
     */
    public boolean dispatch();
    
}

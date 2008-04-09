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
    
    /**
     * @return true if this endpoint can accept a dispatch. False if the 
     * endpoint cannot accept a dispatched (eg is suspended or already dispatched)
     */
    public boolean isReadyForDispatch();
    
}

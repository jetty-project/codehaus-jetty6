package org.mortbay.cometd;


public abstract class AbstractTransport implements Transport
{
    boolean _polling;
    boolean _initialized;

    public boolean isPolling()
    {
        return _polling;
    }

    public void setPolling(boolean polling)
    {
        _polling=polling;
    }

    public boolean isInitialized()
    {
        return _initialized;
    }
    
    public void setInitialized(boolean initialized)
    {
        _initialized=initialized;
    }
}

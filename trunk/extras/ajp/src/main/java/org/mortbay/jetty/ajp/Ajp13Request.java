package org.mortbay.jetty.ajp;

import org.mortbay.io.EndPoint;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;

public class Ajp13Request extends Request
{

    protected String _remoteAddr;
    protected String _remoteHost;
    protected int _remotePort;
    protected HttpConnection _connection;
    protected EndPoint _endp;
    public Ajp13Request(HttpConnection connection)
    {
        super(connection);
        _endp = connection.getEndPoint();
        _remoteAddr = _endp.getRemoteAddr();
        _remoteHost = _endp.getRemoteHost();
        _remotePort = _endp.getRemotePort();
    }

    public String getRemoteAddr()
    {
       return _remoteAddr;
    }
    
    public void setRemoteAddr(String remoteAddr)
    {
        _remoteAddr = remoteAddr;
    }

    public String getRemoteHost()
    {
        return _remoteHost;
    }
    
    public void setRemoteHost(String remoteHost)
    {
        _remoteHost = remoteHost;
    }

    public int getRemotePort()
    {
        return _remotePort;
    }

    public void setRemotePort(int remotePort)
    {
        _remotePort = remotePort;
    }
   

    protected void recycle()
    {
        super.recycle();
        _remoteAddr = _endp.getRemoteAddr();
        _remoteHost = _endp.getRemoteHost();
        _remotePort = _endp.getRemotePort();
    }
    
    
    
    

}

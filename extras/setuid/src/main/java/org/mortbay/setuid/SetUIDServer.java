package org.mortbay.setuid;

import org.mortbay.jetty.Server;
import org.mortbay.log.Log;

/**
 * This extension of {@link Server} will make a JNI call to set the unix UID
 * after the server has been started.
 * This can be used to start the server as root so that priviledged ports may
 * be accessed and then switch to a non-root user for security.
 * 
 * @author gregw
 *
 */
public class SetUIDServer extends Server
{
    int _uid=0;
    int _umask=22;

    public int getUid()
    {
        return _uid;
    }

    public void setUid(int uid)
    {
        _uid=uid;
    }
    
    public void setUmask (int umask)
    {
        _umask=umask;
    }
    
    public int getUmask ()
    {
        return _umask;
    }
    protected void doStart() throws Exception
    {
        Log.info("Setting umask="+_umask);
        SetUmask.setumask(_umask);
        super.doStart();
        if (_uid!=0)
        {
            Log.info("Setting UID="+_uid);
            SetUID.setuid(_uid);
        }
    }
    
}

package org.mortbay.setuid;

import org.mortbay.jetty.Server;
import org.mortbay.log.Log;

/**
 * This extension of {@link Server} will make a JNI call to set the unix UID
 * after the server has been started.
 * This can be used to start the server as root so that priviledged ports may
 * be accessed and then switch to a non-root user for security.
 *
 * The configured umask is set before the server is started and the configured
 * uid is set after the server is started.
 * 
 * @author gregw
 *
 */
public class SetUIDServer extends Server
{
    int _uid=0;
    int _umask=0;


    public int getUmask ()
    {
        return _umask;
    }

    public void setUmask(int umask)
    {
        _umask=umask;
    }
    
    public int getUid()
    {
        return _uid;
    }

    public void setUid(int uid)
    {
        _uid=uid;
    }
    

    protected void doStart() throws Exception
    {
        if (_umask!=0)
        {
            Log.info("Setting umask=0"+Integer.toString(_umask,8));
            SetUID.setumask(_umask);
        }
        super.doStart();
        if (_uid!=0)
        {
            Log.info("Setting UID="+_uid);
            SetUID.setuid(_uid);
        }
    }
    
}

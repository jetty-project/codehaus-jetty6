package org.mortbay.jetty.servlet;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.SessionIdManager;

public class CacheSessionIdManager implements SessionIdManager
{
    protected CacheManager _cacheManager= new CacheManager();
  
    private HashSessionIdManager _temporaryHack = new HashSessionIdManager();

    /* ------------------------------------------------------------ */
    /**
     * @param session
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#addSession(javax.servlet.http.HttpSession)
     */
    public void addSession(HttpSession session)
    {
        _temporaryHack.addSession(session);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param obj
     * @return
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj)
    {
        return _temporaryHack.equals(obj);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#getRandom()
     */
    public Random getRandom()
    {
        return _temporaryHack.getRandom();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#getWorkerName()
     */
    public String getWorkerName()
    {
        return _temporaryHack.getWorkerName();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see java.lang.Object#hashCode()
     */
    public int hashCode()
    {
        return _temporaryHack.hashCode();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @return
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#idInUse(java.lang.String)
     */
    public boolean idInUse(String id)
    {
        return _temporaryHack.idInUse(id);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#invalidateAll(java.lang.String)
     */
    public void invalidateAll(String id)
    {
        _temporaryHack.invalidateAll(id);
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isFailed()
     */
    public boolean isFailed()
    {
        return _temporaryHack.isFailed();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isRunning()
     */
    public boolean isRunning()
    {
        return _temporaryHack.isRunning();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isStarted()
     */
    public boolean isStarted()
    {
        return _temporaryHack.isStarted();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isStarting()
     */
    public boolean isStarting()
    {
        return _temporaryHack.isStarting();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isStopped()
     */
    public boolean isStopped()
    {
        return _temporaryHack.isStopped();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see org.mortbay.component.AbstractLifeCycle#isStopping()
     */
    public boolean isStopping()
    {
        return _temporaryHack.isStopping();
    }

    /* ------------------------------------------------------------ */
    /**
     * @param request
     * @param created
     * @return
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#newSessionId(javax.servlet.http.HttpServletRequest, long)
     */
    public String newSessionId(HttpServletRequest request, long created)
    {
        return _temporaryHack.newSessionId(request,created);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param session
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#removeSession(javax.servlet.http.HttpSession)
     */
    public void removeSession(HttpSession session)
    {
        _temporaryHack.removeSession(session);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param random
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#setRandom(java.util.Random)
     */
    public void setRandom(Random random)
    {
        _temporaryHack.setRandom(random);
    }

    /* ------------------------------------------------------------ */
    /**
     * @param workerName
     * @see org.mortbay.jetty.servlet.HashSessionIdManager#setWorkerName(java.lang.String)
     */
    public void setWorkerName(String workerName)
    {
        _temporaryHack.setWorkerName(workerName);
    }

    /* ------------------------------------------------------------ */
    /**
     * @throws Exception
     * @see org.mortbay.component.LifeCycle#start()
     */
    public void start() throws Exception
    {
        _temporaryHack.start();
    }

    /* ------------------------------------------------------------ */
    /**
     * @throws Exception
     * @see org.mortbay.component.LifeCycle#stop()
     */
    public void stop() throws Exception
    {
        _temporaryHack.stop();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return _temporaryHack.toString();
    }
    

}

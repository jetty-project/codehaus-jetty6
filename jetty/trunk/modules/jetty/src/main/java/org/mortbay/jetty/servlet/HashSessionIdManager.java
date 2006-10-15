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

package org.mortbay.jetty.servlet;

import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.component.AbstractLifeCycle;
import org.mortbay.jetty.SessionIdManager;
import org.mortbay.jetty.servlet.AbstractSessionManager.Session;
import org.mortbay.util.MultiMap;

/* ------------------------------------------------------------ */
/**
 * HashSessionIdManager. An in-memory implementation of the session ID manager.
 */
public class HashSessionIdManager extends AbstractLifeCycle implements SessionIdManager
{
    private final static String __NEW_SESSION_ID="org.mortbay.jetty.newSessionId";

    MultiMap _sessions;
    protected Random _random;
    private String _workerName;

    /* ------------------------------------------------------------ */
    public HashSessionIdManager()
    {
    }

    /* ------------------------------------------------------------ */
    public HashSessionIdManager(Random random)
    {
        _random=random;
    }

    /* ------------------------------------------------------------ */
    /**
     * Get the workname. If set, the workername is dot appended to the session
     * ID and can be used to assist session affinity in a load balancer.
     * 
     * @return String or null
     */
    public String getWorkerName()
    {
        return _workerName;
    }

    /* ------------------------------------------------------------ */
    /**
     * Set the workname. If set, the workername is dot appended to the session
     * ID and can be used to assist session affinity in a load balancer.
     * 
     * @param workerName
     */
    public void setWorkerName(String workerName)
    {
        _workerName=workerName;
    }

    /* ------------------------------------------------------------ */
    protected void doStart()
    {
        if (_random==null)
            _random=new Random();
        _random.nextLong();
        _sessions=new MultiMap();
    }

    /* ------------------------------------------------------------ */
    protected void doStop()
    {
        if (_sessions!=null)
            _sessions.clear(); // Maybe invalidate?
        _sessions=null;
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.SessionManager.MetaManager#idInUse(java.lang.String)
     */
    public boolean idInUse(String id)
    {
        return _sessions.containsKey(id);
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.SessionManager.MetaManager#addSession(javax.servlet.http.HttpSession)
     */
    public void addSession(HttpSession session)
    {
        synchronized (this)
        {
            _sessions.add(session.getId(),session);
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.SessionManager.MetaManager#addSession(javax.servlet.http.HttpSession)
     */
    public void removeSession(HttpSession session)
    {
        synchronized (this)
        {
            _sessions.removeValue(session.getId(),session);
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * @see org.mortbay.jetty.SessionManager.MetaManager#invalidateAll(java.lang.String)
     */
    public void invalidateAll(String id)
    {
        synchronized (this)
        {
            // Do not use interators as this method tends to be called recursively 
            // by the invalidate calls.
            while (_sessions.containsKey(id))
            {
                Session session=(Session)_sessions.getValue(id,0);
                if (session.isValid())
                    session.invalidate();
                else
                    _sessions.removeValue(id,session);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /*
     * new Session ID. If the request has a requestedSessionID which is unique,
     * that is used. The session ID is created as a unique random long,
     * represented as in a base between 30 and 36, selected by timestamp. If the
     * request has a jvmRoute attribute, that is appended as a worker tag, else
     * any worker tag set on the manager is appended. @param request @param
     * created @return Session ID.
     */
    public String newSessionId(HttpServletRequest request, long created)
    {
        synchronized (this)
        {
            // A requested session ID can only be used if it is in use already.
            String requested_id=request.getRequestedSessionId();
            if (requested_id!=null&&idInUse(requested_id))
                return requested_id;

            // Else reuse any new session ID already defined for this request.
            String new_id=(String)request.getAttribute(__NEW_SESSION_ID);
            if (new_id!=null&&idInUse(new_id))
                return new_id;

            // pick a new unique ID!
            String id=null;
            while (id==null||id.length()==0||idInUse(id))
            {
                long r=_random.nextLong();
                if (r<0)
                    r=-r;
                id=Long.toString(r,30+(int)(created%7));
            }

            request.setAttribute(__NEW_SESSION_ID,id);
            return id;
        }
    }

}
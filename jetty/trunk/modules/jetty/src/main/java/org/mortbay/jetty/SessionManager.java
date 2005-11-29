// ========================================================================
// $Id: SessionManager.java,v 1.3 2005/10/30 11:07:33 gregwilkins Exp $
// Copyright 1996-2004 Mort Bay Consulting Pty. Ltd.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at 
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty;

import java.io.Serializable;
import java.util.EventListener;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.mortbay.component.LifeCycle;


    
/* --------------------------------------------------------------------- */
/** Session Manager.
 * The API required to manage sessions for a servlet context.
 *
 * @version $Id: SessionManager.java,v 1.3 2005/10/30 11:07:33 gregwilkins Exp $
 * @author Greg Wilkins
 */
public interface SessionManager extends LifeCycle, Serializable
{
    /* ------------------------------------------------------------ */
    /** Session cookie name.
     * Defaults to JSESSIONID, but can be set with the
     * org.mortbay.jetty.servlet.SessionCookie system property.
     */
    public final static String __SessionCookie=
        System.getProperty("org.mortbay.jetty.servlet.SessionCookie","JSESSIONID");
    
    /* ------------------------------------------------------------ */
    /** Session URL parameter name.
     * Defaults to jsessionid, but can be set with the
     * org.mortbay.jetty.servlet.SessionURL system property.
     */
    public final static String __SessionURL = 
        System.getProperty("org.mortbay.jetty.servlet.SessionURL","jsessionid");

    final static String __SessionUrlPrefix=";"+__SessionURL+"=";

    /* ------------------------------------------------------------ */
    /** Session Domain.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the domain for session cookies. If it is not set, then
     * no domain is specified for the session cookie.
     */
    public final static String __SessionDomain=
        "org.mortbay.jetty.servlet.SessionDomain";
    
    /* ------------------------------------------------------------ */
    /** Session Path.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the path for the session cookie.  If it is not set, then
     * the context path is used as the path for the cookie.
     */
    public final static String __SessionPath=
        "org.mortbay.jetty.servlet.SessionPath";
    
    /* ------------------------------------------------------------ */
    /** Session Max Age.
     * If this property is set as a ServletContext InitParam, then it is
     * used as the max age for the session cookie.  If it is not set, then
     * a max age of -1 is used.
     */
    public final static String __MaxAge=
        "org.mortbay.jetty.servlet.MaxAge";
    
    /* ------------------------------------------------------------ */
    public HttpSession getHttpSession(String id);
    
    /* ------------------------------------------------------------ */
    public HttpSession newHttpSession(HttpServletRequest request);

    /* ------------------------------------------------------------ */
    /** @return true if session cookies should be secure
     */
    public boolean getSecureCookies();

    /* ------------------------------------------------------------ */
    /** @return true if session cookies should be httponly (microsoft extension)
     */
    public boolean getHttpOnly();

    /* ------------------------------------------------------------ */
    public int getMaxInactiveInterval();

    /* ------------------------------------------------------------ */
    public void setMaxInactiveInterval(int seconds);

    /* ------------------------------------------------------------ */
    /** Add an event listener.
     * @param listener An Event Listener. Individual SessionManagers
     * implemetations may accept arbitrary listener types, but they
     * are expected to at least handle
     *   HttpSessionActivationListener,
     *   HttpSessionAttributeListener,
     *   HttpSessionBindingListener,
     *   HttpSessionListener
     */
    public void addEventListener(EventListener listener);
    
    /* ------------------------------------------------------------ */
    public void removeEventListener(EventListener listener);
    
    /* ------------------------------------------------------------ */
    public void clearEventListeners();

    /* ------------------------------------------------------------ */
    /** Get a Cookie for a session.
     * @param session
     * @param contextPath TODO
     * @return
     */
    public Cookie getSessionCookie(HttpSession session,String contextPath, boolean requestIsSecure);
    
    /* ------------------------------------------------------------ */
    /**
     * @return the cross context session meta manager.
     */
    public MetaManager getMetaManager();
    
    /* ------------------------------------------------------------ */
    /**
     * @param meta the cross context session meta manager.
     */
    public void setMetaManager(MetaManager meta);
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public interface Session extends HttpSession
    {
        /* ------------------------------------------------------------ */
        public boolean isValid();

        /* ------------------------------------------------------------ */
        public void access();
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /** MetaManager.
     * Manage cross context sessions.
     * @author gregw
     *
     */
    public interface MetaManager extends LifeCycle
    {
        public boolean crossContext();
        public boolean idInUse(String id);
        public void addSession(HttpSession session);
        public void invalidateAll(String id);
    }

}

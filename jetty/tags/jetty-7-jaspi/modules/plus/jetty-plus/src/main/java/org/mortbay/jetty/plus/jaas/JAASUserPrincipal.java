// ========================================================================
// $Id$
// Copyright 2002-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas;

import java.security.Principal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;



/* ---------------------------------------------------- */
/** JAASUserPrincipal
 * <p>Implements the JAAS version of the 
 *  org.mortbay.http.UserPrincipal interface.
 *
 * @version $Id$
 * @author Jan Bartel (janb)
 */
public class JAASUserPrincipal implements Principal 
{
    private final String _name;
    private final Subject _subject;
    private final LoginContext _loginContext;

    /* ------------------------------------------------ */

    public JAASUserPrincipal(String name, Subject subject, LoginContext loginContext)
    {
        this._name = name;
        this._subject = subject;
        this._loginContext = loginContext;
    }

    /* ------------------------------------------------ */
    /** Get the name identifying the user
     */
    public String getName ()
    {
        return _name;
    }
    
    
    /* ------------------------------------------------ */
    /** Provide access to the Subject
     * @return subject
     */
    public Subject getSubject ()
    {
        return this._subject;
    }
    
    LoginContext getLoginContext ()
    {
        return this._loginContext;
    }
    
    public String toString()
    {
        return getName();
    }
    
}

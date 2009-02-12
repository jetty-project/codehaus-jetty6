// ========================================================================
// $Id$
// Copyright 2008 Mort Bay Consulting Pty. Ltd.
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


package org.mortbay.jetty.security.jaspi.callback;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;

import org.mortbay.jetty.http.security.Credential;

/**
 * CredentialValidationCallback
 *
 * Store a jetty Credential for a user so that it can be
 * validated by jaspi
 */
public class CredentialValidationCallback implements Callback
{
    private Credential _credential;
    private boolean _result;
    private Subject _subject;
    private String _userName;
    
    
    public CredentialValidationCallback (Subject subject, String userName, Credential credential)
    {
        _subject = subject;
        _userName = userName;
        _credential = credential;
    }
    
    public Credential getCredential ()
    {
        return _credential;
    }
    
    public void clearCredential ()
    {
        _credential = null;
    } 
    
    public boolean getResult()
    {
        return _result;
    }
    
    public javax.security.auth.Subject getSubject()
    {
        return _subject;
    }
    
    public java.lang.String getUsername()
    {
        return _userName;     
    }
    
    public void setResult(boolean result)
    {
        _result = result;
    }
    
    
}

//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.jaas.callback;

import java.util.List;

import javax.security.auth.callback.Callback;


/**
 * 
 * RequestParameterCallback
 * 
 * Allows a JAAS callback handler to access any parameter from the j_security_check FORM.
 * This means that a LoginModule can access form fields other than the j_username and j_password
 * fields, and use it, for example, to authenticate a user.
 *
 * @author janb
 * @version $Revision$ $Date$
 *
 */
public class RequestParameterCallback implements Callback
{
    private String _paramName;
    private List _paramValues;
    
    public void setParameterName (String name)
    {
        _paramName = name;
    }
    public String getParameterName ()
    {
        return _paramName;
    }
    
    public void setParameterValues (List values)
    {
        _paramValues = values;
    }
    
    public List getParameterValues ()
    {
        return _paramValues;
    }
}

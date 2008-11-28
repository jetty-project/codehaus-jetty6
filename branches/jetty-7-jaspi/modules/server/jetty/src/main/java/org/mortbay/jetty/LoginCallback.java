/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.mortbay.jetty;

import java.security.Principal;
import java.util.List;

import javax.security.auth.Subject;


/**
 * This is similar to the jaspi PasswordValidationCallback but includes user
 * principal and group info as well.
 * 
 * @version $Rev$ $Date$
 */
public interface LoginCallback
{
    public Subject getSubject();

    public String getUserName();

    //TODO could return Credential type?
    public Object getCredential();
 

    public boolean isSuccess();
  

    public void setSuccess(boolean success);
  

    public Principal getUserPrincipal();
  

    public void setUserPrincipal(Principal userPrincipal);
  

    public List<String> getGroups();
  

    public void setGroups(List<String> groups);
    

    public void setGroups(String[] groups);
  

    public void clearPassword();
   

}

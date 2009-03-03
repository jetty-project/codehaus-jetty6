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

package org.mortbay.jetty.security;

import org.mortbay.jetty.server.UserIdentity;

/**
 * Authentication state of a user.
 * 
 * @version $Rev$ $Date$
 */
public interface Authentication
{    
    public enum Status
    {
        SEND_FAILURE(false), SEND_SUCCESS(true), SEND_CONTINUE(false), SUCCESS(true);
        boolean _success;
        Status(boolean success) {_success=success; }
        public boolean isSuccess(){ return _success;}
    }
    
    Status getAuthStatus();

    String getAuthMethod();
    
    UserIdentity getUserIdentity();
    
    boolean isSuccess();
    
    
    public static final Authentication SUCCESS_UNAUTH_RESULTS = new Authentication()
    {
        public String getAuthMethod() {return null;}
        public Status getAuthStatus() {return Authentication.Status.SUCCESS;}
        public UserIdentity getUserIdentity() {return UserIdentity.UNAUTHENTICATED_IDENTITY;}
        public boolean isSuccess() {return true;}
    };
    
    public static final Authentication SEND_CONTINUE_RESULTS = new Authentication()
    {
        public String getAuthMethod() {return null;}
        public Status getAuthStatus() {return Authentication.Status.SEND_CONTINUE;}
        public UserIdentity getUserIdentity() {return UserIdentity.UNAUTHENTICATED_IDENTITY;}
        public boolean isSuccess() {return false;}
    };
    
    public static final Authentication SEND_FAILURE_RESULTS = new Authentication()
    {
        public String getAuthMethod() {return null;}
        public Status getAuthStatus() {return Authentication.Status.SEND_FAILURE;}
        public UserIdentity getUserIdentity() {return UserIdentity.UNAUTHENTICATED_IDENTITY;}
        public boolean isSuccess() {return false;}
    };
    
}

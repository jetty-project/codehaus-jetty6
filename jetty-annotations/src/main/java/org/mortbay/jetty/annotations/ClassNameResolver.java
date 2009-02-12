//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.annotations;



public interface ClassNameResolver
{
    /**
     * Based on the execution context, should the class represented
     * by "name" be excluded from consideration?
     * @param name
     * @return
     */
    public boolean isExcluded (String name);
    
    
    /**
     * Based on the execution context, if a duplicate class 
     * represented by "name" is detected, should the existing
     * one be overridden or not?
     * @param name
     * @return
     */
    public boolean shouldOverride (String name);
}

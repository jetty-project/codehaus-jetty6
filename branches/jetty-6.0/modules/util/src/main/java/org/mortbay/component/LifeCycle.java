// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.component;

/* ------------------------------------------------------------ */
/** A component LifeCycle.
 * Represents the life cycle interface for an abstract
 * software component. 
 *
 * @author Greg Wilkins (gregw)
 */
public interface LifeCycle
{
    /* ------------------------------------------------------------ */
    /** Start the LifeCycle.
     * @exception Exception An arbitrary exception may be thrown.
     */
    public void start()
        throws Exception;
    
    /* ------------------------------------------------------------ */
    /** Stop the LifeCycle.
     * The LifeCycle may wait for current activities to complete
     * normally, but it can be interrupted.
     * @exception Exception An arbitrary exception may be thrown.
     */
    public void stop()
        throws Exception;
   
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle is starting or has been started. 
     */
    public boolean isRunning();

    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle has been started. 
     */
    public boolean isStarted();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle is starting. 
     */
    public boolean isStarting();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle is stopping 
     */
    public boolean isStopping();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the LifeCycle is failed
     */
    public boolean isFailed();


}


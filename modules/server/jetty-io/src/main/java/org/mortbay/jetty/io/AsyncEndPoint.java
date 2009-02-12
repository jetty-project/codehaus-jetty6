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

package org.mortbay.jetty.io;

public interface AsyncEndPoint extends EndPoint
{
    /* ------------------------------------------------------------ */
    /**
     * Dispatch the endpoint to a thread to attend to it.
     * 
     * @return True If the dispatched succeeded
     */
    public boolean dispatch();
    
    /**
     * @return true if this endpoint can accept a dispatch. False if the 
     * endpoint cannot accept a dispatched (eg is suspended or already dispatched)
     */
    public boolean isReadyForDispatch();
    
}

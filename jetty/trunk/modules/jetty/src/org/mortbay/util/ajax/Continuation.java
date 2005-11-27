//========================================================================
//$Id: Continuation.java,v 1.1 2005/11/14 17:45:56 gregwilkins Exp $
//Copyright 2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.util.ajax;


/* ------------------------------------------------------------ */
/** Continuation.
 * 
 * A continuation is a mechanism by which a HTTP Request can be 
 * suspended and retried after a timeout or an asynchronous event
 * has occured.
 * With the appropriate HTTP Connector, this allows threadless waiting
 * for events (see {@link org.mortbay.jetty.nio.SelectChannelConnector}).
 * 
 * @author gregw
 *
 */
public interface Continuation
{
    
    /* ------------------------------------------------------------ */
    /** Resume the request.
     * Resume a suspended request.  The passed event will be returned in the getObject method.
     * @param event Event to resume the request with.
     */
    public void resume();
    
    /* ------------------------------------------------------------ */
    /** Suspend handling.
     * This method will suspend the request for the timeout or until resume is
     * called.
     * @param timeout
     * @return True if resume called or false if timeout.
     */
    public boolean suspend(long timeout);
    
    /* ------------------------------------------------------------ */
    /** Is this a newly created Continuation.
     * <p>
     * A newly created continuation has not had {@link #getEvent(long)} called on it.
     * </p>
     * @return True if the continuation has just been created and has not yet suspended the request.
     */
    public boolean isNew();
    
    /* ------------------------------------------------------------ */
    /** Is the continuation pending an event or timeout?
     * @return True if neither resume has been called or the continuation has timed out.
     */
    public boolean isPending();
    
    /* ------------------------------------------------------------ */
    /** Arbitrary object associated with the continuation for context.
     * @return An arbitrary object associated with the continuation
     */
    public Object getObject();
    
    /* ------------------------------------------------------------ */
    /** Arbitrary object associated with the continuation for context.
     * @param o An arbitrary object to associate with the continuation
     */
    public void setObject(Object o);
    
}

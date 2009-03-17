//========================================================================
//$Id: RetryRequest.java,v 1.2 2005/11/05 08:32:41 gregwilkins Exp $
//Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.server;

/* ------------------------------------------------------------ */
/** Retry Request
 * This is thrown by a non-blocking {@link Continuation} such as
 * {@link SuspendableSelectChannelEndPoint}.  While it
 * extends ThreadDeath, it does not actually stop the thread calling it.
 * It extends ThreadDeath so as to be an Error that will not be caught
 * by most frameworks.
 * 
 * 
 *
 */
public class RetryRequest extends ThreadDeath
{
}

/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  [2002] - [2007] Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/

/** Regardless of the above rights notice, this file was 
 * obtained under a distribution of the 3.2.0 BlazeDS
 * source which was labeled as available under the
 * e GNU Lesser General Public License Version 3 , 
 * as published by the Free Software Foundation.
 * 
 * This file has been modified and distributed under those
 * terms and parts of this file are copyright webtide LLC 2009
 */

package org.mortbay.jetty.asyncblazeds;

import flex.management.BaseControl;
import flex.management.runtime.messaging.endpoints.EndpointControl;

public class AsyncHTTPEndpointControl extends EndpointControl
{

    private static final String TYPE = "AsyncHTTPEndpoint";

    /**
     * Constructs a <code>AsyncHTTPEndpointControl</code>, assigning managed
     * message endpoint and parent MBean.
     * 
     * @param endpoint
     *            The <code>AsyncHTTPEndpoint</code> managed by this MBean.
     * @param parent
     *            The parent MBean in the management hierarchy.
     */
    public AsyncHTTPEndpointControl(AsyncHTTPEndpoint endpoint, BaseControl parent)
    {
        super(endpoint,parent);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        return TYPE;
    }

}

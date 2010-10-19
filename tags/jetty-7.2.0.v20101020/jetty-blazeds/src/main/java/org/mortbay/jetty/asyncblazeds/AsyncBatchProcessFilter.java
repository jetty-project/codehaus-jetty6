/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2002 - 2007 Adobe Systems Incorporated
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

import org.eclipse.jetty.continuation.ContinuationThrowable;

import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.RecoverableSerializationException;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.io.amf.MessageBody;

/**
 * Filter that breaks down the batched message buffer into individual invocations.
 *
 * @author PS Neville
 */
public class AsyncBatchProcessFilter extends AMFFilter
{
    public AsyncBatchProcessFilter()
    {
    }

    public void invoke(final ActionContext context)
    {
        // Process each action in the body
        int bodyCount = context.getRequestMessage().getBodyCount();

        // Report batch size in Debug mode
        //gateway.getLogger().logDebug("Processing batch of " + bodyCount + " request(s)");

        while (context.getMessageNumber() < bodyCount)
        {
            try
            {
                int responses=context.getResponseMessage().getBodyCount();
                if (responses==context.getMessageNumber())
                {
                    // create the response body

                    MessageBody responseBody = new MessageBody();
                    responseBody.setTargetURI(context.getRequestMessageBody().getResponseURI());

                    // append the response body to the output message
                    context.getResponseMessage().addBody(responseBody);
                }
                    
                //Check that deserialized message body data type was valid. If not, skip this message.
                Object o = context.getRequestMessageBody().getData();

                if (o != null && o instanceof RecoverableSerializationException)
                {
                    context.getResponseMessageBody().setData(((RecoverableSerializationException)o).createErrorMessage());
                    context.getResponseMessageBody().setReplyMethod(MessageIOConstants.STATUS_METHOD);
                    context.incrementMessageNumber();
                    continue;
                }

                // invoke next filter in the chain
                next.invoke(context);
                context.incrementMessageNumber();
            }
            catch (ContinuationThrowable ct)
            {
                throw ct;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}

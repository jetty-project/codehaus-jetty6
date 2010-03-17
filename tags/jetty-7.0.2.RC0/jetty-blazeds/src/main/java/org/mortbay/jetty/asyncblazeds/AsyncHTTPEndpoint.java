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

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.amf.AMFFilter;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.log.LogCategories;
import flex.messaging.messages.Message;

public class AsyncHTTPEndpoint extends BaseAsyncHTTPEndpoint
{
    public static final String LOG_CATEGORY = LogCategories.ENDPOINT_HTTP;
    
    //--------------------------------------------------------------------------
    //
    // Constructor
    //
    //--------------------------------------------------------------------------
    
    /**
     * Constructs an unmanaged <code>AsyncHTTPEndpoint</code>. 
     */
    public AsyncHTTPEndpoint()
    {
        this(false);
    }
    
    /**
     * Constructs a <code>AsyncHTTPEndpoint</code> with the indicated management.
     * 
     * @param enableManagement <code>true</code> if the <code>AsyncHTTPEndpoint</code>
     * is manageable; otherwise <code>false</code>.
     */     
    public AsyncHTTPEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }

    /**
     * Currently this override is a no-op to disable small messages over HTTP
     * endpoints.
     */
    public Message convertToSmallMessage(Message message)
    {
        return message;
    }
    
    //--------------------------------------------------------------------------
    //
    // Protected/Private Methods
    //
    //--------------------------------------------------------------------------
    
    /**
     * Create default filter chain or return current one if already present.
     */
    protected AMFFilter createFilterChain()
    {
        AMFFilter serializationFilter = new AsyncSerializationFilter(getLogCategory());
        AMFFilter batchFilter = new AsyncBatchProcessFilter();
        AMFFilter sessionFilter = new AsyncSessionFilter();
        AMFFilter messageBrokerFilter = new AsyncMessageBrokerFilter(this);

        serializationFilter.setNext(batchFilter);
        batchFilter.setNext(sessionFilter);
        sessionFilter.setNext(messageBrokerFilter);

        return serializationFilter;
    }

    /**
     * Returns MessageIOConstants.XML_CONTENT_TYPE.
     */
    protected String getResponseContentType()
    {
        return MessageIOConstants.XML_CONTENT_TYPE;
    }
    
    /**
     * Returns the log category of the endpoint.
     * 
     * @return The log category of the endpoint.
     */    
    protected String getLogCategory()
    {
        return LOG_CATEGORY;
    }

    /**
     * Returns the deserializer class name used by the endpoint.
     * 
     * @return The deserializer class name used by the endpoint.
     */
    protected String getDeserializerClassName()
    {
        return "flex.messaging.io.amfx.AmfxMessageDeserializer";        
    }
    
    /**
     * Returns the serializer class name used by the endpoint.
     * 
     * @return The serializer class name used by the endpoint.
     */
    protected String getSerializerClassName()
    {
        return "flex.messaging.io.amfx.AmfxMessageSerializer";         
    }

    /**
     * Returns the Java 1.5 specific serializer class name used by the endpoint.
     * 
     * @return The Java 1.5 specific serializer class name used by the endpoint.
     */
    protected String getSerializerJava15ClassName()
    {
        return "flex.messaging.io.amfx.Java15AmfxMessageSerializer";
    }
    
    /**
     * Invoked automatically to allow the <code>AsyncHTTPEndpoint</code> to setup its 
     * corresponding MBean control.
     * 
     * @param broker The <code>MessageBroker</code> that manages this 
     * <code>AsyncHTTPEndpoint</code>.
     */
    protected void setupEndpointControl(MessageBroker broker)
    {
        controller = new AsyncHTTPEndpointControl(this, broker.getControl());
        controller.register();
        setControl(controller);
    }
}

package org.mortbay.jetty.asyncblazeds;


import flex.management.BaseControl;
import flex.management.runtime.messaging.endpoints.EndpointControl;

public class AsyncAMFEndpointControl extends EndpointControl
{

    private static final String TYPE = "AsyncAMFEndpoint";

    /**
     * Constructs a <code>AsyncAMFEndpointControl</code>, assigning managed
     * message endpoint and parent MBean.
     * 
     * @param endpoint
     *            The <code>AsyncAMFEndpoint</code> managed by this MBean.
     * @param parent
     *            The parent MBean in the management hierarchy.
     */
    public AsyncAMFEndpointControl(AsyncAMFEndpoint endpoint, BaseControl parent)
    {
        super(endpoint,parent);
    }

    /** {@inheritDoc} */
    public String getType()
    {
        return TYPE;
    }

}

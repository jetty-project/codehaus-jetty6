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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationSupport;
import org.eclipse.jetty.continuation.ContinuationThrowable;

import flex.messaging.FlexContext;
import flex.messaging.FlexSession;
import flex.messaging.client.AsyncPollHandler;
import flex.messaging.client.FlexClient;
import flex.messaging.client.FlushResult;
import flex.messaging.client.PollFlushResult;
import flex.messaging.client.PollWaitListener;
import flex.messaging.client.UserAgentSettings;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.ConfigurationConstants;
import flex.messaging.endpoints.BaseHTTPEndpoint;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.amf.ActionContext;
import flex.messaging.log.Log;
import flex.messaging.messages.CommandMessage;
import flex.messaging.util.SettingsReplaceUtil;
import flex.messaging.util.UserAgentManager;


/**
 * Base for HTTP-based endpoints that support regular polling and long polling.
 * 
 * If there are messages waiting when the client polls, the request returns immediately
 * with the messages. Otherwise, the client will hold the poll. If the endpoint is configured to wait
 * indefinitely until notified of messages, the client will do a blocking long poll, placing the request
 * thread into a wait state; if configured to wait for a positive value of milliseconds,
 * request processing is suspended and the request goes into a threadless wait. The request returns when
 * messages are available to deliver, or when the configurable wait interval is reached.
 * 
 * The threadless wait is achieved using jetty-7 portable continuations, which work asynchronous on jetty-6
 * and any servlet-3.0 container.
 */
public abstract class BaseAsyncHTTPEndpoint extends BaseHTTPEndpoint implements PollWaitListener
{
    // --------------------------------------------------------------------------
    //
    // Private Static Constants
    //
    // --------------------------------------------------------------------------

    private static final String POLLING_ENABLED = "polling-enabled";
    private static final String POLLING_INTERVAL_MILLIS = "polling-interval-millis";
    private static final String POLLING_INTERVAL_SECONDS = "polling-interval-seconds"; // Deprecated
                                                                                       // configuration
                                                                                       // option.
    private static final String MAX_WAITING_POLL_REQUESTS = "max-waiting-poll-requests";
    private static final String WAIT_INTERVAL_MILLIS = "wait-interval-millis";
    private static final String CLIENT_WAIT_INTERVAL_MILLIS = "client-wait-interval-millis";
    // Force clients that exceed the long-poll limit to wait at least this long
    // between poll requests.
    // This matches the default polling interval defined in the client
    // PollingChannel.
    private static final int DEFAULT_WAIT_FOR_EXCESS_POLL_WAIT_CLIENTS = 3000;

    // User Agent based settings manager
    private UserAgentManager userAgentManager = new UserAgentManager();

    // Access request
    private static ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();

    // --------------------------------------------------------------------------
    //
    // Constructor
    //
    // --------------------------------------------------------------------------

    /**
     * Constructs an unmanaged <code>BaseAsyncHTTPEndpoint</code>.
     */
    public BaseAsyncHTTPEndpoint()
    {
        this(false);
    }

    /**
     * Constructs a <code>BaseAsyncHTTPEndpoint</code> with the indicated
     * management.
     * 
     * @param enableManagement
     *            <code>true</code> if the <code>BaseAsyncHTTPEndpoint</code>
     *            is manageable; otherwise <code>false</code>.
     */
    public BaseAsyncHTTPEndpoint(boolean enableManagement)
    {
        super(enableManagement);
    }

    /**
     * Handle AMF/AMFX encoded messages sent over HTTP, with suspend/resume of the request,
     * using jetty-7 portable continuations.  
     *  
     * @param req The original servlet request.
     * @param res The active servlet response.
     */
    public void service(HttpServletRequest req, HttpServletResponse res)
    {
        Thread thread=Thread.currentThread();
        final Continuation continuation = ContinuationSupport.getContinuation(req);

        ActionContext context= (ActionContext)req.getAttribute("ActionContext");
        try
        {
            // Setup serialization and type marshalling contexts
            request.set(req);
            setThreadLocals();

            if (context==null)
            {
                validateRequestProtocol(req);
                context = new ActionContext();
                req.setAttribute("ActionContext",context);

                // Pass endpoint's mpi settings to the context so that it knows what level of 
                // performance metrics should be gathered during serialization/deserialization
                context.setRecordMessageSizes(isRecordMessageSizes());
                context.setRecordMessageTimes(isRecordMessageTimes());
            }

            // Send invocation through filter chain, which ends at the MessageBroker
            filterChain.invoke(context);

            if (!continuation.isSuspended())
            {
                // After serialization completes, increment endpoint byte counters, 
                // if the endpoint is managed
                if (isManaged())
                {
                    controller.addToBytesDeserialized(context.getDeserializedBytes());
                    controller.addToBytesSerialized(context.getSerializedBytes());
                }

                if (context.getStatus() != MessageIOConstants.STATUS_NOTAMF)
                {
                    if (addNoCacheHeaders)
                        addNoCacheHeaders(req, res);

                    ByteArrayOutputStream outBuffer = context.getResponseOutput();
                    res.setContentType(getResponseContentType());
                    res.setContentLength(outBuffer.size());
                    outBuffer.writeTo(res.getOutputStream());
                    res.flushBuffer();
                }
                else
                {
                    // Not an AMF request, probably viewed in a browser
                    if (redirectURL != null)
                    {
                        try
                        {
                            //Check for redirect URL context-root token
                            redirectURL = SettingsReplaceUtil.replaceContextPath(redirectURL, req.getContextPath());
                            res.sendRedirect(redirectURL);
                        }
                        catch (IllegalStateException alreadyFlushed)
                        {
                        }
                    }
                }
            }
        }
        catch (ContinuationThrowable ct)
        {
            throw ct;
        }
        catch (IOException ioe)
        {
            // This happens when client closes the connection, log it at info level
            log.info(ioe.getMessage());
        }
        catch (Throwable t)
        {
            log.error(t.getMessage(), t);
        }
        finally
        {
            request.set(null);
            clearThreadLocals();
        }        

    }

    // --------------------------------------------------------------------------
    //
    // Initialize, validate, start, and stop methods.
    //
    // --------------------------------------------------------------------------

    /**
     * Initializes the <code>Endpoint</code> with the properties. If subclasses
     * override, they must call <code>super.initialize()</code>.
     * 
     * @param id
     *            Id of the <code>Endpoint</code>.
     * @param properties
     *            Properties for the <code>Endpoint</code>.
     */
    public void initialize(String id, ConfigMap properties)
    {
        super.initialize(id,properties);

        if (properties == null || properties.size() == 0)
            return;

        // General poll props.
        pollingEnabled = properties.getPropertyAsBoolean(POLLING_ENABLED,false);
        pollingIntervalMillis = properties.getPropertyAsLong(POLLING_INTERVAL_MILLIS,-1);
        long pollingIntervalSeconds = properties.getPropertyAsLong(POLLING_INTERVAL_SECONDS,-1); // Deprecated
        if (pollingIntervalSeconds > -1)
            pollingIntervalMillis = pollingIntervalSeconds * 1000;

        // Piggybacking props.
        piggybackingEnabled = properties.getPropertyAsBoolean(ConfigurationConstants.PIGGYBACKING_ENABLED_ELEMENT,false);

        // HTTP poll wait props.
        maxWaitingPollRequests = properties.getPropertyAsInt(MAX_WAITING_POLL_REQUESTS,0);
        waitInterval = properties.getPropertyAsLong(WAIT_INTERVAL_MILLIS,0);
        clientWaitInterval = properties.getPropertyAsInt(CLIENT_WAIT_INTERVAL_MILLIS,0);

        // User Agent props.
        UserAgentManager.setupUserAgentManager(properties,userAgentManager);

        // Set initial state for the canWait flag based on whether we allow
        // waits or not.
        if (maxWaitingPollRequests > 0 && (waitInterval == -1 || waitInterval > 0))
        {
            waitEnabled = true;
            canWait = true;
        }
    }

    // --------------------------------------------------------------------------
    //
    // Variables
    //
    // --------------------------------------------------------------------------

    /**
     * This flag is volatile to allow for consistent reads across thread without
     * needing to pay the cost for a synchronized lock for each read.
     */
    private volatile boolean canWait;

    /**
     * Used to synchronize sets and gets to the number of waiting clients.
     */
    protected final Object lock = new Object();

    /**
     * Set when properties are handled; used as a shortcut for logging to
     * determine whether this instance attempts to put request threads in a wait
     * state or not.
     */
    private boolean waitEnabled;

    /**
     * A count of the number of request threads that are currently in the wait
     * state (including those on their way into or out of it).
     */
    protected int waitingPollRequestsCount;

    /**
     * A Map(notification Object for a waited request thread, Boolean.TRUE).
     */
    private ConcurrentHashMap currentWaitedRequests;

    // --------------------------------------------------------------------------
    //
    // Properties
    //
    // --------------------------------------------------------------------------

    // ----------------------------------
    // clientWaitInterval
    // ----------------------------------

    protected int clientWaitInterval = 0;

    /**
     * Returns the number of milliseconds the client will wait after receiving a
     * response for a poll with server wait before it issues its next poll
     * request. A value of zero or less causes the client to use its default
     * polling interval (based on the channel's polling-interval-millis
     * configuration) and this value is ignored. A value greater than zero will
     * cause the client to wait for the specified interval before issuing its
     * next poll request with a value of 1 triggering an immediate poll from the
     * client as soon as a waited poll response is received.
     */
    public int getClientWaitInterval()
    {
        return clientWaitInterval;
    }

    /**
     * Sets the number of milliseconds a client will wait after receiving a
     * response for a poll with server wait before it issues its next poll
     * request. A value of zero or less causes the client to use its default
     * polling interval (based on the channel's polling-interval-millis
     * configuration) and this value is ignored. A value greater than zero will
     * cause the client to wait for the specified interval before issuing its
     * next poll request with a value of 1 triggering an immediate poll from the
     * client as soon as a waited poll response is received. This property does
     * not effect polling clients that poll the server without a server wait.
     * 
     * @value The number of milliseconds a client will wait before issuing its
     *        next poll when the server is configured to wait.
     */
    public void setClientWaitInterval(int value)
    {
        clientWaitInterval = value;
    }

    // ----------------------------------
    // maxWaitingPollRequests
    // ----------------------------------

    protected int maxWaitingPollRequests = 0;

    /**
     * Returns the maximum number of server poll response threads that will be
     * waiting for messages to arrive for clients.
     */
    public int getMaxWaitingPollRequests()
    {
        return maxWaitingPollRequests;
    }

    /**
     * Sets the maximum number of server poll response threads that will be
     * waiting for messages to arrive for clients. If you set wait-interval to -1,
     * note that the request threads will block and you will need to use a lower
     * limit; if you set wait-interval to use a positive integer, the requests will be
     * suspended using threadless waits, and it is safe to set a higher limit.
     * 
     * @param maxWaitingPollRequests
     *            The maximum number of server poll response threads that will
     *            be waiting for messages to arrive for the client.
     */
    public void setMaxWaitingPollRequests(int maxWaitingPollRequests)
    {
        this.maxWaitingPollRequests = maxWaitingPollRequests;
        if (maxWaitingPollRequests > 0 && (waitInterval == -1 || waitInterval > 0))
        {
            waitEnabled = true;
            canWait = (waitingPollRequestsCount < maxWaitingPollRequests);
        }
    }

    // ----------------------------------
    // pollingEnabled
    // ----------------------------------

    /**
     * @exclude This is a property used on the client.
     */
    protected boolean piggybackingEnabled;

    // ----------------------------------
    // pollingEnabled
    // ----------------------------------

    /**
     * @exclude This is a property used on the client.
     */
    protected boolean pollingEnabled;

    // ----------------------------------
    // pollingIntervalMillis
    // ----------------------------------

    /**
     * @exclude This is a property used on the client.
     */
    protected long pollingIntervalMillis;

    // ----------------------------------
    // waitInterval
    // ----------------------------------

    protected long waitInterval = 0;

    /**
     * Returns the number of milliseconds the server poll response thread will
     * be waiting for messages to arrive for the client.
     */
    public long getWaitInterval()
    {
        return waitInterval;
    }

    /**
     * Sets the number of milliseconds the server poll response thread will be
     * waiting for messages to arrive for the client.
     * 
     * @param waitInterval
     *            The number of milliseconds the server poll response thread
     *            will be waiting for messages to arrive for the client. A value 
     *            of -1 means wait until notified, and will hold the request thread.
     *            A value > 0 puts the request into a threadless wait.
     */
    public void setWaitInterval(long waitInterval)
    {
        this.waitInterval = waitInterval;
        if (maxWaitingPollRequests > 0 && (waitInterval == -1 || waitInterval > 0))
        {
            waitEnabled = true;
            canWait = (waitingPollRequestsCount < maxWaitingPollRequests);
        }
    }

    // --------------------------------------------------------------------------
    //
    // Public Methods
    //
    // --------------------------------------------------------------------------

    /**
     * @exclude Returns a <code>ConfigMap</code> of endpoint properties that the
     *          client needs. This includes properties from
     *          <code>super.describeEndpoint</code> and additional
     *          <code>BaseHTTPEndpoint</code> specific properties under
     *          "properties" key.
     */
    public ConfigMap describeEndpoint()
    {
        ConfigMap endpointConfig = super.describeEndpoint();

        boolean createdProperties = false;
        ConfigMap properties = endpointConfig.getPropertyAsMap("properties",null);

        if (properties == null)
        {
            properties = new ConfigMap();
            createdProperties = true;
        }

        if (pollingEnabled)
        {
            ConfigMap pollingEnabled = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            pollingEnabled.addProperty("","true");
            properties.addProperty(POLLING_ENABLED,pollingEnabled);
        }

        if (pollingIntervalMillis > -1)
        {
            ConfigMap pollingInterval = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            pollingInterval.addProperty("",String.valueOf(pollingIntervalMillis));
            properties.addProperty(POLLING_INTERVAL_MILLIS,pollingInterval);
        }

        if (piggybackingEnabled)
        {
            ConfigMap piggybackingEnabled = new ConfigMap();
            // Adding as a value rather than attribute to the parent
            piggybackingEnabled.addProperty("",String.valueOf(piggybackingEnabled));
            properties.addProperty(ConfigurationConstants.PIGGYBACKING_ENABLED_ELEMENT,piggybackingEnabled);
        }

        if (createdProperties && properties.size() > 0)
            endpointConfig.addProperty(ConfigurationConstants.PROPERTIES_ELEMENT,properties);

        return endpointConfig;
    }

    /**
     * Sets up monitoring of waited poll requests so they can be notified and
     * exit when the endpoint stops.
     * 
     * @see flex.messaging.endpoints.AbstractEndpoint#start()
     */
    public void start()
    {
        if (isStarted())
            return;

        super.start();

        currentWaitedRequests = new ConcurrentHashMap();
    }

    /**
     * Ensures that no poll requests in a wait state are left un-notified when
     * the endpoint stops.
     * 
     * @see flex.messaging.endpoints.AbstractEndpoint#stop()
     */
    public void stop()
    {
        if (!isStarted())
            return;

        // Notify any currently waiting polls.
        for (Object notifier : currentWaitedRequests.keySet())
        {
            synchronized (notifier)
            {
                notifier.notifyAll(); // Break any current waits.
            }
        }
        currentWaitedRequests = null;

        super.stop();
    }

    /**
     * @see flex.messaging.client.PollWaitListener#waitStart(Object)
     */
    public void waitStart(Object notifier)
    {
        currentWaitedRequests.put(notifier,Boolean.TRUE);
    }

    /**
     * @see flex.messaging.client.PollWaitListener#waitEnd(Object)
     */
    public void waitEnd(Object notifier)
    {
        if (currentWaitedRequests != null)
            currentWaitedRequests.remove(notifier);
    }

    // --------------------------------------------------------------------------
    //
    // Protected Methods
    //
    // --------------------------------------------------------------------------

    /**
     * Overrides the base poll handling to support optionally putting Http
     * request handling threads into a wait state until messages are available
     * to be delivered in the poll response or a timeout is reached. The number
     * of threads that may be put in a wait state is bounded by
     * <code>max-waiting-poll-requests</code> and waits will only be attempted
     * if the canWait flag that is based on the
     * <code>max-waiting-poll-requests</code> and the specified
     * <code>wait-interval</code> is true.
     * 
     * @param flexClient
     *            The FlexClient that issued the poll request.
     * @param pollCommand
     *            The poll command from the client.
     * @return The flush info used to build the poll response.
     */
    protected FlushResult handleFlexClientPoll(FlexClient flexClient, CommandMessage pollCommand)
    {
        FlushResult flushResult = null;
        if (canWait && !pollCommand.headerExists(CommandMessage.SUPPRESS_POLL_WAIT_HEADER))
        {
            FlexSession session = FlexContext.getFlexSession();
            // If canWait is true it means we currently have less than the max
            // number of allowed waiting threads.

            // We need to protect writes/reads to the wait count with the
            // endpoint's lock.
            // Also, we have to be careful to handle the case where two threads
            // get to this point when only
            // one wait spot remains; one thread will win and the other needs to
            // revert to a non-waitable poll.
            boolean thisThreadCanWait;
            synchronized (lock)
            {
                ++waitingPollRequestsCount;
                if (waitingPollRequestsCount == maxWaitingPollRequests)
                {
                    thisThreadCanWait = true; // This thread got the last wait
                                              // spot.
                    canWait = false;
                }
                else if (waitingPollRequestsCount > maxWaitingPollRequests)
                {
                    thisThreadCanWait = false; // This thread was beaten out for
                                               // the last spot.
                    --waitingPollRequestsCount; // Decrement the count because
                                                // we're not going to try a poll
                                                // with wait.
                    canWait = false; // All the wait spots are currently
                                     // occupied so prevent further attempts for
                                     // now.
                }
                else
                {
                    // We haven't hit the limit yet, allow this thread to wait.
                    thisThreadCanWait = true;
                }
            }

            // Check the max waiting connections per session count
            if (thisThreadCanWait)
            {
                String userAgentValue = FlexContext.getHttpRequest().getHeader(UserAgentManager.USER_AGENT_HEADER_NAME);
                UserAgentSettings agentSettings = userAgentManager.match(userAgentValue);
                synchronized (session)
                {
                    if (agentSettings != null)
                    {
                        session.maxConnectionsPerSession = agentSettings.getMaxStreamingConnectionsPerSession();
                    }

                    ++session.streamingConnectionsCount;
                    if (session.streamingConnectionsCount <= session.maxConnectionsPerSession)
                    {
                        thisThreadCanWait = true; // We haven't hit the limit
                                                  // yet, allow the wait.
                    }
                    else
                    // (session.streamingConnectionsCount >
                    // session.maxConnectionsPerSession)
                    {
                        thisThreadCanWait = false; // no more from this client
                        --session.streamingConnectionsCount;
                    }
                }

                if (!thisThreadCanWait)
                {
                    // Decrement the waiting poll count, since this poll isn't
                    // going to wait.
                    synchronized (lock)
                    {
                        --waitingPollRequestsCount;
                        if (waitingPollRequestsCount < maxWaitingPollRequests)
                            canWait = true;
                    }
                    if (Log.isDebug())
                    {
                        log.debug("Max long-polling requests per session limit (" + session.maxConnectionsPerSession
                                + ") has been reached, this poll won't wait.");
                    }
                }

            }

            if (thisThreadCanWait)
            {
                if (Log.isDebug())
                    log.debug("Number of waiting threads for endpoint with id '" + getId() + "' is " + waitingPollRequestsCount + ".");

                try
                {
                    // Do we have async results?
                    final HttpServletRequest req = request.get();
                    flushResult = (FlushResult)req.getAttribute("AsyncFlushResults");

                    if (flushResult == null)
                    {
                        // Nothing available. Try non waiting poll.
                        flushResult = flexClient.poll(getId());

                        if (flushResult == null)
                        {
                            // Nothing available. Have we suspended before?
                            final Continuation continuation = ContinuationSupport.getContinuation(req);
                            if (waitInterval <= 0)
                            {
                                flushResult = flexClient.pollWithWait(getId(),FlexContext.getFlexSession(),this,waitInterval);
                            }
                            else if (continuation.isInitial())
                            {
                                continuation.setTimeout(waitInterval * 2);
                                continuation.suspend();
                                flexClient.pollAsync(getId(),new AsyncPollHandler()
                                {
                                    public void asyncPollComplete(FlushResult flushResult)
                                    {
                                        req.setAttribute("AsyncFlushResults",flushResult);
                                        continuation.resume();
                                    }
                                },waitInterval);
                                continuation.undispatch();
                            }
                            else
                            {
                                flushResult = flexClient.pollWithWait(getId(),FlexContext.getFlexSession(),this,1);
                            }
                        }
                    }

                    if (flushResult != null)
                    {
                        // Prevent busy-polling due to multiple clients sharing
                        // a session and swapping each other out too quickly.
                        if ((flushResult instanceof PollFlushResult) && ((PollFlushResult)flushResult).isAvoidBusyPolling()
                                && (flushResult.getNextFlushWaitTimeMillis() < DEFAULT_WAIT_FOR_EXCESS_POLL_WAIT_CLIENTS))
                        {
                            // Force the client polling interval to match the
                            // default defined in the client PollingChannel.
                            flushResult.setNextFlushWaitTimeMillis(DEFAULT_WAIT_FOR_EXCESS_POLL_WAIT_CLIENTS);
                        }
                        else if ((clientWaitInterval > 0) && (flushResult.getNextFlushWaitTimeMillis() == 0))
                        {
                            // If the FlushResult doesn't specify it's own flush
                            // wait time, use the configured clientWaitInterval
                            // if defined.
                            flushResult.setNextFlushWaitTimeMillis(clientWaitInterval);
                        }
                    }
                }
                finally
                {
                    // We're done waiting so decrement the count of waiting
                    // threads and update the canWait flag if necessary
                    synchronized (lock)
                    {
                        --waitingPollRequestsCount;
                        if (waitingPollRequestsCount < maxWaitingPollRequests)
                            canWait = true;
                    }
                    synchronized (session)
                    {
                        --session.streamingConnectionsCount;
                    }

                    if (Log.isDebug())
                        log.debug("Number of waiting threads for endpoint with id '" + getId() + "' is " + waitingPollRequestsCount + ".");
                }
            }
        }
        else if (Log.isDebug() && waitEnabled)
        {
            if (pollCommand.headerExists(CommandMessage.SUPPRESS_POLL_WAIT_HEADER))
                log.debug("Suppressing poll wait for this request because it is part of a batch of messages to process.");
            else
                log.debug("Max waiting poll requests limit '" + maxWaitingPollRequests + "' has been reached for endpoint '" + getId()
                        + "'. FlexClient with id '" + flexClient.getId() + "' will poll with no wait.");
        }

        // If we weren't able to do a poll with wait above for any reason just
        // run the base poll handling logic.
        if (flushResult == null)
        {
            flushResult = super.handleFlexClientPoll(flexClient,pollCommand);
            // If this is an excess poll request that we couldn't wait on, make
            // sure the client doesn't poll the endpoint too aggressively.
            // In this case, force a client wait to match the default polling
            // interval defined in the client PollingChannel.
            if (waitEnabled && (pollingIntervalMillis < DEFAULT_WAIT_FOR_EXCESS_POLL_WAIT_CLIENTS))
            {
                if (flushResult == null)
                {
                    flushResult = new FlushResult();
                }
                flushResult.setNextFlushWaitTimeMillis(DEFAULT_WAIT_FOR_EXCESS_POLL_WAIT_CLIENTS);
            }
        }

        return flushResult;
    }
}

package org.mortbay.servlet;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.mortbay.util.ArrayQueue;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;

/**
 * Denial of Service filter
 * 
 * <p>
 * This filter is based on the {@link QoSFilter}. it is useful for limiting
 * exposure to abuse from request flooding, whether malicious, or as a result of
 * a misconfigured client.
 * <p>
 * The filter keeps track of the number of requests from a connection per
 * second. If a limit is exceeded, the request is either rejected, delayed, or
 * throttled.
 * <p>
 * When a request is throttled, it is placed in a priority queue. Priority is
 * given first to authenticated users and users with an HttpSession, then
 * connections which can be identified by their IP addresses. Connections with
 * no way to identify them are given lowest priority.
 * <p>
 * The {@link #extractUserId(ServletRequest request)} function should be
 * implemented, in order to uniquely identify authenticated users.
 * <p>
 * The following init parameters control the behavior of the filter:
 * 
 * maxRequestsPerSec the maximum number of requests from a connection per
 * second. Requests in excess of this are first delayed, then throttled.
 * 
 * delayMs is the delay given to all requests over the rate limit, before they
 * are considered at all. -1 means just reject request, 0 means no delay,
 * otherwise it is the delay.
 * 
 * maxWaitMs how long to blocking wait for the throttle semaphore.
 * 
 * throttledRequests is the number of requests over the rate limit able to be
 * considered at once.
 * 
 * throttleMs how long to async wait for semaphore.
 * 
 */

public class DoSFilter implements Filter
{
    final static String __TRACKER = "DoSFilter.Tracker";
    final static String __THROTTLED = "DoSFilter.Throttled";

    final static int __DEFAULT_MAX_REQUESTS_PER_SEC = 25;
    final static int __DEFAULT_DELAY_MS = 100;
    final static int __DEFAULT_THROTTLE = 5;
    final static int __DEFAULT_WAIT_MS=50;
    final static long __DEFAULT_THROTTLE_MS = 30000L;

    final static String MAX_REQUESTS_PER_S_INIT_PARAM = "maxRequestsPerSec";
    final static String DELAY_MS_INIT_PARAM = "delayMs";
    final static String THROTTLED_REQUESTS_INIT_PARAM = "throttledRequests";
    final static String MAX_WAIT_INIT_PARAM="maxWaitMs";
    final static String THROTTLE_MS_INIT_PARAM = "throttleMs";

    final static int USER_AUTH = 2;
    final static int USER_SESSION = 2;
    final static int USER_IP = 1;
    final static int USER_UNKNOWN = 0;

    ServletContext _context;

    protected long _delayMs;
    protected long _throttleMs;
    protected long _waitMs;
    protected Semaphore _passes;
    protected Queue<Continuation>[] _queue;

    protected int _maxRequestsPerSec;
    protected ConcurrentHashMap<String, RateTracker> _rateTrackers;

    public void init(FilterConfig filterConfig)
    {
        _context = filterConfig.getServletContext();
        _rateTrackers = new ConcurrentHashMap<String, RateTracker>();

        _queue = new Queue[getMaxPriority() + 1];
        for (int p = 0; p < _queue.length; p++)
            _queue[p] = new ArrayQueue<Continuation>();

        int baseRateLimit = __DEFAULT_MAX_REQUESTS_PER_SEC;
        if (filterConfig.getInitParameter(MAX_REQUESTS_PER_S_INIT_PARAM) != null)
            baseRateLimit = Integer.parseInt(filterConfig.getInitParameter(MAX_REQUESTS_PER_S_INIT_PARAM));
        _maxRequestsPerSec = baseRateLimit;

        long delay = __DEFAULT_DELAY_MS;
        if (filterConfig.getInitParameter(DELAY_MS_INIT_PARAM) != null)
            delay = Integer.parseInt(filterConfig.getInitParameter(DELAY_MS_INIT_PARAM));
        _delayMs = delay;
        
        int passes = __DEFAULT_THROTTLE;
        if (filterConfig.getInitParameter(THROTTLED_REQUESTS_INIT_PARAM) != null)
            passes = Integer.parseInt(filterConfig.getInitParameter(THROTTLED_REQUESTS_INIT_PARAM));
        _passes = new Semaphore(passes,true);

        long wait = __DEFAULT_WAIT_MS;
        if (filterConfig.getInitParameter(MAX_WAIT_INIT_PARAM)!=null)
            wait=Integer.parseInt(filterConfig.getInitParameter(MAX_WAIT_INIT_PARAM));
        _waitMs=wait;
        
        long suspend = __DEFAULT_THROTTLE_MS;
        if (filterConfig.getInitParameter(THROTTLE_MS_INIT_PARAM) != null)
            suspend = Integer.parseInt(filterConfig.getInitParameter(THROTTLE_MS_INIT_PARAM));
        _throttleMs = suspend;

    }
    

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        final long now=System.currentTimeMillis();
        
        // Look for the rate tracker for this request
        RateTracker tracker = (RateTracker)request.getAttribute(__TRACKER);
            
        if (tracker==null)
        {
            // This is the first time we have seen this request.
            
            // get a rate tracker associated with this request, and record one hit
            tracker = getRateTracker(request);
            
            // Calculate the rate and check it is over the allowed limit
            final boolean overRateLimit = tracker.isRateExceeded(now);

            // pass it through if  we are not currently over the rate limit
            if (!overRateLimit)
            {
                chain.doFilter(request,response);
                return;
            }
            
            
            // We are over the limit.
            
            // So either reject it, delay it or throttle it
            switch((int)_delayMs)
            {
                case -1: 
                {
                    // Reject this request
                    ((HttpServletResponse)response).addHeader("DoSFilter","unavailable");
                    ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                    return;
                }
                case 0:
                {
                    // fall through to throttle code
                    request.setAttribute(__TRACKER,tracker);
                    break;
                }
                default:
                {
                    // insert a delay before throttling the request
                    ((HttpServletResponse)response).addHeader("DoSFilter","delayed");
                    Continuation continuation = ContinuationSupport.getContinuation((HttpServletRequest)request,this);
                    request.setAttribute(__TRACKER,tracker);
                    continuation.suspend(_delayMs);
                    // can fall through if this was a waiting continuation
                }
            }
        }

        // Throttle the request
        boolean accepted = false;
        try
        {
            // check if we can afford to accept another request at this time
            accepted = _passes.tryAcquire(_waitMs,TimeUnit.MILLISECONDS);

            if (!accepted)
            {
                // we were not accepted, so either we suspend to wait,or if we were woken up we insist or we fail

                final Continuation continuation = ContinuationSupport.getContinuation((HttpServletRequest)request,this);
                
                Boolean throttled = (Boolean)request.getAttribute(__THROTTLED);
                if (throttled!=Boolean.TRUE && _throttleMs>0)
                {
                    int priority = getPriority(request,tracker);
                    request.setAttribute(__THROTTLED,Boolean.TRUE);
                    ((HttpServletResponse)response).addHeader("DoSFilter","throttled");
                    synchronized (this)
                    {
                        _queue[priority].add(continuation);
                        continuation.reset();
                        continuation.suspend(_throttleMs);
                        // can fall through if this was a waiting continuation
                    }
                }
                
                // we have already been throttled.

                // so were we resumed?
                if (continuation.isResumed())
                {
                    // we were resumed and somebody stole our pass, so we wait for the next one.
                    _passes.acquire();
                    accepted = true;
                }
            }
            
            // if we were accepted (either immediately or after throttle)
            if (accepted)       
                // call the chain
                chain.doFilter(request,response);
            else                
            {
                // fail the request
                ((HttpServletResponse)response).addHeader("DoSFilter","unavailable");
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            }
        }
        catch (InterruptedException e)
        {
            _context.log("DoS",e);
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        finally
        {
            if (accepted)
            {
                // wake up the next highest priority request.
                synchronized (_queue)
                {
                    for (int p = _queue.length; p-- > 0;)
                    {
                        Continuation continuation = _queue[p].poll();

                        if (continuation != null)
                        {
                            continuation.resume();
                            break;
                        }
                    }
                }
                _passes.release();
            }
        }
    }

    /**
     * Get priority for this request, based on user type
     * 
     * @param request
     * @param tracker
     * @return priority
     */
    protected int getPriority(ServletRequest request, RateTracker tracker)
    {
        if (extractUserId(request)!=null)
            return USER_AUTH;
        if (tracker!=null)
            return tracker.getType();
        return USER_UNKNOWN;
    }

    /**
     * @return the maximum priority that we can assign to a request
     */
    protected int getMaxPriority()
    {
        return USER_AUTH;
    }

    /**
     * Return a request rate tracker associated with this connection; keeps
     * track of this connection's request rate. If this is not the first request
     * from this connection, return the existing object with the stored stats.
     * If it is the first request, then create a new request tracker.
     * 
     * Assumes that each connection has an identifying characteristic, and goes
     * through them in order, taking the first that matches: user id (logged
     * in), session id, client IP address. Unidentifiable connections are lumped
     * into one.
     * 
     * When a session expires, its rate tracker is automatically deleted.
     * 
     * @param request
     * @return the request rate tracker for the current connection
     */
    public RateTracker getRateTracker(ServletRequest request)
    {
        HttpServletRequest srequest = (HttpServletRequest)request;

        String loadId;
        final int type;
        
        loadId = extractUserId(request);
        HttpSession session=srequest.getSession(false);
        if (session!=null && !session.isNew())
        {
            loadId=session.getId();
            type = USER_SESSION;
        }
        else
        {
            loadId = request.getRemoteAddr();
            type = USER_IP;
        }

        RateTracker tracker=_rateTrackers.get(loadId);
        
        if (tracker==null)
        {
            RateTracker t = new RateTracker(loadId,type,_maxRequestsPerSec);
            tracker=_rateTrackers.putIfAbsent(loadId,t);
            if (tracker==null)
                tracker=t;
            if (session!=null)
                session.setAttribute(__TRACKER,tracker);
        }
        return tracker;
    }

    public void destroy()
    {
    }

    /**
     * Returns the user id, used to track this connection.
     * This SHOULD be overridden by subclasses.
     * 
     * @param request
     * @return a unique user id, if logged in; otherwise null.
     */
    protected String extractUserId(ServletRequest request)
    {
        return null;
    }

    /**
     * A RateTracker is associated with a connection, and stores request rate
     * data.
     */
    class RateTracker implements HttpSessionBindingListener
    {
        private final String _id;
        private final int _type;
        private final long[] _timestamps;
        private int _next;
        
        public RateTracker(String id, int type,int maxRequestsPerSecond)
        {
            _id = id;
            _type = type;
            _timestamps=new long[maxRequestsPerSecond];
            _next=0;
        }

        /**
         * @return the current calculated request rate over the last second
         */
        public boolean isRateExceeded(long now)
        {
            final long last;
            synchronized (this)
            {
                last=_timestamps[_next];
                _timestamps[_next]=now;
                _next= (_next+1)%_timestamps.length;
            }

            boolean exceeded=last!=0 && (now-last)<1000L;
            // System.err.println("rateExceeded? "+last+" "+(now-last)+" "+exceeded);
            return exceeded;
        }


        public String getId()
        {
            return _id;
        }

        public int getType()
        {
            return _type;
        }

        
        public void valueBound(HttpSessionBindingEvent event)
        {
        }

        public void valueUnbound(HttpSessionBindingEvent event)
        {
            _rateTrackers.remove(_id);
        }
    }
}
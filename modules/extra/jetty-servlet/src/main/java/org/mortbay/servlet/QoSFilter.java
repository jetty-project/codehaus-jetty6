package org.mortbay.servlet;

import java.io.IOException;
import java.util.Queue;
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

import org.mortbay.util.ArrayQueue;

/**
 * Quality of Service Filter.
 * This filter limits the number of active requests to the number set by the "maxRequests" init parameter (default 10).
 * If more requests are received, they are suspended and placed on priority queues.  Priorities are determined by 
 * the {@link #getPriority(ServletRequest)} method and are a value between 0 and the value given by the "maxPriority" 
 * init parameter (default 10), with higher values having higher priority.
 * <p>
 * The maxRequest limit is policed by a {@link Semaphore} and the filter will wait a short while attempting to acquire
 * the semaphore. This wait is controlled by the "waitMs" init parameter and allows the expense of a suspend to be
 * avoided if the semaphore is shortly available.
 * 
 * @author gregw
 *
 */
public class QoSFilter implements Filter
{
    final static int __DEFAULT_MAX_PRIORITY=10;
    final static int __DEFAULT_PASSES=10;
    final static int __DEFAULT_WAIT_MS=50;
    
    ServletContext _context;
    long _waitMs;
    Semaphore _passes;
    Queue<ServletRequest>[] _queue;
    String _suspended="QoSFilter@"+this.hashCode();
    
    public void init(FilterConfig filterConfig) 
    {
        _context=filterConfig.getServletContext();
        
        int max_priority=__DEFAULT_MAX_PRIORITY;
        if (filterConfig.getInitParameter("maxPriority")!=null)
            max_priority=Integer.parseInt(filterConfig.getInitParameter("maxPriority"));
        _queue=new Queue[max_priority+1];
        for (int p=0;p<_queue.length;p++)
            _queue[p]=new ArrayQueue<ServletRequest>();
        
        int passes=__DEFAULT_PASSES;
        if (filterConfig.getInitParameter("maxRequests")!=null)
            passes=Integer.parseInt(filterConfig.getInitParameter("maxRequests"));
        _passes=new Semaphore(passes,true);
        
        long wait = __DEFAULT_WAIT_MS;
        if (filterConfig.getInitParameter("waitMs")!=null)
            wait=Integer.parseInt(filterConfig.getInitParameter("waitMs"));
        _waitMs=wait;
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    throws IOException, ServletException
    {
        boolean accepted=false;

        try
        {
            if (request.isInitial() || request.getAttribute(_suspended)==null)
            {
                accepted=_passes.tryAcquire(_waitMs,TimeUnit.MILLISECONDS);
                
                if (accepted)
                {
                    request.setAttribute(_suspended,Boolean.FALSE);
                }
                else
                {
                    request.setAttribute(_suspended,Boolean.TRUE);
                    request.suspend();
                    int priority = getPriority(request);
                    _queue[priority].add(request);
                    return;
                }
            }
            else
            {
                Boolean suspended=(Boolean)request.getAttribute(_suspended);
                
                if (suspended.booleanValue())
                {
                    request.setAttribute(_suspended,Boolean.FALSE);
                    if (request.isResumed())
                    {
                        _passes.acquire();
                        accepted=true;
                    }
                    else 
                    {
                        // Timeout! try 1 more time.
                        accepted = _passes.tryAcquire(_waitMs,TimeUnit.MILLISECONDS);
                    }
                }
                else
                {
                    // pass through resume of previously accepted request
                    _passes.acquire();
                    accepted = true;
                }
            }
            

            if (accepted)
                chain.doFilter(request,response);
            else
                ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            
            
            
        }
        catch(InterruptedException e)
        {
            _context.log("QoS",e);
            ((HttpServletResponse)response).sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
        finally
        {
            if (accepted)
            {
                for (int p=_queue.length;p-->0;)
                {
                    ServletRequest r=_queue[p].poll();
                    if (r!=null)
                    {
                        r.resume();
                        break;
                    }
                }
                _passes.release();
            }
        }
    }

    /**
     * @param request
     * @return
     */
    protected int getPriority(ServletRequest request)
    {
        HttpServletRequest base_request = (HttpServletRequest)request;
        if (base_request.getUserPrincipal() != null )
            return 2;
        else 
        {
            HttpSession session = base_request.getSession(false);
            if (session!=null && !session.isNew()) 
                return 1;
            else
                return 0;
        }
    }


    public void destroy(){}

}

package org.mortbay.util;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class QoSFilter implements Filter
{
    final static int __DEFAULT_MAX_PRIORITY=10;
    final static int __DEFAULT_PASSES=100;
    final static int __DEFAULT_WAIT_MS=50;
    
    ServletContext _context;
    long _waitMs;
    Semaphore _passes;
    Queue<ServletRequest>[] _queue;
    
    public void init(FilterConfig filterConfig) 
    {
        _context=filterConfig.getServletContext();
        
        int max_priority=__DEFAULT_MAX_PRIORITY;
        if (_context.getInitParameter("maxPriority")!=null)
            max_priority=Integer.parseInt(_context.getInitParameter("maxPriority"));
        _queue=new Queue[max_priority+1];
        for (int p=0;p<_queue.length;p++)
            _queue[p]=new ArrayQueue<ServletRequest>();
        
        int passes=__DEFAULT_PASSES;
        if (_context.getInitParameter("passes")!=null)
            passes=Integer.parseInt(_context.getInitParameter("passes"));
        _passes=new Semaphore(passes,true);
        
        long wait = __DEFAULT_WAIT_MS;
        if (_context.getInitParameter("waitMs")!=null)
            wait=Integer.parseInt(_context.getInitParameter("waitMs"));
        _waitMs=wait;
    }
    
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
    throws IOException, ServletException
    {
        boolean accepted=false;

        try
        {
            if (request.isInitial())
            {
                accepted=_passes.tryAcquire(_waitMs,TimeUnit.MILLISECONDS);

                if (!accepted)
                {
                    request.suspend();
                    int priority = getPriority(request);
                    _queue[priority].add(request);
                    return;
                }
            }
            else if (request.isResumed())
            {
                _passes.acquire();
                accepted=true;
            }
            else if (!request.isTimeout())
            {
                // Pipelined request caused wakeup! 
                // Should disable pipeline handling
                // but for now Let's try 1 more time.
                accepted=_passes.tryAcquire(_waitMs,TimeUnit.MILLISECONDS);
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

    protected int getPriority(ServletRequest request)
    {
        return 0;
    }


    public void destroy(){}

}

package org.mortbay.jetty.test.remote;

public class ThreadLocalServletRequestContext
{
    private static final ThreadLocal<ServletRequestContext> context = new ThreadLocal<ServletRequestContext>()
    {
        @Override
        protected ServletRequestContext initialValue()
        {
            return null;
        }
    };

    public static ServletRequestContext get()
    {
        return context.get();
    }

    public static void set(ServletRequestContext value)
    {
        context.set(value);
    }
}

// ========================================================================
// Copyright (c) Webtide LLC
// ------------------------------------------------------------------------
// All rights reserved. This program and the accompanying materials
// are made available under the terms of the Eclipse Public License v1.0
// and Apache License v2.0 which accompanies this distribution.
//
// The Eclipse Public License is available at
// http://www.eclipse.org/legal/epl-v10.html
//
// The Apache License v2.0 is available at
// http://www.apache.org/licenses/LICENSE-2.0.txt
//
// You may elect to redistribute this code under either of these licenses.
// ========================================================================
package org.mortbay.jetty.webapp.logging;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import junit.framework.Assert;

import org.slf4j.MDC;

/**
 * Test Appender, records the logging events.
 */
public class TestAppender extends Handler
{
    private static final boolean CONSOLE = false;

    public static class LogEvent
    {
        long timestamp;
        Level severity;
        String name;
        String message;
        Throwable t;
        String mdc;

        public LogEvent(Level severity, String name, String message)
        {
            this(-1,severity,name,message,null);
        }

        @SuppressWarnings("unchecked")
        public LogEvent(long timestamp, Level severity, String name, String message, Throwable t)
        {
            super();
            this.timestamp = timestamp;
            this.severity = severity;
            this.name = name;
            this.message = message;
            this.t = t;
            this.mdc = "";

            Map<String, String> mdcMap = MDC.getCopyOfContextMap();
            if (mdcMap != null)
            {
                Set<String> keys = new TreeSet<String>();
                keys.addAll(mdcMap.keySet());
                boolean delim = false;
                for (String key : keys)
                {
                    if (delim)
                    {
                        mdc += ", ";
                    }
                    mdc += key + "=" + mdcMap.get(key);
                    delim = true;
                }
                if (mdc.length() > 0)
                {
                    System.out.println("mdc: " + mdc);
                }
            }
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            LogEvent other = (LogEvent)obj;
            if (message == null)
            {
                if (other.message != null)
                {
                    return false;
                }
            }
            else if (!message.equals(other.message))
            {
                return false;
            }
            if (name == null)
            {
                if (other.name != null)
                {
                    return false;
                }
            }
            else if (!name.equals(other.name))
            {
                return false;
            }
            if (severity == null)
            {
                if (other.severity != null)
                {
                    return false;
                }
            }
            else if (!severity.equals(other.severity))
            {
                return false;
            }

            // Throwable
            if (t == null)
            {
                if (other.t != null)
                {
                    return false;
                }
            }
            else
            {
                if (!t.getClass().equals(other.t.getClass()))
                {
                    return false;
                }
                if (t.getMessage() == null)
                {
                    if (other.t.getMessage() != null)
                    {
                        return false;
                    }
                }
                else if (!t.getMessage().equals(other.t.getMessage()))
                {
                    return false;
                }
            }

            return true;
        }

        public LogEvent expectedThrowable(Throwable t)
        {
            this.t = t;
            return this;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((message == null)?0:message.hashCode());
            result = prime * result + ((name == null)?0:name.hashCode());
            result = prime * result + ((severity == null)?0:severity.hashCode());
            if (t != null)
            {
                result = prime * result + t.getClass().hashCode();
                if (t.getMessage() != null)
                {
                    result = prime * result + t.getMessage().hashCode();
                }
                else
                {
                    result = prime * result + 0;
                }
            }
            else
            {
                result = prime * result + 0;
            }
            return result;
        }

        @Override
        public String toString()
        {
            StringBuffer buf = new StringBuffer();
            buf.append(severity.getName()).append("|");
            buf.append(name).append("|");
            buf.append(message);
            if (t != null)
            {
                buf.append("|").append(t.getClass().getName());
                buf.append("(\"").append(t.getMessage()).append("\")");
            }
            return buf.toString();
        }
    }

    public static TestAppender findAppender()
    {
        // Get root logger.
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

        // Remove all existing Handlers.
        for (java.util.logging.Handler handler : rootLogger.getHandlers())
        {
            if (handler instanceof TestAppender)
            {
                return (TestAppender)handler;
            }
        }

        Assert.fail("TestAppender should have existed in root logger, did you run TestAppender.initialize() yet?");
        return null;
    }

    public static void initialize()
    {
        // Get root logger.
        java.util.logging.Logger rootLogger = java.util.logging.Logger.getLogger("");

        boolean testappenderExists = false;
        // Remove all existing Handlers.
        for (java.util.logging.Handler handler : rootLogger.getHandlers())
        {
            if (handler instanceof TestAppender)
            {
                testappenderExists = true;
                continue; // skip
            }
            rootLogger.removeHandler(handler);
        }

        if (!testappenderExists)
        {
            rootLogger.addHandler(new TestAppender());
        }
    }

    private List<LogEvent> events = new ArrayList<LogEvent>();

    @Override
    public void close() throws SecurityException
    {
        /* nothing to do here */
    }

    public boolean contains(LogEvent expectedEvent)
    {
        return events.contains(expectedEvent);
    }

    public void dump()
    {
        System.out.printf("Captured %s event(s)%n",events.size());
        for (LogEvent event : events)
        {
            System.out.println(event);
        }
    }

    @Override
    public void flush()
    {
        /* nothing to do here */
    }

    public List<LogEvent> getEvents()
    {
        return events;
    }

    @Override
    public void publish(LogRecord record)
    {
        String name = record.getLoggerName();
        Throwable t = record.getThrown();
        
        if (CONSOLE)
        {
            System.err.println(record.getMessage());
        }

        if (name.equals("org.eclipse.jetty.util.log")) // standard jetty logger
        {
            if (t != null)
            {
                // Still interested in seeing throwables (HACK)
                t.printStackTrace(System.err);
            }
            return; // skip storing it.
        }
        events.add(new LogEvent(record.getMillis(),record.getLevel(),name,record.getMessage(),t));
    }

    public void reset()
    {
        events.clear();
    }
}

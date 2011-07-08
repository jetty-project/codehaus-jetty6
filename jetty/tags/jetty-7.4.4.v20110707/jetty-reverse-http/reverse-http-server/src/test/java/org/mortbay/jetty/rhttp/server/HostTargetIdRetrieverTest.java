/*
 * Copyright 2009-2009 Webtide LLC.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mortbay.jetty.rhttp.server;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.servlet.http.HttpServletRequest;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class HostTargetIdRetrieverTest extends TestCase
{
    public void testHostTargetIdRetrieverNoSuffix()
    {
        String host = "test";
        Class<HttpServletRequest> klass = HttpServletRequest.class;
        HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[]{klass}, new Request(host));

        HostTargetIdRetriever retriever = new HostTargetIdRetriever(null);
        String result = retriever.retrieveTargetId(request);

        assertEquals(host, result);
    }

    public void testHostTargetIdRetrieverWithSuffix()
    {
        String suffix = ".rhttp.example.com";
        String host = "test";
        Class<HttpServletRequest> klass = HttpServletRequest.class;
        HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[]{klass}, new Request(host + suffix));

        HostTargetIdRetriever retriever = new HostTargetIdRetriever(suffix);
        String result = retriever.retrieveTargetId(request);

        assertEquals(host, result);
    }

    public void testHostTargetIdRetrieverWithSuffixAndPort()
    {
        String suffix = ".rhttp.example.com";
        String host = "test";
        Class<HttpServletRequest> klass = HttpServletRequest.class;
        HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[]{klass}, new Request(host + suffix + ":8080"));

        HostTargetIdRetriever retriever = new HostTargetIdRetriever(suffix);
        String result = retriever.retrieveTargetId(request);

        assertEquals(host, result);
    }

    public void testHostTargetIdRetrieverNullHost()
    {
        Class<HttpServletRequest> klass = HttpServletRequest.class;
        HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[]{klass}, new Request(null));

        HostTargetIdRetriever retriever = new HostTargetIdRetriever(".rhttp.example.com");
        String result = retriever.retrieveTargetId(request);

        assertNull(result);
    }

    private static class Request implements InvocationHandler
    {
        private final String host;

        private Request(String host)
        {
            this.host = host;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            if ("getHeader".equals(method.getName()))
            {
                if (args.length == 1 && "Host".equals(args[0]))
                {
                    return host;
                }
            }
            return null;
        }
    }

}

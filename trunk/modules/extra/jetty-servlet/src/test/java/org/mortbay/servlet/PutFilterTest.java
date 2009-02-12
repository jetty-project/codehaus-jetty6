//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.servlet.FilterHolder;
import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.mortbay.jetty.util.IO;

import junit.framework.TestCase;

public class PutFilterTest extends TestCase
{
    File _dir;
    ServletTester tester;
    
    protected void setUp() throws Exception
    {
        _dir = File.createTempFile("testPutFilter",null);
        _dir.delete();
        _dir.mkdir();
        _dir.deleteOnExit();
        assertTrue(_dir.isDirectory());
        
        super.setUp();
        tester=new ServletTester();
        tester.setContextPath("/context");
        tester.setResourceBase(_dir.getCanonicalPath());
        tester.addServlet(org.mortbay.jetty.servlet.DefaultServlet.class, "/");
        FilterHolder holder = tester.addFilter(PutFilter.class,"/*",0);
        holder.setInitParameter("delAllowed","true");
        tester.start();
        
        
    }

    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testHandlePut() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        
        // test GET
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_NOT_FOUND,response.getStatus());

        // test PUT0
        request.setMethod("PUT");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data0="Now is the time for all good men to come to the aid of the party";
        request.setContent(data0);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File file=new File(_dir,"file.txt");
        assertTrue(file.exists());
        assertEquals(data0,IO.toString(new FileInputStream(file)));

        // test GET1
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_OK,response.getStatus());
        assertEquals(data0,response.getContent());

        // test PUT1
        request.setMethod("PUT");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data1="How Now BROWN COW!!!!";
        request.setContent(data1);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_OK,response.getStatus());
        
        file=new File(_dir,"file.txt");
        assertTrue(file.exists());
        assertEquals(data1,IO.toString(new FileInputStream(file)));
        
        

        // test PUT2
        request.setMethod("PUT");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data2="Blah blah blah Blah blah";
        request.setContent(data2);
        String to_send = request.generate();
        URL url = new URL(tester.createSocketConnector(true));
        Socket socket=new Socket(url.getHost(),url.getPort());
        OutputStream out = socket.getOutputStream();
        int l = to_send.length();
        out.write(to_send.substring(0,l-10).getBytes());
        out.flush();
        out.write(to_send.substring(l-10,l-5).getBytes());
        out.flush();
        Thread.sleep(100);

        // test GET
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_NOT_FOUND,response.getStatus());

        out.write(to_send.substring(l-5).getBytes());
        out.flush();
        String in=IO.toString(socket.getInputStream());
        
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_OK,response.getStatus());
        assertEquals(data2,response.getContent());
        
        
    }

    public void testHandleDelete() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        // test PUT1
        request.setMethod("PUT");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data1="How Now BROWN COW!!!!";
        request.setContent(data1);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File file=new File(_dir,"file.txt");
        assertTrue(file.exists());
        FileInputStream fis = new FileInputStream(file);
        assertEquals(data1,IO.toString(fis));
        fis.close();
        

        request.setMethod("DELETE");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_NO_CONTENT,response.getStatus());
        

        assertTrue(!file.exists());

        request.setMethod("DELETE");
        request.setURI("/context/file.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_FORBIDDEN,response.getStatus());
           
        
    }

    public void testHandleMove() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();

        // test PUT1
        request.setMethod("PUT");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/file.txt");
        request.setHeader("Content-Type","text/plain");
        String data1="How Now BROWN COW!!!!";
        request.setContent(data1);
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_CREATED,response.getStatus());
        
        File file=new File(_dir,"file.txt");
        assertTrue(file.exists());
        FileInputStream fis = new FileInputStream(file);
        assertEquals(data1,IO.toString(fis));
        fis.close();
        

        request.setMethod("MOVE");
        request.setURI("/context/file.txt");
        request.setHeader("new-uri","/context/blah.txt");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(HttpServletResponse.SC_NO_CONTENT,response.getStatus());
        
        assertTrue(!file.exists());

        File n_file=new File(_dir,"blah.txt");
        assertTrue(n_file.exists());

    }

    public void testHandleOptions()
    {
        // TODO implement
    }

    public void testPassConditionalHeaders()
    {
        // TODO implement
    }

}

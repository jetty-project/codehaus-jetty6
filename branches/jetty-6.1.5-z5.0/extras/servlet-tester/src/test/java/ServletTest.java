

import java.io.IOException;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;
import org.mortbay.util.IO;

public class ServletTest extends TestCase
{
    ServletTester tester;
    
    
    /* ------------------------------------------------------------ */
    protected void setUp() throws Exception
    {
        super.setUp();
        tester=new ServletTester();
        tester.setContextPath("/context");
        tester.addServlet(TestServlet.class, "/servlet/*");
        tester.addServlet(HelloServlet.class, "/hello/*");
        tester.addServlet("org.mortbay.jetty.servlet.DefaultServlet", "/");
        tester.start();
    }

    /* ------------------------------------------------------------ */
    protected void tearDown() throws Exception
    {
        tester.stop();
        tester=null;
        super.tearDown();
    }

    /* ------------------------------------------------------------ */
    public void testServletTesterRaw() throws Exception
    {
        // Raw HTTP test requests
        String requests=
            "GET /context/servlet/info?query=foo HTTP/1.1\r\n"+
            "Host: tester\r\n"+
            "\r\n"+

            "GET /context/hello HTTP/1.1\r\n"+
            "Host: tester\r\n"+
            "\r\n";

        String responses = tester.getResponses(requests);

        String expected=
            "HTTP/1.1 200 OK\r\n"+
            "Content-Type: text/html; charset=iso-8859-1\r\n"+
            "Content-Length: 21\r\n"+
            "\r\n"+
            "<h1>Test Servlet</h1>" +

            "HTTP/1.1 200 OK\r\n"+
            "Content-Type: text/html; charset=iso-8859-1\r\n"+
            "Content-Length: 22\r\n"+
            "\r\n"+
            "<h1>Hello Servlet</h1>";

        assertEquals(expected,responses);
    }

    /* ------------------------------------------------------------ */
    public void testServletTesterClient() throws Exception
    {
        String base_url=tester.createSocketConnector(true);
        
        URL url = new URL(base_url+"/context/hello/info");
        String result = IO.toString(url.openStream());
        assertEquals("<h1>Hello Servlet</h1>",result);
    }

    /* ------------------------------------------------------------ */
    public void testHttpTester() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        
        // test GET
        request.setMethod("GET");
        request.setVersion("HTTP/1.0");
        request.setHeader("Host","tester");
        request.setURI("/context/hello/info");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(200,response.getStatus());
        assertEquals("<h1>Hello Servlet</h1>",response.getContent());

        // test GET with content
        request.setMethod("POST");
        request.setContent("<pre>Some Test Content</pre>");
        request.setHeader("Content-Type","text/html");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(200,response.getStatus());
        assertEquals("<h1>Hello Servlet</h1><pre>Some Test Content</pre>",response.getContent());
        
        // test redirection
        request.setMethod("GET");
        request.setURI("/context");
        request.setContent(null);
        response.parse(tester.getResponses(request.generate()));
        assertEquals(302,response.getStatus());
        assertEquals("http://tester/context/",response.getHeader("location"));

        // test not found
        request.setURI("/context/xxxx");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(404,response.getStatus());
        
    }


    /* ------------------------------------------------------------ */
    public void testBigPost() throws Exception
    {
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        
        String content = "0123456789abcdef";
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+=content;
        content+="!";
        
        request.setMethod("POST");
        request.setVersion("HTTP/1.1");
        request.setURI("/context/hello/info");
        request.setHeader("Host","tester");
        request.setHeader("Content-Type","text/plain");
        request.setContent(content);
        String r=request.generate();
        r = tester.getResponses(r);
        response.parse(r);
        assertTrue(response.getMethod()==null);
        assertEquals(200,response.getStatus());
        assertEquals("<h1>Hello Servlet</h1>"+content,response.getContent());
        
        
    }
    /* ------------------------------------------------------------ */
    public static class HelloServlet extends HttpServlet
    {
        private static final long serialVersionUID=2779906630657190712L;

        protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            doGet(request,response);
        }
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            response.setContentType("text/html");
            response.getWriter().print("<h1>Hello Servlet</h1>");
            if (request.getContentLength()>0)
                response.getWriter().write(IO.toString(request.getInputStream()));
        }
    }
    
    public static class TestServlet extends HttpServlet
    {
        private static final long serialVersionUID=2779906630657190712L;

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            assertEquals("/context",request.getContextPath());
            assertEquals("/servlet",request.getServletPath());
            assertEquals("/info",request.getPathInfo());
            assertEquals("query=foo",request.getQueryString());
            assertEquals(1,request.getParameterMap().size());
            assertEquals(1,request.getParameterValues("query").length);
            assertEquals("foo",request.getParameter("query"));
            
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().print("<h1>Test Servlet</h1>");
        }
    }
}

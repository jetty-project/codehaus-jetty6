

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.mortbay.jetty.testing.HttpTester;
import org.mortbay.jetty.testing.ServletTester;

public class ServletTest extends TestCase
{
    public void testServletTester() throws Exception
    {
        // Setup test context
        ServletTester tester=new ServletTester();
        tester.setContextPath("/context");
        tester.addServlet(TestServlet.class, "/servlet/*");
        tester.addServlet(HelloServlet.class, "/hello");
        tester.addServlet("org.mortbay.jetty.servlet.DefaultServlet", "/");
        tester.start();
        
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

    public void testHttpTester() throws Exception
    {
        // Setup test context
        ServletTester tester=new ServletTester();
        tester.setContextPath("/context");
        tester.addServlet(HelloServlet.class, "/hello/*");
        tester.addServlet("org.mortbay.jetty.servlet.DefaultServlet", "/");
        tester.start();
        
        // generated and parsed test
        HttpTester request = new HttpTester();
        HttpTester response = new HttpTester();
        request.setMethod("GET");
        request.setHeader("Host","tester");
        request.setURI("/context/hello/info");
        request.setVersion("HTTP/1.0");
        response.parse(tester.getResponses(request.generate()));
        assertTrue(response.getMethod()==null);
        assertEquals(200,response.getStatus());
        assertEquals("<h1>Hello Servlet</h1>",response.getContent());
        
        // test redirection
        request.setURI("/context");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(302,response.getStatus());
        assertEquals("http://tester/context/",response.getHeader("location"));

        // test not found
        request.setURI("/context/xxxx");
        response.parse(tester.getResponses(request.generate()));
        assertEquals(404,response.getStatus());
        
    }

    
    public static class HelloServlet extends HttpServlet
    {
        private static final long serialVersionUID=2779906630657190712L;

        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            response.setContentType("text/html");
            response.getWriter().print("<h1>Hello Servlet</h1>");
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

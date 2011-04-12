package javax.servlet.aspect;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.Aspects;

public class AspectServlet extends HttpServlet
    {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        String greeting = "Hello";

        public AspectServlet()
        {
        }

        public AspectServlet(String hi)
        {
            greeting = hi;
        }

        @Override
        protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
        {
            response.setContentType("text/html");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println("<h1>" + greeting + " SimpleServlet</h1>");
            
            ServletContext sc = getServletContext();
            sc.getAttribute("foo");
            
            //Aspects.aspectOf(HttpServlet.class);
        } 
    }
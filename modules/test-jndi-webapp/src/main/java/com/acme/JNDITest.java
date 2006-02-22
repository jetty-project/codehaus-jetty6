/**
 * 
 */
package com.acme;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;

/**
 * @author janb
 *
 */
public class JNDITest extends HttpServlet {

    
    private static final String TABLE1 = "mytestdata1";
    private static final String TABLE2 = "mytestdata2";
    
    private static boolean setupDone = false;
    
    private DataSource myDS;
    private DataSource myDS2;
    Double wiggle;

    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            InitialContext ic = new InitialContext();
            myDS = (DataSource)ic.lookup("java:comp/env/jdbc/mydatasource");
            myDS2 = (DataSource)ic.lookup("java:comp/env/jdbc/mydatasource2");
            
            wiggle = (Double)ic.lookup("java:comp/env/wiggle");          
            
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }

    
    
    /* ------------------------------------------------------------ */
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

    /* ------------------------------------------------------------ */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        boolean doCommit = true;
        
        String action = request.getParameter("completion");
        
        if (action == null)
            doCommit = false;
        if (action.trim().equals("commit"))
            doCommit = true;
        else
            doCommit = false;
        
        try
        {
            response.setContentType("text/html");
            ServletOutputStream out = response.getOutputStream();
            out.println("<html>");
            out.println("<h1>Jetty6 JNDI & Transaction Tests</h1>");
            out.println("<body>");
            doTransaction(out, doCommit);
            out.println("<p>Value of foo in myDS after "+(doCommit?"commit":"rollback")+": <b>"+getFoo(myDS)+"</p>");
            out.println("<p>Value of foo in myDS2 after "+(doCommit?"commit":"rollback")+": <b>"+getFoo(myDS2)+"</p>");
            out.println("<a href=\"index.html\">Try again?</a>");
            out.println("</body>");            
            out.println("</html>");
            out.flush();
        }
        catch (Exception e)
        {
            throw new ServletException(e);
        }
    }
    

    public void doTransaction(ServletOutputStream out, boolean doCommit)
    throws Exception
    {
        //check DataSource and Transactions
        Connection c1 = null; 
        Connection c2 = null;
        Statement s1 = null;
        Statement s2 = null;
        UserTransaction utx = null;
        try
        {
            doSetup();
            
            InitialContext ic = new InitialContext();
            utx = (UserTransaction)ic.lookup("java:comp/UserTransaction");
            
            utx.begin();
            
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();
            
            s1.executeUpdate("update "+TABLE1+" set foo=foo + 1 where id=1");
            s2.executeUpdate("update "+TABLE2+" set foo=foo + 1 where id=1");
            
            s1.close();
            s2.close();
            
            c1.close();
            c2.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            doCommit = false;
        }
        finally
        {
           if (doCommit)
               utx.commit();
           else
               utx.rollback();
        }
        
    }
    
    private Integer getFoo (DataSource ds)
    throws Exception
    {
        Connection c = null;
        Statement s = null;
        Integer value = null;
        try
        {
            c = ds.getConnection();
            s = c.createStatement();
            String tablename = (ds.equals(myDS)?TABLE1:TABLE2);
            ResultSet results = s.executeQuery("select foo from "+tablename+" where id=1");
            if (results.next())
                value = new Integer(results.getInt(1));
            
            results.close();
            
            return value;
        }
        finally
        {
            if (s != null) s.close();
            if (c != null) c.close();
        }
    }
    
    private void doSetup ()
    throws Exception
    {
        
        if (setupDone)
            return;
        
        
        Connection c1=null;
        Connection c2=null;
        Statement s1=null;
        Statement s2=null;
        try
        {
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();
            
            s1.execute("create table "+TABLE1+" ( id INTEGER, foo INTEGER )");
            s1.executeUpdate("insert into "+TABLE1+" (id, foo) values (1, 1)");
            c1.commit();
            s2.execute("create table "+TABLE2+" ( id INTEGER, foo INTEGER )");
            s2.executeUpdate("insert into "+TABLE2+" (id, foo) values (1, 1)");
            c2.commit();
            
            setupDone = true;
        }
        finally
        {
            if (s1 != null) s1.close();
            if (s2 != null) s2.close();
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
        }
    }
    
    private void doTearDown()
    throws Exception
    {
        Connection c1=null;
        Connection c2=null;
        Statement s1=null;
        Statement s2=null;
        try
        {
            c1 = myDS.getConnection();
            c2 = myDS2.getConnection();
            
            s1 = c1.createStatement();
            s2 = c2.createStatement();
            
            s1.execute("drop table testdata");
            c1.commit();
            s2.execute("drop table testdata2");
            c2.commit();
            
        }
        finally
        {
            if (s1 != null) s1.close();
            if (s2 != null) s2.close();
            if (c1 != null) c1.close();
            if (c2 != null) c2.close();
        }
    }
    
    public void destroy ()
    {
        super.destroy();
        try
        {
            doTearDown();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}

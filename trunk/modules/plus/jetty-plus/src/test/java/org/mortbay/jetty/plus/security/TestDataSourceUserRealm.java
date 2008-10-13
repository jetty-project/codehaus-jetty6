package org.mortbay.jetty.plus.security;

import java.security.Principal;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.mortbay.jetty.plus.naming.Resource;



import junit.framework.TestCase;

public class TestDataSourceUserRealm extends TestCase
{
    private EmbeddedDataSource ds;
    
 
    public void setUp () throws Exception
    { 
      
        System.setProperty("derby.system.home", System.getProperty("basedir") + "/target/test-db");
        InitialContext ic = new InitialContext();
        ds = new EmbeddedDataSource();
        ds.setDatabaseName("testDataSourceUserRealm");
        ds.setCreateDatabase("create");
 

        Resource res = new Resource("javax.sql.DataSource/default",ds);
        
        //create tables
        Connection c = ds.getConnection();
       
        Statement s = c.createStatement();
        s.executeUpdate("create table users_ (id integer not null, "+
                                            "username varchar(100) not null unique, "+
                                            "pwd varchar(20) not null, primary key (id))");

        s.executeUpdate("create table roles_ (id integer not null, "+
                                            "role varchar(100) not null unique, primary key(id))");  
        
        s.executeUpdate("create table user_roles_ (user_id integer not null, "+
                                                 "role_id integer not null, "+
                                                 "primary key (user_id, role_id))");
        c.commit();
        s.close();
        
        //populate the database
        PreparedStatement ps = c.prepareStatement("insert into users_ values (?, ?, ?)");
        ps.setInt(1, 1);
        ps.setString(2, "foo");
        ps.setString(3, "fum");
        ps.execute();
        c.commit();
        ps.close();
        ps = c.prepareStatement("insert into roles_ values (?, ?)");
        ps.setInt(1, 1);
        ps.setString(2, "admin");
        ps.execute();     
        c.commit();
        ps.close();
        ps = c.prepareStatement("insert into roles_ values (?, ?)");
        ps.setInt(1,2);
        ps.setString(2,"user");
        ps.execute();     
        c.commit();
        ps.close();
        ps = c.prepareStatement("insert into user_roles_ values (?, ?)");
        ps.setInt(1,1);
        ps.setInt(2,1);
        ps.execute();     
        c.commit();
        ps.close();
    
        ps = c.prepareStatement("insert into user_roles_ values (?, ?)");
        ps.setInt(1,1);
        ps.setInt(2,2);
        ps.execute();
        ps.close();
        c.commit();
        c.close();
        
    }
    
    public void tearDown ()
    throws Exception
    {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        try
        {
            s.executeUpdate("drop table users_");
            System.err.println("Dropped users_");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            s.executeUpdate("drop table roles_");  
            System.err.println("Dropped roles_");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        try
        {
            s.executeUpdate("drop table user_roles_");
            System.err.println("Dropped user_roles_");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        c.commit();
        s.close();
        c.close();
    }

    public void testRealm ()
    throws Exception
    {
       DataSourceUserRealm realm = new DataSourceUserRealm("javax.sql.DataSource/default");
       realm.setName("test");
       realm.setUserTableName("users_");
       realm.setRoleTableName("roles_");
       realm.setUserRoleTableName("user_roles_");
       
       realm.setUserTableKey("id");
       realm.setUserTablePasswordField("pwd");
       realm.setUserTableUserField("username");
        
       realm.setRoleTableKey("id");
       realm.setRoleTableRoleField("role");
       
       realm.setUserRoleTableUserKey("user_id");
       realm.setUserRoleTableRoleKey("role_id");
        
       Principal p = realm.authenticate("foo", "fum", null);
       assertNotNull(p);
       assertTrue(realm.isUserInRole(p, "admin"));
       assertTrue(realm.isUserInRole(p, "user"));
       assertFalse(realm.isUserInRole(p, "prince"));
       
    }

}

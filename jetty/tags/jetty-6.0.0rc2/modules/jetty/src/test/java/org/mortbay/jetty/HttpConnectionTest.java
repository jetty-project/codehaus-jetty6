/*
 * Created on 9/01/2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.mortbay.jetty;

import junit.framework.TestCase;

/**
 * @author gregw
 *
 */
public class HttpConnectionTest extends TestCase
{
    Server server = new Server();
    LocalConnector connector = new LocalConnector();
    
    /**
     * Constructor for RFC2616Test.
     * @param arg0
     */
    public HttpConnectionTest(String arg0)
    {
        super(arg0);
        server.setConnectors(new Connector[]{connector});
        server.setHandler(new DumpHandler());
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        
        server.start();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
        server.stop();
    }
    
    

    /* --------------------------------------------------------------- */
    public void testFragmentedChunk()
    {        
        
        String response=null;
        try
        {
            int offset=0;
            
            // Chunk last
            offset=0; connector.reopen();
            response=connector.getResponses("GET /R1 HTTP/1.1\n"+
                                           "Host: localhost\n"+
                                           "Transfer-Encoding: chunked\n"+
                                           "Content-Type: text/plain\n"+
                                           "\015\012"+
                                           "5;\015\012"+
                                           "12345\015\012"+
                                           "0;\015\012\015\012");
            offset = checkContains(response,offset,"HTTP/1.1 200","Fragment Chunk");
            offset = checkContains(response,offset,"/R1","Fragmented Chunk");
            offset = checkContains(response,offset,"12345","Fragmented Chunk");
            

            response=connector.getResponses("GET /R2 HTTP/1.1\n"+
                                           "Host: localhost\n"+
                                           "Transfer-Encoding: chunked\n"+
                                           "Content-Type: text/plain\n"+
                                           "\015\012"+
                                           "5;\015\012",true);
            response=connector.getResponses("ABCDE\015\012"+
                                           "0;\015\012\015\012");
            System.err.println(response);
            offset = checkContains(response,offset,"HTTP/1.1 200","Fragment Chunk");
            offset = checkContains(response,offset,"/R2","Fragmented Chunk");
            offset = checkContains(response,offset,"ABCDE","Fragmented Chunk");
            
            
        }
        catch(Exception e)
        {
	        e.printStackTrace();
            assertTrue(false);
            if (response!=null)
                System.err.println(response);
        }
    }

    private int checkContains(String s,int offset,String c,String test)
    {
        int o=s.indexOf(c,offset);
        if (o<offset)
        {
            System.err.println("FAILED: "+test);
            System.err.println("'"+c+"' not in:");
            System.err.println(s.substring(offset));
            System.err.flush();
            System.out.println("--\n"+s);
            System.out.flush();
            assertTrue(test,false);
        }
        return o;
    }

    private void checkNotContained(String s,int offset,String c,String test)
    {
        int o=s.indexOf(c,offset);
        assertTrue(test,o<offset);
    }

    private void checkNotContained(String s,String c,String test)
    {
        checkNotContained(s,0,c,test);
    }

    

}



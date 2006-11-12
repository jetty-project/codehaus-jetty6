package org.mortbay.jetty.ajp;

import java.io.IOException;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.ByteArrayEndPoint;
import org.mortbay.io.EndPoint;
import org.mortbay.io.SimpleBuffers;
import org.mortbay.util.TypeUtil;

import junit.framework.TestCase;

public class TestAjpParser extends TestCase
{
    
    
    public void testPacket0() throws Exception
    {
        String packet = "123400f90202000f77696474683d20485454502f312e300000122f636f6e74726f6c2f70726f647563742f2200000e3230382e32372e3230332e31323800ffff000c7777772e756c74612e636f6d000050000005a006000a6b6565702d616c69766500a00b000c7777772e756c74612e636f6d00a00e002b4d6f7a696c6c612f342e302028636f6d70617469626c653b20426f726465724d616e6167657220332e302900a0010043696d6167652f6769662c20696d6167652f782d786269746d61702c20696d6167652f6a7065672c20696d6167652f706a7065672c20696d6167652f706d672c202a2f2a00a008000130000600067570726f643100ff";
        byte[] src = TypeUtil.fromHexString(packet);
        
        ByteArrayBuffer buffer= new ByteArrayBuffer(Ajp13Packet.MAX_PACKET_SIZE);
        SimpleBuffers buffers=new SimpleBuffers(new Buffer[]{buffer});
        
        EndPoint endp = new ByteArrayEndPoint(src,Ajp13Packet.MAX_PACKET_SIZE);
        
        Ajp13Parser parser = new Ajp13Parser(buffers,endp,new EH(),null);
        
        parser.parseAvailable();
    }
    
    
    private static class EH implements Ajp13Parser.EventHandler
    {

        public void content(Buffer ref) throws IOException
        {
            System.err.println(ref);
        }

        public void headerComplete() throws IOException
        {
            System.err.println();
        }

        public void messageComplete(long contextLength) throws IOException
        {
            // TODO Auto-generated method stub
        }

        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            System.err.println(name+": "+value);
        }

        public void parsedMethod(Buffer method) throws IOException
        {
            System.err.println(method);
        }

        public void parsedProtocol(Buffer protocol) throws IOException
        {
            System.err.println(protocol);
            
        }

        public void parsedQueryString(Buffer value) throws IOException
        {
            System.err.println("?"+value);
        }

        public void parsedRemoteAddr(Buffer addr) throws IOException
        {
            System.err.println("addr="+addr);
            
        }

        public void parsedRemoteHost(Buffer host) throws IOException
        {
            System.err.println("host="+host);
            
        }

        public void parsedRequestAttribute(String key, Buffer value) throws IOException
        {
            System.err.println(key+":: "+value);
            
        }

        public void parsedServerName(Buffer name) throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        public void parsedServerPort(int port) throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        public void parsedSslSecure(boolean secure) throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        public void parsedUri(Buffer uri) throws IOException
        {
            System.err.println(uri);
            
        }

        public void startForwardRequest() throws IOException
        {
            // TODO Auto-generated method stub
            
        }
        
    }


}

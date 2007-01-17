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

    public void testPacket1() throws Exception
    {
        String packet = "123401070202000f77696474683d20485454502f312e300000122f636f6e74726f6c2f70726f647563742f2200000e3230382e32372e3230332e31323800ffff000c7777772e756c74612e636f6d000050000005a006000a6b6565702d616c69766500a00b000c7777772e756c74612e636f6d00a00e002b4d6f7a696c6c612f342e302028636f6d70617469626c653b20426f726465724d616e6167657220332e302900a0010043696d6167652f6769662c20696d6167652f782d786269746d61702c20696d6167652f6a7065672c20696d6167652f706a7065672c20696d6167652f706d672c202a2f2a00a008000130000600067570726f64310008000a4145533235362d53484100ff";
        byte[] src = TypeUtil.fromHexString(packet);
        
        ByteArrayBuffer buffer= new ByteArrayBuffer(Ajp13Packet.MAX_PACKET_SIZE);
        SimpleBuffers buffers=new SimpleBuffers(new Buffer[]{buffer});
        
        EndPoint endp = new ByteArrayEndPoint(src,Ajp13Packet.MAX_PACKET_SIZE);
        
        Ajp13Parser parser = new Ajp13Parser(buffers,endp,new EH(),new Ajp13Generator(buffers,endp,0,0));
        
        parser.parseAvailable();
        
        assertTrue(true);
    }    
    
    public void testPacket2() throws Exception
    {
        String packet="1234020102020008485454502f312e3100000f2f6363632d7777777777772f61616100000c38382e3838382e38382e383830ffff00116363632e6363636363636363632e636f6d0001bb010009a00b00116363632e6363636363636363632e636f6d00a00e005a4d6f7a696c6c612f352e30202857696e646f77733b20553b2057696e646f7773204e5420352e313b20656e2d55533b2072763a312e382e312e3129204765636b6f2f32303036313230342046697265666f782f322e302e302e3100a0010063746578742f786d6c2c6170706c69636174696f6e2f786d6c2c6170706c69636174696f6e2f7868746d6c2b786d6c2c746578742f68746d6c3b713d302e392c746578742f706c61696e3b713d302e382c696d6167652f706e672c2a2f2a3b713d302e3500a004000e656e2d75732c656e3b713d302e3500a003000c677a69702c6465666c61746500a002001e49534f2d383835392d312c7574662d383b713d302e372c2a3b713d302e3700000a4b6565702d416c69766500000333303000a006000a6b6565702d616c69766500000c4d61782d466f7277617264730000023130000800124448452d5253412d4145533235362d5348410009004039324643303544413043444141443232303137413743443141453939353132413330443938363838423843433041454643364231363035323543433232353341000b0100ff";
        byte[] src=TypeUtil.fromHexString(packet);
        ByteArrayBuffer buffer=new ByteArrayBuffer(Ajp13Packet.MAX_PACKET_SIZE);
        SimpleBuffers buffers=new SimpleBuffers(new Buffer[]
        { buffer });
        EndPoint endp=new ByteArrayEndPoint(src,Ajp13Packet.MAX_PACKET_SIZE);
        Ajp13Parser parser=new Ajp13Parser(buffers,endp,new EH(),new Ajp13Generator(buffers,endp,0,0));
        parser.parse();
        assertTrue(true);
    }


    
    public void testPacketFragment() throws Exception
    {
        String packet = "123401070202000f77696474683d20485454502f312e300000122f636f6e74726f6c2f70726f647563742f2200000e3230382e32372e3230332e31323800ffff000c7777772e756c74612e636f6d000050000005a006000a6b6565702d616c69766500a00b000c7777772e756c74612e636f6d00a00e002b4d6f7a696c6c612f342e302028636f6d70617469626c653b20426f726465724d616e6167657220332e302900a0010043696d6167652f6769662c20696d6167652f782d786269746d61702c20696d6167652f6a7065672c20696d6167652f706a7065672c20696d6167652f706d672c202a2f2a00a008000130000600067570726f64310008000a4145533235362d53484100ff";
        byte[] src = TypeUtil.fromHexString(packet);
        
        for (int f=1;f<src.length;f++)
        {
            byte[] frag0=new byte[src.length-f];
            byte[] frag1=new byte[f];
            
            System.arraycopy(src,0,frag0,0,src.length-f);
            System.arraycopy(src,src.length-f,frag1,0,f);
        
            ByteArrayBuffer buffer= new ByteArrayBuffer(Ajp13Packet.MAX_PACKET_SIZE);
            SimpleBuffers buffers=new SimpleBuffers(new Buffer[]{buffer});
        
            ByteArrayEndPoint endp = new ByteArrayEndPoint(frag0,Ajp13Packet.MAX_PACKET_SIZE);
        
            Ajp13Parser parser = new Ajp13Parser(buffers,endp,new EH(),new Ajp13Generator(buffers,endp,0,0));
            parser.parseNext();
            
            endp.setIn(new ByteArrayBuffer(frag1));
            parser.parseAvailable();
        }
        
        assertTrue(true);
    }
    
    
    private static class EH implements Ajp13Parser.EventHandler
    {

        public void content(Buffer ref) throws IOException
        {
            // System.err.println(ref);
        }

        public void headerComplete() throws IOException
        {
            // System.err.println();
        }

        public void messageComplete(long contextLength) throws IOException
        {
            // TODO Auto-generated method stub
        }

        public void parsedHeader(Buffer name, Buffer value) throws IOException
        {
            // System.err.println(name+": "+value);
        }

        public void parsedMethod(Buffer method) throws IOException
        {
            // System.err.println(method);
        }

        public void parsedProtocol(Buffer protocol) throws IOException
        {
            // System.err.println(protocol);
            
        }

        public void parsedQueryString(Buffer value) throws IOException
        {
            // System.err.println("?"+value);
        }

        public void parsedRemoteAddr(Buffer addr) throws IOException
        {
            // System.err.println("addr="+addr);
            
        }

        public void parsedRemoteHost(Buffer host) throws IOException
        {
            // System.err.println("host="+host);
            
        }

        public void parsedRequestAttribute(String key, Buffer value) throws IOException
        {
            // System.err.println(key+":: "+value);
            
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
            // System.err.println(uri);
            
        }

        public void startForwardRequest() throws IOException
        {
            // TODO Auto-generated method stub
            
        }

        public void parsedRequestAttribute(String key, int value) throws IOException
        {
            // TODO Auto-generated method stub
            
        }
        
    }


}

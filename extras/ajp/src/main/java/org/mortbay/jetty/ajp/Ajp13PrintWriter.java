package org.mortbay.jetty.ajp;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

public class Ajp13PrintWriter extends PrintWriter
{
        private Ajp13Generator _generator;
        
        public Ajp13PrintWriter(OutputStream out, Ajp13Generator generator)
        {
                super(out);
                _generator = generator;
                
        }

        public void write(int c)
        {
                try
                {
                        _generator.addContent((byte)c);
                }
                catch (IOException e)
                {       
                        e.printStackTrace();
                        setError();
                }
        }

        public void write(char[] buf, int off, int len)
        {
                int size = off + len;
                for(int i=off; i<size; i++)
                {
                        write((int) buf[i]);
                }
                
        }

        public void write(char[] buf)
        {
                for(int i=0; i<buf.length; i++)
                {
                        write((int) buf[i]);
                }
        }

        public void write(String s, int off, int len)
        {
                write(s.toCharArray(), off, len);
        }

        public void write(String s)
        {
                byte[] b = s.getBytes();
                for(int i=0; i<b.length; i++)
                {
                        write((int) b[i]);
                }
        }
        
        
        
        
        

}

package org.mortbay.jetty.io.nio;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.ByteChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;

import org.mortbay.jetty.io.AbstractBuffer;
import org.mortbay.jetty.io.Buffer;

public class RandomAccessFileBuffer extends AbstractBuffer implements Buffer
{
    RandomAccessFile _file;
    FileChannel _channel;
    int _capacity=Integer.MAX_VALUE;

    public RandomAccessFileBuffer(File file) 
        throws FileNotFoundException
    {
        super(READWRITE,true);
        assert file.length()<=Integer.MAX_VALUE;
        _file = new RandomAccessFile(file,"rw");
        _channel=_file.getChannel();
        setGetIndex(0);
        setPutIndex((int)file.length());
    }
    
    public RandomAccessFileBuffer(File file,int capacity) 
        throws FileNotFoundException
    {
        super(READWRITE,true);
        assert capacity>=file.length();
        assert file.length()<=Integer.MAX_VALUE;
        _capacity=capacity;
        _file = new RandomAccessFile(file,"rw");
        _channel=_file.getChannel();
        setGetIndex(0);
        setPutIndex((int)file.length());
    }
    
    public RandomAccessFileBuffer(File file,int capacity,int access) 
        throws FileNotFoundException
    {
        super(access,true);
        assert capacity>=file.length();
        assert file.length()<=Integer.MAX_VALUE;
        _capacity=capacity;
        _file = new RandomAccessFile(file,access==READWRITE?"rw":"r");
        _channel=_file.getChannel();
        setGetIndex(0);
        setPutIndex((int)file.length());
    }

    public byte[] array()
    {
        return null;
    }

    public int capacity()
    {
        return _capacity;
    }

    public void clear()
    {
        try
        {
            synchronized (_file)
            {
                super.clear();
                _file.setLength(0);
            }
        }
        catch(Exception e)
        {
            throw new RuntimeException(e);
        }
    }


    public byte peek()
    {
        synchronized (_file)
        {
            try
            {
                if (_get!=_file.getFilePointer())
                    _file.seek(_get);
                return _file.readByte();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public byte peek(int index)
    {
        synchronized (_file)
        {
            try
            {
                _file.seek(index);
                return _file.readByte();
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public int peek(int index, byte[] b, int offset, int length)
    {
        synchronized (_file)
        {
            try
            {
                _file.seek(index);
                return _file.read(b,offset,length);
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public void poke(int index, byte b)
    {
        synchronized (_file)
        {
            try
            {
                _file.seek(index);
                _file.writeByte(b);
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    public int poke(int index, byte[] b, int offset, int length)
    {
        synchronized (_file)
        {
            try
            {
                _file.seek(index);
                _file.write(b,offset,length);
                return length;
            }
            catch(Exception e)
            {
                throw new RuntimeException(e);
            }
        }
    }
    
    public int writeTo(WritableByteChannel channel,int index, int length)
        throws IOException
    {
        synchronized (_file)
        {
            return (int)_channel.transferTo(index,length,channel);
        }
    }
    
}

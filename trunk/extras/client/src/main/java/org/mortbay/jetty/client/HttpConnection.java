package org.mortbay.jetty.client;

import java.io.IOException;
import java.util.LinkedList;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.ByteArrayBuffer;
import org.mortbay.io.EndPoint;
import org.mortbay.io.SimpleBuffers;
import org.mortbay.io.bio.StringEndPoint;
import org.mortbay.jetty.Generator;
import org.mortbay.jetty.HttpException;
import org.mortbay.jetty.HttpFields;
import org.mortbay.jetty.HttpGenerator;
import org.mortbay.jetty.HttpParser;
import org.mortbay.jetty.Parser;
import org.mortbay.log.Log;
import org.mortbay.util.ajax.Continuation;

public class HttpConnection
{
    private Generator _generator;
    private HttpParser _parser;
    private EndPoint _endp;
    private Exchange _exchange;
    private LinkedList _exchanges=new LinkedList();

    /* ------------------------------------------------------------ */
    HttpConnection(Buffers buffers,EndPoint endp,int hbs,int cbs)
    {
        _endp=endp;
        _generator=new HttpGenerator(buffers,endp,hbs,cbs);
        _parser=new HttpParser(buffers,endp,new Handler(),hbs,cbs);
    }

    /* ------------------------------------------------------------ */
    public void sendExchange(Exchange ex) 
        throws IOException
    {
        _exchanges.add(ex);
    }

    /* ------------------------------------------------------------ */
    private void nextExchange() throws IOException
    {
        if (_exchange!=null || _exchanges.size()==0)
            return;
        
        _exchange=(Exchange)_exchanges.remove(0);
        System.err.println("EX:"+_exchange.getUri());

        _generator.setVersion(11); // TODO
        _generator.setRequest(_exchange.getMethod(),_exchange.getUri());
        
        if (_exchange.getRequestContent()!=null)
        {
            _generator.addContent(_exchange.getRequestContent(),Generator.LAST);
            _generator.completeHeader(_exchange.getRequestFields(),Generator.LAST);
            _generator.complete();
        }
        
    }
    
    /* ------------------------------------------------------------ */
    public void handle() throws IOException
    {
        if (_exchange==null)
            nextExchange();
            
        int no_progress=0;
        while (_endp.isOpen())
        {
            try
            {
                long io=0;

                // Do we have more writting to do?
                if (_generator.isCommitted() && !_generator.isComplete()) 
                    io+=_generator.flush();
                
                if (_endp.isBufferingOutput())
                {
                    _endp.flush();
                    if (_endp.isBufferingOutput())
                        no_progress=0;
                }

                // If we are not ended then parse available
                if (!_parser.isComplete()) 
                    io=_parser.parseAvailable();

                if (io>0)
                    no_progress=0;
                else if (no_progress++>=2) 
                    return;
            }
            catch (IOException e)
            {
                // TODO Handle this!
                return;
            }
            finally
            {
                if (_parser.isComplete() && _generator.isComplete() && !_endp.isBufferingOutput())
                {  
                    _exchange=null;
                    nextExchange();
                    if (_exchange==null)
                        reset(true);
                }
            }
        }
    }


    /* ------------------------------------------------------------ */
    protected void reset(boolean returnBuffers)
    {
        _parser.reset(returnBuffers); 
        _generator.reset(returnBuffers); 
    }

    /* ------------------------------------------------------------ */
    private class Handler extends HttpParser.EventHandler
    {
        public void content(Buffer ref) throws IOException
        {
            System.err.println(ref);
        }

        public void startRequest(Buffer method, Buffer url, Buffer version) throws IOException
        {
            throw new IllegalStateException();
        }

        public void startResponse(Buffer version, int status, Buffer reason) throws IOException
        {
            System.err.println(version+" "+status+" "+reason);
        }
        
    }
    
    
}

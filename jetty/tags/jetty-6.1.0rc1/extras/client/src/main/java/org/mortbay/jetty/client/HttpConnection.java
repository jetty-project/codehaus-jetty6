package org.mortbay.jetty.client;

import java.io.IOException;
import java.util.LinkedList;

import org.mortbay.io.Buffer;
import org.mortbay.io.Buffers;
import org.mortbay.io.EndPoint;
import org.mortbay.jetty.Generator;
import org.mortbay.jetty.HttpGenerator;
import org.mortbay.jetty.HttpParser;

public class HttpConnection
{
    private HttpDestination _destination;
    private EndPoint _endp;
    private HttpExchange _exchange;
    private LinkedList<HttpExchange> _exchanges=new LinkedList<HttpExchange>();
    private Generator _generator;
    private HttpParser _parser;

    /* ------------------------------------------------------------ */
    HttpConnection(HttpDestination destination, Buffers buffers,EndPoint endp,int hbs,int cbs)
    {
        _destination=destination;
        _endp=endp;
        _generator=new HttpGenerator(buffers,endp,hbs,cbs);
        _parser=new HttpParser(buffers,endp,new Handler(),hbs,cbs);
    }

    public HttpDestination getDestination()
    {
        return _destination;
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
    public void send(HttpExchange ex) 
        throws IOException
    {
        _exchanges.add(ex);
        
        // TODO schedule stuff
        
    }


    /* ------------------------------------------------------------ */
    private void nextExchange() throws IOException
    {
        if (_exchange!=null || _exchanges.size()==0)
            return;
        
        _exchange=_exchanges.remove(0);
        System.err.println("EX:"+_exchange.getURI());

        _generator.setVersion(11); // TODO
        _generator.setRequest(_exchange.getMethod(),_exchange.getURI());
        
        if (_exchange.getRequestContent()!=null)
        {
            _generator.addContent(_exchange.getRequestContent(),Generator.LAST);
            _generator.completeHeader(_exchange.getRequestFields(),Generator.LAST);
            _generator.complete();
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
        @Override
        public void content(Buffer ref) throws IOException
        {
            System.err.println(ref);
        }

        @Override
        public void startRequest(Buffer method, Buffer url, Buffer version) throws IOException
        {
            throw new IllegalStateException();
        }

        @Override
        public void startResponse(Buffer version, int status, Buffer reason) throws IOException
        {
            System.err.println(version+" "+status+" "+reason);
        }
        
    }
    
    
}

//========================================================================
//Copyright 2006 Mort Bay Consulting Pty. Ltd.
//------------------------------------------------------------------------
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at 
//http://www.apache.org/licenses/LICENSE-2.0
//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.
//========================================================================

package org.mortbay.jetty.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Response;

public class StatisticsHandler extends HandlerWrapper
{
    transient long _statsStartedAt;
    
    transient int _requests;
    
    transient long _requestsDurationMin;         // min request duration
    transient long _requestsDurationMax;         // max request duration
    transient long _requestsDurationTotal;       // total request duration
    
    transient int _requestsActive;
    transient int _requestsActiveMin;            // min number of connections handled simultaneously
    transient int _requestsActiveMax;
    transient int _responses1xx; // Informal
    transient int _responses2xx; // Success
    transient int _responses3xx; // Redirection
    transient int _responses4xx; // Client Error
    transient int _responses5xx; // Server Error
    

    /* ------------------------------------------------------------ */
    public void statsReset()
    {
        synchronized(this)
        {
            if (isStarted())
                _statsStartedAt=System.currentTimeMillis();
            _requests=0;
            _requestsActiveMax=_requestsActive;
            _responses1xx=0;
            _responses2xx=0;
            _responses3xx=0;
            _responses4xx=0;
            _responses5xx=0;
          
            _requestsActiveMin=_requestsActive;
            _requestsActiveMax=_requestsActive;
            _requestsActive=0;

            _requestsDurationMin=0;
            _requestsDurationMax=0;
            _requestsDurationTotal=0;
        }
    }


    /* ------------------------------------------------------------ */
    public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException
    {
        final Request base_request=(request instanceof Request)?((Request)request):HttpConnection.getCurrentConnection().getRequest();
        final Response base_response=(response instanceof Response)?((Response)response):HttpConnection.getCurrentConnection().getResponse();
        
        try
        {
            synchronized(this)
            {
                _requests++;
                _requestsActive++;
                if (_requestsActive>_requestsActiveMax)
                    _requestsActiveMax=_requestsActive;
            }
            
            super.handle(target, request, response, dispatch);
        }
        finally
        {
            synchronized(this)
            {
                _requestsActive--;
                if (_requestsActive<0)
                    _requestsActive=0;
                if (_requestsActive < _requestsActiveMin)
                    _requestsActiveMin=_requestsActive;
                
                long duration = System.currentTimeMillis()-base_request.getTimeStamp();
                
                _requestsDurationTotal+=duration;
                if (_requestsDurationMin==0 || duration<_requestsDurationMin)
                    _requestsDurationMin=duration;
                if (duration>_requestsDurationMax)
                    _requestsDurationMax=duration;
                
                switch(base_response.getStatus()/100)
                {
                    case 1: _responses1xx++;break;
                    case 2: _responses2xx++;break;
                    case 3: _responses3xx++;break;
                    case 4: _responses4xx++;break;
                    case 5: _responses5xx++;break;
                }
                
            }
        }
    }

    /* ------------------------------------------------------------ */
    protected void doStart() throws Exception
    {
        super.doStart();
        _statsStartedAt=System.currentTimeMillis();
    }

    /* ------------------------------------------------------------ */
    protected void doStop() throws Exception
    {
        super.doStop();
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of requests handled by this context
     * since last call of statsReset(). If setStatsOn(false) then this
     * is undefined.
     */
    public int getRequests() {return _requests;}

    /* ------------------------------------------------------------ */
    /**
     * @return Number of requests currently active.
     * Undefined if setStatsOn(false).
     */
    public int getRequestsActive() {return _requestsActive;}

    /* ------------------------------------------------------------ */
    /**
     * @return Maximum number of active requests
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public int getRequestsActiveMax() {return _requestsActiveMax;}

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of responses with a 2xx status returned
     * by this context since last call of statsReset(). Undefined if
     * if setStatsOn(false).
     */
    public int getResponses1xx() {return _responses1xx;}

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of responses with a 100 status returned
     * by this context since last call of statsReset(). Undefined if
     * if setStatsOn(false).
     */
    public int getResponses2xx() {return _responses2xx;}

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of responses with a 3xx status returned
     * by this context since last call of statsReset(). Undefined if
     * if setStatsOn(false).
     */
    public int getResponses3xx() {return _responses3xx;}

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of responses with a 4xx status returned
     * by this context since last call of statsReset(). Undefined if
     * if setStatsOn(false).
     */
    public int getResponses4xx() {return _responses4xx;}

    /* ------------------------------------------------------------ */
    /**
     * @return Get the number of responses with a 5xx status returned
     * by this context since last call of statsReset(). Undefined if
     * if setStatsOn(false).
     */
    public int getResponses5xx() {return _responses5xx;}

    /* ------------------------------------------------------------ */
    /** 
     * @return Timestamp stats were started at.
     */
    public long getStatsOnMs()
    {
        return System.currentTimeMillis()-_statsStartedAt;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestsActiveMin.
     */
    public int getRequestsActiveMin()
    {
        return _requestsActiveMin;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestsDurationMin.
     */
    public long getRequestsDurationMin()
    {
        return _requestsDurationMin;
    }

    /* ------------------------------------------------------------ */
    /**
     * @return Returns the requestsDurationTotal.
     */
    public long getRequestsDurationTotal()
    {
        return _requestsDurationTotal;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return Average duration of request handling in milliseconds 
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public long getRequestsDurationAve() {return _requests==0?0:(_requestsDurationTotal/_requests);}

    /* ------------------------------------------------------------ */
    /** 
     * @return Get maximum duration in milliseconds of request handling
     * since statsReset() called. Undefined if setStatsOn(false).
     */
    public long getRequestsDurationMax() {return _requestsDurationMax;}
    

}

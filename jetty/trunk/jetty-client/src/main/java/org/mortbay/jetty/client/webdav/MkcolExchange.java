//========================================================================
//Copyright 2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.client.webdav;

import java.io.IOException;

import org.mortbay.jetty.client.CachedExchange;
import org.mortbay.jetty.http.HttpStatus;
import org.mortbay.jetty.io.Buffer;
import org.mortbay.jetty.util.log.Log;


public class MkcolExchange extends CachedExchange
{
    boolean exists = false;

    public MkcolExchange()
    {
        super(true);
    }

    /* ------------------------------------------------------------ */
    protected void onResponseStatus(Buffer version, int status, Buffer reason) throws IOException
    {
        if ( status == HttpStatus.CREATED_201 )
        {
            Log.debug( "MkcolExchange:Status: Successfully created resource" );
            exists = true;
        }

        if ( status == HttpStatus.METHOD_NOT_ALLOWED_405 ) // returned when resource exists
        {
            Log.debug( "MkcolExchange:Status: Resource must exist" );
            exists = true;
        }

        super.onResponseStatus(version, status, reason);
    }

    public boolean exists()
    {
        return exists;
    }
}
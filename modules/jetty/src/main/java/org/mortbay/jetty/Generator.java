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


package org.mortbay.jetty;

import java.io.IOException;

public interface Generator
{
    void complete() throws IOException;

    void completeHeader(HttpFields responseFields, boolean last) throws IOException;

    long flush() throws IOException;

    int getContentBufferSize();

    long getContentWritten();

    void increaseContentBufferSize(int size);

    boolean isCommitted();

    boolean isComplete();

    boolean isPersistent();

    void reset(boolean returnBuffers);

    void resetBuffer();

    void sendError(int code, String reason, String content, boolean close) throws IOException;

    void setHead(boolean head);

    void setResponse(int status, String reason);

    void setSendServerVersion(boolean sendServerVersion);

    void setVersion(int version);

}

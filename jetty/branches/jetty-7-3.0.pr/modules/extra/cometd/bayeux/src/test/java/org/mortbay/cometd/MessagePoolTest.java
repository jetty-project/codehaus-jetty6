//========================================================================
//Copyright 2004-2008 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.cometd;

import org.cometd.Message;

import junit.framework.TestCase;

public class MessagePoolTest extends TestCase
{
    String test="[{\"id\":\"1197508701252\",\"data\":{\"user\":\"Client0002\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 2\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701253\",\"data\":{\"user\":\"Client0003\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 3\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701254\",\"data\":{\"user\":\"Client0004\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 4\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701256\",\"data\":{\"user\":\"Client0005\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 5\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701257\",\"data\":{\"user\":\"Client0006\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 6\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701258\",\"data\":{\"user\":\"Client0007\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 7\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701260\",\"data\":{\"user\":\"Client0008\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 8\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701263\",\"data\":{\"user\":\"Client0009\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 9\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"},{\"id\":\"1197508701268\",\"data\":{\"user\":\"Client0010\",\"chat\":\"xxxxxxxxxxxxxxxxxxxx 10\"},\"channel\":\"/chat/demo\",\"clientId\":\"1xl73fat6x0ft\"}];";

    public void testParse() throws Exception
    {
        MessagePool pool = new MessagePool();
        Message[] messages = pool.parse(test);
        assertEquals(9,messages.length);
    }
}

//========================================================================
//$Id$
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.mortbay.resource.Resource;

/**
 * @version $Revision$
 */
public class ContextHandlerTest extends TestCase
{
    public void testGetResourcePathsWhenSuppliedPathEndsInSlash() throws Exception
    {
        checkResourcePathsForExampleWebApp("/WEB-INF/");
    }

    public void testGetResourcePathsWhenSuppliedPathDoesNotEndInSlash() throws Exception
    {
        checkResourcePathsForExampleWebApp("/WEB-INF");
    }

    private void checkResourcePathsForExampleWebApp(String root) throws IOException, MalformedURLException
    {
        File testDirectory = setupTestDirectory();

        ContextHandler handler = new ContextHandler();

        assertTrue("Not a directory " + testDirectory, testDirectory.isDirectory());
        handler.setBaseResource(Resource.newResource(testDirectory.toURL()));

        List paths = new ArrayList(handler.getResourcePaths(root));
        assertEquals(2, paths.size());

        Collections.sort(paths);
        assertEquals("/WEB-INF/jsp/", paths.get(0));
        assertEquals("/WEB-INF/web.xml", paths.get(1));
    }

    private File setupTestDirectory() throws IOException
    {
        File root = new File(System.getProperty("basedir", "modules/jetty"), "target/" + getClass().getName());
        root.mkdir();

        File webInf = new File(root, "WEB-INF");
        webInf.mkdir();

        new File(webInf, "jsp").mkdir();
        new File(webInf, "web.xml").createNewFile();

        return root;
    }
}

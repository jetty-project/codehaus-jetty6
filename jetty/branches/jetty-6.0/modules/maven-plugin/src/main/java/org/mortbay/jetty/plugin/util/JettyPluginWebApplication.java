//========================================================================
//$Id$
//Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plugin.util;

import java.io.File;
import java.util.List;

/**
 * JettyPluginWebApplication
 * 
 * Type to hide differences in API for various jetty
 * versions. Eg there will be an impl of this class
 * for jetty5 (proxying WebApplicationContext) and
 * jetty6 (proxying WebAppContext).
 *
 */
public interface JettyPluginWebApplication extends Proxy {
    
    public void setContextPath (String path);
    public String getContextPath ();
    public void setWebAppSrcDir (File webAppDir) throws Exception;
    public void setTempDirectory (File tmpDir);
    public void setWebDefaultXmlFile (File webDefaultXml) throws Exception;
    public void setClassPathFiles (List classpathFiles);
    public void setWebXmlFile (File webxml);
    public void setJettyEnvXmlFile (File jettyEnvXml);
    public void configure ();
    public void start () throws Exception;
    public void stop () throws Exception;
    public Object getProxiedObject();

}

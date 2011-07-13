// ========================================================================
// Copyright 2006-2007 Sabre Holdings.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================

package org.mortbay.jetty.ant.types;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jetty.security.LoginService;

/**
 * Specifies a jetty configuration <loginServices/> element for Ant build file.
 *
 * @author Jakub Pawlowicz
 */
public class LoginServices
{

    private List loginServices = new ArrayList();

    public void add(LoginService service)
    {
        loginServices.add(service);
    }

    public List getLoginServices()
    {
        return loginServices;
    }
}

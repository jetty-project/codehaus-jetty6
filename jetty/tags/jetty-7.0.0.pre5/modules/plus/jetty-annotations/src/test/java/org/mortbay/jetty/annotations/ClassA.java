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

package org.mortbay.jetty.annotations;


/**
 * ClassA
 *
 *
 */
@Sample(1)
public class ClassA
{
    private Integer e;
    private Integer f;
    private Integer g;
    private Integer h;
    private Integer j;
    private Integer k;


    public static class Foo
    {
        
    }
    
    @Sample(7)
    private Integer m;
    
    @Sample(2)
    public void a (Integer[] x)
    {
        System.err.println("ClassA.public");
    }
    
    @Sample(3)
    protected void b(Foo[] f)
    {
        System.err.println("ClassA.protected");
    }
    
    @Sample(4)
    void c(int[] x)
    {
        System.err.println("ClassA.package");
    }

    @Sample(5)
    private void d(int x, String y)
    {
        System.err.println("ClassA.private");
    }
    
    @Sample(6)
    protected void l()
    {
        System.err.println("ClassA.protected method l");
    }

    public Integer getE()
    {
        return this.e;
    }
    
    public Integer getF()
    {
        return this.f;
    }
    
    public Integer getG()
    {
        return this.g;
    }
    
    public Integer getJ()
    {
        return this.j;
    }
    
    public void x()
    {
        System.err.println("ClassA.x");
    }
}

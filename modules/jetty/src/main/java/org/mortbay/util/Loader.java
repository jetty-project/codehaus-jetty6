// ========================================================================
// Copyright 2004-2005 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.util;

/* ------------------------------------------------------------ */
/** ClassLoader Helper.
 * This helper class allows classes to be loaded either from the
 * Thread's ContextClassLoader, the classloader of the derived class
 * or the system ClassLoader.
 *
 * <B>Usage:</B><PRE>
 * public class MyClass {
 *     void myMethod() {
 *          ...
 *          Class c=Loader.loadClass(this.getClass(),classname);
 *          ...
 *     }
 * </PRE>          
 * @author Greg Wilkins (gregw)
 */
public class Loader
{
    /* ------------------------------------------------------------ */
    public static Class loadClass(Class loadClass,String name)
        throws ClassNotFoundException
    {
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        if (loader==null)
            loader=loadClass.getClassLoader();
        if (loader==null)
            return Class.forName(name);
        return loader.loadClass(name);
    }

    /* ------------------------------------------------------------ */
    public static Class findAndLoadClass(Class loadClass,String name)
        throws ClassNotFoundException
    {
        ClassNotFoundException ex=null;
        Class c =null;
        ClassLoader loader=Thread.currentThread().getContextClassLoader();
        if (loader!=null)
        {
            try { c=loadClass(loader,name); }
            catch (ClassNotFoundException e) {ex=e;}
        }
        
        if (c==null && loadClass!=null && loadClass.getClassLoader()!=null)
        {
            try { c=loadClass(loader,name); }
            catch (ClassNotFoundException e) {if(ex==null)ex=e;}
        }       

        {
            try { c=Class.forName(name); }
            catch (ClassNotFoundException e) {if(ex==null)ex=e;}
        }   

        if (c!=null)
            return c;
        throw ex;
    }
    

    /* ------------------------------------------------------------ */
    public static Class loadClass(ClassLoader loader,String name)
        throws ClassNotFoundException
    {
        ClassNotFoundException ex=null;
        Class c =null;
        
        try { c=loader.loadClass(name); }
        catch (ClassNotFoundException e) {ex=e;}
        
        if (c==null && loader.getParent()!=null)
        {
            try {loadClass(loader.getParent(),name);}
            catch (ClassNotFoundException e) {if (ex==null) ex=e;}   
        }

        if (c!=null)
            return c;
        throw ex;
    }
    
}


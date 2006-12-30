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

package org.mortbay.util;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * IntrospectionUtil
 *
 *
 */
public class IntrospectionUtil
{
    
    public static Method findMethod (Class clazz, String methodName, Class[] args, boolean checkInheritance, boolean strictArgs)
    throws NoSuchMethodException
    {
        if (clazz == null)
            throw new NoSuchMethodException("No class");
        if (methodName==null || methodName.trim().equals(""))
            throw new NoSuchMethodException("No method name");
        
        Method method = null;
        Method[] methods = clazz.getDeclaredMethods();
        for (int i=0;i<methods.length && method==null;i++)
        {
            if (methods[i].getName().equals(methodName) && checkParams(methods[i].getParameterTypes(), (args==null?new Class[] {}:args), strictArgs))
            {
                method = methods[i];
            }
            
        }
        if (method!=null)
        {
            return method;
        }
        else if (checkInheritance)
                return findInheritedMethod(clazz.getPackage(), clazz.getSuperclass(), methodName, args, strictArgs);
        else
            throw new NoSuchMethodException("No such method "+methodName+" on class "+clazz.getName());

    }
    
    
    
    

    public static Field findField (Class clazz, String targetName, Class targetType, boolean checkInheritance, boolean strictType)
    throws NoSuchFieldException
    {
        if (clazz == null)
            throw new NoSuchFieldException("No class");
        if (targetName==null)
            throw new NoSuchFieldException("No field name");
        
        try
        {
            Field field = clazz.getDeclaredField(targetName);
            if (strictType)
            {
                if (field.getType().equals(targetType))
                    return field;
            }
            else
            {
                if (field.getType().isAssignableFrom(targetType))
                    return field;
            }
            if (checkInheritance)
            {
                    return findInheritedField(clazz.getPackage(), clazz.getSuperclass(), targetName, targetType, strictType);
            }
            else
                throw new NoSuchFieldException("No field with name "+targetName+" in class "+clazz.getName()+" of type "+targetType);
        }
        catch (NoSuchFieldException e)
        {
            return findInheritedField(clazz.getPackage(),clazz.getSuperclass(), targetName,targetType,strictType);
        }
    }
    
    
    
    
    
    public static boolean checkInheritable (Package pack, Member member)
    {
        if (pack==null)
            return false;
        if (member==null)
            return false;
        
        int modifiers = member.getModifiers();
        if (Modifier.isPublic(modifiers))
            return true;
        if (Modifier.isProtected(modifiers))
            return true;
        if (!Modifier.isPrivate(modifiers) && pack.equals(member.getDeclaringClass().getPackage()))
            return true;
       
        return false;
    }
    
    
    public static boolean checkParams (Class[] formalParams, Class[] actualParams, boolean strict)
    {
        if (formalParams==null && actualParams==null)
            return true;
        if (formalParams==null && actualParams!=null)
            return false;
        if (formalParams!=null && actualParams==null)
            return false;

        if (formalParams.length!=actualParams.length)
            return false;

        if (formalParams.length==0)
            return true; 
        
        int j=0;
        if (strict)
        {
            while (j<formalParams.length && formalParams[j].equals(actualParams[j]))
                j++;
        }
        else
        { 
            while ((j<formalParams.length) && (formalParams[j].isAssignableFrom(actualParams[j])))
            {
                j++;
            }
        }

        if (j!=formalParams.length)
        {
            return false;
        }

        return true;
    }
    
    public static boolean checkType (Class formalType, Class actualType, boolean strict)
    {
        if (formalType==null && actualType != null)
            return false;
        if (formalType!=null && actualType==null)
            return false;
        if (formalType==null && actualType==null)
            return true;
       
        if (strict)
            return formalType.equals(actualType);
        else
            return formalType.isAssignableFrom(actualType);
    }

    
    protected static Method findInheritedMethod (Package pack, Class clazz, String methodName, Class[] args, boolean strictArgs)
    throws NoSuchMethodException
    {
        if (clazz==null)
            throw new NoSuchMethodException("No class");
        if (methodName==null)
            throw new NoSuchMethodException("No method name");
        
        Method method = null;
        Method[] methods = clazz.getDeclaredMethods();
        for (int i=0;i<methods.length && method==null;i++)
        {
            if (methods[i].getName().equals(methodName) 
                    && checkInheritable(pack,methods[i])
                    && checkParams(methods[i].getParameterTypes(), args, strictArgs))
                method = methods[i];
        }
        if (method!=null)
        {
            return method;
        }
        else
            return findInheritedMethod(clazz.getPackage(), clazz.getSuperclass(), methodName, args, strictArgs);
    }
    
    protected static Field findInheritedField (Package pack, Class clazz, String fieldName, Class fieldType, boolean strictType)
    throws NoSuchFieldException
    {
        if (clazz==null)
            throw new NoSuchFieldException ("No class");
        if (fieldName==null)
            throw new NoSuchFieldException ("No field name");
        try
        {
            Field field = clazz.getDeclaredField(fieldName);
            if (checkInheritable(pack, field) && checkType(fieldType, field.getType(), strictType))
                return field;
            else
                return findInheritedField(clazz.getPackage(), clazz.getSuperclass(),fieldName, fieldType, strictType);
        }
        catch (NoSuchFieldException e)
        {
            return findInheritedField(clazz.getPackage(), clazz.getSuperclass(),fieldName, fieldType, strictType); 
        }
    }
    
}

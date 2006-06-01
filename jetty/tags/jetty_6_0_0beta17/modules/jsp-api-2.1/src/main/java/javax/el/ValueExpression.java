/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.el;

/**
 *
 */
public abstract class ValueExpression extends Expression {

    public abstract Class<?> getExpectedType();
    
    public abstract Class<?> getType(ELContext context) throws NullPointerException, PropertyNotFoundException, ELException;
    
    public abstract boolean isReadOnly(ELContext context) throws NullPointerException, PropertyNotFoundException, ELException;
    
    public abstract void setValue(ELContext context, Object value) throws NullPointerException, PropertyNotFoundException, PropertyNotWritableException, ELException;
    
    public abstract Object getValue(ELContext context) throws NullPointerException, PropertyNotFoundException, ELException;

}

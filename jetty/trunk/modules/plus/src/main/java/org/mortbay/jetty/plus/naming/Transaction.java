// ========================================================================
// $Id$
// Copyright 2006 Mort Bay Consulting Pty. Ltd.
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

package org.mortbay.jetty.plus.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

/**
 * Transaction
 *
 * Class to represent a JTA UserTransaction impl.
 * 
 * 
 */
public class Transaction extends NamingEntry
{

    public static Transaction getTransaction (String jndiName)
    throws NamingException
    {
        try
        {
            InitialContext ic = new InitialContext();
            return (Transaction)ic.lookup(Transaction.class.getName()+"/"+jndiName);
        }
        catch (NameNotFoundException e)
        {
            return null;
        }
    }
    
    
    
    public Transaction (UserTransaction userTransaction)
    throws NamingException
    {
        super ("UserTransaction", userTransaction);
        Context iContext = new InitialContext();
        Context compContext = (Context)iContext.lookup("java:comp");
        
        //bind into the jndi a UserTransaction at java:comp/UserTransaction
        bind(compContext);       
    }
    
    
    public void bindToEnv ()
    throws NamingException
    {
        return;
    }
    
    
}

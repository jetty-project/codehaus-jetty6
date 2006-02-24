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
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.mortbay.naming.NamingUtil;

/**
 * Transaction
 *
 * Class to represent a JTA UserTransaction impl.
 * 
 * 
 */
public class Transaction extends NamingEntry
{
    public static final String USER_TRANSACTION = "UserTransaction";
    

    public static Transaction getTransaction ()
    throws NamingException
    {
        try
        {
            InitialContext ic = new InitialContext();
            return (Transaction)ic.lookup(Transaction.class.getName()+"/"+USER_TRANSACTION);
        }
        catch (NameNotFoundException e)
        {
            return null;
        }
    }
    
    
    
    public Transaction (UserTransaction userTransaction)
    throws NamingException
    {
        super (USER_TRANSACTION, userTransaction);           
    }
    
    
    public void bindToEnv ()
    throws NamingException
    {
        Context iContext = new InitialContext();
        Context compContext = (Context)iContext.lookup("java:comp");
        NamingUtil.bind(compContext, getJndiName(), new LinkRef(getJndiName())); 
    }
    
    
}

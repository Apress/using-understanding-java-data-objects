/*
Copyright (c) 2002 Yankee Software.

This file is part of the JDO Learning Tools

The JDO Learning Tools is free software; you can use it, redistribute it,
and/or modify it under the terms of the GNU General Public License as
published by the Free Software Foundation; either version 2 of the
License, or (at your option) any later version.

The JDO Learning Tools software is distributed in the hope that it
will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See
the GNU General Public License for more details.

A copy of the GPL Version 2 is contained in LICENSE.TXT in this source
distribution.  If you cannot find LICENSE.TXT, write to the Free
Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
02111-1307 USA or visit www.fsf.org on the web.

Copyright law and the license agreement do not apply to your
understanding of the the concepts, principles, and practices embedded
in this code.  The purpose of the JDO Learning Tools to to help
advance the use and understanding of Java Data Objects, the standard
for transparent persistence for Java objects from the Java Community
Process.

Change History:

Please insert a brief record of any changes made.

Author            Date        Purpose
-----------------+----------+-----------------------------------
David Ezzio       09/01/02   Created
*/
package com.ysoft.jdo.book.factory;

import java.io.IOException;
import java.util.HashMap;

import javax.jdo.*;
import javax.naming.*;
import javax.resource.cci.ConnectionFactory;
import javax.rmi.PortableRemoteObject;

import com.ysoft.jdo.book.common.MsgCenter;


public class JndiLocator
   {
   private static ConnectionFactory cachedCF;
   private static HashMap           pmfMap = new HashMap();

   public static synchronized PersistenceManagerFactory getPMF(
      String jndiName, String propFileName)
         throws NamingException, IOException
      {
      try
         {
         PersistenceManagerFactory cachedPMF = (PersistenceManagerFactory) pmfMap.get(jndiName);

         if (cachedPMF == null)
            {
            Object obj = new InitialContext().lookup(jndiName);
            cachedPMF = (PersistenceManagerFactory) PortableRemoteObject.narrow(obj,
                  PersistenceManagerFactory.class);
            MsgCenter.putMsg("caching PMF in JndiLocator");
            pmfMap.put(jndiName, cachedPMF);
            }

         return cachedPMF;
         }
      catch (NameNotFoundException e)
         {
         MsgCenter.putMsg("Adding PMF to JNDI under name: " + jndiName);
         return addPMF(jndiName, propFileName);
         }
      }

   public static synchronized ConnectionFactory getCF(String jndiName)
         throws NamingException
      {
      if (cachedCF == null)
         {
         Object obj = new InitialContext().lookup(jndiName);
         cachedCF = (ConnectionFactory) PortableRemoteObject.narrow(obj,
               ConnectionFactory.class);
         MsgCenter.putMsg("caching CF (jndi name: " + jndiName +
            ") in JndiLocator");
         }

      return cachedCF;
      }

   public static String getString(String jndiName)
         throws NamingException
      {
      Object obj = new InitialContext().lookup(jndiName);
      return (String) PortableRemoteObject.narrow(obj, String.class);
      }

   private static PersistenceManagerFactory getPMF(String propFileName)
         throws IOException
      {
      JDOFactory.setVerbose(false);
      JDOFactory.useProperties(propFileName);
      return JDOFactory.getPersistenceManagerFactory();
      }

   private static PersistenceManagerFactory addPMF(String jndiName,
      String propFileName)
         throws NamingException, IOException
      {
      PersistenceManagerFactory pmf = getPMF(propFileName);
      JDOFactory.tellConfiguration(pmf);

      // Get a naming context
      InitialContext jndiContext = new InitialContext();

      //System.out.println("Got initial context");
      jndiContext.rebind(jndiName, pmf);
      jndiContext.close();
      return pmf;
      }
   }

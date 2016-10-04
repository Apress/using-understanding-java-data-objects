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
package com.ysoft.jdo.book.factory.lido;

import java.io.*;
import java.util.*;

import com.libelis.lido.PersistenceManagerFactory;
import com.libelis.lido.api.LiDOHelper;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.factory.*;


/**
 * The adaptor pattern is used to isolate the JDO implementation
 * dependencies for creating or fetching an implemented JDO
 * PersistenceManagerFactory, and for closing it.
 */
public class JDOLidoAdaptor implements JDOFactoryAdaptor
   {
   /**
    * We construct the factory and return it here.
    * The code here is entirely implementation and application dependent.
    * This code is for the Tech Trader Kudo implementation.
    */
   public javax.jdo.PersistenceManagerFactory obtainPMF()
      {
      // setup the persistence manager factory
      PersistenceManagerFactory factory = null;

      try
         {
         factory = new com.libelis.lido.PersistenceManagerFactory();

         // configure the factory to connect to Oracle
         factory.setConnectionDriverName("oracle.jdbc.driver.OracleDriver");
         factory.setConnectionURL("jdbc:oracle:thin:@ysoftlaptop:1521:OAK");
         factory.setConnectionUserName("lido");
         factory.setConnectionPassword("lido");
         }
      catch (Exception e)
         {
         MsgCenter.putException("Caught exception opening Lido PMF", e);
         }

      return factory;
      }

   public void close(javax.jdo.PersistenceManagerFactory pmf)
      {
      LiDOHelper.close(pmf);
      }
   }

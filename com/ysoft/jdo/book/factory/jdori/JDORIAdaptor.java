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
package com.ysoft.jdo.book.factory.jdori;

import java.io.*;
import java.util.*;

import javax.jdo.*;

import com.sun.jdori.fostore.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.factory.*;


/**
 * The adaptor pattern is used to isolate the JDO implementation
 * dependencies for creating or fetching an implemented JDO
 * PersistenceManagerFactory, and for closing it.
 */
public class JDORIAdaptor implements JDOFactoryAdaptor
   {
   /**
    * We construct the factory and return it here.
    * The code here is entirely implementation and application dependent.
    * This code is for the JDO reference implementation,
    * and the testing examples in the book.
    */
   public PersistenceManagerFactory obtainPMF()
      {
      String  path   = "FOStoreTestDB"; // the default name of the RI test DB
      File    dbFile = new File(path + ".btd");
      boolean exists = dbFile.exists();

      MsgCenter.putMsg("The database (" + dbFile + ") " +
         (exists ? "" : "does not ") + "exists");

      String url = "fostore:" + path;
      MsgCenter.putMsg("Using URL: (" + url + ")");

      FOStorePMF factory = new FOStorePMF();
      factory.setConnectionCreate(!exists);
      factory.setConnectionUserName("JDO");
      factory.setConnectionPassword("book");
      factory.setConnectionURL(url);

      /*
      // debugging
      class MyFOStorePMF extends FOStorePMF
         {
         public HashMap getLocalAccessors()
            {
            return super.getLocalAccessors();
            }
         }
      MyFOStorePMF f = new MyFOStorePMF();
      HashMap m = f.getLocalAccessors();
      Iterator iter = m.keySet().iterator();
      while (iter.hasNext())
         {
         Object k = iter.next();
         Object v = m.get(k);

         MsgCenter.putMsg("   Key: " + k + ", value: " + v);
         }
      */
      return factory;
      }

   public void close(PersistenceManagerFactory pmf)
      {
      FOStorePMF factory = (FOStorePMF) pmf;

      MsgCenter.putMsg("Closing FOStoreDB");

      if (factory != null)
         {
         try
            {
            MsgCenter.putMsg("Closing the FOStorePMF");
            factory.close(false);
            }
         catch (JDOUserException ex)
            {
            MsgCenter.putMsg("FOStorePMF close threw " + ex);
            MsgCenter.putMsg("   Forcing close");
            factory.close(true);
            }
         finally
            {
            factory = null;
            }
         }
      }
   }

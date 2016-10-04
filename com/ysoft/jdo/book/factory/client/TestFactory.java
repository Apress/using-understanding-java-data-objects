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
package com.ysoft.jdo.book.factory.client;

import javax.jdo.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.factory.*;


public class TestFactory
   {
   private static final int NUMBER_PMS    = 1;
   private static int       numPMs        = NUMBER_PMS;
   private static String    adaptor_class = "com.ysoft.jdo.book.factory.jdori.JDORIAdaptor";

   public static void main(String[] args)
      {
      MsgCenter.putMsg("Starting TestFactory ... ");

      getArgs(args);

      JDOFactory.setVerbose(true);
      JDOFactory.useConstruction(adaptor_class, false, false, false, false,
         false, false);

      PersistenceManagerFactory pmf = JDOFactory.getPersistenceManagerFactory();

      MsgCenter.putMsg("Obtained PersistenceManagerFactory");

      PersistenceManager[] aPM = new PersistenceManager[numPMs];

      for (int x = 0; x < numPMs; x++)
         {
         PersistenceManager pm = pmf.getPersistenceManager();

         if (pm == null)
            throw new JDOUserException(
               "PersistenceManagerFactory returned a null PersistenceManager");

         aPM[x] = pm;
         }

      MsgCenter.putMsg("Just got " + numPMs + " PersistenceManagers!");

      JDOFactory.close();

      MsgCenter.putMsg("-- All done!");

      MsgCenter.shutdown();
      }

   private static int getNum(String numStr)
      {
      try
         {
         return Integer.parseInt(numStr);
         }
      catch (Exception e)
         {
         tellSyntax();
         }

      return 0;
      }

   private static String getString(String str)
      {
      String s = str.trim();

      if (s.length() <= 0)
         tellSyntax();

      return s;
      }

   private static void getArgs(String[] args)
      {
      if (args.length < 1)
         return;
      else if (args.length == 1)
         {
         adaptor_class = getString(args[0]);
         }
      else if ((args.length == 2) && args[0].equalsIgnoreCase("-numPMs"))
         {
         numPMs = getNum(args[1]);
         }
      else if ((args.length == 3) && args[0].equalsIgnoreCase("-numPMs"))
         {
         numPMs           = getNum(args[1]);
         adaptor_class    = getString(args[2]);
         }
      else if ((args.length == 3) && args[1].equalsIgnoreCase("-numPMs"))
         {
         numPMs           = getNum(args[2]);
         adaptor_class    = getString(args[0]);
         }
      else
         tellSyntax();
      }

   private static void tellSyntax()
      {
      System.out.println("Accepts the name of the adaptor class");
      System.out.println(
         "  If not provided, then com.ysoft.jdo.book.factory.jdori.JDORIAdaptor is assumed");
      System.out.println(
         "Also accepts -numPMs <integer> to specify the number of PMs to acquire.");
      System.out.println("  If not provided, it defaults to one.");
      System.exit(1);
      }
   }

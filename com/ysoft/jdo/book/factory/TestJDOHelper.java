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

import java.io.*;
import java.util.*;

import javax.jdo.*;

import com.ysoft.jdo.book.common.Copyright;


public class TestJDOHelper
   {
   public static PersistenceManagerFactory getPMF(String propFileName)
      {
      Properties props;

      try
         {
         props = loadProperties(propFileName);
         props.list(System.out);
         }
      catch (Exception e)
         {
         System.out.println("Caught exception trying to load properties file");
         e.printStackTrace(System.out);
         return null;
         }

      return JDOHelper.getPersistenceManagerFactory(props);
      }

   public static Properties loadProperties(String propFileName)
         throws IOException
      {
      ClassLoader cl     = Thread.currentThread().getContextClassLoader();
      InputStream stream = cl.getResourceAsStream(propFileName);

      if (stream == null)
         throw new IOException("File not found: " + propFileName);

      Properties props = new Properties();
      props.load(stream);

      stream.close();
      return props;
      }

   public static void main(String[] args)
      {
      Copyright.stdout();

      if (args.length < 1)
         {
         System.out.println(
            "usage: java com.ysoft.jdo.book.factory.TestJDOHelper <property file name>");
         System.exit(1);
         }

      PersistenceManagerFactory pmf = getPMF(args[0]);

      if ((pmf != null) && (pmf.getPersistenceManager() != null))
         System.out.println("Got the PMF okay");
      }
   }

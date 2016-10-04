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
package com.ysoft.jdo.book.common;

public class ClassObserver
   {
   public static void describeBriefly(Class c)
      {
      msg("Class: " + c.getName(), 0);

      Class[] interfaces = c.getInterfaces();

      if (interfaces.length > 0)
         {
         msg("supports interfaces:", 1);

         for (int x = 0; x < interfaces.length; x++)
            {
            msg(interfaces[x].getName(), 2);
            }
         }

      Class s = c.getSuperclass();

      if (s != null)
         msg("derived from: " + s.getName(), 1);
      }

   public static void describeBriefly(Object o)
      {
      if (o == null)
         msg("Class observer asked to observe null", 0);
      else
         describeBriefly(o.getClass());
      }

   public static void describeHierarchyBriefly(Class c)
      {
      describeBriefly(c);

      while ((c = c.getSuperclass()) != null)
         {
         // stop the description loop if we are in java. or javax. packages
         if (c.getName().startsWith("java"))
            break;

         describeBriefly(c);
         }
      }

   public static void describeHierarchyBriefly(Object o)
      {
      if (o == null)
         msg("Class observer asked to observe null", 0);
      else
         describeHierarchyBriefly(o.getClass());
      }

   public static void describeHierarchy(Class c)
      {
      describeBriefly(c);

      while ((c = c.getSuperclass()) != null)
         {
         describeBriefly(c);
         }
      }

   public static void describeHierarchy(Object o)
      {
      if (o == null)
         msg("Class observer asked to observe null", 0);
      else
         describeHierarchy(o.getClass());
      }

   private static void msg(String m, int level)
      {
      StringBuffer b = new StringBuffer();

      while (level-- > 0)
         b.append("   ");

      b.append(m);
      MsgCenter.putMsg(b.toString());
      }
   }

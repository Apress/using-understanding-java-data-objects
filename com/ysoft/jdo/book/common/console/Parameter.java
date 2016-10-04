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
package com.ysoft.jdo.book.common.console;

import java.util.*;


public class Parameter
   {
   private Class  pClass;
   private String pName;
   private Object pValue;

   public Parameter(Class pClass, String pName)
      {
      if (pName != null)
         pName = pName.trim();

      if ((pClass == null) || (pName == null) || (pName.length() <= 0))
         throw new IllegalArgumentException(
            "class or name cannot be null or empty");

      this.pClass    = pClass;
      this.pName     = pName;
      }

   public Class getType()
      {
      return pClass;
      }

   public String getName()
      {
      return pName;
      }

   public void set(Object obj)
      {
      if (isPrimitive())
         setPrimitive(obj);
      else if ((obj != null) && !pClass.isInstance(obj))
         throw new IllegalArgumentException(
            "passed object is not an instance of " + pClass.getName());

      pValue = obj;
      }

   public Object get()
      {
      return pValue;
      }

   public boolean isPrimitive()
      {
      return pClass.isPrimitive();
      }

   private void setPrimitive(Object obj)
      {
      if ((pClass == int.class) && obj instanceof Integer)
         ;
      else if ((pClass == short.class) && obj instanceof Short)
         ;
      else if ((pClass == long.class) && obj instanceof Long)
         ;
      else if ((pClass == byte.class) && obj instanceof Byte)
         ;
      else if ((pClass == float.class) && obj instanceof Float)
         ;
      else if ((pClass == double.class) && obj instanceof Double)
         ;
      else if ((pClass == boolean.class) && obj instanceof Boolean)
         ;
      else
         throw new IllegalArgumentException(
            "the value is not of the correct type to wrap the primitive");

      pValue = obj;
      }
   }

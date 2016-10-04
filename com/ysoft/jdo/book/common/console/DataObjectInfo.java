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

import java.lang.reflect.*;
import java.util.*;


public class DataObjectInfo
   {
   private Class    pcClass;
   private String[] persistentFields;

   // package access required for iterator
   Parameter[] parameters;

   public DataObjectInfo(Class pcClass, String[] persistentFields,
      Parameter[] parameters)
      {
      if ((pcClass == null) || (persistentFields == null) ||
               (persistentFields.length <= 0))
         throw new IllegalArgumentException();

      this.pcClass             = pcClass;
      this.persistentFields    = persistentFields;
      this.parameters          = parameters;
      }

   public Object createObject()
      {
      Object retv = null;

      try
         {
         if ((parameters == null) || (parameters.length == 0))
            retv = pcClass.newInstance();
         else
            {
            Class[]  pTypes  = new Class[parameters.length];
            Object[] pValues = new Object[parameters.length];

            for (int x = parameters.length - 1; x >= 0; x--)
               {
               pTypes[x]     = parameters[x].getType();
               pValues[x]    = parameters[x].get();
               }

            Constructor c = pcClass.getConstructor(pTypes);

            retv = c.newInstance(pValues);
            }
         }
      catch (NoSuchMethodException e)
         {
         throw new RuntimeException(
            "could not find constructor matching parameters");
         }
      catch (InstantiationException e)
         {
         throw new RuntimeException("cannot construct this object " +
            e.getMessage());
         }
      catch (IllegalAccessException e)
         {
         throw new RuntimeException("cannot access this constructor " +
            e.getMessage());
         }
      catch (InvocationTargetException e)
         {
         throw new RuntimeException("exception thrown by constructor " +
            e.getTargetException().getMessage());
         }

      return retv;
      }

   public Iterator iterator()
      {
      return new ParameterIterator(this);
      }

   public String getName()
      {
      String n = pcClass.getName();

      int    pos = n.lastIndexOf(".");
      pos++;
      return n.substring(pos);
      }

   public String[] getPersistentAttributes()
      {
      return persistentFields;
      }

   public Class getType()
      {
      return pcClass;
      }
   }


class ParameterIterator implements Iterator
   {
   int            current;
   DataObjectInfo target;

   ParameterIterator(DataObjectInfo target)
      {
      this.target = target;
      }

   public boolean hasNext()
      {
      return current < target.parameters.length;
      }

   public Object next()
      {
      if (current >= target.parameters.length)
         throw new NoSuchElementException();

      return target.parameters[current++];
      }

   public void remove()
      {
      throw new UnsupportedOperationException();
      }
   }

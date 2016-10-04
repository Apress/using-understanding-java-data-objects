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
package com.ysoft.jdo.book.coffee;

public class CoffeeUrn
   {
   private static final int MAX_CUPS  = 40;
   private int              numCups;
   private int              cupsDrawn;
   private String           name;

   private CoffeeUrn()
      {
      }

   public CoffeeUrn(String name)
      {
      if ((name == null) || (name.trim().length() <= 0))
         throw new IllegalArgumentException("The name cannot be null or empty");

      this.name = name.trim();
      }

   public void addCoffee(int amount)
      {
      if (amount < 0)
         throw new IllegalArgumentException("The amount must be >= 0");

      numCups += amount;

      if (numCups > MAX_CUPS)
         numCups = MAX_CUPS;
      }

   public int drawCoffee()
      {
      if (numCups <= 0)
         return 0;

      numCups--;
      cupsDrawn++;
      return 1;
      }

   public String toString()
      {
      return "CoffeeUrn [" + name + "-" + cupsDrawn + "] contains " + numCups +
      " cups";
      }
   }

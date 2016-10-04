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
David Ezzio       11/01/02   Created
*/
package com.ysoft.jdo.book.sayings.persistent;

import java.io.Serializable;
import java.util.Random;


/**
 * This is intended to be a singleton.  There should
 * be only one in the database, although there
 * will be one per active PM in memory.
 */
public class QuoteManager
   {
   private static final int SINGLETON_KEY = 1;
   private static Random    random    = new Random();
   private int              numQuotes;
   private int              pKey      = 1; // the primary key

   // used by JDO
   private QuoteManager()
      {
      }

   // used by QuoteManager factory method
   private QuoteManager(int pk)
      {
      pKey = pk;
      }

   public static Quote makeTempQuote(String q, String s)
      {
      return new Quote(q, s, -1);
      }

   public static QuoteManager newQuoteManager()
      {
      return new QuoteManager(SINGLETON_KEY);
      }

   public int getNumQuotes()
      {
      return numQuotes;
      }

   public static Integer getSingletonKey()
      {
      return new Integer(SINGLETON_KEY);
      }

   public Quote newQuote(String saying, String source)
      {
      return new Quote(saying, source, numQuotes++);
      }

   public int getRandomIndex()
      {
      synchronized (QuoteManager.class)
         {
         return random.nextInt(numQuotes);
         }
      }

   public static class QuoteManagerOID implements Serializable
      {
      public int pKey;

      public QuoteManagerOID()
         {
         }

      public QuoteManagerOID(String pkStr)
         {
         this(Integer.decode(pkStr).intValue());
         }

      public QuoteManagerOID(Integer pkInteger)
         {
         this(pkInteger.intValue());
         }

      public QuoteManagerOID(int pk)
         {
         pKey = pk;
         }

      public String toString()
         {
         return Integer.toString(pKey);
         }

      public Integer toInteger()
         {
         return new Integer(pKey);
         }

      public boolean equals(Object other)
         {
         if (this == other)
            return true;

         if (!(other instanceof QuoteManagerOID))
            return false;

         return (pKey == ((QuoteManagerOID) other).pKey);
         }

      public int hashCode()
         {
         return pKey;
         }
      }
   }

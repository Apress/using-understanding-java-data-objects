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

import java.io.IOException; // debugging only
import java.io.OutputStream; // debugging only
import java.io.Serializable;

import javax.jdo.JDOHelper; // debugging only

import com.ysoft.jdo.book.common.MsgCenter;


public class Quote implements Serializable
   {
   private String saying;
   private String source;
   private int    quoteIndex;

   private Quote()
      {
      // used only by JDO
      }

   // this constructor is used by the QuoteManager
   Quote(String saying, String source, int index)
      {
      if ((saying == null) || (saying.trim().length() <= 0))
         throw new IllegalArgumentException("saying cannot be null or empty");

      saying = saying.trim();

      if ((source == null) || (source.trim().length() <= 0))
         source = "anonymous";
      else
         source = source.trim();

      this.saying        = saying;
      this.source        = source;
      this.quoteIndex    = index;

      MsgCenter.putMsg("Constructed quote: " + this);
      }

   public String getSaying()
      {
      return saying;
      }

   public String getSource()
      {
      return source;
      }

   public int getIndex()
      {
      return quoteIndex;
      }

   public String toString()
      {
      return "\"" + saying + "\"  -- " + source + " [" + quoteIndex + "]";
      }

   /*
   // for debugging only
   private void writeObject(java.io.ObjectOutputStream stream) throws IOException
      {
      MsgCenter.putMsg("Quote.writeObject: " + this);
      //MsgCenter.putMsg("   Quote's PM is " +
      //      (JDOHelper.getPersistenceManager(this).isClosed() ? "closed" : "open"));
      stream.defaultWriteObject();
      }
   */
   }

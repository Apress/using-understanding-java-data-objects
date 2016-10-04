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
package com.ysoft.jdo.book.statetracker;

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;


/**
 * These are transcendent worms, since they may exist in more than
 * one apple.
 */
public class Worm implements InstanceCallbacks
   {
   // The following is a transient field to hold the name built from
   // the datastore OID
   private String OIDString;

   // this is a persistent field
   private String name;

   private Worm()
      {
      // used only by JDO
      }

   public Worm(String name)
      {
      if (name != null)
         name = name.trim();

      if ((name == null) || (name.length() <= 0))
         throw new IllegalArgumentException("the worm must have a name");

      this.name = name;
      }

   public void setOIDString(String name)
      {
      OIDString = name;
      }

   public String getOIDString()
      {
      return OIDString;
      }

   public String toString()
      {
      String retv = getOIDString();
      String n = null;

      try
         {
         n = name;
         }
      catch (JDOUserException e)
         {
         // caught this exception because NTR is false and no active transaction
         }

      if ((retv == null) && (n == null))
         retv = "Unnamed worm";
      else if (retv == null)
         retv = "Worm " + n;
      else if (n != null)
         retv += (" " + n);

      return retv;
      }

   // the methods required by the InstanceCallbacks interface
   public void jdoPostLoad()
      {
      if (getOIDString() == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }

   public void jdoPreClear()
      {
      }

   public void jdoPreDelete()
      {
      }

   public void jdoPreStore()
      {
      if (getOIDString() == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }
   }

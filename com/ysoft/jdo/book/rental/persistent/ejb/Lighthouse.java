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
package com.ysoft.jdo.book.rental.persistent;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDOHelper;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.rental.service.SupportsIdentityString;


public class Lighthouse implements SupportsIdentityString, Serializable,
   InstanceCallbacks
   {
   private String     name;
   private String     description;
   private BigDecimal highSeasonRate;
   private BigDecimal offSeasonRate;
   private String     imageName;
   private String     OIDString; // unmanaged field

   private Lighthouse()
      {
      // used by JDO
      }

   public Lighthouse(String name, String description,
      BigDecimal highSeasonRate, BigDecimal offSeasonRate)
      {
      if ((name == null) || (description == null) || (highSeasonRate == null) ||
               (offSeasonRate == null))
         {
         throw new IllegalArgumentException();
         }

      this.name              = name.trim();
      this.description       = description.trim();
      this.highSeasonRate    = highSeasonRate;
      this.offSeasonRate     = offSeasonRate;
      }

   public String getName()
      {
      return name;
      }

   public String getDescription()
      {
      return description;
      }

   public BigDecimal getHighSeasonRate()
      {
      return highSeasonRate;
      }

   public BigDecimal getOffSeasonRate()
      {
      return offSeasonRate;
      }

   public void setImageName(String name)
      {
      imageName = name;
      }

   public String getImageName()
      {
      MsgCenter.putMsg("in Lighthouse.getImageName");
      return imageName;
      }

   public String toString()
      {
      return getName();
      }

   // required by the SupportsIdentityString interface
   public String getIdentityString()
      {
      if (OIDString == null)
         {
         /*
         // debugging
         if (!JDOHelper.isPersistent(this))
            MsgCenter.putMsg("Lighthouse.getIdentityString: asking for JDOHelper for OID for transient instance");
         */
         Object oid = JDOHelper.getObjectId(this);

         if (oid != null)
            OIDString = oid.toString();
         }

      return OIDString;
      }

   public final boolean equals(Object other)
      {
      if (this == other)
         return true;

      try
         {
         return equals((Lighthouse) other);
         }
      catch (ClassCastException e)
         {
         // return false when not class compatible
         return false;
         }
      }

   public final boolean equals(Lighthouse other)
      {
      if (this == other)
         return true;

      // return false when other is null or this has no identity string
      if ((other == null) || (getIdentityString() == null))
         return false;

      String other_id = other.getIdentityString();
      return OIDString.equals(other_id);
      }

   public final int hashCode()
      {
      String s = getIdentityString();
      return (s != null) ? s.hashCode() : super.hashCode();
      }

   // Using InstanceCallbacks to capture the identity string.
   public void jdoPreStore()
      {
      getIdentityString();

      //MsgCenter.putMsg("Lighthouse (" + OIDString + ") jdoPreStore");
      }

   public void jdoPreDelete()
      {
      //MsgCenter.putMsg("Lighthouse (" + OIDString + ") jdoPreDelete");
      OIDString = null;
      }

   public void jdoPostLoad()
      {
      getIdentityString();

      //MsgCenter.putMsg("Lighthouse (" + OIDString + ") jdoPostLoad");
      }

   public void jdoPreClear()
      {
      getIdentityString();

      //MsgCenter.putMsg("Lighthouse (" + OIDString + ") jdoPreClear");
      }
   }

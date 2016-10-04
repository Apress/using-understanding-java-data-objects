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
package com.ysoft.jdo.book.library;

import javax.jdo.*;


public class Volunteer implements InstanceCallbacks
   {
   private String oidString; // JDO transient field

   // persistent fields
   private Borrower borrower;
   private int      hours_per_week;

   private Volunteer()
      {
      // used only by JDO
      }

   public Volunteer(Borrower borrower)
      {
      if (borrower == null)
         throw new IllegalArgumentException("borrower cannot be null");

      if (borrower.getVolunteer() != null)
         throw new IllegalArgumentException("borrower is already a volunteer");

      this.borrower = borrower;
      borrower.setVolunteer(this);
      }

   public Borrower getBorrower()
      {
      return borrower;
      }

   public int getHoursPerWeek()
      {
      return hours_per_week;
      }

   public void setHoursPerWeek(int hours)
      {
      if (hours >= 0)
         hours_per_week = hours;
      else
         throw new IllegalArgumentException("hours must be >= 0");
      }

   public String toString()
      {
      return "volunteer [" + getOIDString() + "] \"" + borrower.getName() +
      "\"";
      }

   public int hashCode()
      {
      if (oidString != null)
         return oidString.hashCode();

      return super.hashCode();
      }

   public boolean equals(Object obj)
      {
      if (obj == this)
         return true;

      if (obj == null)
         return false;

      if (oidString == null)
         return false;

      if (!(getClass() != obj.getClass()))
         return false;

      Volunteer volunteer = (Volunteer) obj;

      return oidString.equals(volunteer.oidString);
      }

   public void setOIDString(String name)
      {
      oidString = name;
      }

   public String getOIDString()
      {
      if (oidString != null)
         return oidString;
      else
         return "OID unknown";
      }

   // the methods required by the InstanceCallbacks interface
   // our goal is to pull the oidString after load
   public void jdoPostLoad()
      {
      if (oidString == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }

   public void jdoPreClear()
      {
      if (oidString == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }

   public void jdoPreDelete()
      {
      }

   public void jdoPreStore()
      {
      }
   }

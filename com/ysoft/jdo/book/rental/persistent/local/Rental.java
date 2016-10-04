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
package com.ysoft.jdo.book.rental.persistent;

import java.math.BigDecimal;

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDOHelper;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.rental.service.ReservationException;


public class Rental
   {
   private Lighthouse lighthouse;
   private Week       week;
   private BigDecimal price;
   private Customer   customer;

   // the next field is not used in the gui version of the application
   // but is present here to preserve data schema compatibility with the
   // servlet version of the Rental class.
   private int userVersion;

   // the next field is not used in the gui version of the application
   // but is present here to preserve XML metadata compatibility with
   // the servlet version of the Rental class
   private String OIDString; // unmanaged field

   private Rental()
      {
      // used by JDO
      }

   public Rental(Lighthouse lighthouse, Week week)
      {
      if ((lighthouse == null) || (week == null))
         throw new IllegalArgumentException();

      price    = week.isHighSeason() ? lighthouse.getHighSeasonRate()
                                     : lighthouse.getOffSeasonRate();
      this.lighthouse    = lighthouse;
      this.week          = week;
      }

   public Lighthouse getLighthouse()
      {
      return lighthouse;
      }

   public Week getWeek()
      {
      return week;
      }

   public Customer getCustomer()
      {
      return customer;
      }

   public BigDecimal getPrice()
      {
      return price;
      }

   public void cancelReservation(Customer cust)
         throws ReservationException
      {
      if (isAvailable())
         throw new ReservationException("Rental is not booked");

      if (customer != cust)
         throw new ReservationException("Rental is booked by another customer");

      customer = null;
      }

   public void makeReservation(Customer customer)
         throws ReservationException
      {
      if (!isAvailable())
         throw new ReservationException("Rental is already taken");

      if (customer == null)
         throw new IllegalArgumentException();

      this.customer = customer;
      }

   public boolean isAvailable()
      {
      //MsgCenter.putMsg("Rental.isAvailable for " + this);
      return customer == null;
      }

   public boolean isDirty()
      {
      return JDOHelper.isDirty(this);
      }

   public String toString()
      {
      return "reservation, date: " + getWeek().getStartOfWeekString() +
      ", lighthouse: " + getLighthouse().getName() + ", customer: " +
      getCustomer();
      }
   }

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
package com.ysoft.jdo.book.rental.local.client.console;

import java.util.*;

import javax.jdo.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.common.console.*;
import com.ysoft.jdo.book.rental.local.service.*;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.service.*;


public class ReservationClient implements UIClient
   {
   static ReservationService persistenceHandler;
   static int                view;
   static LinkedList         list            = new LinkedList();
   static Collection         reservations;
   static Customer           currentCustomer;
   private static ArrayList  commands        = new ArrayList();
   private UserInterface     ui;

   public ReservationClient()
      {
      try
         {
         if (persistenceHandler == null)
            persistenceHandler = new ReservationService();
         }
      catch (Exception e)
         {
         MsgCenter.putException("could not initialize", e);
         System.exit(1);
         }
      }

   public static void main(String[] args)
      {
      // construct a StdoutMsgHandler which will register itself
      // with the message center
      new StdoutMsgHandler();

      getArgs(args);

      ReservationClient client = new ReservationClient();

      client.run();

      MsgCenter.putMsg("-- All done!");
      }

   public void run()
      {
      // add the commands that we will process
      commands.add(new StartTransaction(this));
      commands.add(new CommitTransaction(this));
      commands.add(new RollbackTransaction(this));
      commands.add(new IdentifyCustomer(this));
      commands.add(new View(this));

      //commands.add(new Review(this));
      commands.add(new MakeReservation(this));
      commands.add(new CancelReservation(this));
      commands.add(new TellConfiguration(this));
      commands.add(new PurgeDatabase(this));
      commands.add(new Populate(this));

      // get commands from UI
      ui = new UserInterface(commands);
      ui.pumpCommands();
      }

   public void handleException(String msg, Exception e)
      {
      if (ui != null)
         ui.handleException(msg, e);
      else
         MsgCenter.putException(msg, e);
      }

   private static void getArgs(String[] args)
      {
      if (args.length > 0)
         tellSyntax();
      }

   private static void tellSyntax()
      {
      System.out.println(
         "No arguments needed.  Uses the factory.properties in the rental.local package");
      System.exit(1);
      }

   public void refreshService()
      {
      // do nothing
      }

   public static void viewReservations()
      {
      if (reservations == null)
         return;

      list.clear();
      list.addAll(reservations);

      int len = list.size();

      if (len == 0)
         MsgCenter.putMsg("No reservations to view");

      for (int x = 0; x < len; x++)
         {
         Rental r = (Rental) list.get(x);
         Week   w = r.getWeek();

         System.out.println((x + 1) + " week of " + w.getStartOfWeekString() +
            " lighthouse: " + r.getLighthouse().getName() + " " +
            ((r.getCustomer() != null) ? "reserved " : "") +
            (r.isDirty() ? "modified" : ""));
         }
      }
   }


class StartTransaction extends Command
   {
   public StartTransaction(UIClient c)
      {
      super(c, new String[] { "begin transaction", "begin", });
      }

   public void execute()
      {
      ReservationClient.persistenceHandler.beginTransaction();
      MsgCenter.putMsg("Okay");
      }
   }


class CommitTransaction extends Command
   {
   public CommitTransaction(UIClient c)
      {
      super(c, new String[] { "commit transaction", "commit", });
      }

   public void execute()
      {
      try
         {
         ReservationClient.persistenceHandler.commitTransaction();
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("commit transaction failed", e);
         }
      }
   }


class RollbackTransaction extends Command
   {
   public RollbackTransaction(UIClient c)
      {
      super(c, new String[] { "rollback transaction", "rollback", });
      }

   public void execute()
      {
      ReservationClient.persistenceHandler.rollbackTransaction();
      MsgCenter.putMsg("Okay");
      }
   }


class View extends Command
   {
   private static final String[] views = 
   {
      "quit", "available", "my reservations", "both"
   };
   static final int              QUIT            = 0;
   static final int              AVAILABLE       = 1;
   static final int              MY_RESERVATIONS = 2;
   static final int              BOTH            = 3;
   private boolean               bad_input;

   public View(UIClient c)
      {
      super(c, new String[] { "view reservations", "view", });
      }

   public void getParameters()
      {
      clear();

      // determine the view
      ReservationClient.view = getInputSelection("Pick view", views);

      if (ReservationClient.view < 1)
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         {
         return;
         }

      switch (ReservationClient.view)
         {
         case AVAILABLE:
            ReservationClient.reservations = ReservationClient.persistenceHandler.getAvailableRentals();
            break;

         case MY_RESERVATIONS:

            if (ReservationClient.currentCustomer == null)
               {
               MsgCenter.putMsg("You need to identify the customer");
               }
            else
               {
               ReservationClient.reservations = ReservationClient.persistenceHandler.getCustomerRentals(ReservationClient.currentCustomer);
               }

            break;

         case BOTH:

            if (ReservationClient.currentCustomer == null)
               {
               MsgCenter.putMsg("You need to identify the customer");
               }
            else
               {
               ReservationClient.reservations = ReservationClient.persistenceHandler.getCustomerAndAvailableRentals(ReservationClient.currentCustomer);
               }

            break;

         default:
            MsgCenter.putMsg("Unrecognized selection");
            return;
         }

      ReservationClient.viewReservations();
      }

   private void clear()
      {
      bad_input                 = false;
      ReservationClient.view    = QUIT;
      }
   }


class Populate extends Command
   {
   public Populate(UIClient c)
      {
      super(c, new String[] { "populate database", });
      }

   public void execute()
      {
      try
         {
         ReservationClient.persistenceHandler.populateDatastore();
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("populate database", e);
         }
      }
   }


class PurgeDatabase extends Command
   {
   public PurgeDatabase(UIClient c)
      {
      super(c, new String[] { "purge database", });
      }

   public void execute()
      {
      try
         {
         ReservationClient.persistenceHandler.cleanDatastore();
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("clean database failed", e);
         }
      }
   }


class TellConfiguration extends Command
   {
   public TellConfiguration(UIClient c)
      {
      super(c, new String[] { "tell configuration", "config", "tell", });
      }

   public void execute()
      {
      try
         {
         ReservationClient.persistenceHandler.tellConfiguration();
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("tell configuration failed", e);
         }
      }
   }


/*
class Review extends Command
   {
   public Review(UIClient c)
      {
      super(c, new String[]
            {
            "review reservations",
            "review",
            });
      }

   public void execute()
      {
      switch (ReservationClient.view)
         {
         case View.AVAILABLE:
         case View.MY_RESERVATIONS:
         case View.BOTH:

            ReservationClient.viewReservations();
            break;

         default:

            MsgCenter.putMsg("Nothing to view");
            break;
         }
      }
   }
*/
class MakeReservation extends Command
   {
   public MakeReservation(UIClient c)
      {
      super(c, new String[] { "make reservation", "make", "reserve", });
      }

   public void getParameters()
      {
      if ((ReservationClient.list == null) ||
               (ReservationClient.list.size() < 1))
         {
         MsgCenter.putMsg("No reservations in view");
         return;
         }

      if (ReservationClient.currentCustomer == null)
         {
         MsgCenter.putMsg(
            "Cannot make reservation without entering customer name");
         return;
         }

      int index = getInputIntWithQuit("Enter the index of the reservation", 1,
            ReservationClient.list.size());

      if (index == 0)
         return;

      Rental r = (Rental) ReservationClient.list.get(index - 1);

      if (r.getCustomer() != null)
         {
         MsgCenter.putMsg("Reservation already made");
         return;
         }

      try
         {
         r.makeReservation(ReservationClient.currentCustomer);
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("make reservation failed", e);
         }
      }

   public void execute()
      {
      // do nothing
      }
   }


class CancelReservation extends Command
   {
   public CancelReservation(UIClient c)
      {
      super(c, new String[] { "cancel reservation", "cancel", });
      }

   public void getParameters()
      {
      if ((ReservationClient.list == null) ||
               (ReservationClient.list.size() < 1))
         {
         MsgCenter.putMsg("No reservations in view");
         return;
         }

      if (ReservationClient.currentCustomer == null)
         {
         MsgCenter.putMsg(
            "Cannot cancel reservation without entering customer name");
         return;
         }

      int index = getInputIntWithQuit("Enter the index of the reservation", 1,
            ReservationClient.list.size());

      if (index == 0)
         return;

      Rental r = (Rental) ReservationClient.list.get(index - 1);

      if (r.getCustomer() == null)
         {
         MsgCenter.putMsg("Reservation not taken");
         return;
         }
      else if (r.getCustomer() != ReservationClient.currentCustomer)
         {
         MsgCenter.putMsg("Reservation taken by a different customer");
         return;
         }

      try
         {
         r.cancelReservation(ReservationClient.currentCustomer);
         MsgCenter.putMsg("Okay");
         }
      catch (ReservationException e)
         {
         reportException("cancel reservation failed", e);
         }
      }

   public void execute()
      {
      // do nothing
      }
   }


class IdentifyCustomer extends Command
   {
   public IdentifyCustomer(UIClient c)
      {
      super(c, new String[] { "identify customer", "identify", });
      }

   public void getParameters()
      {
      ReservationClient.currentCustomer = null;

      String name = getInputString("Enter customer's name");

      List   list = ReservationClient.persistenceHandler.getCustomers(name);

      if (list.size() < 1)
         throw new IllegalStateException("Unexpected return value");
      else if (list.size() == 1)
         {
         ReservationClient.currentCustomer = (Customer) list.get(0);
         }
      else
         {
         ReservationClient.currentCustomer = (Customer) getInputSelection("Select one of matching customers",
               list);
         }

      MsgCenter.putMsg("Okay");
      }

   public void execute()
      {
      // do nothing
      }
   }

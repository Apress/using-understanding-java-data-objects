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
package com.ysoft.jdo.book.rental.local.client.gui;

import java.math.BigDecimal;
import java.util.*;

import javax.jdo.JDODataStoreException;
import javax.jdo.JDOFatalException;
import javax.swing.event.EventListenerList;

import com.ysoft.jdo.book.common.Copyright;
import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.common.swing.*;
import com.ysoft.jdo.book.rental.local.service.*;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.service.*;


/**
 * ReservationClientModel is the link between the GUI client and the application defined
 * ReservationService.
 */
public class ReservationClientModel
   {
   public static final String CONNECTED                            = "connected";
   public static final String CUSTOMER_NAME                        = "customer";
   public static final String CUSTOMER_LIST                        = "customer-list";
   public static final String VIEW_CHANGED                         = "view";
   public static final String DISCONNECTED                         = "disconnect";
   public static final String DATA_CHANGED                         = "data-changed";
   private static final int   VIEW_NOTHING                         = 0;
   private static final int   VIEW_AVAILABLE_RENTALS               = 1;
   private static final int   VIEW_CUSTOMERS_RENTALS               = 2;
   private static final int   VIEW_AVAILABLE_AND_CUSTOMERS_RENTALS = 3;

   static
      {
      Copyright.stdout();
      }

   private boolean            connected;
   private boolean            populated;
   private int                view;
   private EventListenerList  listenerList = new EventListenerList();
   private RentalMatrix       matrix;
   private Customer           curCustomer;
   private ReservationService service;

   public ReservationClientModel()
      {
      }

   public void connect()
      {
      if (connected = startReservationService())
         {
         setView();
         fireModelChangeNewData(CUSTOMER_LIST);
         }
      else
         fireModelChangeNewData(DISCONNECTED);
      }

   public void disconnect()
      {
      setCustomerName(null);
      connected    = false;
      populated    = false;
      stopReservationService();
      fireModelChangeNewData(DISCONNECTED);
      init();
      }

   public boolean isConnected()
      {
      return connected;
      }

   public boolean isPopulatedDatastore()
      {
      return populated;
      }

   public void confirm()
      {
      if (isViewReady())
         {
         try
            {
            service.commitTransaction();
            service.beginTransaction();
            setView(VIEW_CUSTOMERS_RENTALS);
            fireModelChangeNewData(VIEW_CHANGED);
            }
         catch (OptimisticReservationException e)
            {
            MsgCenter.putMsg("Caught OptimisticReservationException in confirm");

            // start the tx before handing exception off to GUI
            service.beginTransaction();
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);

            // the rollback evicted old data
            fireModelChangeNewData(DATA_CHANGED);
            }
         catch (JDOFatalException e)
            {
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);
            MessageHandler.reportWarning(ReservationClient.rcReservationClient,
               "Disconnecting");
            disconnect();
            }
         catch (RuntimeException e)
            {
            // some exceptions may terminate the transaction
            // the JDORI has a bug that does this.
            service.beginTransaction();
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);
            }
         }
      }

   public void refresh()
      {
      if (isViewReady())
         {
         try
            {
            service.rollbackTransaction();
            service.evictAll();
            service.beginTransaction();
            setView();
            }
         catch (ReservationException e)
            {
            // if we can't rollback, evict, and start transaction,
            // then the situation is dire
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);
            MessageHandler.reportWarning(ReservationClient.rcReservationClient,
               "Disconnecting");
            disconnect();
            }
         catch (JDOFatalException e)
            {
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);
            MessageHandler.reportWarning(ReservationClient.rcReservationClient,
               "Disconnecting");
            disconnect();
            }
         catch (RuntimeException e)
            {
            MessageHandler.reportException(ReservationClient.rcReservationClient,
               e);
            }
         }
      }

   public void viewAvailableRentals()
      {
      view = VIEW_AVAILABLE_RENTALS;
      init();
      }

   public boolean isViewAvailableRentals()
      {
      return view == VIEW_AVAILABLE_RENTALS;
      }

   public void viewCustomerRentals()
      {
      view = VIEW_CUSTOMERS_RENTALS;
      init();
      }

   public boolean isViewCustomerRentals()
      {
      return view == VIEW_CUSTOMERS_RENTALS;
      }

   public void viewCustomerAndAvailableRentals()
      {
      view = VIEW_AVAILABLE_AND_CUSTOMERS_RENTALS;
      init();
      }

   public boolean isViewCustomerAndAvailableRentals()
      {
      return view == VIEW_AVAILABLE_AND_CUSTOMERS_RENTALS;
      }

   public void setCustomerName(String name)
      {
      boolean fireChange = false;

      //System.out.println("Setting customer name, curCustomer: " + curCustomer + ", new name: " + name);
      // clean up user's input
      if (name != null)
         {
         name = name.trim();

         if (name.length() <= 0)
            name = null;
         else if (name.equalsIgnoreCase("none"))
            name = null;
         }

      if (name == null)
         {
         if (curCustomer != null)
            {
            curCustomer    = null;
            fireChange     = true;
            }
         }
      else if ((curCustomer == null) ||
               !name.equalsIgnoreCase(curCustomer.getName()))
         {
         fireChange = setCustomer(name);
         }

      if (fireChange)
         {
         if (view != VIEW_NOTHING)
            {
            if (curCustomer == null)
               view = VIEW_AVAILABLE_RENTALS;

            init();
            }

         fireModelChangeNewData(CUSTOMER_NAME);
         fireModelChangeNewData(VIEW_CHANGED);
         }
      }

   public String getCustomerName()
      {
      String retv = "none";

      try
         {
         if (curCustomer != null)
            retv = curCustomer.getName();
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      //System.out.println("getCustomerName: " + retv);
      return retv;
      }

   public String[] getCustomerNames()
      {
      String[] retv = { "none" };

      try
         {
         if (service != null)
            {
            List listOfCustomers = service.getCustomers();
            List listOfNames = new LinkedList();
            listOfNames.add("none");

            for (int x = 0; x < listOfCustomers.size(); x++)
               {
               listOfNames.add(((Customer) listOfCustomers.get(x)).getName());
               }

            retv = (String[]) listOfNames.toArray(retv);
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      /*
      System.out.println("getCustomerNames returns:");
      for (int x = 0; x < retv.length; x++)
         System.out.println("   " + retv[x]);
      */
      return retv;
      }

   public boolean isCustomerDefined()
      {
      return curCustomer != null;
      }

   public Date getRentalDate(int index)
      {
      Date retv = null;

      if (isViewReady())
         retv = matrix.getRentalDate(index);

      return retv;
      }

   public boolean isAvailable(int dateIndex, int lighthouseIndex)
      {
      boolean retv = false;

      if (isViewReady())
         retv = matrix.isAvailable(view == VIEW_CUSTOMERS_RENTALS, dateIndex,
               lighthouseIndex);

      return retv;
      }

   public boolean isModifiable(int dateIndex, int lighthouseIndex)
      {
      boolean retv = false;

      if (isViewReady())
         retv = matrix.isModifiable(curCustomer, dateIndex, lighthouseIndex);

      return retv;
      }

   public void setAvailable(int dateIndex, int lighthouseIndex, boolean flag)
      {
      if (isViewReady())
         {
         if (curCustomer != null)
            {
            if (!flag)
               matrix.makeReservation(curCustomer, dateIndex, lighthouseIndex);
            else
               matrix.cancelReservation(curCustomer, dateIndex, lighthouseIndex);
            }
         else
            System.out.println("No customer");
         }
      }

   public BigDecimal getPrice(int dateIndex, int lighthouseIndex)
      {
      BigDecimal retv = null;

      if (isViewReady() && isModifiable(dateIndex, lighthouseIndex))
         retv = matrix.getPrice(dateIndex, lighthouseIndex);

      return retv;
      }

   public String getLighthouseDescription(int index)
      {
      String retv = null;

      if (isViewReady())
         retv = matrix.getLighthouseDescription(index);

      return retv;
      }

   public String getLighthouseImageName(int index)
      {
      String retv = null;

      if (isViewReady())
         retv = matrix.getLighthouseImageName(index);

      return retv;
      }

   public void cleanDatastore()
      {
      try
         {
         service.rollbackTransaction();
         service.cleanDatastore();
         populated = !service.isCleanDatastore();
         service.beginTransaction();
         init();
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      }

   public void populateDatastore()
      {
      try
         {
         service.rollbackTransaction();
         service.populateDatastore();
         populated = !service.isCleanDatastore();
         service.beginTransaction();
         init();
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      }

   public int getNumLighthouses()
      {
      int retv = 0;

      if (isViewReady())
         retv = matrix.getNumLighthouses();

      return retv;
      }

   public int getNumRentalDates()
      {
      int retv = 0;

      if (isViewReady())
         retv = matrix.getNumDates();

      return retv;
      }

   public String getLighthouseName(int index)
      {
      String retv = null;

      if (isViewReady())
         retv = matrix.getLighthouseName(index);

      return retv;
      }

   private boolean setView()
      {
      return setView(view);
      }

   /**
    * returns true if ModelChangeEvent was fired
    */
   private boolean setView(int view)
      {
      boolean retv = true;

      switch (view)
         {
         case VIEW_AVAILABLE_RENTALS:
            viewAvailableRentals();
            break;

         case VIEW_CUSTOMERS_RENTALS:
            viewCustomerRentals();
            break;

         case VIEW_AVAILABLE_AND_CUSTOMERS_RENTALS:
            viewCustomerAndAvailableRentals();
            break;

         default:

            if (view == VIEW_NOTHING)
               this.view = VIEW_NOTHING;

            retv = false;
            break;
         }

      return retv;
      }

   public void addModelChangeListener(ModelChangeListener l)
      {
      listenerList.add(ModelChangeListener.class, l);
      }

   public void removeModelChangeListener(ModelChangeListener l)
      {
      listenerList.remove(ModelChangeListener.class, l);
      }

   protected void fireModelChangeNewData(String componentName)
      {
      //System.out.println("firing Model Change event: " + componentName);
      ModelChangeEvent event = null;

      // Guaranteed to return a non-null array
      Object[] listeners = listenerList.getListenerList();

      // Process the listeners last to first, notifying
      // those that are interested in this event
      for (int i = listeners.length - 2; i >= 0; i -= 2)
         {
         if (listeners[i] == ModelChangeListener.class)
            {
            // Lazily create the event:
            if (event == null)
               event = new ModelChangeEvent(this, componentName);

            ((ModelChangeListener) listeners[i + 1]).newData(event);
            }
         }
      }

   private void init()
      {
      boolean fireChange = false;

      if (isConnected())
         {
         fireChange = initMatrix();

         //phoneyMatrixInitialization();
         //fireChange = true;
         }
      else if (matrix != null)
         {
         matrix        = null;
         fireChange    = true;
         }

      if (fireChange)
         {
         fireModelChangeNewData(CONNECTED);
         }
      }

   /*
   private void phoneyMatrixInitialization()
      {
      List listLighthouses = new LinkedList();
      listLighthouses.add(new Lighthouse("Glorious", "A real nice place",
            new BigDecimal("189.00"), new BigDecimal("109.00")));
      listLighthouses.add(new Lighthouse("Respite", "A real nice place to stay",
            new BigDecimal("169.00"), new BigDecimal("89.00")));

      List listWeeks = new LinkedList();
      Week w = Week.getFirstWeek();
      listWeeks.add(w);
      for (int x = 1; x < 16; x++)
         {
         boolean hs = x >= 4 && x < 12;
         listWeeks.add(w = Week.getNextWeek(w.getStartOfWeek(), hs));
         }

      List listRentals = new LinkedList();
      for (int x = 0; x < listWeeks.size(); x++)
         {
         for (int y = 0; y < listLighthouses.size(); y++)
            {
            listRentals.add(new Rental((Lighthouse) listLighthouses.get(y),
                  (Week) listWeeks.get(x)));
            }
         }

      matrix = new RentalMatrix(this, listLighthouses, listRentals);
      }
   */
   private boolean initMatrix()
      {
      boolean retv = false;

      try
         {
         Collection c = null;

         switch (view)
            {
            case VIEW_AVAILABLE_RENTALS:
               c = service.getAvailableRentals();
               break;

            case VIEW_CUSTOMERS_RENTALS:
               c = service.getCustomerRentals(curCustomer);
               break;

            case VIEW_AVAILABLE_AND_CUSTOMERS_RENTALS:
               c = service.getCustomerAndAvailableRentals(curCustomer);
               break;

            default:

               //throw new ReservationException("asked for init when viewing nothing");
               return false;
            }

         List listOfRentals     = new LinkedList(c);
         List listOfLighthouses = service.getLighthouses();

         /*
         // debugging
         Iterator iter = listOfRentals.iterator();
         System.out.println("Rental list has " + listOfRentals.size() + " members");
         List taken = new LinkedList();
         while (iter.hasNext())
            {
            Rental r = (Rental) iter.next();
            System.out.println("   " + r);
            if (r.getCustomer() != null)
               taken.add(r);
            }
         service.setSpecials(taken);
         */
         matrix    = new RentalMatrix(this, listOfLighthouses, listOfRentals);

         retv = matrix != null;
         }

      /*
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      */
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   private boolean isViewReady()
      {
      return (matrix != null) && (view != VIEW_NOTHING);
      }

   private boolean startReservationService()
      {
      boolean retv = false;
      populated = false;

      try
         {
         service = new ReservationService();
         service.beginTransaction();
         populated    = !service.isCleanDatastore();
         retv         = true;
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   private void stopReservationService()
      {
      try
         {
         if (service != null)
            {
            service.rollbackTransaction();
            service = null;
            }
         }

      /*
            catch (ReservationException e)
               {
               MessageHandler.reportException(ReservationClient.rcReservationClient, e);
               }
      */

      // we don't handle JDOFatalException, because the exception handling
      // for that exception calls this method
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      }

   private boolean setCustomer(String name)
      {
      Customer selectedCustomer = null;

      //System.out.println("setCustomer: " + name);
      try
         {
         List list = service.getCustomers(name);

         if ((list == null) || (list.size() < 0))
            throw new ReservationException(
               "null or zero length customer list from service");

         if (list.size() > 1)
            {
            StringBuffer buf = new StringBuffer();
            buf.append(
               "More than one customer found for this name, please be more specific\n");

            for (int x = 0; x < list.size(); x++)
               {
               buf.append("   ");
               buf.append(list.get(x).toString());
               buf.append('\n');
               }

            boolean response = MessageHandler.reportErrorWithQuestion(ReservationClient.rcReservationClient,
                  buf.toString(), "Pick the first customer?");

            if (response)
               selectedCustomer = (Customer) list.get(0);
            }
         else
            selectedCustomer = (Customer) list.get(0);
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      if (selectedCustomer != null)
         curCustomer = selectedCustomer;

      return selectedCustomer != null;
      }
   }


/**
 * The RentalMatrix converts between and row and column model of the
 * table and the list of rentals available from ReservationService.
 */
class RentalMatrix
   {
   private int                    numLighthouses;
   private int                    numWeeks;
   private List                   listOfLighthouses;
   private List                   listOfWeeks;
   private ReservationClientModel rcm;
   Rental[][]                     rentals;

   RentalMatrix(ReservationClientModel rcm, List listOfLighthouses,
      List listOfRentals)
      {
      this.rcm = rcm;

      if ((listOfLighthouses != null) && (listOfRentals != null))
         {
         numLighthouses            = listOfLighthouses.size();
         this.listOfLighthouses    = listOfLighthouses;

         listOfWeeks = new LinkedList();

         Week lastWeek = null;

         for (int x = 0; x < listOfRentals.size(); x++)
            {
            Rental r = (Rental) listOfRentals.get(x);
            Week   w = r.getWeek();

            if (lastWeek != w)
               {
               lastWeek = w;
               listOfWeeks.add(w);
               }
            }

         numWeeks    = listOfWeeks.size();
         rentals     = new Rental[numWeeks][numLighthouses];

         for (int x = 0; x < listOfRentals.size(); x++)
            {
            Rental r                = (Rental) listOfRentals.get(x);
            int    week_index       = listOfWeeks.indexOf(r.getWeek());
            int    lighthouse_index = listOfLighthouses.indexOf(r.getLighthouse());

            if ((week_index < 0) || (week_index > numWeeks) ||
                     (lighthouse_index < 0) ||
                     (lighthouse_index > numLighthouses))
               {
               MessageHandler.reportError(ReservationClient.rcReservationClient,
                  "Unexpected index in constructing rental matrix: Lighthouse " +
                  lighthouse_index + ", week " + week_index);
               }
            else
               rentals[week_index][lighthouse_index] = r;
            }
         }
      }

   int getNumLighthouses()
      {
      return numLighthouses;
      }

   String getLighthouseName(int index)
      {
      //System.out.println("matrix model: lighthouse name for lighthouse: " + index);
      String retv = null;

      try
         {
         if ((index >= 0) && (index < numLighthouses))
            {
            Lighthouse s = (Lighthouse) listOfLighthouses.get(index);
            retv = s.getName();
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   String getLighthouseDescription(int index)
      {
      String retv = null;

      try
         {
         if ((index >= 0) && (index < numLighthouses))
            {
            Lighthouse s = (Lighthouse) listOfLighthouses.get(index);
            retv = s.getDescription();
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   int getNumDates()
      {
      return numWeeks;
      }

   void makeReservation(Customer customer, int dateIndex, int lighthouseIndex)
      {
      try
         {
         if ((dateIndex >= 0) && (dateIndex < numWeeks) &&
                  (lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            Rental r = rentals[dateIndex][lighthouseIndex];

            if ((r != null) && r.isAvailable())
               r.makeReservation(customer);
            }
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      }

   void cancelReservation(Customer customer, int dateIndex, int lighthouseIndex)
      {
      try
         {
         if ((dateIndex >= 0) && (dateIndex < numWeeks) &&
                  (lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            Rental r = rentals[dateIndex][lighthouseIndex];

            if ((r != null) && !r.isAvailable() &&
                     (r.getCustomer() == customer))
               {
               //System.out.println("cancelling rental at " + r.getLighthouse() + " for " + r.getCustomer());
               r.cancelReservation(customer);
               }
            }
         }
      catch (ReservationException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }
      }

   Date getRentalDate(int index)
      {
      Date retv = null;

      try
         {
         if ((index >= 0) && (index < numWeeks))
            {
            Week w = (Week) listOfWeeks.get(index);
            retv = w.getStartOfWeek();
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   boolean isAvailable(boolean defaultValue, int dateIndex, int lighthouseIndex)
      {
      boolean retv = defaultValue;

      try
         {
         if ((dateIndex >= 0) && (dateIndex < numWeeks) &&
                  (lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            Rental r = rentals[dateIndex][lighthouseIndex];

            if (r != null)
               retv = r.isAvailable();
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      //System.out.println("Row: " + dateIndex + ", col: " + lighthouseIndex + ", available: " + retv);
      return retv;
      }

   boolean isModifiable(Customer cCustomer, int dateIndex, int lighthouseIndex)
      {
      boolean retv = false;

      //boolean  verbose = false;
      //String   reason = null;
      //System.out.print("Is lighthouse: " + lighthouseIndex + " for week: " + dateIndex + " for customer: "
      //   + cCustomer + " modifiable? ");
      try
         {
         if ((dateIndex >= 0) && (dateIndex < numWeeks) &&
                  (lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            /*
            // debugging
            if (dateIndex < 2 && lighthouseIndex == 0)
               verbose = true;
            */
            Rental r = rentals[dateIndex][lighthouseIndex];

            if (r != null)
               {
               Customer c = r.getCustomer();

               if ((c == null) || (c == cCustomer))
                  retv = true;

               /*
               if (verbose)
                  reason = "curCustomer is " + (cCustomer == null ? "null" : "not null") +
                        ", rental is " + (c == null ? "not taken" : "taken") +
                        ", curCustomer is " + (cCustomer == c ? "" : "not ") +
                        "the rental customer";
               */
               }

            /*
            else if (verbose)
               reason = "rental is null";

            // debugging
            if (verbose)
               System.out.println("isModifiable(" + dateIndex + "," + lighthouseIndex + ") " + retv +
                     ", " + reason);
            */
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      //System.out.println(retv);
      return retv;
      }

   BigDecimal getPrice(int dateIndex, int lighthouseIndex)
      {
      BigDecimal retv = null;

      try
         {
         if ((dateIndex >= 0) && (dateIndex < numWeeks) &&
                  (lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            Rental r = rentals[dateIndex][lighthouseIndex];

            if (r != null)
               {
               retv = r.getPrice();
               }
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }

   String getLighthouseImageName(int lighthouseIndex)
      {
      String retv = null;

      try
         {
         if ((lighthouseIndex >= 0) && (lighthouseIndex < numLighthouses))
            {
            Lighthouse s = (Lighthouse) listOfLighthouses.get(lighthouseIndex);
            retv = s.getImageName();
            }
         }
      catch (JDOFatalException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         MessageHandler.reportWarning(ReservationClient.rcReservationClient,
            "Disconnecting");
         rcm.disconnect();
         }
      catch (RuntimeException e)
         {
         MessageHandler.reportException(ReservationClient.rcReservationClient, e);
         }

      return retv;
      }
   }

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
David Ezzio       10/01/02   Created
*/
package com.ysoft.jdo.book.rental.servlet.service;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.service.*;


public class ReservationService
   {
   private PersistenceManagerFactory pmf;
   private PersistenceManager        pm;
   private Transaction               tx;
   private Query                     queryCustomerRentals;
   private Query                     queryAvailableRentals;
   private Query                     queryCustomerAndAvailableRentals;
   private Query                     queryForCustomer;
   private Query                     queryForLighthouses;

   public ReservationService()
         throws ReservationException
      {
      MsgCenter.putMsg("Constructing reservation service");

      try
         {
         pmf = PMFLocator.getPMF(
               "com/ysoft/jdo/book/rental/servlet/service/factory.properties");
         open();
         }
      catch (IOException e)
         {
         throw new ReservationException("unable to load properties file", e);
         }
      }

   public Collection getAvailableRentals()
      {
      Collection c = (Collection) queryAvailableRentals.execute();

      // working around Intellibo 3.1.0 query bug
      //return sortRentalCollection(c);
      //MsgCenter.putMsg("ReservationService.getAvailableRentals: returning " + c.size() + " objects");
      return c;
      }

   public Collection getCustomerRentals(Customer customer)
      {
      if (customer == null)
         throw new IllegalArgumentException();

      /* --- change for servlet version: must handle unmanaged customer objects -- */
      if (!JDOHelper.isPersistent(customer))
         {
         Customer temp = customer;
         customer = null;

         String oidString = temp.getIdentityString();

         if (oidString != null)
            {
            Object oid = pm.newObjectIdInstance(Customer.class, oidString);

            if (oid != null)
               customer = (Customer) pm.getObjectById(oid, false);
            }
         }

      Collection c = null;

      if (customer != null)
         c = (Collection) queryCustomerRentals.execute(customer);
      else
         c = new HashSet();

      //MsgCenter.putMsg("ReservationService.getCustomerRentals: returning " + c.size() + " objects");
      return c;
      }

   public Collection getCustomerAndAvailableRentals(Customer customer)
      {
      if (customer == null)
         throw new IllegalArgumentException();

      /* --- change for servlet version: must handle unmanaged customer objects -- */
      if (!JDOHelper.isPersistent(customer))
         {
         Customer temp = customer;
         customer = null;

         String oidString = temp.getIdentityString();

         if (oidString != null)
            {
            Object oid = pm.newObjectIdInstance(Customer.class, oidString);

            if (oid != null)
               customer = (Customer) pm.getObjectById(oid, false);
            }
         }

      Collection c = null;

      if (customer != null)
         {
         c = (Collection) queryCustomerAndAvailableRentals.execute(customer);

         // work around SQL generation bug in Kodo 2.3.4
         //MsgCenter.putMsg("ReservationService.getCustomerAndAvailableRentals: from pm query " + c.size() + " objects");
         // we need to maintain the results order and drop duplicates
         ArrayList list = new ArrayList(c.size());
         Iterator  iter = c.iterator();

         while (iter.hasNext())
            {
            Object o = iter.next();

            if (!list.contains(o))
               list.add(o);
            }

         c = list;
         }
      else
         c = getAvailableRentals();

      //MsgCenter.putMsg("ReservationService.getCustomerAndAvailableRentals: returning " + c.size() + " objects");
      return c;
      }

   public List getCustomers(String name)
      {
      LinkedList retv = new LinkedList();

      Customer   tempCustomer = new Customer(name);
      Collection c            = (Collection) queryForCustomer.execute(tempCustomer.getName());
      retv.addAll(c);
      queryForCustomer.closeAll();

      if (retv.size() < 1)
         {
         retv.add(tempCustomer);

         /* --- change for servlet version as transaction may not be active (NTR)
         // make the customer persistent so we can use it
         // in queries
         pm.makePersistent(tempCustomer);
         */
         }

      return retv;
      }

   public List getLighthouses()
      {
      LinkedList retv = new LinkedList();
      Collection c = (Collection) queryForLighthouses.execute();
      retv.addAll(c);
      return retv;
      }

   /* ---- new for servlet version ----- */
   public void close()
      {
      if (pmf == null)
         throw new IllegalStateException(
            "PersistenceManagerFactory is not initialized");

      try
         {
         cleanupQueries(true);

         // close pm if open
         if ((pm != null) && !pm.isClosed())
            {
            MsgCenter.putMsg("Closing PM: " + System.identityHashCode(pm));

            if ((tx != null) && tx.isActive())
               tx.rollback();

            pm.close();
            }
         }
      finally
         {
         pm    = null;
         tx    = null;
         }
      }

   /**
    * Makes all of the data objects in the passed collection transient
    * if they are not so already.  Also does a retrieve on each persistent object.
    */
   public void makeTransientAll(Collection c)
      {
      if (c == null)
         return;

      MsgCenter.putMsg("ReservationService.makeTransientAll: " + c.size() +
         " data objects");

      /*
      // debugging
      Iterator iter = c.iterator();
      while (iter.hasNext())
         {
         Object pc = iter.next();

         if (JDOHelper.isPersistent(pc))
            {
            MsgCenter.putMsg("ReservationService.makeTransientAll: Found persistent object" + pc);
            pm.retrieve(pc);
            pm.makeTransient(pc);
            MsgCenter.putMsg("ReservationService.makeTransientAll: retrieve and made transient the object");
            pm.retrieve(pc);
            pm.makeTransient(pc);
            MsgCenter.putMsg("ReservationService.makeTransientAll: did it again!");
            break;
            }
         }
      */

      // workaround for kodo bug (version 2.3.2)
      // take out of the collection any objects that are not persistent
      Iterator iter = c.iterator();

      while (iter.hasNext())
         {
         Object pc = iter.next();

         if (!JDOHelper.isPersistent(pc))
            {
            iter.remove();
            }
         }

      MsgCenter.putMsg("ReservationService.makeTransientAll: " + c.size() +
         " persistent objects");

      // retrieve the OIDString for the persistent objects that will be made transient
      // this avoids the need to implement InstanceCallbacks in the objects to capture the
      // the identity string.
      int x = 0;
      iter = c.iterator();

      while (iter.hasNext())
         {
         Object obj = iter.next();

         if (obj instanceof SupportsIdentityString)
            {
            ((SupportsIdentityString) obj).getIdentityString();
            x++;
            }
         }

      MsgCenter.putMsg(
         "ReservationService.makeTransientAll: got identity string for " + x +
         " supporting objects");

      pm.retrieveAll(c);
      pm.makeTransientAll(c);
      }

   /*
   // debugging call
   public static void tattleOnPMs(Object pc, ReservationService s)
      {
      if (s == null && pc == null)
         ; // do nothing
      else if (s == null)
         {
         MsgCenter.putMsg("ReservationService.tattleOnPMs: object's pm hashcode = " +
               System.identityHashCode(JDOHelper.getPersistenceManager(pc)));
         }
      else if (pc == null)
         {
         MsgCenter.putMsg("ReservationService.tattleOnPMs: service pm hashcode = " +
               System.identityHashCode(s.pm));
         }
      else
         {
         MsgCenter.putMsg("ReservationService.tattleOnPMs: service pm hashcode = " +
               System.identityHashCode(s.pm) + "; object's pm hashcode = " +
               System.identityHashCode(JDOHelper.getPersistenceManager(pc)));
         }
      }
   */

   /**
    * Finds the corresponding persistent Rental objects, and if reserved,
    * it cancels the reservation, and if available, it reserves the rental.
    *
    * @param unmanagedRentals the collection of rental objects that need to be changed.
    *                            None of the rental objects are managed by JDO.
    * @param unmanagedCustomer the customer who either has reservation or will reserve
    *                            the rental.
    * @throws ExtendedOptimisticException when the unmanaged rental's state does not correspond
    *                            to the persistent rental's state.
    * @throws OptimisticReservationException when the persistent rental's state does not correspond
    *                            to the state in the datastore.
    * @throws ReservationException when the customer that holds the reservation is different from
    *                            the customer that is attempting to cancel or make the reservation.
    */
   public void flipReservations(Collection unmanagedRentals,
      Customer unmanagedCustomer)
         throws ExtendedOptimisticException, OptimisticReservationException, 
            ReservationException
      {
      try
         {
         // start the transaction
         tx.begin();

         // get the persistent Customer
         String oidString = unmanagedCustomer.getIdentityString();

         // find the persistent customer record
         Customer pCustomer = null;

         // if the unmanaged Customer object doesn't have an identity string,
         // then we have a new customer
         if (oidString == null)
            pCustomer = unmanagedCustomer;

         // otherwise, we find the corresponding persistent Customer object
         else
            {
            try
               {
               pCustomer = (Customer) pm.getObjectById(pm.newObjectIdInstance(
                        Customer.class, oidString), true);
               }
            catch (JDODataStoreException e)
               {
               throw new ExtendedOptimisticException("The system has deleted this customer's record",
                  e);
               }
            }

         // get the persistent Rental objects and do the flip
         Iterator iter = unmanagedRentals.iterator();

         while (iter.hasNext())
            {
            Rental uRental = (Rental) iter.next();

            oidString = uRental.getIdentityString();

            if (oidString == null)
               throw new IllegalStateException(
                  "unmanaged Rental object without identity string: " +
                  uRental);

            Rental pRental = null;

            try
               {
               pRental = (Rental) pm.getObjectById(pm.newObjectIdInstance(
                        Rental.class, oidString), true);
               }
            catch (JDODataStoreException e)
               {
               throw new ExtendedOptimisticException(
                  "The system has deleted the rental record: " + uRental, e);
               }

            // if the persistent versions are not compatible, then an error
            if (!pRental.isSameVersion(uRental))
               throw new ExtendedOptimisticException(
                  "The rental record has been altered by another user, " +
                  "please review your change and submit it again");

            if (pRental.getCustomer() == null)
               {
               // make reservation
               pRental.makeReservation(pCustomer);
               }
            else
               {
               // cancel reservation
               pRental.cancelReservation(pCustomer);
               }
            }

         commitTransaction();
         }
      finally
         {
         if (tx.isActive())
            tx.rollback();
         }
      }

   private void commitTransaction()
         throws OptimisticReservationException
      {
      try
         {
         cleanupQueries();
         tx.commit();
         MsgCenter.putMsg("transaction committed in PM: " +
            System.identityHashCode(pm));
         }
      catch (JDOUserException e)
         {
         /*
         // debugging
         Object obj1 = e.getFailedObject();
         MsgCenter.putMsg("Does exception contain failed object? " + (obj1 != null ? "true" : "false"));
         if (obj1 != null)
            MsgCenter.putMsg("Failed object: " + obj1);
         */

         // we know how to recover if it is an optimistic lock exception
         boolean     recoverable = false;
         Throwable[] ne = e.getNestedExceptions();

         if (ne != null)
            {
            MsgCenter.putMsg(
               "ReservationService.commitTransaction: Looking for optimistic errors in " +
               ne.length + " nested exceptions");

            for (int x = 0; x < ne.length; x++)
               {
               if (!(ne[x] instanceof JDOUserException))
                  {
                  recoverable = false;
                  break;
                  }

               JDOUserException ue = (JDOUserException) ne[x];
               Object           fo = ue.getFailedObject();

               if (fo == null)
                  {
                  recoverable = false;
                  break;
                  }

               if (fo instanceof PersistenceCapable)
                  {
                  recoverable = true;

                  //pm.refresh(fo);
                  }

               // this required due to bug in Kodo 2.3.0 RC1
               else if ((fo = pm.getObjectById(fo, false)) != null)
                  {
                  recoverable = true;

                  //pm.refresh(fo);
                  }
               }
            }

         if (recoverable)
            {
            // refresh all transactional instances
            // we lose the changes that the user made.
            pm.refreshAll();
            throw new OptimisticReservationException(
               "Concurrent changes by other users prevented your changes, try again.");
            }
         else
            throw e;
         }
      }

   private void cleanupQueries()
      {
      cleanupQueries(false);
      }

   private void cleanupQueries(boolean kill)
      {
      if (queryCustomerAndAvailableRentals != null)
         queryCustomerAndAvailableRentals.closeAll();

      if (queryCustomerRentals != null)
         queryCustomerRentals.closeAll();

      if (queryAvailableRentals != null)
         queryAvailableRentals.closeAll();

      if (queryForCustomer != null)
         queryForCustomer.closeAll();

      if (queryForLighthouses != null)
         queryForLighthouses.closeAll();

      if (kill)
         {
         //MsgCenter.putMsg("nulling out queries");
         queryCustomerAndAvailableRentals    = null;
         queryCustomerRentals                = null;
         queryAvailableRentals               = null;
         queryForCustomer                    = null;
         queryForLighthouses                 = null;
         }
      }

   private void setupQueries()
      {
      // start transactions
      tx.begin();

      //MsgCenter.putMsg("creating queries");
      // extent used for all queries
      Extent extent = pm.getExtent(Rental.class, false);

      // set up query for availability
      queryAvailableRentals = pm.newQuery(extent, "customer == null");

      // working around Intellibo 3.1.0 query bug in handling ordering clause
      queryAvailableRentals.setOrdering(
         "week.startDate ascending, lighthouse.name ascending");

      // set up query for customer's rentals
      queryCustomerRentals = pm.newQuery(extent, "customer == c");
      queryCustomerRentals.declareParameters("Customer c");
      queryCustomerRentals.setOrdering(
         "week.startDate ascending, lighthouse.name ascending");

      // set up query for customer's rentals and available rentals
      queryCustomerAndAvailableRentals = pm.newQuery(extent,
            "customer == c || customer == null");
      queryCustomerAndAvailableRentals.declareParameters("Customer c");
      queryCustomerAndAvailableRentals.setOrdering(
         "week.startDate ascending, lighthouse.name ascending");

      extent              = pm.getExtent(Customer.class, false);
      queryForCustomer    = pm.newQuery(extent, "name == n");
      queryForCustomer.declareParameters("String n");

      extent                 = pm.getExtent(Lighthouse.class, false);
      queryForLighthouses    = pm.newQuery(extent, "");

      tx.commit();
      }

   private void open()
      {
      close();
      pm = pmf.getPersistenceManager();
      MsgCenter.putMsg("Opening PM: " + System.identityHashCode(pm));
      tx = pm.currentTransaction();
      setupQueries();
      }

   /*
   // this method is used only so long as we are working around Intellibo 3.1.0 query bug
   // Ordinarily, the query would sort for us, but ...
   // Using this with Intellibo 3.1.0 just runs into another bug
   private Collection sortRentalCollection(Collection c)
      {
      Rental nextRental = null;
      Iterator iter = c.iterator();
      ArrayList list = new ArrayList();

      while (iter.hasNext())
         {
         nextRental = (Rental) iter.next();
         insert(list, nextRental);
         }

      return list;
      }

   // simple insertion sort
   private void insert(ArrayList list, Rental r)
      {
      // sort as done in the following unusable ordering clause
      //queryAvailableRentals.setOrdering("week.startDate ascending, lighthouse.name ascending");

      int insert_point = 0;
      Date startDate = r.getWeek().getStartOfWeek();

      for (int x = 0; x < list.size(); x++)
         {
         Rental curRental = (Rental) list.get(x);
         Date curDate = curRental.getWeek().getStartOfWeek();

         if (curDate.after(startDate))
            {
            insert_point = x;
            break;
            }
         else if (curDate.equals(startDate) &&
               curRental.getLighthouse().getName().compareTo(r.getLighthouse().getName()) > 0)
            {
            insert_point = x;
            break;
            }
         }

      if (insert_point < list.size())
         list.add(insert_point, r);
      else
         list.add(r);
      }
   */
   }

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
package com.ysoft.jdo.book.rental.ejb.service.impl;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import javax.ejb.*;
import javax.jdo.*;
import javax.jdo.spi.PersistenceCapable;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.common.ejb.EJBHelper;
import com.ysoft.jdo.book.factory.*;
import com.ysoft.jdo.book.rental.ejb.service.*;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.service.*;


public class ReservationServiceEJB implements SessionBean, ReservationService
   {
   // various state that is not client specific
   private ConnectionFactory cFactory;
   private SessionContext    sessionContext;
   private boolean           test_serialization = true;

   /**
    * Called when the bean is created.
    */
   public void ejbCreate()
         throws CreateException
      {
      //msg("ejbCreate called");
      }

   /**
    * Called when the bean is destroyed.
    */
   public void ejbRemove()
      {
      //msg("ejbRemove called");
      }

   /**
    * Called when the container wants to inform us of
    * the session context.
    */
   public void setSessionContext(SessionContext sc)
      {
      //msg("setSessionContext called");
      sessionContext = sc;

      try
         {
         cFactory = JndiLocator.getCF("java:/jdoCF");
         }
      catch (Exception e)
         {
         throw new EJBException("Unable to get ConnectionFactory using \"java:/jdoCF\" name",
            e);
         }
      }

   public void ejbActivate()
      {
      // stateless session beans are not activated
      }

   public void ejbPassivate()
      {
      // stateless session beans are not passivated
      }

   public Collection getAvailableRentals()
      {
      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

         // return results from query
         Extent extent                = pm.getExtent(Rental.class, false);
         Query  queryAvailableRentals = pm.newQuery(extent, "customer == null");
         queryAvailableRentals.setOrdering(
            "week.startDate ascending, lighthouse.name ascending");

         Collection c = (Collection) queryAvailableRentals.execute();

         if (test_serialization)
            {
            testSerialization(c, "collection of available rentals");
            test_serialization = false;
            }

         //MsgCenter.putMsg("ReservationService.getAvailableRentals: returning " + c.size() + " objects");
         return (Collection) EJBHelper.respond(c);
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            {
            MsgCenter.putMsg("getAvailableRentals: closing PM");
            pm.close();
            }
         }
      }

   public Collection getCustomerRentals(Customer tCustomer)
      {
      if (tCustomer == null)
         throw new IllegalArgumentException();

      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

         // get the persistent customer object if it exists
         Customer pCustomer = null;
         String   oidString = tCustomer.getIdentityString();

         if (oidString != null)
            {
            Object oid = pm.newObjectIdInstance(Customer.class, oidString);

            if (oid != null)
               pCustomer = (Customer) pm.getObjectById(oid, false);
            }

         Collection c = null;

         if (pCustomer != null)
            {
            // return results from query
            Extent extent               = pm.getExtent(Rental.class, false);
            Query  queryCustomerRentals = pm.newQuery(extent, "customer == c");
            queryCustomerRentals.declareParameters("Customer c");
            queryCustomerRentals.setOrdering(
               "week.startDate ascending, lighthouse.name ascending");
            c = (Collection) queryCustomerRentals.execute(pCustomer);
            }
         else
            c = new ArrayList();

         return (Collection) EJBHelper.respond(c);
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            pm.close();
         }
      }

   public Collection getCustomerAndAvailableRentals(Customer tCustomer)
      {
      if (tCustomer == null)
         throw new IllegalArgumentException();

      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

         Customer pCustomer = null;
         String   oidString = tCustomer.getIdentityString();

         if (oidString != null)
            {
            Object oid = pm.newObjectIdInstance(Customer.class, oidString);

            if (oid != null)
               pCustomer = (Customer) pm.getObjectById(oid, false);
            }

         Collection c    = null;
         ArrayList  list = null;

         if (pCustomer != null)
            {
            // get results from query
            Extent extent = pm.getExtent(Rental.class, false);
            Query  queryCustomerAndAvailableRentals = pm.newQuery(extent,
                  "customer == c || customer == null");
            queryCustomerAndAvailableRentals.declareParameters("Customer c");
            queryCustomerAndAvailableRentals.setOrdering(
               "week.startDate ascending, lighthouse.name ascending");
            c    = (Collection) queryCustomerAndAvailableRentals.execute(pCustomer);

            // work around SQL generation bug in Kodo 2.3.4
            //MsgCenter.putMsg("ReservationService.getCustomerAndAvailableRentals: from pm query " + c.size() + " objects");
            // we need to maintain the results order and drop duplicates
            list = new ArrayList(c.size());

            Iterator iter = c.iterator();

            while (iter.hasNext())
               {
               Object o = iter.next();

               if (!list.contains(o))
                  list.add(o);
               }

            queryCustomerAndAvailableRentals.close(c);
            c = list;
            }
         else
            c = getAvailableRentals();

         return (Collection) EJBHelper.respond(c);
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            pm.close();
         }
      }

   public List getCustomers(String name)
      {
      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

         LinkedList retv         = new LinkedList();
         Customer   tempCustomer = new Customer(name);

         // get results from query
         Extent extent           = pm.getExtent(Customer.class, false);
         Query  queryForCustomer = pm.newQuery(extent, "name == n");
         queryForCustomer.declareParameters("String n");

         Collection c = (Collection) queryForCustomer.execute(tempCustomer.getName());

         // construct and return the list
         retv.addAll(c);
         queryForCustomer.close(c);

         if (retv.size() < 1)
            {
            // the new customer is not added to the db until he is used to make a reservation
            // so we return here a transient customer that has never been added to the db
            retv.add(tempCustomer);
            }

         return (List) EJBHelper.respond(retv);
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            pm.close();
         }
      }

   public List getLighthouses()
      {
      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

         // get results from query
         Extent     extent              = pm.getExtent(Lighthouse.class, false);
         Query      queryForLighthouses = pm.newQuery(extent, "");
         Collection c                   = (Collection) queryForLighthouses.execute();

         // construct and return the list
         LinkedList retv = new LinkedList(c);

         queryForLighthouses.close(c);
         return (List) EJBHelper.respond(retv);
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            pm.close();
         }
      }

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
    * @throws ReservationException when the customer that holds the reservation is different from
    *                            the customer that is attempting to cancel or make the reservation.
    */
   public void flipReservations(Collection unmanagedRentals,
      Customer unmanagedCustomer)
         throws ExtendedOptimisticException, ReservationException
      {
      PersistenceManager pm = null;

      try
         {
         pm = getPersistenceManager();
         JDOFactory.tellConfiguration(pm);

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
                        Customer.class, oidString), false);
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
                        Rental.class, oidString), false);
               }
            catch (JDODataStoreException e)
               {
               throw new ExtendedOptimisticException(
                  "The system has deleted the rental record: " + uRental, e);
               }

            // if the persistent versions are not compatible, then an error
            if (!pRental.isSameVersion(uRental))
               throw new ExtendedOptimisticException(
                  "The rental record has been altered by another user, please review your change and submit it again");

            Customer rCustomer = pRental.getCustomer();

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
         }
      catch (ReservationException e)
         {
         // prevent transaction commit
         sessionContext.setRollbackOnly();
         throw e;
         }
      catch (ResourceException e)
         {
         throw new EJBException("Unable to get connection: " + e);
         }
      finally
         {
         if (pm != null)
            pm.close();
         }
      }

   private PersistenceManager getPersistenceManager()
         throws ResourceException
      {
      return (PersistenceManager) cFactory.getConnection();
      }

   private void testSerialization(Object o, String msg)
      {
      try
         {
         ByteArrayOutputStream buf = new ByteArrayOutputStream();
         ObjectOutputStream    out = new ObjectOutputStream(buf);
         out.writeObject(o);
         out.close();
         MsgCenter.putMsg("Performed serialization on " + msg + ": size " +
            buf.size());
         }
      catch (Exception e)
         {
         MsgCenter.putException("Caught exception when serializing " + msg, e);
         }
      }
   }

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
package com.ysoft.jdo.book.rental.local.service;

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
      //MsgCenter.putMsg("Getting reservation service");
      try
         {
         Properties props = loadProperties(
               "com/ysoft/jdo/book/rental/persistent/factory.properties");
         pmf = JDOHelper.getPersistenceManagerFactory(props);
         cyclePM();
         }
      catch (IOException e)
         {
         throw new ReservationException("unable to load properties file", e);
         }
      }

   public void beginTransaction()
      {
      if (!tx.isActive())
         {
         tx.begin();
         MsgCenter.putMsg("transaction started");
         }
      else
         MsgCenter.putMsg("transaction already started");
      }

   public void rollbackTransaction()
      {
      cleanupQueries();
      tx.rollback();
      MsgCenter.putMsg("transaction rolled back");
      }

   public void commitTransaction()
         throws OptimisticReservationException
      {
      try
         {
         cleanupQueries();
         tx.commit();
         MsgCenter.putMsg("transaction committed");
         }
      catch (JDOUserException e) // look for JDO 1.0 optimistic failure
         {
         /*
         // debugging
         Object obj1 = e.getFailedObject();
         System.out.println("Does exception contain failed object? " + (obj1 != null ? "true" : "false"));
         if (obj1 != null)
            System.out.println("Failed object: " + obj1);
         */

         // we know how to recover if it is an optimistic lock exception
         boolean     recovered = true;
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
                  recovered = false;
                  break;
                  }

               JDOException ue = (JDOException) ne[x];
               Object       fo = ue.getFailedObject();

               if (fo == null)
                  {
                  recovered = false;
                  break;
                  }

               /* refresh not needed as the combination of rollback and
                  RestoreValues == false will cause JDO to evict the
                  persistent-clean and persistent-dirty objects

               if (fo instanceof PersistenceCapable)
                  {
                  MsgCenter.putMsg("refreshing " + fo);
                  recovered = true;
                  pm.refresh(fo);
                  }
               */
               }
            }

         // simulate JDO 1.0.1 rollback action
         if (recovered)
            {
            if (tx.isActive())
               {
               try
                  {
                  MsgCenter.putMsg(
                     "Rolling back on detection of JDO 1.0 style optimistic failure");
                  tx.rollback();
                  }
               catch (Exception logit)
                  {
                  MsgCenter.putException("caught exception rolling back", logit);
                  }
               }

            throw new OptimisticReservationException(
               "Concurrent changes by other users prevented your changes, try again.");
            }
         else
            throw e;
         }

      // catch JDO 1.0.1 optimistic failures
      //catch (JDOOptimisticVerificationException e) 1.0 implementations don't yet have this type
      catch (JDOFatalDataStoreException e)
         {
         throw new OptimisticReservationException(
            "Concurrent changes by other users prevented your changes, try again.");
         }

      /*
      // debugging
      catch (RuntimeException e)
         {
         MsgCenter.putException("caught exception during commitTransaction, tx is " +
               (tx.isActive() ? "active" : "inactive"), e);
         throw e;
         }
      */
      }

   public Collection getAvailableRentals()
      {
      Collection c = (Collection) queryAvailableRentals.execute();
      return c;
      }

   public Collection getCustomerRentals(Customer customer)
      {
      if (customer == null)
         throw new IllegalArgumentException();

      Collection c = (Collection) queryCustomerRentals.execute(customer);
      return c;
      }

   public Collection getCustomerAndAvailableRentals(Customer customer)
      {
      if (customer == null)
         throw new IllegalArgumentException();

      Collection c = (Collection) queryCustomerAndAvailableRentals.execute(customer);
      return c;
      }

   public void evictAll()
         throws ReservationException
      {
      if (tx.isActive())
         throw new ReservationException(
            "evictAll works only when a transaction is not active");

      // note: this method is relying on non-conforming behavior that is currently found in nearly
      // all JDO implementations.  In fact, the evictAll method is supposed to evict only persistent-clean
      // instances, but most implementations are evicting PNT as well, which is what we need here.
      pm.evictAll();
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

         // make the customer persistent so we can use it
         // in queries
         pm.makePersistent(tempCustomer);
         }

      return retv;
      }

   public List getCustomers()
      {
      LinkedList retv = new LinkedList();

      Extent     extent = pm.getExtent(Customer.class, false);

      Iterator   iter = extent.iterator();

      while (iter.hasNext())
         retv.add(iter.next());

      extent.closeAll();
      return retv;
      }

   public List getLighthouses()
      {
      LinkedList retv = new LinkedList();
      Collection c = (Collection) queryForLighthouses.execute();
      retv.addAll(c);
      return retv;
      }

   // utility methods for testing
   public void populateDatastore()
         throws ReservationException
      {
      try
         {
         tx.begin();

         if (isCleanDatastore())
            {
            Lighthouse s1 = new Lighthouse("Curtis",
                  "Curtis Island Light in Camden was built in 1896.",
                  new BigDecimal(1850), new BigDecimal(1150));
            s1.setImageName("CurtisIslandLight");

            Lighthouse s2 = new Lighthouse("Nubble",
                  "Cape Neddick Light in York Beach is the southernmost of Maine's sixty lights.",
                  new BigDecimal(1900), new BigDecimal(1375));
            s2.setImageName("NubbleLight");

            Lighthouse s3 = new Lighthouse("Bass Harbor",
                  "Bass Harbor Light on Mt. Desert guards the entrance to Blue Hill Bay.",
                  new BigDecimal(1400), new BigDecimal(895));
            s3.setImageName("BassHarborLight");

            Week w = Week.getFirstWeek();

            for (int x = 0; x < 16; x++)
               {
               pm.makePersistent(new Rental(s1, w));
               pm.makePersistent(new Rental(s2, w));
               pm.makePersistent(new Rental(s3, w));

               // x hasn't been incremented yet, the index is one behind the place
               w = Week.getNextWeek(w.getStartOfWeek(), ((x >= 3) && (x < 11)));
               }
            }
         else
            MsgCenter.putMsg("The database is already populated");

         tx.commit();
         }
      catch (JDOFatalException e)
         {
         // let the caller handle the serious problems
         throw e;
         }
      catch (RuntimeException e)
         {
         throw new ReservationException("unable to populate the database", e);
         }
      }

   public void cleanDatastore()
         throws ReservationException
      {
      try
         {
         tx.begin();

         Extent[] extents = new Extent[]
            {
               pm.getExtent(Week.class, false),
               pm.getExtent(Lighthouse.class, false),
               pm.getExtent(Rental.class, false),
               pm.getExtent(Customer.class, false),
            };
         LinkedList list = new LinkedList();

         for (int x = 0; x < extents.length; x++)
            {
            Iterator iter = extents[x].iterator();

            while (iter.hasNext())
               list.add(iter.next());

            extents[x].closeAll();
            }

         pm.deletePersistentAll(list);
         tx.commit();
         }
      catch (JDOFatalException e)
         {
         // let the caller handle the serious problems
         throw e;
         }
      catch (RuntimeException e)
         {
         throw new ReservationException("unable to purge the database", e);
         }
      }

   public boolean isCleanDatastore()
         throws ReservationException
      {
      boolean started_transaction = false;
      boolean retv = true;

      try
         {
         if (!tx.isActive())
            {
            tx.begin();
            started_transaction = true;
            }

         Extent[] extents = new Extent[]
            {
               pm.getExtent(Week.class, false),
               pm.getExtent(Lighthouse.class, false),
               pm.getExtent(Rental.class, false),
               pm.getExtent(Customer.class, false),
            };

         for (int x = 0; x < extents.length; x++)
            {
            Iterator iter = extents[x].iterator();

            if (iter.hasNext())
               {
               retv = false;
               break;
               }

            extents[x].close(iter);
            }

         if (started_transaction)
            tx.commit();

         return retv;
         }
      catch (JDOFatalException e)
         {
         // let the caller handle the serious problems
         throw e;
         }
      catch (RuntimeException e)
         {
         if (started_transaction)
            {
            try
               {
               tx.rollback();
               }
            catch (Exception log)
               {
               MsgCenter.putException("caught exception trying to rollback", log);
               }
            }

         throw new ReservationException("unable to determine whether database is clean",
            e);
         }
      }

   public void tellConfiguration()
         throws ReservationException
      {
      try
         {
         MsgCenter.putMsg("Current Configuration: Opt:" + tx.getOptimistic() +
            ", RtV: " + tx.getRetainValues() + ", RsV: " +
            tx.getRestoreValues() + ", NTR: " + tx.getNontransactionalRead() +
            ", NTW: " + tx.getNontransactionalWrite() + ", IC: " +
            pm.getIgnoreCache() + ", active: " + tx.isActive());
         }
      catch (JDOFatalException e)
         {
         // let the caller handle the serious problems
         throw e;
         }
      catch (RuntimeException e)
         {
         throw new ReservationException("unable to determine the configuration",
            e);
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

   private static Properties loadProperties(String propFileName)
         throws IOException
      {
      ClassLoader cl     = Thread.currentThread().getContextClassLoader();
      InputStream stream = cl.getResourceAsStream(propFileName);

      if (stream == null)
         throw new IOException("File not found: " + propFileName);

      Properties props = new Properties();
      props.load(stream);

      stream.close();
      return props;
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

   private void cyclePM()
      {
      if (pmf == null)
         throw new IllegalStateException(
            "PersistenceManagerFactory is not initialized");

      cleanupQueries(true);

      // regardless of exceptions, we are going to null out the fields
      PersistenceManager p = pm;
      Transaction        t = tx;
      pm    = null;
      tx    = null;

      // close pm if open
      if ((p != null) && !p.isClosed())
         {
         if ((t != null) && t.isActive())
            t.rollback();

         p.close();
         }

      pm    = pmf.getPersistenceManager();
      tx    = pm.currentTransaction();
      setupQueries();
      MsgCenter.putMsg("pm cycled");
      }

   /*
   // debugging
   public void setSpecials(List taken)
      {
      taken_rentals = new Rental[taken.size()];
      taken_rentals = (Rental []) taken.toArray(taken_rentals);
      }
   */
   }

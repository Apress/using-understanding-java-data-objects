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
package com.ysoft.jdo.book.coffee;

import java.io.*;
import java.util.*;

import javax.jdo.*;

import com.ysoft.jdo.book.common.Copyright;
import com.ysoft.jdo.book.factory.JDOFactory;


public class MegaCups
   {
   static
      {
      Copyright.stdout();
      }

   private static String                    propsFilename = "com/ysoft/jdo/book/coffee/factory.properties";
   private static PersistenceManagerFactory pmf;
   private static String[]                  worker_names = 
   {
      "Mark", "Sam", "Julie", "Susan", "Frank"
   };
   private static boolean no_kitchen_help;

   public static void main(String[] args)
      {
      // get command line argument
      getArgs(args);

      // get the PMF
      try
         {
         JDOFactory.useProperties(propsFilename);
         }
      catch (IOException e)
         {
         msg("Caught IOException: " + e.getMessage());

         synchronized (Worker.class)
            {
            e.printStackTrace();
            }

         return;
         }

      pmf = JDOFactory.getPersistenceManagerFactory();

      if (pmf == null)
         throw new RuntimeException(
            "Did not create a PersistenceManagerFactory");

      // get the oid of the CoffeeUrn that will be used
      PersistenceManager pm = pmf.getPersistenceManager();
      pm.currentTransaction().begin();

      Iterator iter = null;

      try
         {
         Extent extent = pm.getExtent(CoffeeUrn.class, true);
         iter = extent.iterator();
         }
      catch (JDOUnsupportedOptionException e)
         {
         msg("This implementation does not support extents");

         // try a different approach
         Query      q = pm.newQuery(CoffeeUrn.class);

         Collection c = (Collection) q.execute();
         iter = c.iterator();
         }

      CoffeeUrn u = null;

      while ((u == null) && iter.hasNext())
         {
         u = (CoffeeUrn) iter.next();
         }

      if (u == null)
         {
         u = new CoffeeUrn("Kitchen");
         pm.makePersistent(u);
         }

      Object oid = pm.getObjectId(u);
      pm.currentTransaction().commit();

      if (oid == null)
         throw new RuntimeException("The coffee urn's object identity is null");

      msg("This program will end in one minute");

      // create the worker who fills up the coffee urn
      int      start   = 0;
      Worker   w       = null;
      Thread   t       = null;
      Worker[] workers = new Worker[worker_names.length];

      if (!no_kitchen_help)
         {
         start    = 1;

         w    = new Worker(worker_names[0], oid, true, pmf);
         t    = new Thread(w);
         t.setDaemon(true);
         t.start();
         workers[0] = w;
         }

      for (int x = start; x < worker_names.length; x++)
         {
         w    = new Worker(worker_names[x], oid, false, pmf);
         t    = new Thread(w);
         t.setDaemon(true);
         t.start();
         workers[x] = w;
         }

      try
         {
         // sleep for 1 minute to let the workers work
         // before terminating the program
         Thread.sleep(60000);
         }
      catch (InterruptedException ignore)
         {
         }

      for (int x = 0; x < workers.length; x++)
         {
         workers[x].blowWhistle();
         }

      msg("-- All done!");
      }

   private static void getArgs(String[] args)
      {
      Vector  v             = new Vector();
      boolean getting_names = false;

      for (int x = 0; x < args.length; x++)
         {
         if (args[x].equalsIgnoreCase("-names"))
            getting_names = true;
         else if (args[x].equalsIgnoreCase("-nofilling"))
            {
            getting_names      = false;
            no_kitchen_help    = true;
            }
         else if (getting_names)
            v.add(args[x]);
         else
            {
            msg("[-nofilling] [-names worker-name, ...]");
            System.exit(1);
            }
         }

      if ((v.size() > 1) || ((v.size() == 1) && no_kitchen_help))
         {
         worker_names = new String[v.size()];
         v.copyInto(worker_names);
         }

      msg("Using property file: " + propsFilename);
      }

   static synchronized void msg(String m)
      {
      System.out.println(m);
      }
   }


class Worker implements Runnable
   {
   private static int                MAX_TOLERANCE_FOR_MISSING_COFFEE = 3;
   private static long               lastTimeIgnored;
   private PersistenceManagerFactory pmf;
   private PersistenceManager        pm;
   private CoffeeUrn                 urn;
   private Object                    oidCoffeeUrn;
   private boolean                   kitchenHelp;
   private String                    name;
   private boolean                   quit;
   private int                       missingCups;
   private boolean                   end_of_day;

   public Worker(String name, Object coffeeUrnIdentity, boolean makesCoffee,
      PersistenceManagerFactory pmf)
      {
      if ((name == null) || (name.trim().length() <= 0))
         throw new IllegalArgumentException("The worker must have a name");

      this.name       = name.trim();
      oidCoffeeUrn    = coffeeUrnIdentity;
      kitchenHelp     = makesCoffee;
      this.pmf        = pmf;
      }

   public void run()
      {
      // get the PersistenceManager
      pm = pmf.getPersistenceManager();

      // find the CoffeeUrn
      pm.currentTransaction().begin();
      urn = (CoffeeUrn) pm.getObjectById(oidCoffeeUrn, true);
      MegaCups.msg(name + " found: " + urn);
      pm.currentTransaction().commit();

      if (kitchenHelp)
         brewCoffeeEverySoOften();
      else
         drinkCoffeeEverySoOften();
      }

   private void brewCoffeeEverySoOften()
      {
      while (!whistleBlew())
         {
         brewCoffee();

         try
            {
            Thread.sleep(14000);
            }
         catch (InterruptedException ignore)
            {
            }
         }

      MegaCups.msg(name + " has gone home at the end of the day");
      allDone();
      }

   private void drinkCoffeeEverySoOften()
      {
      while (!quit && !whistleBlew())
         {
         drinkCoffee();

         try
            {
            Thread.sleep(2000);
            }
         catch (InterruptedException ignore)
            {
            }
         }

      if (quit)
         MegaCups.msg(name + " has quit because of the lack of coffee");
      else
         MegaCups.msg(name + " has gone home at the end of the day");

      allDone();
      }

   private void brewCoffee()
      {
      pm.currentTransaction().begin();
      urn.addCoffee(20);
      MegaCups.msg(name + " added coffee to " + urn);
      pm.currentTransaction().commit();
      }

   private void drinkCoffee()
      {
      pm.currentTransaction().begin();

      if (urn.drawCoffee() < 1)
         {
         missingCups++;
         MegaCups.msg(name + " found the coffee urn empty: " + urn);

         if (missingCups > MAX_TOLERANCE_FOR_MISSING_COFFEE)
            {
            MegaCups.msg(name +
               " is threatening to quit over the lack of coffee");

            if (!Worker.askManagerToFix())
               {
               resign();
               }
            else
               {
               missingCups = 0;
               MegaCups.msg(
                  "The manager has promised to fix the coffee problem for " +
                  name);
               }
            }
         }
      else
         MegaCups.msg(name + " drank a cup of coffee from " + urn);

      pm.currentTransaction().commit();
      }

   synchronized void blowWhistle()
      {
      end_of_day = true;

      try
         {
         if (!quit)
            wait();
         }
      catch (InterruptedException ignore)
         {
         }
      }

   private synchronized void resign()
      {
      quit = true;
      }

   private synchronized boolean whistleBlew()
      {
      return end_of_day;
      }

   private synchronized void allDone()
      {
      notify();
      }

   // workers call this method when they find that
   // the coffee urn has been empty too often.
   // If this method returns true, then the manager
   // is promising to fix the problem.
   // Otherwise, this method returns false and the
   // worker will quit.
   static synchronized boolean askManagerToFix()
      {
      // If it has been more than 5 seconds since the
      // last worker quit, the manager will ignore the
      // problem.
      // After one worker quits, he realizes he's
      // got to do something.
      long now = System.currentTimeMillis();

      if ((now - 5000) < lastTimeIgnored)
         return true;

      lastTimeIgnored = now;
      return false;
      }
   }

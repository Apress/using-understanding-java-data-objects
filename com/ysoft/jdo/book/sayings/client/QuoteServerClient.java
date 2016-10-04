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
package com.ysoft.jdo.book.sayings.client;

import java.rmi.RemoteException;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.jdo.spi.PersistenceCapable;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import javax.transaction.*;

import com.ysoft.jdo.book.common.ClassObserver;
import com.ysoft.jdo.book.sayings.persistent.*;
import com.ysoft.jdo.book.sayings.service.*;


public class QuoteServerClient
   {
   private static int                                                          numLoops =
      1;
   private static long                                                         sleepMS =
      0L;
   private static int                                                          numTxs =
      0;
   private static String                                                       newSaying;
   private static String                                                       newSource;
   private static com.ysoft.jdo.book.sayings.service.session.QuoteServerHome   qsHome =
      null;
   private static com.ysoft.jdo.book.sayings.service.entity.QuoteServerHome    qsHome2 =
      null;
   private static com.ysoft.jdo.book.sayings.service.session.QuoteServerRemote qsRemote =
      null;
   private static com.ysoft.jdo.book.sayings.service.entity.QuoteServerRemote  qsRemote2 =
      null;
   private static UserTransaction                                              userTransaction;

   public static void main(String[] args)
      {
      // do this so that we download exceptions that are thrown unexpectedly
      //System.setSecurityManager(new java.rmi.RMISecurityManager());
      //System.setSecurityManager(new SecurityManager());
      getParameters(args);

      QuoteServer quoteServer = findQuoteServer();

      if (quoteServer == null)
         throw new RuntimeException("Unable to connect to QuoteServerEJB");

      // call the business method
      if (newSaying == null)
         {
         if (numTxs == 0)
            {
            System.out.println("Now calling getQuote " + numLoops +
               " times with " + sleepMS + "ms wait after each call");
            }
         else
            {
            System.out.println("Now calling getQuote " + numLoops +
               " times for each of " + numTxs + " transactions, sleeping " +
               sleepMS + " ms after each call");
            }
         }
      else
         System.out.println("Adding quote to system: " + newSaying + " -- " +
            newSource);

      try
         {
         if (newSaying != null)

            //quoteServer.addQuote(newQuote.getSaying(), newQuote.getSource());
            quoteServer.addQuote(newSaying, newSource);
         else
            {
            int     exceptionCount = 0;
            boolean one_time_only = (numTxs == 0);

            for (int y = 0; (y < numTxs) || one_time_only; y++)
               {
               one_time_only = false;

               // get a user transaction
               if ((numTxs > 0) && (userTransaction == null))
                  {
                  try
                     {
                     //java:comp/UserTransaction
                     Object obj = new InitialContext().lookup("UserTransaction");
                     userTransaction = (UserTransaction) PortableRemoteObject.narrow(obj,
                           UserTransaction.class);
                     }
                  catch (NamingException e)
                     {
                     System.out.println("User transaction not found");
                     numTxs = 1;
                     System.out.println(e);
                     e.printStackTrace(System.out);
                     }
                  }

               // start the user transaction
               if (userTransaction != null)
                  {
                  try
                     {
                     userTransaction.begin();
                     System.out.println("started user transaction");
                     }

                  /*
                  catch (NotSupportedException e)
                  catch (SystemException e)
                  */
                  catch (Exception e)
                     {
                     System.out.println("User transaction could not begin");
                     numTxs             = 1;
                     userTransaction    = null;
                     System.out.println(e);
                     e.printStackTrace(System.out);
                     }
                  }

               for (int x = 0; x < numLoops; x++)
                  {
                  try
                     {
                     Quote q = quoteServer.getQuote();
                     System.out.println(q);

                     if (x == 0)
                        System.out.println(
                           "Quote implements PersistenceCapable? " +
                           (q instanceof PersistenceCapable));

                     if ((x + 1) < numLoops)
                        Thread.sleep(sleepMS);
                     }
                  catch (QuoteServerException e)
                     {
                     System.out.println("caught QuoteServerException: " + e);
                     e.printStackTrace();

                     if (++exceptionCount >= 5)
                        break;

                     System.out.println("caught app exception number: " +
                        exceptionCount);
                     }
                  catch (RemoteException e)
                     {
                     System.out.println(
                        "caught RemoteException calling QuoteServer: " + e);
                     e.printStackTrace();

                     if (++exceptionCount >= 5)
                        break;

                     System.out.println("caught app exception number: " +
                        exceptionCount);
                     }
                  }

               String action = null;

               if (userTransaction != null)
                  {
                  try
                     {
                     /*
                     if (y % 2 == 0)
                        {
                     */
                     userTransaction.commit();
                     System.out.println("committed user transaction");
                     action = "commit";

                     /*
                        }
                     else
                        {
                        userTransaction.rollback();
                        System.out.println("rolled back user transaction");
                        action = "roll back";
                        }
                     */
                     }

                  /*
                  catch (RollbackException e)
                  catch (HeuristicMixedException e)
                  catch (HeuristicRollbackException e)
                  catch (SystemException e)
                  */
                  catch (Exception e)
                     {
                     System.out.println("could not " + action +
                        " user transaction");
                     System.out.println(e);
                     e.printStackTrace(System.out);
                     }
                  }
               }
            }
         }
      catch (InterruptedException e)
         {
         System.out.println("caught InterruptedException sleeping: " + e);
         }
      catch (RemoteException e)
         {
         System.out.println("caught RemoteException calling QuoteServer: " + e);
         e.printStackTrace();
         }
      catch (QuoteServerException e)
         {
         System.out.println("caught QuoteServerException: " + e);
         e.printStackTrace();
         }

      // remove the session bean
      try
         {
         if (qsRemote != null)
            qsRemote.remove();
         }
      catch (RemoveException e)
         {
         System.out.println("caught RemoveException removing the bean" + e);
         }
      catch (RemoteException e)
         {
         System.out.println("caught RemoteException removing the bean" + e);
         }

      System.out.println("-- All done!");
      }

   private static void getParameters(String[] args)
      {
      int len = args.length;

      if (len == 0)
         return;
      else if (len == 2)
         {
         if (args[0].trim().equalsIgnoreCase("-loop"))
            {
            acceptLoopCount(args[1]);
            }
         else
            tellSyntax();
         }
      else if (len == 4)
         {
         String arg0 = args[0].trim();
         String arg1 = args[1].trim();
         String arg2 = args[2].trim();
         String arg3 = args[3].trim();

         if (arg0.equalsIgnoreCase("-loop") && arg2.equalsIgnoreCase("-sleep"))
            {
            acceptLoopCount(arg1);
            acceptSleepDuration(arg3);
            }
         else if (arg0.equalsIgnoreCase("-sleep") &&
                  arg2.equalsIgnoreCase("-loop"))
            {
            acceptLoopCount(arg3);
            acceptSleepDuration(arg1);
            }
         else if (arg0.equalsIgnoreCase("-quote") &&
                  arg2.equalsIgnoreCase("-source"))
            {
            newSaying    = arg1;
            newSource    = arg3;
            }
         else if (arg2.equalsIgnoreCase("-quote") &&
                  arg0.equalsIgnoreCase("-source"))
            {
            newSaying    = arg3;
            newSource    = arg1;
            }
         else
            tellSyntax();
         }
      else if (len == 6)
         {
         boolean got_sleep  = true;
         boolean got_count  = true;
         boolean got_tcount = true;

         if (args[0].equalsIgnoreCase("-loop"))
            acceptLoopCount(args[1]);
         else if (args[2].equalsIgnoreCase("-loop"))
            acceptLoopCount(args[3]);
         else if (args[4].equalsIgnoreCase("-loop"))
            acceptLoopCount(args[5]);
         else
            got_count = false;

         if (args[0].equalsIgnoreCase("-sleep"))
            acceptSleepDuration(args[1]);
         else if (args[2].equalsIgnoreCase("-sleep"))
            acceptSleepDuration(args[3]);
         else if (args[4].equalsIgnoreCase("-sleep"))
            acceptSleepDuration(args[5]);
         else
            got_sleep = false;

         if (args[0].equalsIgnoreCase("-numTx"))
            acceptNumTransactions(args[1]);
         else if (args[2].equalsIgnoreCase("-numTx"))
            acceptNumTransactions(args[3]);
         else if (args[4].equalsIgnoreCase("-numTx"))
            acceptNumTransactions(args[5]);
         else
            got_tcount = false;

         if (!got_count || !got_sleep || !got_tcount)
            tellSyntax();
         }
      else
         tellSyntax();
      }

   private static void acceptLoopCount(String s)
      {
      try
         {
         numLoops = Integer.parseInt(s);

         //System.out.println("got loop count: " + numLoops);
         }
      catch (NumberFormatException e)
         {
         tellSyntax();
         }
      }

   private static void acceptSleepDuration(String s)
      {
      try
         {
         sleepMS = Integer.parseInt(s);

         //System.out.println("got sleep duration: " + sleepMS);
         }
      catch (NumberFormatException e)
         {
         tellSyntax();
         }
      }

   private static void acceptNumTransactions(String s)
      {
      try
         {
         numTxs = Integer.parseInt(s);

         //System.out.println("got number transactions: " + numTxs);
         }
      catch (NumberFormatException e)
         {
         tellSyntax();
         }
      }

   private static void tellSyntax()
      {
      System.out.println("QuoteServerClient connects to the QuoteServer EJB");
      System.out.println(
         "   options: -loop <number> -sleep <number> [-numTx <number>]");
      System.out.println("   or: -quote \"quote\" -source \"source\"");
      System.out.println();
      System.out.println(
         "   where the loop number indicates how many quotes to get,");
      System.out.println(
         "   the sleep number indicates the sleep before getting another quote,");
      System.out.println(
         "   and the numTx indicates the number of transactions.");
      System.out.println();
      System.out.println(
         "   If you provide a numTx parameter, then the the client controls the");
      System.out.println(
         "   managed transaction and gets the loop number of quotes per transaction.");
      System.out.println();
      System.out.println(
         "   If you supply a quote, it will be added to the datastore");

      System.exit(1);
      }

   private static QuoteServer findQuoteServer()
      {
      QuoteServer qs = null;

      try
         {
         // Get a naming context
         InitialContext jndiContext = new InitialContext();
         System.out.println("Got initial context");

         // Get a reference to the Bean's Home interface
         Object ref = jndiContext.lookup("QuoteServer");
         System.out.println("Got reference");

         // Get a reference from this to the Bean's Home interface
         try
            {
            qsHome = (com.ysoft.jdo.book.sayings.service.session.QuoteServerHome) PortableRemoteObject.narrow(ref,
                  com.ysoft.jdo.book.sayings.service.session.QuoteServerHome.class);
            System.out.println(
               "Narrowed the reference to session.QuoteServerHome");
            }
         catch (ClassCastException e)
            {
            qsHome2 = (com.ysoft.jdo.book.sayings.service.entity.QuoteServerHome) PortableRemoteObject.narrow(ref,
                  com.ysoft.jdo.book.sayings.service.entity.QuoteServerHome.class);
            System.out.println(
               "Narrowed the reference to entity.QuoteServerHome");
            }

         if (qsHome != null)
            {
            // Create a remote QuoteServer session bean
            qsRemote = qsHome.create();
            System.out.println(
               "Got a reference to a session.QuoteServerRemote object");

            /*
            //ClassObserver.describeBriefly(qsRemote.getClass());
            qsRemote = (com.ysoft.jdo.book.sayings.service.session.QuoteServerRemote)
                  PortableRemoteObject.narrow(qsRemote,
                  com.ysoft.jdo.book.sayings.service.session.QuoteServerRemote.class);
            System.out.println("Narrowed the reference to session.QuoteServerRemote");
            */
            // Create a reference to just the QuoteServer interface
            qs = (QuoteServer) PortableRemoteObject.narrow(qsRemote,
                  QuoteServer.class);
            }
         else
            {
            // Find a remote QuoteServer entity bean
            qsRemote2    = qsHome2.findByPrimaryKey(new Integer(1));
            qs           = (QuoteServer) PortableRemoteObject.narrow(qsRemote2,
                  QuoteServer.class);
            }

         if (qs != null)
            System.out.println("Got a reference to a QuoteServer object");
         }
      catch (NamingException e)
         {
         //System.out.println(e.getMessage());
         e.printStackTrace(System.out);
         }
      catch (CreateException e)
         {
         //System.out.println(e.getMessage());
         e.printStackTrace(System.out);
         }
      catch (FinderException e)
         {
         e.printStackTrace(System.out);
         }
      catch (RemoteException e)
         {
         //System.out.println(e.getMessage());
         e.printStackTrace(System.out);
         }

      return qs;
      }
   }

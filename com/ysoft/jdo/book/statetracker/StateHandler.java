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
package com.ysoft.jdo.book.statetracker;

import java.util.*;

import javax.jdo.*;
import javax.transaction.Status;
import javax.transaction.Synchronization;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.factory.JDOFactory;


public class StateHandler
   {
   private PersistenceManager pm;
   private Transaction        transaction;
   private String             adaptor_class_name = "com.ysoft.jdo.book.factory.jdori.JDORIAdaptor";

   public StateHandler()
      {
      JDOFactory.setVerbose(false);
      getPM(false, false, false, false, false, false);
      }

   public boolean makePersistent(Object pc)
      {
      try
         {
         pm.makePersistent(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception makePersistent", e);
         }

      return false;
      }

   public boolean deletePersistent(Object pc)
      {
      try
         {
         pm.deletePersistent(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception deletePersistent", e);
         }

      return false;
      }

   public boolean makeTransient(Object pc)
      {
      try
         {
         pm.makeTransient(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception makeTransient", e);
         }

      return false;
      }

   public boolean makeTransactional(Object pc)
      {
      try
         {
         pm.makeTransactional(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception makeTransactional", e);
         }

      return false;
      }

   public boolean makeNontransactional(Object pc)
      {
      try
         {
         pm.makeNontransactional(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception makeNontransactional", e);
         }

      return false;
      }

   public boolean refresh(Object pc)
      {
      try
         {
         pm.refresh(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception refresh", e);
         }

      return false;
      }

   public boolean refreshAll()
      {
      try
         {
         pm.refreshAll();
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception refreshAll", e);
         }

      return false;
      }

   public boolean retrieve(Object pc)
      {
      //MsgCenter.putMsg("PM.retrieve not implemented");
      try
         {
         pm.retrieve(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception retrieve", e);
         }

      return false;
      }

   public boolean evict(Object pc)
      {
      try
         {
         pm.evict(pc);
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception evict", e);
         }

      return false;
      }

   public boolean evictAll()
      {
      try
         {
         pm.evictAll();
         return true;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception evictAll", e);
         }

      return false;
      }

   public boolean beginTransaction()
      {
      boolean retv = false;

      try
         {
         if (!transaction.isActive())
            {
            transaction.begin();
            retv = true;
            }
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception transaction.begin", e);
         }

      return retv;
      }

   public boolean commitTransaction()
      {
      boolean retv = false;

      try
         {
         if (transaction.isActive())
            {
            transaction.commit();
            retv = true;
            }
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception transaction.commit", e);
         }

      return retv;
      }

   public boolean rollbackTransaction()
      {
      boolean retv = false;

      try
         {
         if (transaction.isActive())
            {
            transaction.rollback();
            retv = true;
            }
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception transaction.rollback", e);
         }

      return retv;
      }

   public void throwExceptionOnCommit()
      {
      TxSynchronization s = (TxSynchronization) transaction.getSynchronization();
      s.setForException();
      }

   public boolean isActiveTransaction()
      {
      return transaction.isActive();
      }

   public ArrayList findAll(Class c)
      {
      try
         {
         Extent    all = pm.getExtent(c, true);

         ArrayList results = new ArrayList();

         Iterator  iter = all.iterator();

         while (iter.hasNext())
            results.add(iter.next());

         all.close(iter);

         return results;
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception iterating extent for " + c, e);
         return null;
         }
      }

   public void configureTransaction(boolean optimistic, boolean retainValues,
      boolean restoreValues, boolean nontransactionalRead,
      boolean nontransactionalWrite)
      {
      // first tell what we are configuring to
      TxOptions txo = getTxOptions();

      try
         {
         txo.active = transaction.isActive();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's Active property",
            e);
         }

      txo.optimistic       = optimistic;
      txo.retainValues     = retainValues;
      txo.restoreValues    = restoreValues;
      txo.ntr              = nontransactionalRead;
      txo.ntw              = nontransactionalWrite;

      outputTransactionConfiguration("Configuring transaction to: ",
         new TxOptionStrings(txo));

      // now configure the transaction as requested
      try
         {
         transaction.setOptimistic(optimistic);
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception altering transaction Optimistic property",
            e);
         }

      try
         {
         transaction.setRetainValues(retainValues);
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception altering transaction RetainValues property",
            e);
         }

      //MsgCenter.putMsg("PM.setRestoreValues is not implemented");
      try
         {
         transaction.setRestoreValues(restoreValues);
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception altering transaction RestoreValues property",
            e);
         }

      try
         {
         transaction.setNontransactionalRead(nontransactionalRead);
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception altering transaction NontransactionalRead property",
            e);
         }

      try
         {
         transaction.setNontransactionalWrite(nontransactionalWrite);
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception altering transaction NontransactionalWrite property",
            e);
         }

      // now tell what the configuration is
      outputTransactionConfiguration("After configuring transaction: ",
         new TxOptionStrings(getTxOptions()));
      }

   public void tellTransactionConfiguration()
      {
      outputTransactionConfiguration("Current transaction properties: ",
         new TxOptionStrings(getTxOptions()));
      }

   private void outputTransactionConfiguration(String header,
      TxOptionStrings oStrings)
      {
      MsgCenter.putMsg(header + oStrings.activeString + ", " +
         oStrings.optString + ", " + oStrings.retainVString + ", " +
         oStrings.restoreVString + ", " + oStrings.NTRString + ", " +
         oStrings.NTWString);
      }

   public void openPersistenceManager()
      {
      getPM(false, false, false, false, false, false);
      }

   public boolean isOpenPersistenceManager()
      {
      return pm != null;
      }

   public void closePersistenceManager()
      {
      if (pm != null)
         {
         pm.close();
         JDOFactory.close();
         pm = null;
         }
      }

   public void shutdown()
      {
      if (pm != null)
         {
         if (pm.currentTransaction().isActive())
            pm.currentTransaction().rollback();

         closePersistenceManager();
         }
      }

   public void makeDirty(Object obj, String fieldName)
      {
      JDOHelper.makeDirty(obj, fieldName);
      }

   private void getPM(boolean optimistic, boolean ntRead, boolean ntWrite,
      boolean retainValues, boolean restoreValues, boolean ignoreCache)
      {
      closePersistenceManager();

      // we use construction because the client will configure the PM
      String cn = System.getProperty("adaptor-class");

      if (cn != null)
         adaptor_class_name = cn;

      JDOFactory.useConstruction(adaptor_class_name, ignoreCache, optimistic,
         ntRead, ntWrite, retainValues, restoreValues);

      PersistenceManagerFactory pmf = JDOFactory.getPersistenceManagerFactory();

      pm             = pmf.getPersistenceManager();
      transaction    = pm.currentTransaction();
      transaction.setSynchronization(new TxSynchronization());
      }

   private TxOptions getTxOptions()
      {
      TxOptions options = new TxOptions();

      try
         {
         options.active = transaction.isActive();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's Active property",
            e);
         }

      try
         {
         options.optimistic = transaction.getOptimistic();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's Optimistic property",
            e);
         }

      try
         {
         options.retainValues = transaction.getRetainValues();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's RetainValues property",
            e);
         }

      //MsgCenter.putMsg("PM.getRestoreValues is not implemented");
      try
         {
         options.restoreValues = transaction.getRestoreValues();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's RestoreValues property",
            e);
         }

      try
         {
         options.ntr = transaction.getNontransactionalRead();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's NontransactionalRead property",
            e);
         }

      try
         {
         options.ntw = transaction.getNontransactionalWrite();
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception getting transaction's NontransactionalWrite property",
            e);
         }

      return options;
      }

   private class TxOptionStrings
      {
      String activeString;
      String optString;
      String retainVString;
      String restoreVString;
      String NTRString;
      String NTWString;

      public TxOptionStrings(TxOptions opts)
         {
         activeString      = opts.active ? "active" : "inactive";
         optString         = opts.optimistic ? "Opt" : "!Opt";
         retainVString     = opts.retainValues ? "RetainV" : "!RetainV";
         restoreVString    = opts.restoreValues ? "RestoreV" : "!RestoreV";
         NTRString         = opts.ntr ? "NTR" : "!NTR";
         NTWString         = opts.ntw ? "NTW" : "!NTW";
         }
      }

   private class TxOptions
      {
      boolean active;
      boolean optimistic;
      boolean retainValues;
      boolean restoreValues;
      boolean ntr;
      boolean ntw;
      }
   }


class TxSynchronization implements Synchronization
   {
   private boolean one_shot;

   public void beforeCompletion()
      {
      MsgCenter.putMsg("Synchronization.beforeCompletion called");

      if (one_shot)
         {
         one_shot = false;
         throw new JDOUserException("Exception thrown in beforeCompletion");
         }
      }

   public void afterCompletion(int status)
      {
      MsgCenter.putMsg("Synchronization.afterCompletion called with status: " +
         getStatusString(status));
      }

   public void setForException()
      {
      one_shot = true;
      }

   private String getStatusString(int status)
      {
      String msg = null;

      switch (status)
         {
         case Status.STATUS_COMMITTED:
            msg = "committed";
            break;

         case Status.STATUS_ROLLEDBACK:
            msg = "rolledback";
            break;

         case Status.STATUS_ACTIVE:
            msg = "active (unexpected)";
            break;

         case Status.STATUS_COMMITTING:
            msg = "committing (unexpected)";
            break;

         case Status.STATUS_MARKED_ROLLBACK:
            msg = "marked rollback (unexpected)";
            break;

         case Status.STATUS_NO_TRANSACTION:
            msg = "no transaction (unexpected)";
            break;

         case Status.STATUS_PREPARED:
            msg = "prepared (unexpected)";
            break;

         case Status.STATUS_PREPARING:
            msg = "preparing (unexpected)";
            break;

         case Status.STATUS_ROLLING_BACK:
            msg = "rolling back (unexpected)";
            break;

         case Status.STATUS_UNKNOWN:
            msg = "unknown (unexpected)";
            break;

         default:
            msg = "unrecognized (unknown value)";
            break;
         }

      return msg;
      }
   }

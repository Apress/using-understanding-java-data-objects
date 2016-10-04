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

/**
 * This is the EJB implementation class for the QuoteServer.
 * This class implements both the SessionBean interface and the
 * the business interface QuoteServer.  This bean will be deployed
 * as a stateless BMT session bean.
 */
package com.ysoft.jdo.book.sayings.service.session;

import java.util.*;

import javax.ejb.*;
import javax.jdo.*;
import javax.naming.*;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.factory.*;
import com.ysoft.jdo.book.sayings.persistent.*;
import com.ysoft.jdo.book.sayings.persistent.QuoteManager.QuoteManagerOID;
import com.ysoft.jdo.book.sayings.service.*;


public class QuoteServerEJB implements SessionBean, QuoteServer
   {
   private SessionContext            sessionContext;
   private PersistenceManagerFactory pmf;
   private PersistenceManager        pm;
   private QuoteManager              qm;
   private Query                     query;
   private int                       invocationCount;
   private boolean                   specialEvent;

   /**
    * Called when the bean is created.
    */
   public void ejbCreate()
         throws CreateException
      {
      msg("ejbCreate called");
      }

   /**
    * Called when the bean is destroyed.
    */
   public void ejbRemove()
      {
      msg("ejbRemove called");
      cleanup();
      }

   public void ejbActivate()
      {
      // stateless session beans are not activated
      }

   public void ejbPassivate()
      {
      // stateless session beans are not passivated
      }

   /**
    * Called when the container wants to inform us of
    * the session context.
    */
   public void setSessionContext(SessionContext sc)
      {
      msg("setSessionContext called");
      sessionContext = sc;

      try
         {
         pmf    = JndiLocator.getPMF("Std_NTR_PMF",
               "com/ysoft/jdo/book/sayings/service/factory.properties");
         pm = getPersistenceManager();
         }
      catch (Exception e)
         {
         throw new EJBException("Unable to get PMF using \"Std_NTR_PMF\" name",
            e);
         }
      }

   public Quote getQuote()
         throws QuoteServerException
      {
      incInvocationCount();
      reportUserTransaction();
      JDOFactory.tellConfiguration(pm);

      if (specialEvent && (qm != null))
         {
         pm.evict(qm);
         }

      if (qm == null)
         qm = getQuoteManager(pm, false);

      if (query == null)
         query = getQuery(pm);

      Quote quote = getQuote(pm, qm, query);
      msg("returning quote: " + quote);
      return quote;
      }

   public void addQuote(String q, String s)
         throws QuoteServerException
      {
      q    = normalizeString(q);
      s    = normalizeString(s);

      try
         {
         pm.currentTransaction().begin();
         MsgCenter.putMsg("Bean started transaction");
         JDOFactory.tellConfiguration(pm);

         if (qm == null)
            qm = getQuoteManager(pm);

         Quote quote = qm.newQuote(q, s);
         pm.makePersistent(quote);
         pm.currentTransaction().commit();
         msg("made quote persistent: " + quote);
         }
      finally
         {
         if (pm.currentTransaction().isActive())
            {
            try
               {
               pm.currentTransaction().rollback();
               }
            catch (RuntimeException logIt)
               {
               // log it rather than throw it, since it
               // was an earlier exception that forced the rollback.
               }
            }
         }
      }

   private PersistenceManager getPersistenceManager()
      {
      PersistenceManager pManager = null;
      msg("opening a PM");
      pManager = pmf.getPersistenceManager();
      JDOFactory.tellConfiguration(pManager);
      return pManager;
      }

   /*
      private void reportConfigurable(PersistenceManager pm)
         {
         boolean pmf_configurable = false;
         boolean opt_configurable = false;
         boolean ntr_configurable = false;

         boolean opt = pmf.getOptimistic();
         try
            {
            pmf.setOptimistic(!opt);
            pmf.setOptimistic(opt);
            pmf_configurable = true;
            }
         catch (RuntimeException ignore)
            {
            }

         Transaction tx = pm.currentTransaction();
         try
            {
            if (!tx.isActive())
               {
               opt = tx.getOptimistic();
               tx.setOptimistic(!opt);
               tx.setOptimistic(opt);
               opt_configurable = true;
               }
            }
         catch (RuntimeException ignore)
            {
            }

         try
            {
            boolean ntr = tx.getNontransactionalRead();
            tx.setNontransactionalRead(!ntr);
            tx.setNontransactionalRead(ntr);
            ntr_configurable = true;
            }
         catch (RuntimeException ignore)
            {
            }

         msg("The PMF is " + (pmf_configurable ? "" : "not ") + "configurable");
         msg("The transaction's optimistic setting is " + (opt_configurable ? "" : "not ") + "configurable");
         msg("The transaction's nontransactional read setting is " + (ntr_configurable ? "" : "not ") + "configurable");
         }
   */
   private void reportUserTransaction()
      {
      UserTransaction ut = sessionContext.getUserTransaction();

      String          msg = "unknown";

      try
         {
         switch (ut.getStatus())
            {
            case Status.STATUS_ACTIVE:
               msg = "active";
               break;

            case Status.STATUS_NO_TRANSACTION:
               msg = "inactive";
               break;

            default:
               msg = "unknown";
            }
         }
      catch (Exception ignore)
         {
         }

      msg("The user transaction is " + msg);
      }

   private void incInvocationCount()
      {
      // the invocation count insures that the singleton QuoteManager, which is read
      // in NTR mode for all getQuote invocations, will be evicted occasionally.
      // Otherwise, this bean would never see any new quotes added, unless this
      // bean itself was used to make the addition, in which case, since RetainValues
      // is false, the eviction would occur at the end of the add transaction.
      invocationCount++;
      specialEvent = (invocationCount % 5) == 0;
      }

   private void msg(String m)
      {
      MsgCenter.putMsg("Stateless BMT QuoteServer (" + hashCode() +
         getPMHashCode() + "): " + m);
      }

   private void msg(String m, Exception e)
      {
      MsgCenter.putException("Stateless BMT QuoteServer (" + hashCode() +
         getPMHashCode() + "): " + m, e);
      }

   private String getPMHashCode()
      {
      if ((pm != null) && !pm.isClosed())
         return "-" + pm.hashCode();
      else
         return "";
      }

   private String normalizeString(String s)
         throws QuoteServerException
      {
      if (s != null)
         {
         s = s.trim();

         if (s.length() <= 0)
            s = null;
         }

      if (s == null)
         throw new QuoteServerException(
            "Neither the quotation nor the source can be null or empty");

      return s;
      }

   private QuoteManager getQuoteManager(PersistenceManager pm)
      {
      return getQuoteManager(pm, true);
      }

   private QuoteManager getQuoteManager(PersistenceManager pm,
      boolean createIfNone)
      {
      return getQuoteManager(pm, createIfNone,
         new QuoteManagerOID(QuoteManager.getSingletonKey()));
      }

   private QuoteManager getQuoteManager(PersistenceManager pm,
      boolean createIfNone, QuoteManagerOID key)
      {
      msg("getting QuoteManager");

      QuoteManager quoteManager = null;

      try
         {
         quoteManager = (QuoteManager) pm.getObjectById(key, true);
         }
      catch (JDODataStoreException e)
         {
         // do nothing here, creation follows next
         }

      // create it if it doesn't exist, but only if correct key is used.
      if ((quoteManager == null) && createIfNone)
         {
         msg("Initializing QuoteManager");

         if (!QuoteManager.getSingletonKey().equals(key.toInteger()))
            throw new EJBException("Cannot create a quote manager with key " +
               key + "; use Integer(1) instead");

         quoteManager = QuoteManager.newQuoteManager();
         pm.makePersistent(quoteManager);
         }

      return quoteManager;
      }

   private Quote getQuote(PersistenceManager pm2, QuoteManager qm2, Query query)
         throws QuoteServerException
      {
      try
         {
         // now qet a random quote
         Quote quote = null;

         if ((qm2 == null) || (qm2.getNumQuotes() < 1))
            quote = QuoteManager.makeTempQuote("Nothing to say", "The System");
         else
            {
            int        index   = qm2.getRandomIndex();
            Collection results = (Collection) query.execute(new Integer(index));
            Iterator   iter    = results.iterator();

            if (iter.hasNext())
               {
               quote = (Quote) iter.next();

               if (iter.hasNext())
                  throw new QuoteServerException(
                     "more than one quote with index: " + index);
               }
            else
               {
               throw new QuoteServerException("No quote for index: " + index);
               }
            }

         return quote;
         }
      finally
         {
         if (query != null)
            query.closeAll();
         }
      }

   private Query getQuery(PersistenceManager pm2)
      {
      Extent extent = pm2.getExtent(Quote.class, false);
      Query  query = pm2.newQuery(extent, "quoteIndex == i");

      // workaround bug in Lido 1.3.0 B3
      //query.declareParameters("Integer i");
      query.declareParameters("int i");
      return query;
      }

   private void cleanup()
      {
      msg("closing PM");

      if ((pm != null) && !pm.isClosed())
         {
         pm.close();
         }

      pm       = null;
      qm       = null;
      query    = null;
      }
   }

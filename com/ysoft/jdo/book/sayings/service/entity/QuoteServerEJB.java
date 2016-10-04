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
 * This class implements both the EntityBean interface and the
 * the business interface QuoteServer.  This bean will be deployed
 * as a BMP entity bean.
 */
package com.ysoft.jdo.book.sayings.service.entity;

import java.util.*;

import javax.ejb.*;
import javax.jdo.*;
import javax.naming.*;
import javax.resource.ResourceException;
import javax.resource.cci.ConnectionFactory;
import javax.transaction.Status; // debugging only
import javax.transaction.Synchronization; // debugging only

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.common.ejb.EJBHelper;
import com.ysoft.jdo.book.factory.*;
import com.ysoft.jdo.book.sayings.persistent.*;
import com.ysoft.jdo.book.sayings.persistent.QuoteManager.QuoteManagerOID;
import com.ysoft.jdo.book.sayings.service.*;


/**
 * The QuoteServerEJB entity bean is a coarse grained object that
 * aggregates several objects in the database.
 * It is a singleton like the QuoteManager that it includes, and all the
 * Quotes in the database are contained within it.
 */
public class QuoteServerEJB implements javax.ejb.EntityBean, QuoteServer
   {
   private EntityContext      context;
   private ConnectionFactory  cFactory;
   private PersistenceManager pm;
   private Query              query;
   private QuoteManager       qm;

   public Integer ejbCreate()
         throws CreateException
      {
      msg("ejbCreate called");

      PersistenceManager persistenceManager = null;

      try
         {
         // get a persistence manager
         persistenceManager = getPersistenceManager();

         QuoteManagerOID key = new QuoteManagerOID(QuoteManager.getSingletonKey());

         // if we get a QuoteManager then we can't create one
         if (getQuoteManager(persistenceManager, false, key) != null)
            {
            throw new CreateException(
               "The one and only QuoteServer already exists");
            }

         // create the one and only QuoteManager
         QuoteManager qm = getQuoteManager(persistenceManager, true, key);

         // unlike most entity beans, we are mapping between the QuoteManagerOID
         // and the QuoteServer EJB primary key (an Integer).
         // Without the mapping, the value returned here would be the
         // JDO identity object, in this case, the QuoteManagerOID key.
         return key.toInteger();
         }
      catch (CreateException e)
         {
         throw e;
         }
      catch (ResourceException e)
         {
         throw new CreateException("Caught exception in ejbCreate: " + e);
         }

      /*
      catch (Exception e)
         {
         throw new CreateException("Caught exception in ejbCreate: " + e);
         }
      */
      finally
         {
         if (persistenceManager != null)
            persistenceManager.close();
         }
      }

   public void ejbPostCreate()
      {
      // do nothing
      }

   public void ejbRemove()
         throws RemoveException
      {
      msg("ejbRemove called");
      throw new RemoveException("remove not allowed");
      }

   public Integer ejbFindByPrimaryKey(Integer key)
         throws FinderException
      {
      msg("ejbFindByPrimaryKey called");

      QuoteManagerOID    oid = new QuoteManagerOID(key);

      PersistenceManager persistenceManager = null;

      try
         {
         // get a persistence manager
         persistenceManager = getPersistenceManager();

         // if we can't get the QuoteManager we can't find the QuoteServer
         if (getQuoteManager(persistenceManager, false, oid) == null)
            throw new ObjectNotFoundException(
               "can't find the only QuoteServer by key: " + oid +
               "; use Integer(" + QuoteManager.getSingletonKey() + ") for key");

         return oid.toInteger();
         }
      catch (ResourceException e)
         {
         throw new FinderException("Caught exception in ejbFindByPrimaryKey: " +
            e);
         }

      /*
      catch (Exception e)
         {
         throw new FinderException("Caught exception in ejbFindByPrimaryKey: " + e);
         }
      */
      finally
         {
         if (persistenceManager != null)
            persistenceManager.close();
         }
      }

   public void setEntityContext(EntityContext ctx)
      {
      msg("setEntityContext called");
      context = ctx;

      try
         {
         cFactory = JndiLocator.getCF("java:/jdoCF");
         }
      catch (NamingException e)
         {
         throw new EJBException("Unable to get ConnectionFactory using \"java:/jdoCF\" name: ",
            e);
         }
      }

   public void unsetEntityContext()
      {
      msg("unsetEntityContext called");
      cFactory    = null;
      context     = null;
      }

   public void ejbActivate()
      {
      msg("ejbActivate called");
      }

   public void ejbPassivate()
      {
      msg("ejbPassivate called");
      cleanup();
      }

   public void ejbLoad()
      {
      msg("ejbLoad called");
      }

   public void ejbStore()
      {
      msg("ejbStore called");
      cleanup();
      }

   public void addQuote(String quote, String source)
         throws QuoteServerException
      {
      msg("addQuote called");
      quote     = normalizeString(quote);
      source    = normalizeString(source);

      setup();
      JDOFactory.tellConfiguration(pm);

      Quote q = qm.newQuote(quote, source);
      pm.makePersistent(q);
      }

   public Quote getQuote()
         throws QuoteServerException
      {
      msg("getQuote called");
      setup();
      JDOFactory.tellConfiguration(pm);

      Quote quote = getQuote(pm, qm, query);

      /*
      // debugging
      if (quote.getIndex() == 3)
         throw new QuoteServerException("Bogus QuoteServerException for index 3");
      else if (quote.getIndex() == 7)
         throw new JDOException("Bogus JDOException for index 7");
      */
      return (Quote) EJBHelper.respond(quote);
      }

   private PersistenceManager getPersistenceManager()
         throws ResourceException
      {
      //msg("getting PersistenceManager");
      PersistenceManager persistenceManager = (PersistenceManager) cFactory.getConnection();

      //JDOFactory.tellConfiguration(persistenceManager);
      return persistenceManager;
      }

   private void msg(String m)
      {
      MsgCenter.putMsg("Entity BMP QuoteServer (" + hashCode() +
         getPMHashCode() + "): " + m);
      }

   private void msg(String m, Exception e)
      {
      MsgCenter.putException("Entity BMP QuoteServer (" + hashCode() +
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

   private void setup()
      {
      //msg("called setup");
      if (pm == null)
         {
         try
            {
            PersistenceManager tPM = getPersistenceManager();
            makeSyncTracks(tPM);
            query = getQuery(tPM);

            QuoteManagerOID oid = new QuoteManagerOID((Integer) context.getPrimaryKey());
            qm    = getQuoteManager(tPM, false, oid);
            pm    = tPM; // flags that setup complete
            }
         catch (ResourceException e)
            {
            throw new EJBException("Unable to get PM: ", e);
            }
         }
      }

   private void cleanup()
      {
      //msg("called cleanup");
      if (pm != null)
         {
         //msg("closing PM and cleaning up");
         // clear bean's references to persistent objects
         qm = null;

         // close the pm if not closed
         if (!pm.isClosed())
            pm.close();

         // clear bean's references to pm and its resources
         query    = null;
         pm       = null; // also the flag that cleanup has occurred
         }
      }

   // debugging only
   private void makeSyncTracks(PersistenceManager pm)
      {
      pm.currentTransaction().setSynchronization(new Synchronization()
            {
            public void beforeCompletion()
               {
               msg("Synchronization.beforeCompletion called");
               }

            public void afterCompletion(int status)
               {
               msg("Synchronization.afterCompletion called with status: " +
                  getStatusString(status));
               }

            private String getStatusString(int status)
               {
               String retv = null;

               switch (status)
                  {
                  case Status.STATUS_COMMITTED:
                     retv = "committed";
                     break;

                  case Status.STATUS_ROLLEDBACK:
                     retv = "rolledback";
                     break;

                  default:
                     retv = Integer.toString(status);
                     break;
                  }

               return retv;
               }
            });
      }

   private QuoteManager getQuoteManager(PersistenceManager pm,
      boolean createIfNone, QuoteManagerOID key)
      {
      //msg("getting QuoteManager");
      QuoteManager quoteManager = null;

      try
         {
         quoteManager = (QuoteManager) pm.getObjectById(key, true);
         }
      catch (JDODataStoreException e)
         {
         // do nothing here, creation follows next
         }

      // create it if it doesn't exist
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

         msg("returning quote: " + quote);
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
   }

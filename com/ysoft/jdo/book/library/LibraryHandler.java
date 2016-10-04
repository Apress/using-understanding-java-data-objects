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
package com.ysoft.jdo.book.library;

import java.io.*;
import java.util.*;

import javax.jdo.*;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.factory.JDOFactory;


/**
 * Provides the persistence aware actions on
 * the model of persistent library objects.
 */
public class LibraryHandler
   {
   private PersistenceManager pm;
   private Transaction        trans;
   private String             properties_file_name = "com/ysoft/jdo/book/library/factory.properties";

   public LibraryHandler()
      {
      JDOFactory.setVerbose(false);
      getPM();
      }

   public List find(Class pcClass, String filter)
      {
      Extent ext = pm.getExtent(pcClass, true);
      Query  q = pm.newQuery(pcClass);
      q.setCandidates(ext);
      q.setFilter(filter);
      return runQuery(q);
      }

   public List find(Class pcClass, String filter, String variables)
      {
      MsgCenter.putMsg("Find");
      MsgCenter.putMsg("   Class: " + pcClass.getName());
      MsgCenter.putMsg("   Filter: " + filter);
      MsgCenter.putMsg("   Variables: " + variables);

      Extent ext = pm.getExtent(pcClass, true);
      Query  q = pm.newQuery(pcClass);
      q.setCandidates(ext);
      q.setFilter(filter);
      q.declareVariables(variables);
      return runQuery(q);
      }
      
   public List find(
         Class pcClass, 
         String filter, 
         String variables, 
         String parameterDeclaration, 
         Object parameter)
      {
      MsgCenter.putMsg("Find");
      MsgCenter.putMsg("   Class: " + pcClass.getName());
      MsgCenter.putMsg("   Filter: " + filter);
      MsgCenter.putMsg("   Variables: " + variables);
      MsgCenter.putMsg("   Parameters: " + parameterDeclaration);
      
      Extent ext = pm.getExtent(pcClass, true);
      Query  q = pm.newQuery(pcClass);
      q.setCandidates(ext);
      q.setFilter(filter);
      if (variables != null)
         q.declareVariables(variables);
      if (parameterDeclaration != null)
         q.declareParameters(parameterDeclaration);
      return runQuery(q, parameter);
      }

   public List findAll(Class pcClass)
      {
      return findAll(pm.getExtent(pcClass, true));
      }

   public List findInCollection(Class pcClass, Collection c, String filter)
      {
      // return empty list if received a null Collection
      if (c == null)
         return new ArrayList();

      Query q = pm.newQuery(pcClass);
      q.setCandidates(c);
      q.setFilter(filter);
      return runQuery(q);
      }

   public List findInCollection(Class pcClass, Collection c, String filter,
      String variables)
      {
      // return empty list if received a null Collection
      if (c == null)
         return new ArrayList();

      Query q = pm.newQuery(pcClass);
      q.setCandidates(c);
      q.setFilter(filter);
      q.declareVariables(variables);
      return runQuery(q);
      }

   public void add(Object pcObject)
      {
      if (pcObject == null)
         return;

      MsgCenter.putMsg("   Adding: " + pcObject);
      pm.makePersistent(pcObject);
      }

   public void delete(Object pcObject)
      {
      if (pcObject == null)
         return;

      MsgCenter.putMsg("   Deleting: " + pcObject);
      pm.deletePersistent(pcObject);
      }

   public boolean startTransaction()
      {
      boolean retv = !trans.isActive();

      if (retv)
         trans.begin();

      return retv;
      }

   public boolean commitTransaction()
      {
      boolean retv = trans.isActive();

      if (retv)
         trans.commit();

      return retv;
      }

   public boolean rollbackTransaction()
      {
      boolean retv = trans.isActive();

      if (retv)
         trans.rollback();

      return retv;
      }

   public void getPM()
      {
      if (pm != null)
         {
         pm.close();
         JDOFactory.close();
         pm = null;
         }

      try
         {
         //JDOFactory.setVerbose(true);
         JDOFactory.useProperties(properties_file_name);
         }
      catch (IOException e)
         {
         MsgCenter.putException("Could not find factory.properties", e);
         throw new RuntimeException("Sorry, not properly configured");
         }

      PersistenceManagerFactory pmf = JDOFactory.getPersistenceManagerFactory();
      pm    = pmf.getPersistenceManager();

      // there's a bug in the JDO RI that causes the pm setting to be true
      //MsgCenter.putMsg("PM.IgnoreCache : " + pm.getIgnoreCache());
      //MsgCenter.putMsg("PMF.IgnoreCache: " + pmf.getIgnoreCache());
      //Query q = pm.newQuery();
      //MsgCenter.putMsg("Query.IgnoreCache: " + q.getIgnoreCache());
      trans = pm.currentTransaction();
      }

   public boolean[] getPMConfiguration()
      {
      boolean[]   retv = new boolean[7];

      Transaction trans = pm.currentTransaction();

      retv[0]    = trans.getOptimistic();
      retv[1]    = trans.getNontransactionalRead();
      retv[2]    = trans.getNontransactionalWrite();
      retv[3]    = trans.getRetainValues();
      retv[4]    = trans.getRestoreValues();

      // there's a bug in the JDO RI that causes the PM setting to be true
      // even when PMF is false.  The Query picks up the correct value.
      Query q = pm.newQuery();
      retv[5]    = q.getIgnoreCache();

      retv[6] = trans.isActive();

      return retv;
      }

   private List runQuery(Query q)
      {
      Collection c = (Collection) q.execute();
      ArrayList  r = new ArrayList();
      Iterator   iter = c.iterator();

      while (iter.hasNext())
         {
         r.add(iter.next());
         }

      q.close(c);
      return r;
      }
      
   private List runQuery(Query q, Object param)
      {
      Collection c = (Collection) q.execute(param);
      ArrayList  r = new ArrayList();
      Iterator   iter = c.iterator();

      while (iter.hasNext())
         {
         r.add(iter.next());
         }

      q.close(c);
      return r;
      }

   private List findAll(Extent ext)
      {
      List     results = new ArrayList();

      Iterator iter = ext.iterator();

      while (iter.hasNext())
         results.add(iter.next());

      ext.close(iter);

      return results;
      }
   }

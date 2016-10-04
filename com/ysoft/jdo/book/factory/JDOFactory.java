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
package com.ysoft.jdo.book.factory;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.jdo.*;
import javax.naming.*;

import com.ysoft.jdo.book.common.*;


/**
 * Handles establishing a connection, configuring
 * a PMF to the desired settings, and verifying
 * the PMF configuration.
 * <p>
 * This is a simple factory that assumes the application
 * will use only one configuration of the PersistenceManagerFactory.
 */
public class JDOFactory
   {
   // singleton factory
   private static PersistenceManagerFactory factory           = null;
   private static boolean                   isVerbose         = false;
   private static JDOFactoryAdaptor         factoryAdaptor;
   private static volatile boolean          already_told;
   private static Class                     lastClassReported;

   /*
    * Sets the verbose flag.  When verbose is turned on,
    * information is logged when the PersistenceManagerFactory
    * is initialized.
    *
    */
   public static synchronized void setVerbose(boolean flag)
      {
      isVerbose = flag;
      }

   /**
    * @return  the singleton persistence manager factory instance.
    */
   public static PersistenceManagerFactory getPersistenceManagerFactory()
      {
      checkInitialization();
      return factory;
      }

   /**
    * Returns a PersistenceManager with the factory configuration.
    */
   public static PersistenceManager getPersistenceManager()
      {
      checkInitialization();

      PersistenceManager pm = factory.getPersistenceManager();

      if (isVerbose && !already_told)
         {
         already_told = true;
         System.out.println("The PersistenceManager is " +
            ((pm instanceof Serializable) ? "" : "not ") + "serialiable");
         }

      return factory.getPersistenceManager();
      }

   public static synchronized void useConstruction(String adaptorClassName,
      boolean ignoreCache, boolean optimistic, boolean nontransactionalRead,
      boolean nontransactionalWrite, boolean retainValues, boolean restoreValues)
      {
      MsgCenter.putMsg("Using adaptor class: " + adaptorClassName);

      if (factory != null)
         throw new JDOUserException(
            "JDOFactory already initialized.  Close to re-initialize");

      loadAdaptor(adaptorClassName);

      factory.setIgnoreCache(ignoreCache);
      factory.setOptimistic(optimistic);
      factory.setNontransactionalRead(nontransactionalRead);
      factory.setNontransactionalWrite(nontransactionalWrite);
      factory.setRetainValues(retainValues);
      try
         {
         factory.setRestoreValues(restoreValues);
         }
      // workaround for TriActive JDO bug
      catch (JDOUnsupportedOptionException e)
         {
         MsgCenter.putMsg("This implementation does not permit setting the RestoreValues property");
         }
      }

   public static void useProperties(String propFileName)
         throws IOException
      {
      useProperties(loadProperties(propFileName));
      }

   public static Properties loadProperties(String propFileName)
         throws IOException
      {
      ClassLoader cl     = Thread.currentThread().getContextClassLoader();
      InputStream stream = cl.getResourceAsStream(propFileName);

      if (stream == null)
         throw new IOException("File not found: " + propFileName);

      Properties props = new Properties();
      props.load(stream);

      stream.close();

      if (isVerbose)
         props.list(System.out);

      return props;
      }

   public static synchronized void useProperties(Properties props)
      {
      if (factory != null)
         throw new JDOUserException(
            "JDOFactory already initialized.  Close to re-initialize");

      factory = JDOHelper.getPersistenceManagerFactory(props);

      // tell us all about this factory
      if (isVerbose)
         tellAll(factory, "JDOHelper.getPersistenceManagerFactory");
      }

   /**
    * Some PM factories require a close.  JDOFactory supports this only
    * for PMF that are obtained by construction.
    */
   public static synchronized void close()
      {
      if (factory != null)
         {
         try
            {
            if (factoryAdaptor != null)
               factoryAdaptor.close(factory);
            }
         finally
            {
            factory           = null;
            factoryAdaptor    = null;
            }
         }
      }

   /**
    * Send to the MsgCenter information on the PMF configuration.
    */
   public static void tellConfiguration(PersistenceManagerFactory pmf)
      {
      if (pmf == null)
         {
         MsgCenter.putMsg("Asked to report configuration of null PMF");
         return;
         }

      reportClass("The PMF class", pmf.getClass());

      try
         {
         MsgCenter.putMsg("PMF configuration: " + "Opt: " +
            pmf.getOptimistic() + ", NTR: " + pmf.getNontransactionalRead() +
            ", NTW: " + pmf.getNontransactionalWrite() + ", RtV: " +
            pmf.getRetainValues() + ", RsV: " + pmf.getRestoreValues() +
            ", IC: " + pmf.getIgnoreCache() + ", MT: " +
            pmf.getMultithreaded());
         }
      catch (JDOException e)
         {
         MsgCenter.putMsg("Caught exception examining PMF configuration");
         }
      }

   /**
    * Send to the MsgCenter information on the PMF configuration.
    */
   public static void tellConfiguration(PersistenceManager pm)
      {
      if (pm == null)
         {
         MsgCenter.putMsg("Asked to report configuration of null PM");
         return;
         }

      if (pm.isClosed())
         {
         MsgCenter.putMsg("Asked to report configuration of closed PM");
         return;
         }

      reportClass("The PM class", pm.getClass());

      try
         {
         Transaction tx = pm.currentTransaction();
         MsgCenter.putMsg("PM & TX configuration: " + "Active: " +
            tx.isActive() + ", Opt: " + tx.getOptimistic() + ", NTR: " +
            tx.getNontransactionalRead() + ", NTW: " +
            tx.getNontransactionalWrite() + ", RtV: " + tx.getRetainValues() +
            ", RsV: " + tx.getRestoreValues() + ", IC: " + pm.getIgnoreCache() +
            ", MT: " + pm.getMultithreaded());
         }
      catch (JDOException e)
         {
         MsgCenter.putMsg("Caught exception examining PM configuration");
         }
      }

   private static synchronized void reportClass(String header, Class c)
      {
      if (c != lastClassReported)
         {
         lastClassReported = c;

         if (c != null)
            MsgCenter.putMsg(header + ": " + c.getName());
         }
      }

   /**
    * Obtains a PMF by loading an adaptor which will construct it.
    */
   private static void loadAdaptor(String whichAdaptor)
      {
      if (factory == null)
         {
         boolean problems = false;

         try
            {
            Class aClass = Class.forName(whichAdaptor);
            factoryAdaptor    = (JDOFactoryAdaptor) aClass.newInstance();

            // create the persistence manager factory
            factory = factoryAdaptor.obtainPMF();

            // tell us all about this factory
            if (isVerbose)
               tellAll(factory, whichAdaptor);
            }
         catch (NoClassDefFoundError e)
            {
            MsgCenter.putException("missing class required by \"" +
               whichAdaptor + "\"", e);
            problems = true;
            }
         catch (ClassNotFoundException e)
            {
            MsgCenter.putException("did not find JDO implementation adaptor", e);
            problems = true;
            }
         catch (InstantiationException e)
            {
            MsgCenter.putException("could not construct JDO implementation adaptor",
               e);
            problems = true;
            }
         catch (IllegalAccessException e)
            {
            MsgCenter.putException("could not construct JDO implementation adaptor",
               e);
            problems = true;
            }
         catch (RuntimeException e)
            {
            MsgCenter.putException("error using JDO implementation adaptor", e);
            problems = true;
            }

         if (problems)
            {
            factory = null;
            throw new JDOFatalUserException(
               "Unable to obtain a PersistenceManagerFactory");
            }
         }
      }

   private static synchronized void checkInitialization()
      {
      if (factory == null)
         throw new JDOUserException("factory has not been initialized");
      }

   /**
    * print to standard out all available information.
    */
   private static void tellAll(PersistenceManagerFactory factory,
      String whichAdaptor)
      {
      // tell which adaptor we used
      MsgCenter.putMsg("Loaded factory adaptor: " + whichAdaptor);
      MsgCenter.putMsg("");

      // check on the options supported and unsupported by the factory
      String[] allopts =
      {
         "javax.jdo.option.TransientTransactional",
         "javax.jdo.option.NontransactionalRead",
         "javax.jdo.option.NontransactionalWrite",
         "javax.jdo.option.RetainValues", "javax.jdo.option.Optimistic",
         "javax.jdo.option.ApplicationIdentity",
         "javax.jdo.option.DatastoreIdentity",
         "javax.jdo.option.NonDurableIdentity", "javax.jdo.option.ArrayList",
         "javax.jdo.option.HashMap", "javax.jdo.option.Hashtable",
         "javax.jdo.option.LinkedList", "javax.jdo.option.TreeMap",
         "javax.jdo.option.TreeSet", "javax.jdo.option.Vector",
         "javax.jdo.option.Map", "javax.jdo.option.List",
         "javax.jdo.option.Array", "javax.jdo.option.NullCollection",
         "javax.jdo.query.JDOQL", "javax.jdo.option.ChangeApplicationIdentity",
      };

      HashSet  set = new HashSet(allopts.length);

      for (int x = 0; x < allopts.length; x++)
         set.add(allopts[x]);

      MsgCenter.putMsg("Supported JDO Options");

      if (factory == null)
         {
         MsgCenter.putMsg("Did not get a factory");
         return;
         }

      // get the options that are supported
      Collection options = factory.supportedOptions();

      if (options == null)
         {
         MsgCenter.putMsg(
            "The factory cannot determine the JDO options supported");
         }

      if (options != null)
         {
         Iterator iter = options.iterator();

         while (iter.hasNext())
            {
            String s = (String) iter.next();
            MsgCenter.putMsg("   " + s);

            if (!set.contains(s))
               MsgCenter.putMsg("      above option not standard");
            else
               set.remove(s);
            }

         MsgCenter.putMsg("Unsupported JDO Options");

         iter = set.iterator();

         while (iter.hasNext())
            {
            MsgCenter.putMsg("   " + ((String) iter.next()));
            }
         }

      // check on non-configurable options
      MsgCenter.putMsg("Non-configurable properties");

      // the try block required because the Beta 1 version of the
      // the reference implementation threw a null pointer exception
      try
         {
         Properties props = factory.getProperties();

         // implementation is not required to supply non-configurable
         // properties
         if (props != null)
            {
            Iterator iter = props.keySet().iterator();

            while (iter.hasNext())
               {
               Object obj = iter.next();
               MsgCenter.putMsg("   Key: " + obj + ", value: " +
                  props.get(obj));
               }
            }
         }
      catch (NullPointerException e)
         {
         MsgCenter.putMsg(
            "   Oops, PersistenceManagerFactory.getProperties() throws null pointer exception");
         }

      // check the default settings for the factory properties
      MsgCenter.putMsg("Initial PMF transaction settings");
      MsgCenter.putMsg("   Optimistic: " + factory.getOptimistic());
      MsgCenter.putMsg("   Non-trans read: " +
         factory.getNontransactionalRead());
      MsgCenter.putMsg("   Non-trans write: " +
         factory.getNontransactionalWrite());
      MsgCenter.putMsg("   RetainValues: " + factory.getRetainValues());
      try
         {
         MsgCenter.putMsg("   RestoreValues: " + factory.getRestoreValues());
         }
      // workaround for TriActive Beta 3 bug
      catch (JDOUnsupportedOptionException e)
         {
         MsgCenter.putMsg("   RestoreValues: This implementation does not support this option");
         }
      MsgCenter.putMsg("Connection information");
      MsgCenter.putMsg("   Connection driver: " +
         factory.getConnectionDriverName());
      MsgCenter.putMsg("   Connection factory: " +
         factory.getConnectionFactoryName());
      MsgCenter.putMsg("   Connection factory2: " +
         factory.getConnectionFactory2Name());
      MsgCenter.putMsg("   Connection URL: " + factory.getConnectionURL());
      MsgCenter.putMsg("   Connection UserName: " +
         factory.getConnectionUserName());
      MsgCenter.putMsg("Caching info");
      MsgCenter.putMsg("   Ignore Cache: " + factory.getIgnoreCache());
      MsgCenter.putMsg("Threading setting for PM's");
      MsgCenter.putMsg("   Multithreading turned on: " +
         factory.getMultithreaded());

      // get more info on JDBC drivers
      String dName = factory.getConnectionDriverName();

      if ((dName != null) && (dName.indexOf("jdbc") >= 0))
         {
         try
            {
            Class  dClass = (Class) Class.forName(dName);
            Driver driver = (Driver) dClass.newInstance();

            MsgCenter.putMsg("JDBC Driver: " + dName);
            MsgCenter.putMsg("   Major version: " + driver.getMajorVersion());
            MsgCenter.putMsg("   Minor version: " + driver.getMinorVersion());
            }
         catch (Exception e)
            {
            MsgCenter.putException("Exception trying to use JDBC driver", e);
            }
         }

      // determine whether this PMF can be serialized
      try
         {
         ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(
                  "temp.ser"));
         out.writeObject(factory);
         out.close();
         MsgCenter.putMsg("This PMF can be serialized");
         }
      catch (IOException e)
         {
         MsgCenter.putMsg("This PMF can NOT be serialized");
         }

      // determine whether this PMF implements referencable
      // objects that implement referenceable can be put into a jndi context
      // even if they are not serializable.
      try
         {
         if (factory instanceof Referenceable)
            {
            MsgCenter.putMsg("This PMF implements javax.naming.Referenceable");

            Referenceable rfactory = (Referenceable) factory;
            Reference     r = rfactory.getReference();
            MsgCenter.putMsg("   reference class name: " + r.getClassName());
            MsgCenter.putMsg("   factory class location: " +
               r.getFactoryClassLocation());
            MsgCenter.putMsg("   factory class name: " +
               r.getFactoryClassName());

            for (int x = 0;; x++)
               {
               RefAddr ra = r.get(x);

               if (ra == null)
                  break;

               if (x == 0)
                  MsgCenter.putMsg("   Listing addresses for the reference");

               if (ra instanceof StringRefAddr)
                  MsgCenter.putMsg("      StringRefAddr: " + ra.getType() +
                     "," + ra.getContent());
               else if (ra instanceof BinaryRefAddr)
                  MsgCenter.putMsg("      BinaryRefAddr: " + ra.getType());
               else
                  MsgCenter.putMsg("      RefAddr: " + ra.getType());
               }
            }
         else
            MsgCenter.putMsg(
               "This PMF does NOT implement javax.naming.Referenceable");
         }
      catch (Exception e)
         {
         }
      catch (NoClassDefFoundError err)
         {
         MsgCenter.putException("Class required to access as Referenceable", err);
         }
      }
   }

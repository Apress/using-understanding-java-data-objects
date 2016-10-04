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
package com.ysoft.jdo.book.common;

import java.io.IOException;
import java.util.*;


/**
 * This class implements a publish and subscribe mechanism
 * for message handling.  It is assumed that someone will
 * subscribe, and therefore, during application startup,
 * it will cache up to two dozen
 * messages while waiting for the first subscriber.
 */
public class MsgCenter
   {
   private static Vector     list              = new Vector();
   private static String     defaultMsgHandler;
   private static boolean    not_empty;
   private static final int  MAX_MESSAGES = 24;
   private static final List cache        = new ArrayList(MAX_MESSAGES + 1);
   private static boolean    cache_empty  = false;

   static
      {
      try
         {
         Application.loadProperties(MsgCenter.class);
         }
      catch (IOException e)
         {
         System.err.println(
            "Could not load com/ysoft/jdo/book/common/package.properties");

         // e.printStackTrace();
         }

      defaultMsgHandler = System.getProperty(
            "com.ysoft.jdo.book.common.defaultMsgHandler");

      if ((defaultMsgHandler != null) && (defaultMsgHandler.length() == 0))
         defaultMsgHandler = null;
      }

   public static void putMsg(String msg)
      {
      registerDefault();

      if (list.size() > 0)
         {
         emptyCache();

         Iterator iter = list.iterator();

         while (iter.hasNext())
            {
            MsgHandler h = (MsgHandler) iter.next();
            h.putMsg(msg);
            }
         }
      else
         {
         cache(msg);
         }
      }

   public static void putException(String msg, Throwable t)
      {
      registerDefault();

      if (list.size() > 0)
         {
         emptyCache();

         Iterator iter = list.iterator();

         while (iter.hasNext())
            {
            MsgHandler h = (MsgHandler) iter.next();
            h.putException(msg, t);
            }
         }
      else
         {
         cache(msg, t);
         }
      }

   public static void shutdown()
      {
      Iterator iter = list.iterator();

      try
         {
         while (iter.hasNext())
            {
            MsgHandler h = (MsgHandler) iter.next();

            h.shutdown();
            }
         }
      catch (NoSuchElementException e)
         {
         }
      }

   public static void register(MsgHandler h)
      {
      if (h != null)
         {
         list.add(h);
         not_empty = true;
         }
      }

   private static void registerDefault()
      {
      if (not_empty || (defaultMsgHandler == null))
         return;

      try
         {
         Class cl = Class.forName(defaultMsgHandler);
         cl.newInstance();
         }
      catch (ClassCastException e)
         {
         System.err.println(
            "property <com.ysoft.jdo.book.common.defaultMsgHandler> is not a MsgHandler");
         e.printStackTrace();
         }
      catch (ClassNotFoundException e)
         {
         System.err.println("default message handler: " + defaultMsgHandler +
            " not found");
         e.printStackTrace();
         }
      catch (InstantiationException e)
         {
         System.err.println("default message handler: " + defaultMsgHandler +
            " could not be instantiated");
         e.printStackTrace();
         }
      catch (IllegalAccessException e)
         {
         System.err.println("cannot instantiated default message handler: " +
            defaultMsgHandler);
         e.printStackTrace();
         }
      }

   private static void cache(String msg, Throwable t)
      {
      if (cache.size() < MAX_MESSAGES)
         cache.add(new MsgCacheEntry(msg, t));
      else if (cache.size() == MAX_MESSAGES)
         cache.add(new MsgCacheEntry("MsgCenter cache capacity exceeded!", null));
      }

   private static void cache(String msg)
      {
      cache(msg, null);
      }

   private static void emptyCache()
      {
      // prevent recursion when emptying the cache
      if (cache_empty)
         return;

      cache_empty = true;

      // empty the cache
      for (int x = 0; x < cache.size(); x++)
         {
         MsgCacheEntry entry = (MsgCacheEntry) cache.get(x);

         if (entry.t != null)
            putException(entry.msg, entry.t);
         else
            putMsg(entry.msg);
         }

      cache.clear();
      }

   private static class MsgCacheEntry
      {
      private String    msg;
      private Throwable t;

      MsgCacheEntry(String msg, Throwable t)
         {
         this.msg    = msg;
         this.t      = t;
         }
      }
   }

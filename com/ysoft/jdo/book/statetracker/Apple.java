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

import javax.jdo.InstanceCallbacks;
import javax.jdo.JDOHelper;

import com.ysoft.jdo.book.common.MsgCenter;


public class Apple implements Monitored, InstanceCallbacks
   {
   // The following is an unmanaged field to hold the string identity
   private String OIDString;

   // The following is a transient field that holds the apple's
   // monitor.  This allows the client to deal with apples, but
   // get the apple's monitor when it wants to exercise the monitor
   // instead.
   private Monitor monitor;

   // The following are JDO transient fields
   private String  transientName;
   private int     transientSize;
   private Date    transientPicked;
   private HashSet transientWorms;
   private Worm    transientHeadWorm;

   // The following are JDO transactional fields
   private String  transactionalName;
   private int     transactionalSize;
   private Date    transactionalPicked;
   private HashSet transactionalWorms;
   private Worm    transactionalHeadWorm;

   // The following are JDO persistent fields
   private String  persistentName;
   private int     persistentSize;
   private Date    persistentPicked;
   private HashSet persistentWorms;
   private Worm    persistentHeadWorm;

   // this constructor is also called by JDO
   private Apple()
      {
      monitor = new Monitor(this);
      }

   public Apple(AppleState state)
      {
      this();

      if (state == null)
         throw new IllegalArgumentException();

      setTransientState(state);
      setTransactionalState(state);
      setPersistentState(state);
      }

   public AppleState getTransientState()
      {
      AppleState state = new AppleState();

      state.name        = transientName;
      state.size        = transientSize;
      state.picked      = transientPicked;
      state.worms       = transientWorms;
      state.headWorm    = transientHeadWorm;

      return state;
      }

   public void setTransientState(AppleState newState)
      {
      if (newState == null)
         return;

      if ((transientName == null) || !transientName.equals(newState.name))
         {
         transientName = newState.name;

         //MsgCenter.putMsg("applying new transient name");
         }

      if (transientSize != newState.size)
         {
         transientSize = newState.size;

         //MsgCenter.putMsg("applying new transient size");
         }

      if ((transientPicked == null) ||
               !transientPicked.equals(newState.picked))
         {
         transientPicked = newState.picked;

         //MsgCenter.putMsg("applying new transient pick date");
         }

      if (newState.worms != null)
         {
         if (transientWorms == null)
            {
            transientWorms = new HashSet();
            transientWorms.addAll(newState.worms);

            //MsgCenter.putMsg("created new transient worm set");
            }
         else if (!transientWorms.equals(newState.worms))
            {
            transientWorms.clear();
            transientWorms.addAll(newState.worms);

            //MsgCenter.putMsg("applying new transient worm set");
            }
         }

      if (transientHeadWorm != newState.headWorm)
         {
         transientHeadWorm = newState.headWorm;

         //MsgCenter.putMsg("applying new transient head worm");
         }
      }

   public AppleState getTransactionalState()
      {
      AppleState state = new AppleState();

      state.name        = transactionalName;
      state.size        = transactionalSize;
      state.picked      = transactionalPicked;
      state.worms       = transactionalWorms;
      state.headWorm    = transactionalHeadWorm;

      return state;
      }

   public void setTransactionalState(AppleState newState)
      {
      if (newState == null)
         return;

      if ((transactionalName == null) ||
               !transactionalName.equals(newState.name))
         {
         transactionalName = newState.name;

         //MsgCenter.putMsg("applying new transactional name");
         }

      if (transactionalSize != newState.size)
         {
         transactionalSize = newState.size;

         //MsgCenter.putMsg("applying new transactional size");
         }

      if ((transactionalPicked == null) ||
               !transactionalPicked.equals(newState.picked))
         {
         transactionalPicked = newState.picked;

         //MsgCenter.putMsg("applying new transactional pick date");
         }

      if (newState.worms != null)
         {
         if (transactionalWorms == null)
            {
            transactionalWorms = new HashSet();
            transactionalWorms.addAll(newState.worms);

            //MsgCenter.putMsg("created new transactional worm set");
            }
         else if (!transactionalWorms.equals(newState.worms))
            {
            transactionalWorms.clear();
            transactionalWorms.addAll(newState.worms);

            //MsgCenter.putMsg("applying new transactional worm set");
            }
         }

      if (transactionalHeadWorm != newState.headWorm)
         {
         transactionalHeadWorm = newState.headWorm;

         //MsgCenter.putMsg("applying new transactional head worm");
         }
      }

   public AppleState getPersistentState()
      {
      AppleState state = new AppleState();

      state.name        = persistentName;
      state.size        = persistentSize;
      state.picked      = persistentPicked;
      state.worms       = persistentWorms;
      state.headWorm    = persistentHeadWorm;

      return state;
      }

   public void setPersistentState(AppleState newState)
      {
      if (newState == null)
         return;

      if ((persistentName == null) || !persistentName.equals(newState.name))
         {
         persistentName = newState.name;

         //MsgCenter.putMsg("applying new persistent name");
         }

      if (persistentSize != newState.size)
         {
         persistentSize = newState.size;

         //MsgCenter.putMsg("applying new persistent size");
         }

      if ((persistentPicked == null) ||
               !persistentPicked.equals(newState.picked))
         {
         persistentPicked = newState.picked;

         //MsgCenter.putMsg("applying new persistent pick date");
         }

      if (newState.worms != null)
         {
         if (persistentWorms == null)
            {
            persistentWorms = new HashSet();
            persistentWorms.addAll(newState.worms);

            //MsgCenter.putMsg("created new persistent worm set");
            }
         else if (!persistentWorms.equals(newState.worms))
            {
            persistentWorms.clear();
            persistentWorms.addAll(newState.worms);

            //MsgCenter.putMsg("applying new persistent worm set");
            }
         }

      if (persistentHeadWorm != newState.headWorm)
         {
         persistentHeadWorm = newState.headWorm;

         //MsgCenter.putMsg("applying new persistent head worm");
         }
      }

   // these two methods tickle the default fetch group
   public String getPersistentName()
      {
      return persistentName;
      }

   public int getPersistentSize()
      {
      return persistentSize;
      }

   public String captureIdentityString()
      {
      // this code assumes that each object's identity value cannot be changed
      // and therefore, it does not have to be recaptured if it has already been
      // captured.
      if (OIDString == null)
         {
         Object oid = JDOHelper.getObjectId(this);

         if (oid != null)
            OIDString = oid.toString();
         }

      return OIDString;
      }

   // the four methods required by the InstanceCallbacks interface
   public void jdoPostLoad()
      {
      captureIdentityString();
      MsgCenter.putMsg(getName() + " jdoPostLoad");
      }

   public void jdoPreClear()
      {
      MsgCenter.putMsg(getName() + " jdoPreClear");
      }

   public void jdoPreDelete()
      {
      OIDString = null;
      MsgCenter.putMsg(getName() + " jdoPreDelete");
      }

   public void jdoPreStore()
      {
      captureIdentityString();
      MsgCenter.putMsg(getName() + " jdoPreStore");
      }

   // Three methods required by the Monitored interface
   public boolean isClearPersistentState()
      {
      boolean retv = true;

      if ((persistentSize != 0) || (persistentName != null) ||
               (persistentPicked != null) || (persistentHeadWorm != null) ||
               (persistentWorms != null))
         {
         retv = false;
         }

      return retv;
      }

   public Monitor getMonitor()
      {
      return monitor;
      }

   public Object clone()
         throws CloneNotSupportedException
      {
      Apple apple = (Apple) super.clone();
      return apple;
      }

   // we want the default equality and hashcode methods of Object
   // because we want a transient object to be different from a
   // persistent object
   public String toString()
      {
      return getName();
      }

   private String getName()
      {
      if (OIDString != null)
         return OIDString + " [JVM ID:" + System.identityHashCode(this) + "]";

      try
         {
         // there is no OID, so return one of the other names
         Apple  apple    = (Apple) clone();
         String tempName = null;

         if (apple.transientName != null)
            tempName = "Apple transientName: " + apple.transientName;

         if ((tempName == null) && (apple.persistentName != null))
            tempName = "Apple persistentName: " + apple.persistentName;

         if ((tempName == null) && (apple.transactionalName != null))
            tempName = "Apple transactionalName: " + apple.transactionalName;

         if (tempName != null)
            return tempName;
         }
      catch (Exception e)
         {
         MsgCenter.putMsg("");
         MsgCenter.putException("can't read managed name", e);
         return "Apple unavailable name";
         }

      return "Apple with no name [JVM ID:" + System.identityHashCode(this) +
      "]";
      }
   }

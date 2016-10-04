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

import javax.jdo.*;

import com.ysoft.jdo.book.common.MsgCenter;


public class Monitor
   {
   private static final String JDO_TRANSIENT                   = "transient";
   private static final String JDO_TRANSIENT_CLEAN             = "transient-clean";
   private static final String JDO_TRANSIENT_DIRTY             = "transient-dirty";
   private static final String JDO_PERSISTENT_CLEAN            = "persistent-clean";
   private static final String JDO_PERSISTENT_DIRTY            = "persistent-dirty";
   private static final String JDO_PERSISTENT_DELETED          = "persistent-deleted";
   private static final String JDO_PERSISTENT_NEW              = "persistent-new";
   private static final String JDO_PERSISTENT_NEW_DELETED      = "persistent-new-deleted";
   private static final String JDO_PERSISTENT_NONTRANSACTIONAL = "persistent-nontransactional";
   private static final String JDO_HOLLOW                      = "hollow";
   private Monitored           jdoObject;

   public Monitor(Monitored jdoObject)
      {
      if (jdoObject == null)
         throw new IllegalArgumentException(
            "the jdoObject object cannot be null");

      if (!(jdoObject instanceof Monitored))
         throw new IllegalArgumentException(
            "the jdoObject must implement the Monitored interface");

      this.jdoObject = (Monitored) jdoObject;
      }

   public String getManagementState()
      {
      // determine various flags
      boolean isDirty         = JDOHelper.isDirty(jdoObject);
      boolean isNew           = JDOHelper.isNew(jdoObject);
      boolean isTransactional = JDOHelper.isTransactional(jdoObject);
      boolean isPersistent    = JDOHelper.isPersistent(jdoObject);
      boolean isDeleted       = JDOHelper.isDeleted(jdoObject);

      String  jdoState = null;

      if (!isPersistent && !isTransactional && !isDirty && !isNew &&
               !isDeleted)
         jdoState = JDO_TRANSIENT;
      else if (!isPersistent && isTransactional && !isDirty && !isNew &&
               !isDeleted)
         jdoState = JDO_TRANSIENT_CLEAN;
      else if (!isPersistent && isTransactional && isDirty && !isNew &&
               !isDeleted)
         jdoState = JDO_TRANSIENT_DIRTY;
      else if (isPersistent && isTransactional && isDirty && isNew &&
               !isDeleted)
         jdoState = JDO_PERSISTENT_NEW;
      else if (isPersistent && !isTransactional && !isDirty && !isNew &&
               !isDeleted)
         {
         try
            {
            Monitored cx = (Monitored) jdoObject.clone();

            if (cx.isClearPersistentState())
               jdoState = JDO_HOLLOW;
            else
               jdoState = JDO_PERSISTENT_NONTRANSACTIONAL;
            }
         catch (Exception e)
            {
            jdoState = "caught exception: " + e.getMessage();
            MsgCenter.putMsg("");
            MsgCenter.putException("caught exception comparing persistent state",
               e);
            }
         }
      else if (isPersistent && isTransactional && !isDirty && !isNew &&
               !isDeleted)
         jdoState = JDO_PERSISTENT_CLEAN;
      else if (isPersistent && isTransactional && isDirty && !isNew &&
               !isDeleted)
         jdoState = JDO_PERSISTENT_DIRTY;
      else if (isPersistent && isTransactional && isDirty && !isNew &&
               isDeleted)
         jdoState = JDO_PERSISTENT_DELETED;
      else if (isPersistent && isTransactional && isDirty && isNew &&
               isDeleted)
         jdoState = JDO_PERSISTENT_NEW_DELETED;
      else
         {
         jdoState = "Unrecognized state: " + (isPersistent ? "" : "!") +
            "persistent, " + (isTransactional ? "" : "!") + "transactional, " +
            (isDirty ? "" : "!") + "dirty, " + (isNew ? "" : "!") + "new, " +
            (isDeleted ? "" : "!") + "deleted";
         }

      return jdoState;
      }
   }

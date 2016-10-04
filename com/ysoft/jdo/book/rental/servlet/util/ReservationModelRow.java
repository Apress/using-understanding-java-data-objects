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
David Ezzio       10/01/02   Created
*/
package com.ysoft.jdo.book.rental.servlet.util;

import java.io.Serializable;
import java.util.*;

import com.ysoft.jdo.book.rental.persistent.*;


public class ReservationModelRow implements Serializable
   {
   private ArrayList nodes;
   private Week      week;

   public ReservationModelRow(int numberColumns, Week w)
      {
      if ((numberColumns < 0) || (w == null))
         throw new IllegalArgumentException();

      week    = w;

      // initialize the list with empty nodes
      nodes = new ArrayList(numberColumns);

      for (int x = 0; x < numberColumns; x++)
         nodes.add(new ReservationModelNode());
      }

   public List getNodes()
      {
      return nodes;
      }

   public String getWeekString()
      {
      return week.getStartOfWeekString();
      }

   ReservationModelNode getNode(int index)
      {
      return (ReservationModelNode) nodes.get(index);
      }

   void addDataObjects(Collection container)
      {
      if (week != null)
         container.add(week);

      if (nodes != null)
         {
         Iterator iter = nodes.iterator();

         while (iter.hasNext())
            {
            ReservationModelNode node = (ReservationModelNode) iter.next();
            node.addDataObjects(container);
            }
         }
      }
   }

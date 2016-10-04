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

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.rental.persistent.*;


public class ReservationModelNode implements Serializable
   {
   private Rental rental;
   private String id;

   /**
    * No-args constructor that initializes an
    * empty node.
    */
   public ReservationModelNode()
      {
      }

   /**
    *
    */
   public static String makeNodeID(int rowIndex, int colIndex)
      {
      return "Node:" + rowIndex + "-" + colIndex;
      }

   public static int getRowIndex(String nodeID)
      {
      int retv = 0;

      int hyphen_pos = getHyphenPos(nodeID);

      if (hyphen_pos <= 5)
         hyphen_pos = -1;
      else
         {
         String part = nodeID.substring(5, hyphen_pos);

         try
            {
            retv = Integer.parseInt(part);
            }
         catch (RuntimeException e)
            {
            hyphen_pos = -1;
            }
         }

      if (hyphen_pos < 0)
         throw new IllegalArgumentException("not a nodeID: " + nodeID);

      return retv;
      }

   public static int getColIndex(String nodeID)
      {
      int retv = 0;

      int hyphen_pos = getHyphenPos(nodeID);

      if (hyphen_pos <= 5)
         hyphen_pos = -1;
      else
         {
         String part = nodeID.substring(hyphen_pos + 1);

         try
            {
            retv = Integer.parseInt(part);
            }
         catch (RuntimeException e)
            {
            hyphen_pos = -1;
            }
         }

      if (hyphen_pos < 0)
         throw new IllegalArgumentException("not a nodeID: " + nodeID);

      return retv;
      }

   private static int getHyphenPos(String nodeID)
      {
      int    hyphen_pos = 0;

      String part = nodeID.substring(0, 5);

      if (!part.equalsIgnoreCase("Node:"))
         hyphen_pos = -1;

      if (hyphen_pos == 0)
         hyphen_pos = nodeID.indexOf('-');

      //MsgCenter.putMsg("getHyphenPos returning: " + hyphen_pos);
      return hyphen_pos;
      }

   /**
    * This method initializes a node with its rental
    * information and its node ID.
    */
   public void initialize(Rental r, int rowIndex, int colIndex)
      {
      if ((r == null) || (rowIndex < 0) || (colIndex < 0))
         throw new IllegalArgumentException();

      rental    = r;
      id        = makeNodeID(rowIndex, colIndex);
      }

   /**
    * Return the node's id string, or "null" if this node does not
    * have rental information associated with it.
    */
   public String getId()
      {
      if (id != null)
         return id;
      else
         return "null";
      }

   /**
    * Return true if this node has rental information associated with
    * it, and false otherwise.
    */
   public boolean isModifiable()
      {
      return rental != null;
      }

   /**
    * Return true if this node has rental information associated with
    * it, and the rental is not reserved.
    */
   public boolean isAvailable()
      {
      if (rental != null)
         return rental.isAvailable();
      else
         return false;
      }

   /**
    * Return the price for renting this node, or "null" if
    * there is no rental information associated with this node.
    */
   public String getPriceString()
      {
      if (rental != null)
         return "$" + rental.getPrice().toString();
      else
         return "null";
      }

   void addDataObjects(Collection container)
      {
      if (rental != null)
         {
         container.add(rental);

         Customer c = rental.getCustomer();

         if (c != null)
            container.add(c);
         }
      }

   Rental getRental()
      {
      return rental;
      }
   }

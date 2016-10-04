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

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import javax.servlet.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.servlet.service.ReservationService;


public class ReservationModel implements Serializable,
   ReservationServletConstants
   {
   private static final int  NUM_WEEKS            = 16; // initial estimate
   private static final long VIEW_EXPIRATION_TIME = 1000L * 60L * 10L; // 10 minutes
   private int               numRows;
   private int               numCols;
   private ArrayList         rows;
   private List              lighthouses;
   private int               view                 = VIEW_AVAILABLE;
   private Customer          customer;
   private int               stepCount;

   //private long            timeViewSet;

   /**
    * The constructor that the controller uses.  The value of the flag is not used.
    */
   public ReservationModel(boolean flag)
      {
      }

   /**
    * Provided for bean compatibility, but in fact the model should not be constructed
    * by the JSP page but rather by the controller.  If called, this constructor throws
    * an IllegalStateException.
    */
   public ReservationModel()
      {
      throw new IllegalStateException(
         "The model should be created by the controller");
      }

   /**
    * Initializes the model with the list of rentals that will be
    * displayed.
    */
   public void initRentals(List rentals, List lighthouses)
      {
      if ((rentals == null) || (lighthouses == null))
         throw new NullPointerException();

      numCols = lighthouses.size();

      /*
      // debugging
      MsgCenter.putMsg("Got " + numCols + " lighthouses");
      for (int x = 0; x < numCols; x++)
         MsgCenter.putMsg("   " + lighthouses.get(x));
      */

      // get the list of weeks in this rental list
      LinkedList listOfWeeks = new LinkedList();
      Week       lastWeek = null;

      for (int x = 0; x < rentals.size(); x++)
         {
         Rental r = (Rental) rentals.get(x);

         //MsgCenter.putMsg("ReservationModel: rental (" + r + ")");
         Week w = r.getWeek();

         if (lastWeek != w)
            {
            lastWeek = w;
            listOfWeeks.add(w);
            }
         }

      numRows    = listOfWeeks.size();
      rows       = new ArrayList(numRows);

      // initialize the list of rows with empty nodes
      for (int x = 0; x < numRows; x++)
         rows.add(new ReservationModelRow(numCols, (Week) listOfWeeks.get(x)));

      // initialize the list of lighthouses
      this.lighthouses = lighthouses;

      //MsgCenter.putMsg("Got " + rentals.size() + " rentals to put into the model");
      //MsgCenter.putMsg("Got " + numRows + " weeks to put into the model");
      // initialize some nodes with rental information
      for (int x = 0; x < rentals.size(); x++)
         {
         Rental r                = (Rental) rentals.get(x);
         int    week_index       = listOfWeeks.indexOf(r.getWeek());
         int    lighthouse_index = lighthouses.indexOf(r.getLighthouse());

         //MsgCenter.putMsg("ReservationModel: rental (" + r + ")");
         if ((week_index < 0) || (week_index > numRows) ||
                  (lighthouse_index < 0) || (lighthouse_index > numCols))
            {
            MsgCenter.putMsg(
               "Unexpected index in constructing reservation model: Lighthouse " +
               lighthouse_index + ", week " + week_index);
            }
         else
            {
            ReservationModelRow  row  = (ReservationModelRow) rows.get(week_index);
            ReservationModelNode node = row.getNode(lighthouse_index);
            node.initialize(r, week_index, lighthouse_index);
            }
         }
      }

   /**
    * Returns the list of ModelRows in the column.
    */
   public List getModelRows()
      {
      return rows;
      }

   /**
    * Returns the number of rows (weeks) in the
    * the model.
    */
   public int getNumRows()
      {
      return numRows;
      }

   /**
    * Returns the number of columns (rental units)
    * in the model.
    */
   public int getNumColumns()
      {
      return numCols;
      }

   /**
    * Returns a header for each reservation model column (lighthouse).
    */
   public List getLighthouses()
      {
      return lighthouses;
      }

   /**
    * Returns a list of all Rental objects where the
    * reservation needs to be flipped.  If reserved, it should
    * be cancelled, and if available it should be reserved.
    * These Rental objects are unmanaged by JDO.
    */
   public Collection getModifiedRentals(String[] reservationIDs)
      {
      if (reservationIDs == null)
         reservationIDs = new String[0];

      /*
      //debugging
      for (int x = 0; x < reservationIDs.length; x++)
         {
         MsgCenter.putMsg("Got from browser, reservation ID: " + reservationIDs[x]);
         }
      */

      // the list of all Rental objects where reserved status has been altered
      HashSet modList = new HashSet();

      // get the model nodes of all known reserved rentals
      Collection knownReserved = getReservedRentalNodes();
      Iterator   iter = knownReserved.iterator();

      while (iter.hasNext())
         {
         ReservationModelNode node = (ReservationModelNode) iter.next();

         // if the node's id is on the list of reserved rentals
         // sent from the browser, then it hasn't changed.
         // So, remove it.  Otherwise, it has been changed, so
         // add the rental object to the modList.
         if (!remove(node.getId(), reservationIDs))
            {
            //MsgCenter.putMsg("ReservationModel: adding cancellation at node " + node.getId() +
            //      " to the modified list");
            modList.add(node.getRental());
            }
         }

      // any reserved rentals still on the list sent from the browser
      // have also been changed, since they are not reserved in the
      // model.  So add them as well to the modList.
      for (int x = 0; x < reservationIDs.length; x++)
         {
         if (reservationIDs[x] == null) // was removed above


            continue;

         Rental               rental  = null;
         String               node_id = null;
         ReservationModelNode node    = getNode(reservationIDs[x]);

         if (node != null)
            {
            rental     = node.getRental();
            node_id    = node.getId();
            }

         if (rental != null)
            {
            //MsgCenter.putMsg("ReservationModel: adding new reservation at node " + node_id +
            //      " to the modified list");
            modList.add(rental);
            }
         else
            throw new IllegalStateException(
               "Unexpected result in ReservationModel.getModifiedRentals for node" +
               node_id);
         }

      return modList;
      }

   /**
    * Returns a collection of all the objects that are
    * instances of application data classes.  Some of these
    * objects may be persistent, and some may be unmanaged.
    */
   public Collection getDataObjects()
      {
      HashSet retv = new HashSet();

      if (lighthouses != null)
         retv.addAll(lighthouses);

      if (customer != null)
         retv.add(customer);

      if (rows != null)
         {
         Iterator iter = rows.iterator();

         while (iter.hasNext())
            {
            ReservationModelRow row = (ReservationModelRow) iter.next();
            row.addDataObjects(retv);
            }
         }

      return retv;
      }

   /**
    * Sets the view.
    */
   public void setView(int v)
      {
      if (!Verifier.isAcceptableView(v))
         v = VIEW_AVAILABLE;

      view = v;

      //timeViewSet = System.currentTimeMillis();
      }

   public int getView()
      {
      return view;
      }

   public boolean isViewAvailable()
      {
      return view == VIEW_AVAILABLE;
      }

   public boolean isViewCustomer()
      {
      return view == VIEW_CUSTOMER;
      }

   public boolean isViewBoth()
      {
      return view == VIEW_BOTH;
      }

   public Customer getCustomer()
      {
      return customer;
      }

   public void setCustomer(Customer c)
      {
      // if moving from known to unknown customer
      // then change view and clear the action cache
      if ((customer != null) && (c == null))
         {
         view = VIEW_AVAILABLE;

         //clearActionCache();
         }

      customer = c;
      }

   public String getCustomerName()
      {
      if (customer != null)
         return customer.getName();
      else
         return NO_CUSTOMER_STRING;
      }

   public boolean isCustomerKnown()
      {
      return customer != null;
      }

   public int getStep()
      {
      return stepCount;
      }

   public void bumpStep()
      {
      stepCount++;
      }

   public boolean isCurrentStep(int step)
      {
      return step == stepCount;
      }

   public boolean isCurrentStep(String step)
      {
      boolean retv = false;

      if (step != null)
         {
         int s = -1;

         try
            {
            s = Integer.parseInt(step);
            }
         catch (Exception ignore)
            {
            }

         retv = isCurrentStep(s);
         }

      return retv;
      }

   private ReservationModelNode getNode(String id)
      {
      int rowIndex = ReservationModelNode.getRowIndex(id);
      int colIndex = ReservationModelNode.getColIndex(id);

      //MsgCenter.putMsg("getNode, row: " + rowIndex + ", col: " + colIndex + ", numRows: " + getNumRows());
      ReservationModelRow row = (ReservationModelRow) rows.get(rowIndex);

      if (row == null)
         throw new ArrayIndexOutOfBoundsException("indexing model row " +
            rowIndex + " that does not exist");

      ReservationModelNode node = row.getNode(colIndex);

      if (node == null)
         throw new ArrayIndexOutOfBoundsException("indexing model column " +
            colIndex + " that does not exist");

      return node;
      }

   private Collection getReservedRentalNodes()
      {
      HashSet returnSet = new HashSet();

      for (int x = 0; x < rows.size(); x++)
         {
         ReservationModelRow row = (ReservationModelRow) rows.get(x);

         List                listNodes = row.getNodes();

         for (int y = 0; y < listNodes.size(); y++)
            {
            ReservationModelNode node = (ReservationModelNode) listNodes.get(y);

            Rental               rental = node.getRental();

            if ((rental != null) && (rental.getCustomer() != null))
               returnSet.add(node);
            }
         }

      return returnSet;
      }

   private boolean remove(String nodeID, String[] arrayNodeIDs)
      {
      boolean retv = false;

      for (int x = 0; x < arrayNodeIDs.length; x++)
         {
         if (nodeID.equalsIgnoreCase(arrayNodeIDs[x]))
            {
            arrayNodeIDs[x]    = null;
            retv               = true;

            // we expect to find no more than one entry on the array
            break;
            }
         }

      return retv;
      }

   // debugging

   /*
   private void writeObject(java.io.ObjectOutputStream stream) throws IOException
      {
      MsgCenter.putMsg("ReservationModel: being serialized");

      try
         {
         // default serialization action
         stream.defaultWriteObject();
         }
      catch (IOException e)
         {
         MsgCenter.putException("caught during model serialization", e);
         throw e;
         }
      catch (RuntimeException e)
         {
         MsgCenter.putException("caught during model serialization", e);
         throw e;
         }
      }
   */
   }

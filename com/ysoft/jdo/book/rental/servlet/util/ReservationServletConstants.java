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

public interface ReservationServletConstants
   {
   public static final String MODEL_NAME                 = "model";
   public static final String VIEW_AVAILABLE_STRING      = "available";
   public static final String VIEW_CUSTOMER_STRING       = "customer";
   public static final String VIEW_BOTH_STRING           = "both";
   public static final String CUSTOMER_PARAM             = "customer";
   public static final String MAINE_PAGE                 = "/maine.jsp";
   public static final String LIGHTHOUSE_PAGE            = "/lighthouse.jsp";
   public static final String RESERVATION_PARAM          = "reservation";
   public static final String CONCURRENCY_EXCEPTION_PAGE = "/concurrency_exception.jsp";
   public static final String NO_CUSTOMER_STRING         = "unknown";
   public static final String STEP_PARAM                 = "step";
   public static final String EXCEPTION_PAGE             = "/exception.jsp";

   // servlet actions
   public static final int VIEW_UNCHANGED         = -1;
   public static final int NO_ACTION              = SessionLock.NO_ACTION;
   public static final int VIEW_AVAILABLE         = 1;
   public static final int VIEW_CUSTOMER          = 2;
   public static final int VIEW_BOTH              = 3;
   public static final int VIEW_LIGHTHOUSE        = 4;
   public static final int MODIFY_RESERVATIONS    = 5;
   public static final int IDENTIFY_CUSTOMER      = 6;
   public static final int REFRESH                = 7;
   public static final int DISPLAY_VIEW_AVAILABLE = VIEW_AVAILABLE +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_VIEW_CUSTOMER = VIEW_CUSTOMER +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_VIEW_BOTH = VIEW_BOTH +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_VIEW_LIGHTHOUSE = VIEW_LIGHTHOUSE +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_MODIFY_RESERVATIONS = MODIFY_RESERVATIONS +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_IDENTIFY_CUSTOMER = IDENTIFY_CUSTOMER +
      SessionLock.DISPLAY_ACTION_LINE;
   public static final int DISPLAY_REFRESH = REFRESH +
      SessionLock.DISPLAY_ACTION_LINE;

   // used primarily for debugging
   public static class Formatter
      {
      public static String getString(int action)
         {
         String string = null;

         switch (action)
            {
            case VIEW_UNCHANGED:
               string = "VIEW_UNCHANGED";
               break;

            case NO_ACTION:
               string = "NO_ACTION";
               break;

            case VIEW_AVAILABLE:
               string = "VIEW_AVAILABLE";
               break;

            case VIEW_CUSTOMER:
               string = "VIEW_CUSTOMER";
               break;

            case VIEW_BOTH:
               string = "VIEW_BOTH";
               break;

            case VIEW_LIGHTHOUSE:
               string = "VIEW_LIGHTHOUSE";
               break;

            case MODIFY_RESERVATIONS:
               string = "MODIFY_RESERVATIONS";
               break;

            case IDENTIFY_CUSTOMER:
               string = "IDENTITY_CUSTOMER";
               break;

            case REFRESH:
               string = "REFRESH";
               break;

            case DISPLAY_VIEW_AVAILABLE:
               string = "DISPLAY_VIEW_AVAILABLE";
               break;

            case DISPLAY_VIEW_CUSTOMER:
               string = "DISPLAY_VIEW_CUSTOMER";
               break;

            case DISPLAY_VIEW_BOTH:
               string = "DISPLAY_VIEW_BOTH";
               break;

            case DISPLAY_VIEW_LIGHTHOUSE:
               string = "DISPLAY_VIEW_LIGHTHOUSE";
               break;

            case DISPLAY_MODIFY_RESERVATIONS:
               string = "DISPLAY_MODIFY_RESERVATIONS";
               break;

            case DISPLAY_IDENTIFY_CUSTOMER:
               string = "DISPLAY_IDENTIFY_CUSTOMER";
               break;

            case DISPLAY_REFRESH:
               string = "DISPLAY_REFRESH";
               break;

            default:
               string = "UNKNOWN ACTION";
               break;
            }

         return string;
         }
      }

   public static class Verifier
      {
      public static boolean isAcceptableView(int v)
         {
         switch (v)
            {
            case VIEW_AVAILABLE:
            case VIEW_CUSTOMER:
            case VIEW_BOTH:
               return true;
            }

         return false;
         }
      }
   }

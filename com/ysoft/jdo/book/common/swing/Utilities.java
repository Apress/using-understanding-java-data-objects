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
package com.ysoft.jdo.book.common.swing;

import java.awt.*;


public class Utilities
   {
   public static final void centerOnParent(Window window)
      {
      Dimension screen_size   = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension window_size   = window.getSize();
      Rectangle parent_bounds = null;

      Component parent = window.getParent();

      if (parent != null)
         parent_bounds = parent.getBounds();

      if (parent_bounds != null)
         {
         // check that our parent bounds are not blank, which they might be
         // if we get the Swing default owner frame (see SwingUtilities.getSharedOwner)
         if ((parent_bounds.width == 0) || (parent_bounds.height == 0))
            parent_bounds = null;
         }

      centerOnParent(window, window_size, screen_size, parent_bounds);
      }

   public static final void centerOnScreen(Window window)
      {
      Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension window_size = window.getSize();

      centerOnParent(window, window_size, screen_size, null);
      }

   private static final void centerOnParent(Component window,
      Dimension child_size, Dimension screen_size, Rectangle parent_bounds)
      {
      int center_x = 0;
      int center_y = 0;

      // first calc the center point
      if (parent_bounds == null)
         {
         center_x    = screen_size.width / 2;
         center_y    = screen_size.height / 2;
         }
      else
         {
         center_x    = parent_bounds.x + (parent_bounds.width / 2);
         center_y    = parent_bounds.y + (parent_bounds.height / 2);
         }

      // now calculate the origin of the child relative to the center point
      int child_x = 0;
      int child_y = 0;

      child_x    = center_x - (child_size.width / 2);
      child_y    = center_y - (child_size.height / 2);

      // now adjust if window runs off the window
      if ((child_x + child_size.width) > screen_size.width)
         {
         child_x -= ((child_x + child_size.width) - screen_size.width);
         }

      if ((child_y + child_size.height) > screen_size.height)
         {
         child_y -= ((child_y + child_size.height) - screen_size.height);
         }

      // now adjust if window origin is off screen
      if (child_x < 0)
         child_x = 0;

      if (child_y < 0)
         child_y = 0;

      // set the location
      window.setLocation(child_x, child_y);
      }
   }

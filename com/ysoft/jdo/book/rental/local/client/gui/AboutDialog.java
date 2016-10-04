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
package com.ysoft.jdo.book.rental.local.client.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.border.*;

import com.ysoft.jdo.book.common.exceptions.WrappingException;
import com.ysoft.jdo.book.common.swing.*;


public class AboutDialog extends JDialog
   {
   private ReservationClient   rc;
   private Container           adContentPane;
   private JPanel              adMainPanel;
   private NonEditableTextArea adTextArea;
   private String              heading;
   private String              text;

   public AboutDialog(ReservationClient rc, String heading, String text)
      {
      super(rc, true);
      this.rc         = rc;
      this.heading    = heading;
      this.text       = text;
      }

   public void init()
      {
      setTitle(heading);
      setResizable(false);

      // create the main panel
      adMainPanel    = new JPanel(new BorderLayout());
      adTextArea     = new NonEditableTextArea(3, 20);
      adTextArea.setText(text);
      adMainPanel.add(adTextArea, BorderLayout.CENTER);

      // set up the content pane
      adContentPane = getContentPane();
      adContentPane.add(adMainPanel, BorderLayout.CENTER);

      pack();
      Utilities.centerOnParent(this);
      }
   }

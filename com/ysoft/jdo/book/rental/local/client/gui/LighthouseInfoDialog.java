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


public class LighthouseInfoDialog extends JDialog
   {
   private static final int NO_MODIFIERS          = 0;
   ReservationClient        rc;
   String                   lighthouseName;
   String                   imageFileName;
   String                   lighthouseDescription;
   Container                cidContentPane;
   JPanel                   cidMainPanel;
   ImageJPanel              cidImagePanel;
   JLabel                   cidInfoLabel;

   public LighthouseInfoDialog(ReservationClient rc, String lighthouseName,
      String lighthouseDesc, String imageFileName)
      {
      super(rc, true);
      this.rc                       = rc;
      this.lighthouseName           = lighthouseName;
      this.imageFileName            = imageFileName;
      this.lighthouseDescription    = lighthouseDesc;
      }

   public void init()
         throws DialogException
      {
      // read the image
      Image infoImage = readImage("images/" + imageFileName + ".bmp");

      setTitle("More info on " + lighthouseName);
      setResizable(false);

      // create the main panel
      cidMainPanel    = new JPanel(new BorderLayout());

      cidInfoLabel = new JLabel(lighthouseDescription);
      cidInfoLabel.setHorizontalAlignment(SwingConstants.CENTER);
      cidImagePanel = new ImageJPanel(infoImage);
      cidMainPanel.add(cidImagePanel, BorderLayout.CENTER);
      cidMainPanel.add(cidInfoLabel, BorderLayout.SOUTH);

      // set up the content pane
      cidContentPane = getContentPane();
      cidContentPane.add(cidMainPanel, BorderLayout.CENTER);

      // instruct window to size to accommodate the preferred
      // size of its components
      pack();

      //Dimension d = cidMainPanel.getPreferredSize();
      //System.out.println("Preferred size of main panel is: " + d);
      // position in center of the screen
      Utilities.centerOnParent(this);
      }

   private Image readImage(String filename)
         throws DialogException
      {
      Image image = null;

      try
         {
         BitmapReader reader = new BitmapReader(filename);
         image = reader.getImage();
         logBitmapInfo(reader.getInfoStrings());
         }
      catch (BitmapReader.FileFormatException e)
         {
         String[] info = e.getInfoStrings();

         System.out.println("Got FileFormatException in file: " + filename);
         System.out.println("   " + e.getMessage());
         logBitmapInfo(e.getInfoStrings());

         throw new DialogException("Problem reading image file (" + filename +
            ") format: " + e.getMessage());
         }
      catch (IOException e)
         {
         System.out.println("Got IOException reading file: " + filename);
         System.out.println("   " + e.getMessage());

         throw new DialogException("Problem reading bitmap file: " + filename, e);
         }

      if (image == null)
         throw new DialogException("Unexpected problem loading image: " +
            filename);

      int h = image.getHeight(this);
      int w = image.getWidth(this);

      if ((h <= 0) || (w <= 0))
         throw new DialogException("Unable to obtain image height and width: " +
            filename);

      return image;
      }

   private void logBitmapInfo(String[] info)
      {
      if (info != null)
         {
         System.out.println("Printing info strings");

         for (int x = 0; x < info.length; x++)
            System.out.println("   " + info[x]);
         }
      }
   }


class DialogException extends WrappingException
   {
   public DialogException()
      {
      super();
      }

   public DialogException(String msg)
      {
      super(msg);
      }

   public DialogException(Exception e)
      {
      super(e);
      }

   public DialogException(String msg, Exception e)
      {
      super(msg, e);
      }
   }


class ImageJPanel extends JPanel
   {
   Image image;

   ImageJPanel(Image image)
      {
      super();

      this.image = image;

      int       h = image.getHeight(this);
      int       w = image.getWidth(this);
      Dimension d = new Dimension(w, h);
      setPreferredSize(d);
      }

   protected void paintComponent(Graphics g)
      {
      //System.out.println("ImageJPanel.paintComponent called, with ui: " + ((ui == null) ? "null" : "not null"));
      // draw our image
      g.drawImage(image, 0, 0, this);
      }
   }

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
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import com.ysoft.jdo.book.common.exceptions.*;


public class MessageHandler
   {
   private static final String APP_HEADER   = "Maine Lighthouse Rental: ";
   private static final String ERROR_HEADER = APP_HEADER + "Error Message";
   private static final String EXCEPTION_HEADER = APP_HEADER +
      "Exception Report";
   private static final String EXCEPTION_TRACE_HEADER = APP_HEADER +
      "Exception Trace Report";
   private static final String CONFIRMATION_HEADER = APP_HEADER +
      "please confirm";
   private static final String WARNING_HEADER = APP_HEADER + "Warning Message";
   private static final String INFORMATION_HEADER = APP_HEADER +
      "Informational Message";
   private static final String TAB_EXPANSION = "     ";

   public static void reportError(Component owner, String msg)
      {
      JOptionPane.showMessageDialog(owner, msg, ERROR_HEADER,
         JOptionPane.ERROR_MESSAGE);
      returnFocusToOwner(owner);
      }

   public static void reportError(String msg)
      {
      reportError(null, msg);
      }

   public static boolean reportErrorWithQuestion(Component owner, String msg,
      String question)
      {
      int user_action = JOptionPane.showConfirmDialog(owner,
            msg + "\n\n" + question, ERROR_HEADER, JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);
      returnFocusToOwner(owner);
      return (user_action == JOptionPane.YES_OPTION);
      }

   public static void reportException(Component owner, WrappingException e)
      {
      StringBuffer bMsg = new StringBuffer();
      String       eMsg = e.getMessage();

      if ((eMsg == null) || (eMsg.trim().length() == 0))
         {
         eMsg = e.getClass().getName();
         }

      bMsg.append(e.getMessage());

      if (e.isWrappingException())
         {
         Throwable t = e.getWrappedException();
         eMsg = t.getMessage();

         if ((eMsg == null) || (eMsg.trim().length() == 0))
            eMsg = t.getClass().getName();

         bMsg.append(";\nNested exception:\n   ");
         bMsg.append(eMsg);
         }

      bMsg.append("\n\nDo you want to see the exception trace?");

      int user_action = JOptionPane.showConfirmDialog(owner, bMsg.toString(),
            EXCEPTION_HEADER, JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.ERROR_MESSAGE);

      if (user_action == JOptionPane.YES_OPTION)
         reportExceptionTrace(owner, e.getStackTraceString());

      returnFocusToOwner(owner);
      }

   public static void reportException(Component owner, Throwable e)
      {
      StringBuffer bMsg = new StringBuffer();
      String       eMsg = e.getMessage();

      if ((eMsg == null) || (eMsg.trim().length() == 0))
         {
         eMsg = e.getClass().getName();
         }

      bMsg.append(e.getMessage());

      bMsg.append("\n\nDo you want to see the exception trace?");

      int user_action = JOptionPane.showConfirmDialog(owner, bMsg.toString(),
            EXCEPTION_HEADER, JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE);

      if (user_action == JOptionPane.YES_OPTION)
         reportExceptionTrace(owner, getStackTrace(e));

      returnFocusToOwner(owner);
      }

   public static boolean confirm(Component owner, String msg, String question)
      {
      int user_action = JOptionPane.showConfirmDialog(owner,
            msg + "\n\n" + question, CONFIRMATION_HEADER,
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      returnFocusToOwner(owner);
      return (user_action == JOptionPane.YES_OPTION);
      }

   public static void reportWarning(Component owner, String msg)
      {
      JOptionPane.showMessageDialog(owner, msg, WARNING_HEADER,
         JOptionPane.WARNING_MESSAGE);
      returnFocusToOwner(owner);
      }

   public static void reportInformation(Component owner, String msg)
      {
      JOptionPane.showMessageDialog(owner, msg, INFORMATION_HEADER,
         JOptionPane.INFORMATION_MESSAGE);
      returnFocusToOwner(owner);
      }

   private static void reportExceptionTrace(Component owner, String msg)
      {
      // create the trace dialog
      JDialog dialog = new ExceptionTraceDialog(owner, EXCEPTION_TRACE_HEADER,
            msg);

      dialog.show();
      }

   private static void returnFocusToOwner(Component owner)
      {
      if ((owner != null) && owner instanceof Dialog)
         {
         // we request focus because the JOptionPane makes the Frame containing
         // the dialog the owner, and that makes it receive the focus by
         // default.  This overrides that behavior allowing the dialog to
         // get the focus back.
         owner.requestFocus();
         }
      }

   /**
    * Returns the stack trace
    */
   private static String getStackTrace(Throwable t)
      {
      StringWriter buf = new StringWriter();
      PrintWriter  out = new PrintWriter(buf);

      t.printStackTrace(out);

      out.close();

      // convert tabs and carriage returns
      StringBuffer sBuf = buf.getBuffer();

      for (int x = 0; x < sBuf.length(); x++)
         {
         switch (sBuf.charAt(x))
            {
            case '\t':
               sBuf.delete(x, x + 1);
               sBuf.insert(x, TAB_EXPANSION);
               break;

            case '\r':
               sBuf.delete(x, x + 1);
               break;

            default:
               break;
            }
         }

      return buf.getBuffer().toString();
      }
   }


class ExceptionTraceDialog extends JDialog
   {
   private static final int NO_MODIFIERS = 0;

   ExceptionTraceDialog(Component owner, String header, String msg)
      {
      super(JOptionPane.getFrameForComponent(owner), header, true);

      setTitle(header);
      setResizable(false);

      // define button listener and enter key stroke
      ActionListener button_listener = new TraceButtonListener(this);
      KeyStroke      enterKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,
            NO_MODIFIERS);

      // create the text area and the OK button
      NonEditableTextArea textArea   = new NonEditableTextArea(12, 60);
      JScrollPane         scrollPane = new JScrollPane(textArea);
      textArea.setText(msg);

      JButton okButton = new JButton("OK");
      okButton.addActionListener(button_listener);
      okButton.registerKeyboardAction(button_listener, okButton.getText(),
         enterKeyStroke, JComponent.WHEN_FOCUSED);

      JPanel jPanel = new JPanel(new FlowLayout());
      jPanel.add(okButton);

      // set up the content pane
      Container pane = getContentPane();
      pane.add(scrollPane, BorderLayout.CENTER);
      pane.add(jPanel, BorderLayout.SOUTH);

      // instruct window to size to accommodate the preferred
      // size of its components
      pack();

      // position in center of the owner
      setLocationRelativeTo(owner);
      }
   }


class TraceButtonListener implements ActionListener
   {
   private JDialog dialog;

   public TraceButtonListener(JDialog dialog)
      {
      this.dialog = dialog;
      }

   public void actionPerformed(ActionEvent e)
      {
      dialog.dispose();
      }
   }

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
package com.ysoft.jdo.book.common.console;

import java.io.IOException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.ysoft.jdo.book.common.Application;


public abstract class Command
   {
   protected UIClient client;
   private DateFormat dateFormat;
   private String[]   cmdStrings;

   public Command(UIClient client, String[] cmds)
      {
      this.client        = client;
      this.cmdStrings    = cmds;
      }

   public abstract void execute();

   public static String canonicalCmdString(String input)
      {
      // returns the input string in canonical form
      // no leading or trailing whitespace
      // only spaces for embedded whitespace
      // no sequences of 2 or more spaces
      // always in lower case
      StringBuffer buf = new StringBuffer(input.toLowerCase());

      // replace any non-space whitespace with spaces
      int len = buf.length();

      for (int x = len; x >= 0; x--)
         {
         char c = buf.charAt(x);

         if (Character.isWhitespace(c) && (c != ' '))
            buf.replace(x, x, " ");
         }

      // trim the string at beginning, end, and in the middle as well
      for (int x = len; x >= 0; x--)
         {
         // if we are not looking at a space, then nothing to do
         if (buf.charAt(x) != ' ')
            continue;

         // remove space at end of string (and adjust length)
         if (x == len)
            {
            len--;
            buf.replace(x, x, "");
            }

         // remove space if preceded by a space
         else if (x >= 1)
            {
            if (buf.charAt(x - 1) == ' ')
               buf.replace(x, x, "");
            }

         // remove space at beginning of string
         else if (x == 0)
            {
            buf.replace(0, 0, "");
            }
         }

      return buf.toString();
      }

   public String getDescription()
      {
      return getPreferredCommand();
      }

   public String getPreferredCommand()
      {
      return cmdStrings[0];
      }

   public void getParameters()
      {
      // default implementation does nothing
      }

   public boolean willExecuteCommand(String cmd)
      {
      return matchString(cmdStrings, cmd);
      }

   protected void reportException(String msg, Throwable t)
      {
      if (t instanceof Exception)
         {
         client.handleException(msg, (Exception) t);
         }
      else
         {
         System.out.println(msg);
         t.printStackTrace(System.out);
         }

      // refresh the object reference when a RemoteException is thrown
      if (t instanceof RemoteException)
         {
         client.refreshService();
         }
      }

   protected String getInputString(String msg)
      {
      UserInterface.displayPrompt(msg + ": ");

      try
         {
         byte[] entered = new byte[200];
         int    len = System.in.read(entered);

         String input = new String(entered).trim();
         System.out.println();

         if (input.length() == 0)
            input = null;

         return input;
         }
      catch (IOException e)
         {
         e.printStackTrace(System.out);
         return null;
         }
      }

   protected int getInputInt(String msg)
      {
      int retv = Integer.MAX_VALUE;

      while (retv == Integer.MAX_VALUE)
         {
         UserInterface.displayPrompt(msg + ": ");

         try
            {
            byte[] entered = new byte[80];
            int    len = System.in.read(entered);
            System.out.println();

            String  input = new String(entered);

            Integer i = Integer.decode(input.trim());
            retv = i.intValue();
            }
         catch (IOException e)
            {
            e.printStackTrace(System.out);
            retv = 0;
            }
         catch (NumberFormatException e)
            {
            System.out.println("not a valid number");
            }
         }

      return retv;
      }

   protected int getInputIntWithQuit(String msg, int low, int high)
      {
      int retv = 0;

      while (true)
         {
         String m = msg + " or 0 to quit this selection";
         retv = getInputInt(m);

         if ((retv == 0) || ((retv >= low) && (retv <= high)))
            break;
         }

      return retv;
      }

   protected float getInputFloat(String msg)
      {
      float retv = Integer.MAX_VALUE;

      while (retv == Integer.MAX_VALUE)
         {
         UserInterface.displayPrompt(msg + ": ");

         try
            {
            byte[] entered = new byte[80];
            int    len = System.in.read(entered);
            System.out.println();

            String input = new String(entered);

            retv = Float.parseFloat(input);
            }
         catch (IOException e)
            {
            e.printStackTrace(System.out);
            retv = 0;
            }
         catch (NumberFormatException e)
            {
            System.out.println("not a valid number");
            }
         }

      return retv;
      }

   protected Object getInputSelection(String msg, List list)
      {
      if ((list == null) || (list.size() <= 0))
         return null;

      String[] selections = new String[list.size()];

      for (int x = 0; x < selections.length; x++)
         {
         selections[x] = list.get(x).toString();
         }

      int x = getInputSelection(msg, selections);

      return list.get(x);
      }

   protected int getInputSelection(String msg, String[] selections)
      {
      int selection = -1;

      do
         {
         System.out.println(msg + ": ");

         for (int x = 0; x < selections.length; x++)
            {
            StringBuffer buf  = new StringBuffer(selections[x]);
            int          slen = buf.length();

            System.out.print("   " + (x + 1) + ". ");

            String margin = (x >= 10) ? "       " : "      ";

            int    pos     = 0;
            int    nextPos = 0;

            // print out the segments of the selection text so that
            // it formats within 80 columns of text.
            for (int y = 0; pos < slen; y++)
               {
               int z;

               for (z = 0; ((z + pos) < slen) && (z < 70); z++)
                  {
                  if (buf.charAt(z + pos) == ' ')
                     nextPos = z + pos;
                  }

               if ((z + pos) == slen)
                  nextPos = slen;
               else if ((nextPos - pos) < 40)
                  {
                  if ((slen - pos) > 70)
                     nextPos = pos + 70;
                  else
                     nextPos = slen;
                  }

               if (y > 0)
                  System.out.print(margin);

               System.out.println(selections[x].substring(pos, nextPos).trim());
               pos = nextPos;
               }
            }

         //System.out.println();
         UserInterface.displayPrompt("Enter selection: ");

         try
            {
            byte[] entered = new byte[80];
            int    len = System.in.read(entered);
            System.out.println();

            String  input = new String(entered);

            Integer i = Integer.decode(input.trim());

            if (i != null)
               {
               selection = i.intValue();

               // we don't accept 0 from the user
               if (selection == 0)
                  selection = -1;
               }
            }
         catch (IOException e)
            {
            e.printStackTrace(System.out);
            selection = 0;
            }
         catch (NumberFormatException e)
            {
            System.out.println("not a valid number");
            }
         }
      while ((selection < 0) || (selection > selections.length));

      return selection - 1;
      }

   protected Date getInputDate(String msg)
      {
      Date retv = null;

      while (retv == null)
         {
         UserInterface.displayPrompt(msg + " (mm-dd-yy): ");

         try
            {
            byte[] entered = new byte[80];
            int    len = System.in.read(entered);
            System.out.println();

            String input = new String(entered);

            if (dateFormat == null)
               dateFormat = new SimpleDateFormat("MM-dd-yy");

            retv = dateFormat.parse(input);

            System.out.println("   Date accepted:" + retv);
            }
         catch (ParseException e)
            {
            System.out.println("   *** not a valid date");
            }
         catch (IOException e)
            {
            e.printStackTrace(System.out);
            break;
            }
         }

      return retv;
      }

   protected boolean getInputBoolean(String msg)
      {
      int sel = getInputSelection(msg, new String[] { "true", "false" });

      if (sel == 1)
         return false;
      else
         return true;
      }

   protected boolean getConstructionParameters(DataObjectInfo doInfo,
      HashMap doSelections)
      {
      boolean   retv = true;

      Iterator  iter = doInfo.iterator();

      Parameter p = null;

      while (iter.hasNext())
         {
         p = (Parameter) iter.next();

         Class  c   = p.getType();
         String msg = "Enter " + p.getName();

         if (p.isPrimitive())
            {
            if (c == int.class)
               {
               p.set(new Integer(this.getInputInt(msg)));
               }
            else if (c == float.class)
               {
               p.set(new Float(this.getInputFloat(msg)));
               }
            else if (c == boolean.class)
               {
               p.set(new Boolean(this.getInputBoolean(msg)));
               }
            else
               retv = false;
            }
         else if (c == String.class)
            p.set(getInputString(msg));
         else if (c == Date.class)
            p.set(getInputDate(msg));
         else
            {
            List list = (List) doSelections.get(c);

            if (list != null)
               p.set(getInputSelection(msg, list));
            else
               retv = false;
            }
         }

      return retv;
      }

   private boolean matchString(String[] cmds, String cmd)
      {
      if ((cmd == null) || (cmds == null))
         return false;

      for (int x = 0; x < cmds.length; x++)
         {
         if (cmd.equalsIgnoreCase(cmds[x]))
            return true;
         }

      return false;
      }
   }

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
import java.util.*;

import com.ysoft.jdo.book.common.Application;
import com.ysoft.jdo.book.common.MsgCenter;


public class UserInterface
   {
   private static boolean needsLineEnd;
   private static boolean usePromptIndicator;

   static
      {
      try
         {
         Application.loadProperties(UserInterface.class);
         needsLineEnd    = Boolean.getBoolean(
               "com.ysoft.jdo.book.common.console.flushSystemOut");
         usePromptIndicator = Boolean.getBoolean(
               "com.ysoft.jdo.book.common.console.usePromptIndicator");
         }
      catch (IOException e)
         {
         System.out.println(
            "Could not load com.ysoft.jdo.book.common.console.package.properties");
         e.printStackTrace();
         }
      }

   ArrayList commands;
   int       numCommands;

   public UserInterface(ArrayList commands)
      {
      this.commands    = commands;
      numCommands      = commands.size();
      }

   public static void handleException(String msg, Exception e)
      {
      MsgCenter.putException(msg, e);
      }

   public static void displayPrompt(String prompt)
      {
      System.out.print(prompt);

      // in some execution environments (debuggers for example)
      // it is necessary to flush each line with a CRLF
      if (needsLineEnd)
         System.out.println();

      // to ease the use of the result console log in
      // the book's listings, a prompt indicator is nice
      if (usePromptIndicator)
         System.out.print("--> ");
      }

   public Command getCommand()
      {
      Command cmd = null;

      try
         {
CmdLoop: 
         while (true)
            {
            displayPrompt("enter command: ");

            try
               {
               byte[] entered = new byte[80];
               int    len = System.in.read(entered);

               //System.out.println();
               String input = new String(entered).trim().toLowerCase();

               if (input.equals("help") || input.equals("?"))
                  {
                  System.out.println("commands: ");
                  System.out.println();
                  System.out.println("   " + "quit");

                  for (int x = 0; x < numCommands; x++)
                     {
                     Command c = (Command) commands.get(x);
                     System.out.println("   " + c.getPreferredCommand());
                     }

                  System.out.println();
                  }
               else if (input.equals("quit"))
                  {
                  return null;
                  }
               else
                  {
                  for (int x = 0; x < commands.size(); x++)
                     {
                     cmd = (Command) commands.get(x);

                     if (cmd.willExecuteCommand(input))
                        break CmdLoop;
                     }

                  System.out.println("?");
                  }
               }
            catch (IOException e)
               {
               e.printStackTrace(System.out);
               return null;
               }
            }

         cmd.getParameters();

         return cmd;
         }
      catch (ClassCastException e)
         {
         System.err.println("A command was not recognized");
         e.printStackTrace(System.err);
         System.exit(1);
         }

      return null;
      }

   public void pumpCommands()
      {
      Command cmd       = null;
      boolean try_again;

      do
         {
         try
            {
            try_again    = false;
            cmd          = getCommand();
            }
         catch (RuntimeException e)
            {
            handleException("Error getting parameter", e);
            try_again = true;
            }
         }
      while (try_again || execute(cmd));
      }

   private boolean execute(Command command)
      {
      if (command == null)
         return false;

      try
         {
         command.execute();
         }
      catch (Exception e)
         {
         handleException("exception caught in command", e);
         }

      return true;
      }
   }

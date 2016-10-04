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
package com.ysoft.jdo.book.statetracker.client;

import java.util.*;

import javax.jdo.JDOException;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.common.console.*;
import com.ysoft.jdo.book.statetracker.*;


public class StateTracker implements UIClient
   {
   static StateHandler      persistenceHandler;
   static String            queryVariable;
   static ArrayList         allApples    = new ArrayList();
   static ArrayList         allWorms     = new ArrayList();
   static Apple             currentApple;
   private static String[]  doNames;
   private static ArrayList commands     = new ArrayList();
   private UserInterface    ui;

   public StateTracker()
      {
      if (persistenceHandler == null)
         persistenceHandler = new StateHandler();
      }

   public static void main(String[] args)
      {
      // construct a StdoutMsgHandler which will register itself
      // with the message center
      new StdoutMsgHandler();

      getArgs(args);

      StateTracker st = new StateTracker();

      st.run();

      persistenceHandler.shutdown();

      MsgCenter.putMsg("-- All done!");
      }

   public void run()
      {
      // add the commands that we will process
      commands.add(new StartTransaction(this));
      commands.add(new CommitTransaction(this));
      commands.add(new RollbackTransaction(this));
      commands.add(new ActiveTransaction(this));

      commands.add(new FindAll(this));
      commands.add(new AddApple(this));
      commands.add(new SelectApple(this));
      commands.add(new ModifyApple(this));

      //commands.add(new DropApple(this));
      commands.add(new AddWorm(this));
      commands.add(new DeleteWorm(this));

      commands.add(new ViewRawAppleState(this));
      commands.add(new ViewManagedAppleState(this));
      commands.add(new ReportManagementState(this));

      commands.add(new MakePersistent(this));
      commands.add(new DeletePersistent(this));
      commands.add(new MakeTransactional(this));
      commands.add(new MakeNontransactional(this));
      commands.add(new MakeTransient(this));
      commands.add(new Evict(this));
      commands.add(new EvictAll(this));
      commands.add(new Refresh(this));
      commands.add(new RefreshAll(this));
      commands.add(new Retrieve(this));

      commands.add(new TickleDefaultFetchGroup(this));
      commands.add(new DirtyAppleField(this));
      commands.add(new ThrowExceptionInCommit(this));

      commands.add(new ConfigureTransaction(this));
      commands.add(new TellTransactionConfiguration(this));

      commands.add(new OpenPM(this));
      commands.add(new IsOpenPM(this));
      commands.add(new ClosePM(this));

      // get commands from UI
      ui = new UserInterface(commands);
      ui.pumpCommands();
      }

   public void refreshService()
      {
      // do nothing
      }

   static void viewResults(List c)
      {
      if (c == null)
         {
         MsgCenter.putMsg("No results were available");
         return;
         }

      MsgCenter.putMsg("Found " + c.size() + " objects in the results");

      Iterator iter = c.iterator();

      while (iter.hasNext())
         {
         MsgCenter.putMsg("   " + iter.next());
         }
      }

   static String cleanupQueryString(String str)
      {
      if (str == null)
         return "";

      String retv = str.trim();

      if (str.startsWith("\""))
         {
         str = str.substring(1);

         if (str.endsWith("\""))
            str = str.substring(0, str.length() - 1);
         }

      return str;
      }

   public void handleException(String msg, Exception e)
      {
      if (ui != null)
         ui.handleException(msg, e);
      else
         MsgCenter.putException(msg, e);
      }

   private static void getArgs(String[] args)
      {
      if (args.length > 1)
         tellSyntax();
      else if (args.length < 1)
         return;

      String adaptor_class = args[0].trim();

      if (adaptor_class.length() <= 0)
         tellSyntax();

      System.setProperty("adaptor-class", adaptor_class);
      }

   private static void tellSyntax()
      {
      System.out.println("Accepts one argument: the name of the adaptor class");
      System.out.println(
         "  If not provided, then com.ysoft.jdo.book.factory.ri.JDORIAdaptor is assumed");
      System.exit(1);
      }
   }


class StartTransaction extends Command
   {
   public StartTransaction(UIClient c)
      {
      super(c, new String[] { "begin", "begin transaction", });
      }

   public void execute()
      {
      boolean started = StateTracker.persistenceHandler.beginTransaction();

      if (started)
         MsgCenter.putMsg("Okay");
      else
         MsgCenter.putMsg("Transaction was already started");
      }
   }


class CommitTransaction extends Command
   {
   public CommitTransaction(UIClient c)
      {
      super(c, new String[] { "commit", });
      }

   public void execute()
      {
      boolean committed = StateTracker.persistenceHandler.commitTransaction();

      if (committed)
         MsgCenter.putMsg("Okay");
      else
         MsgCenter.putMsg("Transaction was not started");
      }
   }


class RollbackTransaction extends Command
   {
   public RollbackTransaction(UIClient c)
      {
      super(c, new String[] { "rollback", });
      }

   public void execute()
      {
      boolean rolledback = StateTracker.persistenceHandler.rollbackTransaction();

      if (rolledback)
         MsgCenter.putMsg("Okay");
      else
         MsgCenter.putMsg("Transaction was not started");
      }
   }


class ActiveTransaction extends Command
   {
   public ActiveTransaction(UIClient c)
      {
      super(c, new String[] { "active", "is active", });
      }

   public void execute()
      {
      boolean active = StateTracker.persistenceHandler.isActiveTransaction();
      MsgCenter.putMsg("Transaction is " + (active ? "active" : "inactive"));
      }
   }


class ConfigureTransaction extends Command
   {
   private boolean bad_input;
   private boolean opt;
   private boolean ntR;
   private boolean ntW;
   private boolean retainv;
   private boolean restorev;

   public ConfigureTransaction(UIClient c)
      {
      super(c, new String[] { "configure", "configure transaction", });
      }

   public void getParameters()
      {
      clear();

      opt         = getInputBoolean("Optimistic transaction?");
      ntR         = getInputBoolean("Non-transactional Read?");
      ntW         = getInputBoolean("Non-transactional Write?");
      retainv     = getInputBoolean("Retain values?");
      restorev    = getInputBoolean("Restore values?");
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      StateTracker.persistenceHandler.configureTransaction(opt, retainv,
         restorev, ntR, ntW);
      }

   private void clear()
      {
      bad_input    = false;
      opt          = false;
      ntR          = false;
      ntW          = false;
      retainv      = false;
      restorev     = false;
      }
   }


class TellTransactionConfiguration extends Command
   {
   public TellTransactionConfiguration(UIClient c)
      {
      super(c, new String[] { "configuration", "transaction configuration", });
      }

   public void execute()
      {
      StateTracker.persistenceHandler.tellTransactionConfiguration();
      }
   }


class ThrowExceptionInCommit extends Command
   {
   public ThrowExceptionInCommit(UIClient c)
      {
      super(c, new String[] { "toss exception", });
      }

   public void execute()
      {
      StateTracker.persistenceHandler.throwExceptionOnCommit();
      MsgCenter.putMsg("Okay");
      }
   }


class FindAll extends Command
   {
   public FindAll(UIClient c)
      {
      super(c, new String[] { "find all", "find all apples", });
      }

   public void execute()
      {
      ArrayList list = StateTracker.persistenceHandler.findAll(Apple.class);

      if (list != null)
         {
         StateTracker.allApples = list;
         StateTracker.viewResults(StateTracker.allApples);
         }

      list = StateTracker.persistenceHandler.findAll(Worm.class);

      if (list != null)
         StateTracker.allWorms = list;
      }
   }


class AddApple extends Command
   {
   private boolean    bad_input;
   private AppleState appleState;

   public AddApple(UIClient c)
      {
      super(c, new String[] { "add apple", "new apple", });
      }

   public void getParameters()
      {
      clear();

      AppleState state = new AppleState();

      state.name    = getInputString("Enter apple's name");

      state.size = getInputInt("Enter apple's size (> 0)");

      if (state.size <= 0)
         {
         MsgCenter.putMsg("apple's size cannot be less than zero");
         bad_input = true;
         return;
         }

      state.picked = getInputDate("Enter date picked");

      ArrayList allWorms = new ArrayList(StateTracker.allWorms);
      ArrayList worms = new ArrayList();

      while (getInputBoolean("Add a worm?"))
         {
         Worm w = (Worm) getInputSelection("Pick a worm", allWorms);

         if (w != null)
            {
            allWorms.remove(w);
            worms.add(w);
            }
         }

      if (worms.size() > 0)
         state.headWorm = (Worm) getInputSelection("Pick the head worm", worms);

      state.worms    = new HashSet(worms);
      appleState     = state;
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      Apple apple = new Apple(appleState);
      StateTracker.allApples.add(apple);
      MsgCenter.putMsg(
         "Okay, the new transient apple has been added to the selection list");
      }

   private void clear()
      {
      bad_input     = false;
      appleState    = null;
      }
   }


class SelectApple extends Command
   {
   private boolean bad_input;

   public SelectApple(UIClient c)
      {
      super(c, new String[] { "select apple", "select", });
      }

   public void getParameters()
      {
      clear();

      Apple apple = (Apple) getInputSelection("Select an apple",
            StateTracker.allApples);

      if (apple != null)
         StateTracker.currentApple = apple;
      else
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         MsgCenter.putMsg("Did not get a good selection");
      else
         MsgCenter.putMsg("Okay");
      }

   public void clear()
      {
      bad_input = false;
      }
   }


class MakePersistent extends Command
   {
   public MakePersistent(UIClient c)
      {
      super(c, new String[] { "make persistent", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.makePersistent(
                  StateTracker.currentApple))
         {
         StateTracker.currentApple.captureIdentityString();
         MsgCenter.putMsg("Okay");
         }
      }
   }


class DeletePersistent extends Command
   {
   public DeletePersistent(UIClient c)
      {
      super(c, new String[] { "delete persistent", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.deletePersistent(
                  StateTracker.currentApple))
         {
         MsgCenter.putMsg("Okay");
         }
      }
   }


class MakeTransactional extends Command
   {
   public MakeTransactional(UIClient c)
      {
      super(c, new String[] { "make transactional", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.makeTransactional(
                  StateTracker.currentApple))
         MsgCenter.putMsg("Okay");
      }
   }


class MakeNontransactional extends Command
   {
   public MakeNontransactional(UIClient c)
      {
      super(c, new String[] { "make nontransactional", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.makeNontransactional(
                  StateTracker.currentApple))
         MsgCenter.putMsg("Okay");
      }
   }


class MakeTransient extends Command
   {
   public MakeTransient(UIClient c)
      {
      super(c, new String[] { "make transient", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.makeTransient(
                  StateTracker.currentApple))
         {
         // in this app, once an apple becomes transient, it is no longer tied
         // to the persistent object identity (no matching occurs)
         // StateTracker.currentApple.setOIDString(null);
         MsgCenter.putMsg("Okay");
         }
      }
   }


class Evict extends Command
   {
   public Evict(UIClient c)
      {
      super(c, new String[] { "evict", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.evict(StateTracker.currentApple))
         MsgCenter.putMsg("Okay");
      }
   }


class EvictAll extends Command
   {
   public EvictAll(UIClient c)
      {
      super(c, new String[] { "evict all", });
      }

   public void execute()
      {
      if (StateTracker.persistenceHandler.evictAll())
         MsgCenter.putMsg("Okay");
      }
   }


class Refresh extends Command
   {
   public Refresh(UIClient c)
      {
      super(c, new String[] { "refresh", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.refresh(StateTracker.currentApple))
         MsgCenter.putMsg("Okay");
      }
   }


class RefreshAll extends Command
   {
   public RefreshAll(UIClient c)
      {
      super(c, new String[] { "refresh all", });
      }

   public void execute()
      {
      if (StateTracker.persistenceHandler.refreshAll())
         MsgCenter.putMsg("Okay");
      }
   }


class Retrieve extends Command
   {
   public Retrieve(UIClient c)
      {
      super(c, new String[] { "retrieve", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.persistenceHandler.retrieve(StateTracker.currentApple))
         MsgCenter.putMsg("Okay");
      }
   }


/*
class DropApple extends Command
   {
   public DropApple(UIClient c)
      {
      super(c, new String[]
            {
            "drop",
            "drop apple",
            });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      if (StateTracker.allApples.remove(StateTracker.currentApple))
         {
         StateTracker.currentApple = null;
         MsgCenter.putMsg("Okay");
         }
      else
         MsgCenter.putMsg("Unexpected error");
      }
   }
*/
class AddWorm extends Command
   {
   private boolean bad_input;
   private String  name;

   public AddWorm(UIClient c)
      {
      super(c, new String[] { "add worm", "new worm", });
      }

   public void getParameters()
      {
      clear();

      name = getInputString("Enter worm's name");
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      Worm newWorm = new Worm(name);
      StateTracker.allWorms.add(newWorm);
      MsgCenter.putMsg(
         "Okay, but worms are made persistent only by being in a persistent apple");
      }

   private void clear()
      {
      bad_input    = false;
      name         = null;
      }
   }


class DeleteWorm extends Command
   {
   private boolean bad_input;
   private Worm    worm;

   public DeleteWorm(UIClient c)
      {
      super(c, new String[] { "delete worm", });
      }

   public void getParameters()
      {
      clear();

      if (StateTracker.allWorms.size() <= 0)
         {
         MsgCenter.putMsg("No worms to delete");
         bad_input = true;
         return;
         }

      worm = (Worm) getInputSelection("Select worm to delete",
            StateTracker.allWorms);

      if (worm == null)
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         MsgCenter.putMsg("Bad input");
      else
         {
         StateTracker.allWorms.remove(worm);

         if (StateTracker.persistenceHandler.deletePersistent(worm))
            MsgCenter.putMsg("Okay");
         }
      }

   public void clear()
      {
      bad_input    = false;
      worm         = null;
      }
   }


class ViewRawAppleState extends Command
   {
   public ViewRawAppleState(UIClient c)
      {
      super(c,
         new String[] { "snoop", "view raw", "view raw state", "view raw apple", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      try
         {
         Apple apple = (Apple) StateTracker.currentApple.clone();

         MsgCenter.putMsg("Viewing raw state for: " + apple);
         MsgCenter.putMsg("   transient state: " + apple.getTransientState());
         MsgCenter.putMsg("   transactional state: " +
            apple.getTransactionalState());
         MsgCenter.putMsg("   persistent state: " + apple.getPersistentState());
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception viewing raw apple state", e);
         }
      }
   }


class ViewManagedAppleState extends Command
   {
   public ViewManagedAppleState(UIClient c)
      {
      super(c,
         new String[]
         {
            "view", "view managed", "view managed state", "view managed apple",
         });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      Apple apple = StateTracker.currentApple;

      try
         {
         MsgCenter.putMsg("Viewing managed state for: " + apple);
         MsgCenter.putMsg("   transient state: " + apple.getTransientState());
         MsgCenter.putMsg("   transactional state: " +
            apple.getTransactionalState());
         MsgCenter.putMsg("   persistent state: " + apple.getPersistentState());
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception using managed view", e);
         }
      }
   }


class ReportManagementState extends Command
   {
   public ReportManagementState(UIClient c)
      {
      super(c,
         new String[] { "get JDO state", "get state", "JDO state", "state", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      String state = StateTracker.currentApple.getMonitor().getManagementState();

      MsgCenter.putMsg(StateTracker.currentApple + " is in JDO state " + state);
      }
   }


class ModifyApple extends Command
   {
   private boolean    bad_input;
   private AppleState transientState;
   private AppleState transactionalState;
   private AppleState persistentState;

   public ModifyApple(UIClient c)
      {
      super(c, new String[] { "modify apple", "modify", });
      }

   public void getParameters()
      {
      clear();

      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         bad_input = true;
         return;
         }

      int choice = getInputSelection("Select state to change",
            new String[]
            {
               "Transient state", "Transactional state", "Persistent state"
            });

      try
         {
         AppleState state  = null;
         Apple      apple  = StateTracker.currentApple;
         String     header = null;

         switch (choice)
            {
            case 0:
               state = (AppleState) apple.getTransientState();
               transientState = state;
               header = "transient";
               break;

            case 1:
               state = (AppleState) apple.getTransactionalState();
               transactionalState = state;
               header = "transactional";
               break;

            case 2:
               state = (AppleState) apple.getPersistentState();
               persistentState = state;
               header = "persistent";
               break;

            default:
               bad_input = true;
               return;
            }

         getModifications(header, state);
         }
      catch (Exception e)
         {
         bad_input = true;
         MsgCenter.putException("caught exception while modifying current apple",
            e);
         return;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      try
         {
         StateTracker.currentApple.setTransientState(transientState);
         StateTracker.currentApple.setTransactionalState(transactionalState);
         StateTracker.currentApple.setPersistentState(persistentState);
         MsgCenter.putMsg("Okay");
         }
      catch (Exception e)
         {
         MsgCenter.putException("caught exception modifying state", e);
         }
      }

   private void clear()
      {
      bad_input             = false;
      transientState        = null;
      transactionalState    = null;
      persistentState       = null;
      }

   private void getModifications(String header, AppleState state)
      {
      ArrayList allWorms = new ArrayList(StateTracker.allWorms);
      ArrayList worms = null;

      if (state.worms == null)
         worms = new ArrayList();
      else
         worms = new ArrayList(state.worms);

      allWorms.removeAll(worms);

      int  selection = 0;
      Worm w = null;

      while ((selection = getInputSelection("Modify " + header + " property",
                     new String[]
                     {
                        "Stop", "name (" + state.name + ")",
                        "size (" + state.size + ")",
                        "pick date (" + state.getPickedDate() + ")",
                        "add worm to existing " + worms.size() + " worms",
                        "delete worm from existing " + worms.size() + " worms",
                        "select head worm (" + state.headWorm + ")",
                     })) > 0)
         {
         if (selection < 1)
            break;

         switch (selection)
            {
            case 1: // name
               state.name = getInputString("Enter new name (" + state.name +
                     ")");
               break;

            case 2: // size

               int size = getInputInt("Enter new size (" + state.size + ")");

               if (size > 0)
                  state.size = size;
               else
                  MsgCenter.putMsg("apple's size cannot be less than zero");

               break;

            case 3: // pick date
               state.picked = getInputDate("Enter new pick date (" +
                     state.getPickedDate() + ")");
               break;

            case 4: // add worm
               w = (Worm) getInputSelection("Pick a worm", allWorms);

               if (w != null)
                  {
                  allWorms.remove(w);
                  worms.add(w);
                  }

               break;

            case 5: // delete worm
               w = (Worm) getInputSelection("Pick a worm", worms);

               if (w != null)
                  {
                  worms.remove(w);
                  allWorms.add(w);
                  }

               break;

            case 6: // select head worm

               if (worms.size() > 0)
                  state.headWorm = (Worm) getInputSelection("Pick the head worm",
                        worms);
               else
                  state.headWorm = null;

               break;

            default:
               break;
            }
         }

      state.worms = new HashSet(worms);
      }
   }


class TickleDefaultFetchGroup extends Command
   {
   public TickleDefaultFetchGroup(UIClient c)
      {
      super(c, new String[] { "tickle default fetch group", "tickle", });
      }

   public void execute()
      {
      if (StateTracker.currentApple == null)
         {
         MsgCenter.putMsg("No apple selected");
         return;
         }

      MsgCenter.putMsg("Tickling " + StateTracker.currentApple +
         " whose size is: " + StateTracker.currentApple.getPersistentSize());
      }
   }


class OpenPM extends Command
   {
   public OpenPM(UIClient c)
      {
      super(c, new String[] { "open", "open pm", });
      }

   public void execute()
      {
      StateTracker.persistenceHandler.openPersistenceManager();
      MsgCenter.putMsg("Okay");
      }
   }


class ClosePM extends Command
   {
   public ClosePM(UIClient c)
      {
      super(c, new String[] { "close", "close pm", });
      }

   public void execute()
      {
      StateTracker.persistenceHandler.closePersistenceManager();
      MsgCenter.putMsg("Okay");
      }
   }


class IsOpenPM extends Command
   {
   public IsOpenPM(UIClient c)
      {
      super(c, new String[] { "is open", "is open pm", });
      }

   public void execute()
      {
      if (StateTracker.persistenceHandler.isOpenPersistenceManager())
         MsgCenter.putMsg("PM is open");
      else
         MsgCenter.putMsg("PM is closed");
      }
   }


class DirtyAppleField extends Command
   {
   private boolean bad_input;
   private String  fieldName;

   public DirtyAppleField(UIClient c)
      {
      super(c, new String[] { "dirty", "dirty an apple field", });
      }

   public void getParameters()
      {
      clear();

      String[] fieldNames = new String[]
         {
            "transactionalName", "transactionalSize", "transactionalPicked",
            "transactionalWorms", "transactionalHeadWorm", "persistentName",
            "persistentSize", "persistentPicked", "persistentWorms",
            "persistentHeadWorm",
         };

      int      selected = getInputSelection("Select a field", fieldNames);
      fieldName = fieldNames[selected];
      }

   public void execute()
      {
      if (bad_input)
         MsgCenter.putMsg("Did not get a good selection");
      else
         {
         StateTracker.persistenceHandler.makeDirty(StateTracker.currentApple,
            fieldName);
         MsgCenter.putMsg("Okay");
         }
      }

   public void clear()
      {
      bad_input    = false;
      fieldName    = null;
      }
   }

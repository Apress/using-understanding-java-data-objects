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
package com.ysoft.jdo.book.library.client;

import java.util.*;

import javax.jdo.JDOException;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.common.console.*;
import com.ysoft.jdo.book.library.*;


public class Library implements UIClient
   {
   static
      {
      Copyright.stdout();
      }

   static LibraryHandler persistenceHandler;
   static HashMap        lastViewed    = new HashMap();
   static String         queryVariable;
   static String         paramDeclaration;
   static Object         parameter;

   // DataObjectInfo is used to provide information
   // on the object model for the general client application
   static DataObjectInfo[] doInfo = 
   {
      new DataObjectInfo(Book.class,
         new String[]
         {
            "", "String title", "Borrower borrower", "Date checkout",
            "Collection:Category categories"
         }, new Parameter[] { new Parameter(String.class, "title") }),
      new DataObjectInfo(Borrower.class,
         new String[]
         {
            "", "String name", "Collection:Book books",
            "Volunteer volunteer"
         }, new Parameter[] { new Parameter(String.class, "name") }),
      new DataObjectInfo(Volunteer.class,
         new String[] { "", "int hours_per_week", "Borrower borrower" },
         new Parameter[] { new Parameter(Borrower.class, "borrower") }),
      new DataObjectInfo(Category.class,
         new String[] { "", "String name", "Collection:Book books" },
         new Parameter[] { new Parameter(String.class, "name") })
   };
   private static String[] doNames;
   private static ArrayList commands = new ArrayList();
   private UserInterface   ui;

   public Library()
      {
      if (persistenceHandler == null)
         persistenceHandler = new LibraryHandler();
      }

   public static void main(String[] args)
      {
      /* This is handled when the Copyright class starts up
      // construct a StdoutMsgHandler which will register itself
      // with the message center

      //new StdoutMsgHandler();
      getArgs(args);
      */
      Library pc = new Library();

      pc.run();

      MsgCenter.putMsg("-- All done!");
      }

   public void run()
      {
      // add the commands that we will process
      commands.add(new StartTransaction(this));
      commands.add(new CommitTransaction(this));
      commands.add(new RollbackTransaction(this));

      //commands.add(new ConfigurePM(this));
      commands.add(new GetPMConfiguration(this));
      commands.add(new ViewDataBeanAttributes(this));
      commands.add(new DefineQueryVariable(this));
      commands.add(new DefineParameter(this));
      commands.add(new FindAll(this));
      commands.add(new Find(this));
      commands.add(new FindInResults(this));
      commands.add(new Add(this));
      commands.add(new Delete(this));
      commands.add(new ViewVolunteer(this));
      commands.add(new ViewBorrower(this));
      commands.add(new ViewBook(this));
      commands.add(new ViewCategory(this));
      commands.add(new BorrowBook(this));
      commands.add(new ReturnBook(this));
      commands.add(new ModifyVolunteer(this));
      commands.add(new ModifyBook(this));
      commands.add(new Populate(this));
      commands.add(new ClearDatabase(this));

      // get commands from UI
      ui = new UserInterface(commands);
      ui.pumpCommands();
      }

   private static void getArgs(String[] args)
      {
      if (args.length > 0)
         tellSyntax();
      }

   private static void tellSyntax()
      {
      System.out.println(
         "No arguments needed.  Uses the factory.properties in the library package");
      System.exit(1);
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

      // determine whether we are looking at people or dogs
      Iterator iter = c.iterator();

      Object   first = null;

      while (iter.hasNext())
         {
         if (first == null)
            {
            first = iter.next();
            MsgCenter.putMsg("   " + first);
            }
         else
            MsgCenter.putMsg("   " + iter.next());
         }

      if (first != null)
         lastViewed.put(first.getClass(), c);
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

   static String[] getDONames()
      {
      if (doNames == null)
         {
         doNames = new String[doInfo.length];

         for (int x = 0; x < doInfo.length; x++)
            doNames[x] = doInfo[x].getName();
         }

      return doNames;
      }

   public void handleException(String msg, Exception e)
      {
      if (ui != null)
         ui.handleException(msg, e);
      else
         MsgCenter.putException(msg, e);
      }

   static boolean isDBEmpty()
      {
      List books = persistenceHandler.findAll(Book.class);

      if (books.size() > 0)
         return false;

      List categories = persistenceHandler.findAll(Category.class);

      if (categories.size() > 0)
         return false;

      List borrowers = persistenceHandler.findAll(Borrower.class);

      if (borrowers.size() > 0)
         return false;

      return true;
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
      boolean started = Library.persistenceHandler.startTransaction();

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
      boolean committed = Library.persistenceHandler.commitTransaction();

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
      boolean rolledback = Library.persistenceHandler.rollbackTransaction();

      if (rolledback)
         MsgCenter.putMsg("Okay");
      else
         MsgCenter.putMsg("Transaction was not started");
      }
   }


class GetPMConfiguration extends Command
   {
   public GetPMConfiguration(UIClient c)
      {
      super(c,
         new String[]
         {
            "get pm config", "pm config", "configuration", "get config",
         });
      }

   public void execute()
      {
      boolean[] opts = Library.persistenceHandler.getPMConfiguration();

      MsgCenter.putMsg("PM configuration");
      MsgCenter.putMsg("   Optimistic flag    : " + opts[0]);
      MsgCenter.putMsg("   NT Read flag       : " + opts[1]);
      MsgCenter.putMsg("   NT Write flag      : " + opts[2]);
      MsgCenter.putMsg("   Retain values flag : " + opts[3]);
      MsgCenter.putMsg("   Restore values flag: " + opts[4]);
      MsgCenter.putMsg("   Ignore cache flag  : " + opts[5]);
      MsgCenter.putMsg("   Transaction active : " + opts[6]);
      }
   }


class ViewDataBeanAttributes extends Command
   {
   private boolean  bad_input;
   private int      doInfoIndex;

   public ViewDataBeanAttributes(UIClient c)
      {
      super(c, new String[] { "view attributes", "attributes" });
      }

   public void getParameters()
      {
      clear();

      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Pick a data object class", dbNames);

      if (selection >= 0)
         doInfoIndex = selection;
      else
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("Persistent attributes for " + Library.doInfo[doInfoIndex].getName());
      String [] attributes = Library.doInfo[doInfoIndex].getPersistentAttributes();

      for (int x = 0; x < attributes.length; x++)
         MsgCenter.putMsg("   " + attributes[x]);
      }

   private void clear()
      {
      bad_input     = false;
      doInfoIndex   = 0;
      }
   }


class DefineQueryVariable extends Command
   {
   public DefineQueryVariable(UIClient c)
      {
      super(c, new String[] { "define query variable", "define variable", });
      }

   public void getParameters()
      {
      String vStr = getInputString("Enter query variable declaration");
      Library.queryVariable = Library.cleanupQueryString(vStr);
      }

   public void execute()
      {
      MsgCenter.putMsg("Okay");
      }
   }

class DefineParameter extends Command
   {
   private boolean  bad_input;

   public DefineParameter(UIClient c)
      {
      super(c, new String[] { "define query parameter", "define parameter", });
      }

   public void getParameters()
      {
      clear();
      
      String[] dbNames = Library.getDONames();
      Class pcClass = null;
      String pcClassName = null;
      String pcParamName = null;

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Select the type of the parameter", dbNames);

      if (selection >= 0)
         {
         pcClass = Library.doInfo[selection].getType();
         pcClassName = dbNames[selection];
         pcParamName = pcClassName.toLowerCase();
         }
      else
         {
         bad_input = true;
         return;
         }

      try
         {
         List list = (ArrayList) Library.lastViewed.get(pcClass);

         if ((list == null) || (list.size() <= 0))
            {
            list = Library.persistenceHandler.findAll(pcClass);
            Library.lastViewed.put(pcClass, list);
            }

         if (list.size() == 0)
            {
            MsgCenter.putMsg("Nothing to view");
            bad_input = true;
            }
         else if (list.size() == 1)
            Library.parameter = list.get(0);
         else
            Library.parameter = getInputSelection("Pick the " + pcClassName, list);

         if (Library.parameter == null)
            bad_input = true;
            
         Library.paramDeclaration = pcClassName + " " + pcParamName;
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (!bad_input)
         MsgCenter.putMsg("Okay, parameter declaration is: \"" + Library.paramDeclaration + "\"");
      }
      
   private void clear()
      {
      bad_input = false;
      }
   }


class FindAll extends Command
   {
   private boolean bad_input;
   private Class   pcClass;

   public FindAll(UIClient c)
      {
      super(c, new String[] { "find all", });
      }

   public void getParameters()
      {
      clear();

      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Find all what", dbNames);

      if (selection >= 0)
         pcClass = Library.doInfo[selection].getType();
      else
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      Library.viewResults(Library.persistenceHandler.findAll(pcClass));
      }

   private void clear()
      {
      bad_input    = false;
      pcClass      = null;
      }
   }


class Find extends Command
   {
   private boolean bad_input;
   private Class   pcClass;
   private String  queryStr;

   public Find(UIClient c)
      {
      super(c, new String[] { "find", });
      }

   public void getParameters()
      {
      clear();

      // determine the query class
      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Find what type of objects", dbNames);

      if (selection >= 0)
         pcClass = Library.doInfo[selection].getType();
      else
         bad_input = true;

      // determine the query filter
      queryStr    = getInputString("Enter query string");
      queryStr    = Library.cleanupQueryString(queryStr);
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      List results = null;

      if (Library.paramDeclaration != null)
         {
         // null out variable string early in case of exception
         String vStr = Library.queryVariable;
         String pStr = Library.paramDeclaration;
         Object param = Library.parameter;
         
         Library.queryVariable    = null;
         Library.paramDeclaration = null;
         Library.parameter = null;

         results = Library.persistenceHandler.find(pcClass, queryStr, vStr, pStr, param);
         }
      else if (Library.queryVariable != null)
         {
         // null out variable string early in case of exception
         String s = Library.queryVariable;
         Library.queryVariable    = null;

         results = Library.persistenceHandler.find(pcClass, queryStr, s);
         }
      else
         {
         results = Library.persistenceHandler.find(pcClass, queryStr);
         }

      Library.viewResults(results);
      }

   private void clear()
      {
      bad_input    = false;
      queryStr     = null;
      pcClass      = null;
      }
   }


class FindInResults extends Command
   {
   private boolean bad_input;
   private String  queryStr;
   private Class   pcClass;

   public FindInResults(UIClient c)
      {
      super(c, new String[] { "find in results", });
      }

   public void getParameters()
      {
      clear();

      // determine the query class
      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Find what type of objects", dbNames);

      if (selection >= 0)
         pcClass = Library.doInfo[selection].getType();
      else
         bad_input = true;

      // get the query filter
      queryStr    = getInputString("Enter query string");
      queryStr    = Library.cleanupQueryString(queryStr);
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      List results = null;

      if (Library.queryVariable != null)
         {
         results    = Library.persistenceHandler.findInCollection(pcClass,
               (Collection) Library.lastViewed.get(pcClass), queryStr,
               Library.queryVariable);
         Library.queryVariable = null;
         }
      else
         {
         results = Library.persistenceHandler.findInCollection(pcClass,
               (Collection) Library.lastViewed.get(pcClass), queryStr);
         }

      Library.viewResults(results);
      }

   private void clear()
      {
      bad_input    = false;
      queryStr     = null;
      pcClass      = null;
      }
   }


class Add extends Command
   {
   private boolean bad_input;
   private Object  newDataObject;

   public Add(UIClient c)
      {
      super(c, new String[] { "add data object", "add", "add object", });
      }

   public void getParameters()
      {
      clear();

      // determine the data object class and get its DataObjectInfo
      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Select the type of object to add",
            dbNames);

      DataObjectInfo doInfo = null;

      if (selection >= 0)
         doInfo = Library.doInfo[selection];
      else
         {
         bad_input = true;
         return;
         }

      // get all the parameters
      if (!getConstructionParameters(doInfo, Library.lastViewed))
         {
         bad_input = true;
         return;
         }

      // get the object
      try
         {
         newDataObject = doInfo.createObject();
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
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

      Library.persistenceHandler.add(newDataObject);
      MsgCenter.putMsg("Okay");
      }

   private void clear()
      {
      bad_input        = false;
      newDataObject    = null;
      }
   }


class Delete extends Command
   {
   private boolean bad_input;
   private Object  dataObject;

   public Delete(UIClient c)
      {
      super(c, new String[] { "delete data object", "delete object", "delete", });
      }

   public void getParameters()
      {
      clear();

      // determine the data object class and get its DataObjectInfo
      String[] dbNames = Library.getDONames();

      if (dbNames.length <= 0)
         {
         MsgCenter.putMsg("No data objects to select");
         bad_input = true;
         return;
         }

      int selection = getInputSelection("Select the type of object to delete",
            dbNames);

      if (selection >= 0)
         {
         Class pcClass = Library.doInfo[selection].getType();
         List  list = (List) Library.lastViewed.get(pcClass);

         dataObject = getInputSelection("Select the object to delete", list);
         }
      else
         {
         bad_input = true;
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

      if (dataObject == null)
         return;

      Library.persistenceHandler.delete(dataObject);
      MsgCenter.putMsg("Okay");
      }

   private void clear()
      {
      bad_input     = false;
      dataObject    = null;
      }
   }


class ViewVolunteer extends Command
   {
   private boolean   bad_input;
   private Volunteer volunteer;

   public ViewVolunteer(UIClient c)
      {
      super(c, new String[] { "view volunteer", });
      }

   public void getParameters()
      {
      clear();

      try
         {
         List list = (ArrayList) Library.lastViewed.get(Volunteer.class);

         if ((list == null) || (list.size() <= 0))
            {
            list = Library.persistenceHandler.findAll(Volunteer.class);
            Library.lastViewed.put(Volunteer.class, list);
            }

         if (list.size() == 0)
            {
            MsgCenter.putMsg("Nothing to view");
            bad_input = true;
            }
         else if (list.size() == 1)
            volunteer = (Volunteer) list.get(0);
         else
            volunteer = (Volunteer) getInputSelection("Pick the volunteer", list);

         if (volunteer == null)
            bad_input = true;
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("Info on " + volunteer);
      MsgCenter.putMsg("   hours per week: " + volunteer.getHoursPerWeek());

      Borrower  b = volunteer.getBorrower();
      ArrayList l = new ArrayList();
      l.add(b);
      Library.lastViewed.put(Borrower.class, l);

      MsgCenter.putMsg("   Borrower:" + b);
      }

   private void clear()
      {
      bad_input    = false;
      volunteer    = null;
      }
   }


class ViewBorrower extends Command
   {
   private boolean  bad_input;
   private Borrower borrower;

   public ViewBorrower(UIClient c)
      {
      super(c, new String[] { "view borrower", });
      }

   public void getParameters()
      {
      clear();

      try
         {
         List list = (ArrayList) Library.lastViewed.get(Borrower.class);

         if ((list == null) || (list.size() <= 0))
            {
            list = Library.persistenceHandler.findAll(Borrower.class);
            Library.lastViewed.put(Borrower.class, list);
            }

         if (list.size() == 0)
            {
            MsgCenter.putMsg("Nothing to view");
            bad_input = true;
            }
         else if (list.size() == 1)
            borrower = (Borrower) list.get(0);
         else
            borrower = (Borrower) getInputSelection("Pick the borrower", list);

         if (borrower == null)
            bad_input = true;
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("Info on " + borrower);

      Volunteer v = borrower.getVolunteer();

      if (v != null)
         MsgCenter.putMsg("  volunteers " + v.getHoursPerWeek() +
            " hours per week");

      List list = borrower.getBooks();

      if ((list != null) && (list.size() > 0))
         {
         MsgCenter.putMsg("   has borrowed " + list.size() + " books");

         for (int x = 0; x < list.size(); x++)
            MsgCenter.putMsg("      " + list.get(x));
         }

      Library.lastViewed.put(Book.class, list);
      }

   private void clear()
      {
      bad_input    = false;
      borrower     = null;
      }
   }


class ViewBook extends Command
   {
   private boolean bad_input;
   private Book    book;

   public ViewBook(UIClient c)
      {
      super(c, new String[] { "view book", });
      }

   public void getParameters()
      {
      clear();

      try
         {
         List list = (ArrayList) Library.lastViewed.get(Book.class);

         if ((list == null) || (list.size() <= 0))
            {
            list = Library.persistenceHandler.findAll(Book.class);
            Library.lastViewed.put(Book.class, list);
            }

         if (list.size() == 0)
            {
            MsgCenter.putMsg("Nothing to view");
            bad_input = true;
            }
         else if (list.size() == 1)
            book = (Book) list.get(0);
         else
            book = (Book) getInputSelection("Pick the book", list);

         if (book == null)
            bad_input = true;
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("Info on " + book);

      Borrower b = book.getBorrower();

      if (b != null)
         MsgCenter.putMsg("   borrowed by " + b + " on " + book.getCheckout());

      List list = book.getCategories();

      if ((list != null) && (list.size() > 0))
         {
         MsgCenter.putMsg("   in categories");

         for (int x = 0; x < list.size(); x++)
            MsgCenter.putMsg("      " + list.get(x));
         }

      Library.lastViewed.put(Category.class, list);
      }

   private void clear()
      {
      bad_input    = false;
      book         = null;
      }
   }


class ViewCategory extends Command
   {
   private boolean  bad_input;
   private Category category;

   public ViewCategory(UIClient c)
      {
      super(c, new String[] { "view category", });
      }

   public void getParameters()
      {
      clear();

      try
         {
         List list = (ArrayList) Library.lastViewed.get(Category.class);

         if ((list == null) || (list.size() <= 0))
            {
            list = Library.persistenceHandler.findAll(Category.class);
            Library.lastViewed.put(Category.class, list);
            }

         if (list.size() == 0)
            {
            MsgCenter.putMsg("Nothing to view");
            bad_input = true;
            }
         else if (list.size() == 1)
            category = (Category) list.get(0);
         else
            category = (Category) getInputSelection("Pick the category", list);

         if (category == null)
            bad_input = true;
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("Info on " + category);

      List list = category.getBooks();

      if ((list != null) && (list.size() > 0))
         {
         MsgCenter.putMsg("   books in this category");

         for (int x = 0; x < list.size(); x++)
            MsgCenter.putMsg("      " + list.get(x));
         }

      Library.lastViewed.put(Book.class, list);
      }

   private void clear()
      {
      bad_input    = false;
      category     = null;
      }
   }


class BorrowBook extends Command
   {
   private boolean  bad_input;
   private Book     book;
   private Borrower borrower;

   public BorrowBook(UIClient c)
      {
      super(c, new String[] { "borrow book", });
      }

   public void getParameters()
      {
      clear();

      List list = Library.persistenceHandler.find(Book.class, "borrower == null");
      Library.lastViewed.put(Book.class, list);

      book = (Book) getInputSelection("Select the book to borrow", list);

      List borrowers = (List) Library.lastViewed.get(Borrower.class);

      if ((borrowers == null) || (borrowers.size() <= 0))
         {
         borrowers = Library.persistenceHandler.findAll(Borrower.class);
         Library.lastViewed.put(Borrower.class, borrowers);
         }

      borrower = (Borrower) getInputSelection("Select the borrower", borrowers);

      if ((book == null) || (borrower == null))
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      borrower.borrowBook(book);
      MsgCenter.putMsg("okay");
      }

   private void clear()
      {
      bad_input    = false;
      book         = null;
      borrower     = null;
      }
   }


class ReturnBook extends Command
   {
   private boolean bad_input;
   private Book    book;

   public ReturnBook(UIClient c)
      {
      super(c, new String[] { "return book", });
      }

   public void getParameters()
      {
      clear();

      List l = (List) Library.lastViewed.get(Book.class);

      List list = new ArrayList(l);

      if (list != null)
         {
         for (int x = 0; x < list.size(); x++)
            if (((Book) list.get(x)).getBorrower() == null)
               list.remove(x--);
         }

      if ((list == null) || (list.size() <= 0))
         list = Library.persistenceHandler.find(Book.class, "borrower != null");

      book = (Book) getInputSelection("Select the book to return", list);

      if (book == null)
         bad_input = true;
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      Borrower b = book.getBorrower();

      if (b != null)
         {
         b.returnBook(book);
         MsgCenter.putMsg("okay");
         }
      else
         MsgCenter.putMsg("book was not checked out");
      }

   private void clear()
      {
      bad_input    = false;
      book         = null;
      }
   }


class ModifyVolunteer extends Command
   {
   private boolean bad_input;

   public ModifyVolunteer(UIClient c)
      {
      super(c, new String[] { "modify volunteer", });
      }

   public void getParameters()
      {
      try
         {
         clear();

         List list = (List) Library.lastViewed.get(Volunteer.class);

         if ((list == null) || (list.size() <= 0))
            list = Library.persistenceHandler.findAll(Volunteer.class);

         Volunteer volunteer = (Volunteer) getInputSelection("Select the volunteer to modify",
               list);

         if (volunteer == null)
            {
            bad_input = true;
            return;
            }

         int newHours = getInputInt("Enter new hours per week (old: " +
               volunteer.getHoursPerWeek() + "): ");

         if (newHours <= 0)
            {
            MsgCenter.putMsg("cannot volunteer for <= 0 hours");
            bad_input = true;
            return;
            }

         volunteer.setHoursPerWeek(newHours);

         list = new ArrayList();
         list.add(volunteer);

         Library.lastViewed.put(Volunteer.class, list);
         }
      catch (RuntimeException e)
         {
         reportException("getting parameters", e);
         bad_input = true;
         }
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("okay");
      }

   private void clear()
      {
      bad_input = false;
      }
   }


class ModifyBook extends Command
   {
   private boolean bad_input;

   public ModifyBook(UIClient c)
      {
      super(c, new String[] { "modify book", });
      }

   public void getParameters()
      {
      clear();

      List list = (List) Library.lastViewed.get(Book.class);

      if ((list == null) || (list.size() <= 0))
         list = Library.persistenceHandler.findAll(Book.class);

      Book book = (Book) getInputSelection("Select the book to modify", list);

      if (book == null)
         {
         bad_input = true;
         return;
         }

      List cList = book.getCategories();

      MsgCenter.putMsg(book + " has " + cList.size() + " categories");

      for (int x = 0; x < cList.size(); x++)
         MsgCenter.putMsg("   " + cList.get(x));

      MsgCenter.putMsg("");

      int add_or_delete = getInputSelection("add or delete categories",
            new String[] { "Add", "Delete" });

      List aList = Library.persistenceHandler.findAll(Category.class);
      List dList = null;
      String m  = null;

      if (add_or_delete == 0)
         {
         aList.removeAll(cList);
         dList    = aList;
         m        = "select categories to add";
         }
      else
         {
         dList    = cList;
         m        = "select categories to remove";
         }

      Category c = (Category) getInputSelection(m, dList);

      if (add_or_delete == 0)
         book.addCategory(c);
      else
         book.removeCategory(c);
      }

   public void execute()
      {
      if (bad_input)
         {
         MsgCenter.putMsg("Did not get good input");
         return;
         }

      MsgCenter.putMsg("okay");
      }

   private void clear()
      {
      bad_input = false;
      }
   }


class Populate extends Command
   {
   LibraryHandler libhand = Library.persistenceHandler;

   public Populate(UIClient c)
      {
      super(c, new String[] { "populate database", });
      }

   public void execute()
      {
      // start transaction if not already started
      boolean started = Library.persistenceHandler.startTransaction();

      // check that library is not already populated
      if (!Library.isDBEmpty())
         {
         MsgCenter.putMsg("The database is not empty");

         if (started)
            Library.persistenceHandler.rollbackTransaction();

         return;
         }

      // create three borrowers Tom, Dick, and Harry
      Borrower tom   = new Borrower("Tom");
      Borrower dick  = new Borrower("Dick");
      Borrower harry = new Borrower("Harry");

      // make them persistent
      libhand.add(tom);
      libhand.add(dick);
      libhand.add(harry);

      // make Dick a volunteer
      Volunteer vDick = new Volunteer(dick);

      // create six books
      Book fishing  = new Book("Gone Fishing");
      Book hunting  = new Book("Gone Hunting");
      Book sailing  = new Book("Gone Sailing");
      Book fighting = new Book("Gone to War");
      Book visiting = new Book("Gone Visiting");
      Book working  = new Book("Gone to Work");
      Book sleeping = new Book("Gone to Bed");

      libhand.add(fishing);
      libhand.add(hunting);
      libhand.add(sailing);
      libhand.add(fighting);
      libhand.add(visiting);
      libhand.add(working);
      libhand.add(sleeping);

      // create categories for the books
      Category outdoors = new Category("Outdoors");
      Category military = new Category("Military");
      Category sport    = new Category("Sportsman");
      Category travel   = new Category("Travel");
      Category industry = new Category("Industry");
      Category space    = new Category("Space");

      // link the books with the categories
      fishing.addCategory(outdoors);
      fishing.addCategory(sport);
      hunting.addCategory(outdoors);
      hunting.addCategory(sport);
      sailing.addCategory(outdoors);
      sailing.addCategory(travel);
      fighting.addCategory(military);
      fighting.addCategory(travel);
      visiting.addCategory(travel);
      working.addCategory(industry);

      // this category has no books
      libhand.add(space);

      // borrow some books
      tom.borrowBook(fishing);
      dick.borrowBook(hunting);
      dick.borrowBook(sailing);
      harry.borrowBook(working);

      // commit the transaction
      boolean committed = Library.persistenceHandler.commitTransaction();

      if (committed)
         MsgCenter.putMsg("Database populated and transaction committed");
      else
         MsgCenter.putMsg("Unable to populate the database");
      }
   }


class ClearDatabase extends Command
   {
   LibraryHandler libhand = Library.persistenceHandler;

   public ClearDatabase(UIClient c)
      {
      super(c, new String[] { "clear database", });
      }

   public void execute()
      {
      // start transaction if not already started
      boolean started = Library.persistenceHandler.startTransaction();

      if (Library.isDBEmpty())
         {
         MsgCenter.putMsg("The database is already empty!");

         if (started)
            Library.persistenceHandler.rollbackTransaction();

         return;
         }

      // clear everything
      clearBooks();
      clearCategories();
      clearVolunteers();
      clearBorrowers();

      // commit the transaction
      boolean committed = Library.persistenceHandler.commitTransaction();

      if (committed)
         MsgCenter.putMsg("Database cleared");
      else
         MsgCenter.putMsg("Unable to clear the database");
      }

   private void clearBooks()
      {
      List     books = libhand.findAll(Book.class);
      Iterator iter = books.iterator();

      while (iter.hasNext())
         libhand.delete(iter.next());
      }

   private void clearCategories()
      {
      List     categories = libhand.findAll(Category.class);
      Iterator iter = categories.iterator();

      while (iter.hasNext())
         libhand.delete(iter.next());
      }

   private void clearVolunteers()
      {
      List     vs   = libhand.findAll(Volunteer.class);
      Iterator iter = vs.iterator();

      while (iter.hasNext())
         libhand.delete(iter.next());
      }

   private void clearBorrowers()
      {
      List     bs   = libhand.findAll(Borrower.class);
      Iterator iter = bs.iterator();

      while (iter.hasNext())
         libhand.delete(iter.next());
      }
   }

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
package com.ysoft.jdo.book.library;

import java.util.*;

import javax.jdo.*;


public class Borrower implements InstanceCallbacks
   {
   private String oidString; // JDO transient field

   // persistent fields
   private String name;

   //private ArrayList books;  // intellibo cannot handle HashSet (v 2.5)
   private HashSet   books;
   private Volunteer volunteer;

   private Borrower()
      {
      // used only by JDO
      }

   public Borrower(String name)
      {
      if (name != null)
         name = name.trim();

      if ((name == null) || (name.length() <= 0))
         throw new IllegalArgumentException("name cannot be empty or null");

      this.name    = name;
      books        = new HashSet();
      }

   public String getName()
      {
      return name;
      }

   public List getBooks()
      {
      return new ArrayList(books);
      }

   public void borrowBook(Book book)
      {
      if (book == null)
         return;

      books.add(book);
      book.setBorrower(this);
      }

   public void returnBook(Book book)
      {
      if (book == null)
         return;

      books.remove(book);
      book.clearBorrower();
      }

   void setVolunteer(Volunteer volunteer)
      {
      this.volunteer = volunteer;
      }

   public Volunteer getVolunteer()
      {
      return volunteer;
      }

   public String toString()
      {
      return "borrower [" + getOIDString() + "] \"" + name + "\"";
      }

   public int hashCode()
      {
      if (oidString != null)
         return oidString.hashCode();

      return super.hashCode();
      }

   public boolean equals(Object obj)
      {
      if (obj == this)
         return true;

      if (obj == null)
         return false;

      if (oidString == null)
         return false;

      if (!(getClass() != obj.getClass()))
         return false;

      Borrower borrower = (Borrower) obj;

      return oidString.equals(borrower.oidString);
      }

   public void setOIDString(String name)
      {
      oidString = name;
      }

   public String getOIDString()
      {
      if (oidString != null)
         return oidString;
      else
         return "OID unknown";
      }

   // the methods required by the InstanceCallbacks interface
   // our goal is to pull the oidString after load
   public void jdoPostLoad()
      {
      if (oidString == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }

   public void jdoPreClear()
      {
      if (oidString == null)
         setOIDString(JDOHelper.getObjectId(this).toString());
      }

   public void jdoPreDelete()
      {
      }

   public void jdoPreStore()
      {
      }
   }

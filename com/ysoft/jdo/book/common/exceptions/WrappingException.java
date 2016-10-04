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
package com.ysoft.jdo.book.common.exceptions;

import java.io.*;


/**
 * WrappingException handles a nested exception.  If the
 * WrappingException is handled locally then the nested exception
 * is available.  But if the WrappingException is passed to a remote
 * client, then serialization saves the stack trace of both the
 * WrappingException and its nested exception, but it does not pass
 * the nested exception across the wire.  Thus the client does not
 * need to load the server side exception class.
 */
public class WrappingException extends Exception
   {
   private static final String TAB_EXPANSION     = "     ";
   private String              savedStackTrace;
   private transient boolean   local_exception   = true;
   private transient Exception wrapped_exception;

   /**
    * Creates an exception with no message and no wrapped exception.
    */
   public WrappingException()
      {
      this(null, null);
      }

   /**
    * Creates an exception with a message, but no wrapped exception.
    */
   public WrappingException(String msg)
      {
      this(msg, null);
      }

   /**
    * Creates an exception that wraps a exception, but without a message of its own.
    */
   public WrappingException(Exception e)
      {
      this(null, e);
      }

   public WrappingException(String msg, Exception e)
      {
      super(msg);
      wrapped_exception = e;
      }

   /**
    * If there is a nested Exception, returns it; otherwise,
    * returns null.
    */
   public Exception getWrappedException()
      {
      return wrapped_exception;
      }

   /**
    * Returns true if this exception was created to wrap another exception.
    * Returns false otherwise.
    */
   public boolean isWrappingException()
      {
      return wrapped_exception != null;
      }

   /**
    * Returns true if this exception was created remotely.
    */
   public boolean wasCreatedRemotely()
      {
      return !local_exception;
      }

   /**
    * Return the stack trace as a string.
    */
   public String getStackTraceString()
      {
      return getStackTraceString(this);
      }

   /**
    * This method may be called on any throwable to get
    * the stack trace as a string.  This is useful when
    * printing stack traces to a JspWriter which is not
    * a PrintWriter.
    */
   public static String getStackTraceString(Throwable t)
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

   /**
    * Save the stack trace, and suppress the wrapped exception
    */
   public void saveStackTrace()
      {
      if (savedStackTrace == null)
         {
         savedStackTrace      = getStackTraceString();
         wrapped_exception    = null;
         }
      }

   /**
    * Returns the stack trace for this exception saved
    * by the saveStackTrace method.
    */
   public String getSavedStackTrace()
      {
      return savedStackTrace;
      }

   public void printStackTrace()
      {
      printStackTrace(System.err);
      }

   public void printStackTrace(java.io.PrintStream stream)
      {
      if (wasCreatedRemotely())
         {
         stream.println("Remotely thrown exception: " + getMessage());

         // the wrapped exception's stack trace is embedded in
         // the saved wrapping exception's stack trace.
         String trace = getSavedStackTrace();

         if (trace == null)
            stream.println("remote stack trace not saved");
         else
            stream.println(getSavedStackTrace());
         }
      else
         {
         super.printStackTrace(stream);

         if (isWrappingException())
            {
            stream.println("Wrapped exception:");
            wrapped_exception.printStackTrace(stream);
            }
         }
      }

   public void printStackTrace(java.io.PrintWriter writer)
      {
      if (wasCreatedRemotely())
         {
         writer.println("Remotely thrown exception:");

         // the wrapped exception's stack trace is embedded in
         // the saved wrapping exception's stack trace.
         String trace = getSavedStackTrace();

         if (trace == null)
            writer.println("remote stack trace not saved");
         else
            writer.println(getSavedStackTrace());
         }
      else
         {
         super.printStackTrace(writer);

         if (isWrappingException())
            {
            writer.println("Wrapped exception:");
            wrapped_exception.printStackTrace(writer);
            }
         }
      }

   public String toString()
      {
      return super.toString();
      }

   // intercept the serialization mechanism
   private void writeObject(java.io.ObjectOutputStream stream)
         throws IOException
      {
      saveStackTrace();
      stream.defaultWriteObject();
      }
   }

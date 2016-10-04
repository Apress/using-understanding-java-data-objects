/*
Copyright (c) 2003 Yankee Software.

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
David Ezzio       03/19/03   Created
*/
package com.ysoft.jdo.book.common.ejb;

import java.io.*;

import javax.ejb.EJBException;


public class EJBHelper
   {
   // the default array size of 32 is very likely too small
   private static final int INITIAL_ARRAY_SIZE = 1000;

   /**
    * Generates a graph of unmanaged objects starting with obj.  This insures
    * that the response is generated before the tx is completed and before
    * the PM is closed.  The graph is generated via serialization-deserialization
    * using a byte buffer.
    */
   public static Object respond(Object obj)
      {
      if (obj == null)
         return null;

      try
         {
         ByteArrayOutputStream outBytes = new ByteArrayOutputStream(INITIAL_ARRAY_SIZE);
         ObjectOutputStream    out = new ObjectOutputStream(outBytes);

         out.writeObject(obj);
         out.close();

         ByteArrayInputStream inBytes = new ByteArrayInputStream(outBytes.toByteArray());
         ObjectInputStream    in = new ObjectInputStream(inBytes);

         Object               retv = in.readObject();
         in.close();

         return retv;
         }
      catch (ClassNotFoundException e)
         {
         throw new EJBException("Exception serializing response: ", e);
         }
      catch (IOException e)
         {
         throw new EJBException("Exception serializing response: ", e);
         }
      }
   }

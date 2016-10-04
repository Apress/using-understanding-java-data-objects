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
package com.ysoft.jdo.book.common;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Properties;


public class Application
   {
   private static HashSet    loadedProps      = new HashSet();
   private static Properties top;
   private static boolean    attemptedTopLoad;

   static
      {
      Copyright.stdout();
      }

   /**
    * Loads the properties in "package.properties" in the
    * package of the class passed, if they have not already been
    * loaded.  The loaded properties become accessible through
    * system properites.
    */
   public static void loadProperties(Class c)
         throws IOException
      {
      synchronized (Application.class)
         {
         if (!attemptedTopLoad)
            {
            attemptedTopLoad = true;

            try
               {
               top = System.getProperties();
               }
            catch (SecurityException e)
               {
               System.err.println(
                  "Unable to load system properties due to security manager");
               }
            }
         }

      URL url = c.getResource("package.properties");

      if (url == null)
         {
         throw new IOException("No package.properties file found for class: " +
            c.toString());
         }

      // if the properties for this class's package are not already loaded
      if (loadedProps.add(url))
         {
         InputStream stream = c.getResourceAsStream("package.properties");

         if (stream == null)
            throw new IOException(
               "No package.properties file found for class: " + c.toString());

         Properties props = new Properties(top);
         props.load(stream);
         stream.close();
         top = props;

         // this call allows various system supplied calls that work with
         // the system properties to work for the application properties
         // as well.
         System.setProperties(top);
         }
      }
   }

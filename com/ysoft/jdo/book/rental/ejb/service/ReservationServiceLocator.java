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
David Ezzio       11/01/02   Created
*/
package com.ysoft.jdo.book.rental.ejb.service;

import java.io.IOException;
import java.rmi.*;
import java.util.*;

import javax.ejb.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;

import com.ysoft.jdo.book.common.MsgCenter;
import com.ysoft.jdo.book.factory.JDOFactory;
import com.ysoft.jdo.book.rental.service.ReservationException;


public class ReservationServiceLocator
   {
   private static ReservationServiceHome home;

   public static synchronized ReservationService get()
         throws NamingException, CreateException, RemoteException
      {
      ReservationServiceRemote remote  = null;
      ReservationService       service = null;

      // we retry once if the home interface is cached
      for (int x = 0; x <= 1; x++)
         {
         try
            {
            if (home == null)
               {
               // if failure encountered, we won't retry
               x++;

               // load the naming context configuration
               Properties namingProps = JDOFactory.loadProperties(
                     "jndi.properties");

               // Get a naming context
               InitialContext jndiContext = new InitialContext(namingProps);

               // Get a reference to the Bean's Home interface
               Object ref = jndiContext.lookup("ReservationService");

               // cast to home interface
               home = (ReservationServiceHome) PortableRemoteObject.narrow(ref,
                     ReservationServiceHome.class);
               }

            // Create a remote ReservationService session bean
            remote    = home.create();

            // Create a reference to just the ReservationService interface
            service = (ReservationService) PortableRemoteObject.narrow(remote,
                  ReservationService.class);

            return service;
            }
         catch (CreateException e)
            {
            if (x >= 1)
               throw e;

            home = null;
            MsgCenter.putException("Caught exception attempting to get ReservationService EJB, retrying",
               e);
            }
         catch (RemoteException e)
            {
            if (x >= 1)
               throw e;

            home = null;
            MsgCenter.putException("Caught exception attempting to get ReservationService EJB, retrying",
               e);
            }
         catch (NamingException log_it)
            {
            MsgCenter.putException("Caught NamingException for ReservationService",
               log_it);
            throw log_it;
            }
         catch (IOException log_it)
            {
            MsgCenter.putException("Caught IOException loading jndi.properties",
               log_it);
            throw new NamingException(
               "JNDI context properties (file: jndi.properties) not found");
            }
         }

      // should never be executed
      return null;
      }

   public static void release(ReservationService service)
      {
      ReservationServiceRemote remote = (ReservationServiceRemote) service;

      try
         {
         remote.remove();
         }
      catch (RemoteException log_it)
         {
         MsgCenter.putException("Caught exception removing EJB", log_it);
         }
      catch (RemoveException log_it)
         {
         MsgCenter.putException("Caught exception removing EJB", log_it);
         }
      }
   }

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
package com.ysoft.jdo.book.rental.servlet.ejb;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

import javax.ejb.CreateException;
import javax.jdo.*;
import javax.naming.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.ysoft.jdo.book.common.*;
import com.ysoft.jdo.book.rental.ejb.service.*;
import com.ysoft.jdo.book.rental.persistent.*;
import com.ysoft.jdo.book.rental.service.*;
import com.ysoft.jdo.book.rental.servlet.util.*;


public class ReservationServlet extends HttpServlet
      implements ReservationServletConstants
   {
   private static final String JSP_EXCEPTION_VAR = "javax.servlet.jsp.jspException";

   // servlet life-cycle callback
   // no need to call super.init()
   public void init()
         throws ServletException
      {
      // init the MsgCenter servlet logger
      new ServletLogger(getServletConfig().getServletContext());
      Copyright.stdout();
      MsgCenter.putMsg(getServletInfo());

      // debugging
      new StdoutMsgHandler();
      }

   // basic info
   public String getServletInfo()
      {
      return "The Maine Lighthouse Rental Servlet, an example for Using and Understanding Java Data Objects\n" +
      "Uses ReservationService EJB";
      }

   /**
    * The doPost method is called by the servlet container when a HTTP POST request is received.
    */
   public void doPost(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
      {
      respond(request, response);
      }

   /**
    * The doGet method is called by the servlet container when a HTTP GET request is received.
    */
   public void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
      {
      respond(request, response);
      }

   /**
    * The respond method handles both HTTP GET and POST methods in the same way.
    * All dynamic requests are handled by performing an action and displaying
    * a result (the MVC pattern for servlets).  The action puts dynamic objects
    * into the session space, hence multithreaded access to the session must be serialized.
    * <P>
    * In general, the SessionLock will not be contested.  The user will click and
    * wait for a response.  The use of the SessionLock is design only to deal
    * with the exceptional circumstance where the user is generating requests
    * before receiving the earlier response.
    * <P>
    * As a general strategy to improve performance and make all actions idempotent
    * under the exceptional circumstance mentioned earlier,
    * the SessionLock will indicate for the response whether it should perform the
    * action AND display the results or whether it should display the result of the
    * previous action.
    */
   private void respond(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
      {
      SessionLock        lock      = null;
      ReservationService service   = null;
      String             forwardTo = MAINE_PAGE;

      try
         {
         // get the data service for this request
         service    = ReservationServiceLocator.get();

         // get the session lock and lock it for no action
         // we have twenty seconds to release the lock before our
         // claim is discarded
         lock = SessionLock.getInstance(request);

         int action = lock.lock(NO_ACTION, 20);

         // get/create the model, which is
         // stored in session space so it can be reused
         ReservationModel model = ModelHandle.getInstance(request).getModel();

         logMsg("Request is from customer: " + model.getCustomerName());

         // if the lock did not determine our action,
         // determine it now
         if (action == NO_ACTION)
            {
            action = getAction(request, model);
            lock.setAction(action);
            }

         // handle each action
         switch (action)
            {
            case MODIFY_RESERVATIONS:

               if (!handleReservations(request, service, model))
                  {
                  // got an exception handling reservations.
                  // Forward to the exception page
                  forwardTo = CONCURRENCY_EXCEPTION_PAGE;
                  break;
                  }

               viewCustomerReservations(request, service, model);

            // purposeful fall through
            case DISPLAY_MODIFY_RESERVATIONS:

               //logMsg("Post: modify reservations");
               break;

            case IDENTIFY_CUSTOMER:

               switch (identifyCustomer(request, service, model))
                  {
                  case VIEW_CUSTOMER:
                     viewCustomerReservations(request, service, model);
                     break;

                  case VIEW_BOTH:
                     viewBoth(request, service, model);
                     break;

                  case VIEW_UNCHANGED:
                     break;

                  default:
                     viewAvailableRentals(request, service, model);
                     break;
                  }

            // purposeful fall through
            case DISPLAY_IDENTIFY_CUSTOMER:
               break;

            case VIEW_AVAILABLE:
               viewAvailableRentals(request, service, model);

            // purposeful fall through
            case DISPLAY_VIEW_AVAILABLE:
               break;

            case VIEW_CUSTOMER:
               viewCustomerReservations(request, service, model);

            // purposeful fall through
            case DISPLAY_VIEW_CUSTOMER:
               break;

            case VIEW_BOTH:
               viewBoth(request, service, model);

            // purposeful fall through
            case DISPLAY_VIEW_BOTH:
               break;

            case VIEW_LIGHTHOUSE:
            case DISPLAY_VIEW_LIGHTHOUSE:
               forwardTo = LIGHTHOUSE_PAGE;
               break;

            default:
               logMsg("Post: unrecognized action");
               throw new ServletException("unrecognized action");
            }

         // since the model doesn't have live application data objects
         // close the service right away to conserve resources
         ReservationServiceLocator.release(service);
         service = null;

         // forward to display page
         forward(forwardTo, request, response);
         }
      catch (SessionLockException e)
         {
         // debugging
         logMsg("Caught SessionLockException, throwing ServletException", e);

         throw new ServletException("lock failure", e);
         }

      /*
      catch (ReservationException e)
         {
         // debugging
         logMsg("Caught ReservationException, throwing ServletException", e);

         throw new ServletException("failure in ReservationService", e);
         }
      */
      catch (RemoteException e)
         {
         logMsg("Caught RemoteException in respond, throwing ServletException",
            e);
         throw new ServletException("failure connecting to remote ReservationService",
            e);
         }
      catch (NamingException e)
         {
         logMsg("Caught NamingException in respond, throwing ServletException",
            e);
         throw new ServletException("failure connecting to remote ReservationService",
            e);
         }
      catch (CreateException e)
         {
         logMsg("Caught CreateException in respond, throwing ServletException",
            e);
         throw new ServletException("failure connecting to remote ReservationService",
            e);
         }

      // debugging catches
      catch (JDOException e)
         {
         logMsg("Caught JDOException in respond, rethrowing", e);
         throw e;
         }
      catch (RuntimeException e)
         {
         logMsg("Caught RuntimeException in respond, rethrowing", e);
         throw e;
         }
      catch (ServletException e)
         {
         logMsg("Caught ServletException in respond, rethrowing", e);
         throw e;
         }

      // end of debugging catches
      finally
         {
         // close the service if not already closed
         if (service != null)
            {
            ReservationServiceLocator.release(service);
            }

         if (lock != null)
            lock.unlock();
         }
      }

   private void viewAvailableRentals(HttpServletRequest request,
      ReservationService service, ReservationModel model)
         throws RemoteException
      {
      logMsg("in viewAvailableRentals");

      // get the available rentals
      Collection rentals     = service.getAvailableRentals();
      ArrayList  listRentals = new ArrayList(rentals);
      List       lighthouses = service.getLighthouses();

      // update the model
      model.initRentals(listRentals, lighthouses);
      model.setView(VIEW_AVAILABLE);
      model.bumpStep();
      }

   private void viewCustomerReservations(HttpServletRequest request,
      ReservationService service, ReservationModel model)
         throws RemoteException
      {
      logMsg("in viewCustomerReservations");

      // get the customer's reservations
      Collection rentals     = service.getCustomerRentals(model.getCustomer());
      ArrayList  listRentals = new ArrayList(rentals);
      List       lighthouses = service.getLighthouses();

      // update the model
      model.initRentals(listRentals, lighthouses);
      model.setView(VIEW_CUSTOMER);
      model.bumpStep();
      }

   private void viewBoth(HttpServletRequest request,
      ReservationService service, ReservationModel model)
         throws RemoteException
      {
      logMsg("in viewBoth");

      // get the available rentals and the customer's reservations
      Collection rentals     = service.getCustomerAndAvailableRentals(model.getCustomer());
      ArrayList  listRentals = new ArrayList(rentals);
      List       lighthouses = service.getLighthouses();

      // update the model
      model.initRentals(listRentals, lighthouses);
      model.setView(VIEW_BOTH);
      model.bumpStep();
      }

   // returns VIEW_UNCHANGED if the view should not be changed,
   // othewise, returns one of the view values
   private int identifyCustomer(HttpServletRequest request,
      ReservationService service, ReservationModel model)
         throws RemoteException
      {
      String   c            = request.getParameter(CUSTOMER_PARAM);
      Customer old_customer = model.getCustomer();
      Customer new_customer = null;
      int      retv         = VIEW_UNCHANGED;
      logMsg("in identifyCustomer with customer value: " + c);

      if ((c != null) && (c.length() > 0) &&
               !c.equalsIgnoreCase(NO_CUSTOMER_STRING))
         {
         List list = service.getCustomers(c);

         if (list.size() == 1)
            new_customer = (Customer) list.get(0);
         }

      if ((old_customer == null) || (new_customer == null))
         {
         if (old_customer != new_customer)
            {
            model.setCustomer(new_customer);
            retv = model.getView();
            }
         else
            retv = VIEW_AVAILABLE;
         }
      else
         {
         // always set the customer so that the persistent
         // Customer referenced by the reserved rentals will
         // be retrieved and made transient.
         model.setCustomer(new_customer);

         // check if the memory object refer to distinct customers
         if (!old_customer.equals(new_customer))
            {
            retv = model.getView();
            }
         }

      //logMsg("in identifyCustomer, old: " + old_customer + "; new: " + new_customer);
      return retv;
      }

   private boolean handleReservations(HttpServletRequest request,
      ReservationService service, ReservationModel model)
         throws RemoteException
      {
      boolean  retv         = true;
      String[] reservations = request.getParameterValues(RESERVATION_PARAM);

      // debugging
      if (reservations == null)
         logMsg("Got a null array for reservations");
      else
         logMsg("Got " + reservations.length + " reservations");

      Collection modifiedRentals = model.getModifiedRentals(reservations);
      logMsg("" + modifiedRentals.size() + " reservations were modified");

      if (modifiedRentals.size() > 0)
         {
         try
            {
            service.flipReservations(modifiedRentals, model.getCustomer());
            }
         catch (OptimisticReservationException e)
            {
            // put the exception in JSP scoped variable space
            request.setAttribute(JSP_EXCEPTION_VAR, e);

            // no need to rollback transaction
            // since the service is good for this request only
            // indicate to caller that exception was caught and needs to be handled
            retv = false;
            }
         catch (ReservationException e)
            {
            // put the exception in JSP scoped variable space
            request.setAttribute(JSP_EXCEPTION_VAR, e);

            // indicate to caller that exception was caught and needs to be handled
            retv = false;
            }
         }

      return retv;
      }

   private int getAction(HttpServletRequest request, ReservationModel model)
      {
      String  path_info   = request.getPathInfo();
      int     retv        = NO_ACTION;
      boolean new_session = isNewSession(request);

      if ((path_info != null) && (path_info.charAt(0) == '/'))
         path_info = path_info.substring(1);

      //logMsg("Extra Path info: " + path_info);
      if ((path_info == null) || (path_info.length() <= 0))
         retv = VIEW_AVAILABLE;
      else if (path_info.equalsIgnoreCase("view"))
         {
         String view_type = request.getParameter("view");

         if (new_session || (view_type == null) || (view_type.length() < 0))
            retv = VIEW_AVAILABLE;
         else if (view_type.equalsIgnoreCase(VIEW_AVAILABLE_STRING))
            retv = VIEW_AVAILABLE;
         else if (view_type.equalsIgnoreCase(VIEW_BOTH_STRING))
            {
            if (model.isCustomerKnown())
               retv = VIEW_BOTH;
            else
               retv = VIEW_AVAILABLE;
            }
         else if (view_type.equalsIgnoreCase(VIEW_CUSTOMER_STRING))
            {
            if (model.isCustomerKnown())
               retv = VIEW_CUSTOMER;
            else
               retv = VIEW_AVAILABLE;
            }
         }
      else if (path_info.equalsIgnoreCase("customer"))
         {
         retv = IDENTIFY_CUSTOMER;
         }

      // we check for a new session now
      // because we've allowed a new sesson to identify the customer just above
      else if (new_session)
         {
         retv = VIEW_AVAILABLE;
         }
      else if (path_info.equalsIgnoreCase("reservations"))
         {
         // check step to insure that the browser's view of reservations
         // corresponds to the model (it might not if the user clicks on back
         // or forward buttons).
         String step = request.getParameter(STEP_PARAM);

         // not all browsers honor the DISABLED attribute of a INPUT CHECKBOX field
         // so reroute any requests that mistakenly are made
         if (model.isCustomerKnown() && model.isCurrentStep(step))
            retv = MODIFY_RESERVATIONS;
         else
            retv = model.getView();
         }
      else if (path_info.equalsIgnoreCase("lighthouse"))
         {
         retv = VIEW_LIGHTHOUSE;
         }
      else if (path_info.equalsIgnoreCase("refresh"))
         {
         // since every request gets a refreshed view
         // this is effectively a review
         retv = model.getView();
         }

      return retv;
      }

   private void forward(String jspPage, HttpServletRequest request,
      HttpServletResponse response)
         throws ServletException, IOException
      {
      request.getRequestDispatcher(jspPage).forward(request, response);
      }

   private void logMsg(String msg)
      {
      System.out.println(msg);
      }

   private void logMsg(String msg, Throwable t)
      {
      System.out.println(msg);

      t.printStackTrace(System.out);

      if (t instanceof ServletException)
         {
         Throwable root = ((ServletException) t).getRootCause();

         if (root != null)
            {
            System.out.println("Root cause:");
            root.printStackTrace(System.out);
            }
         }
      }

   private boolean isNewSession(HttpServletRequest request)
      {
      HttpSession session = request.getSession();
      boolean     retv = session.isNew();

      if (retv)
         logMsg("Session is new");

      return retv;
      }
   }


/**
 * Wraps the ReservationModel and stores it in the servlet's session space.
 * Provides utility methods to insure that the model is also stored in the
 * request variable space where the JSP pages will look for it.
 * This wrapper class also provides debugging capabilities.
 */
class ModelHandle implements HttpSessionBindingListener, Serializable
   {
   // private name for this handle in session variable space
   private static final String HANDLE_NAME = "com.ysoft.jdo.book.rental.servlet.util.ModelHandle";

   // in the opr design, the model is serialized
   private ReservationModel model;

   private ModelHandle()
      {
      }

   public static ModelHandle getInstance(HttpServletRequest request)
      {
      HttpSession session = request.getSession();

      // find or create the handle in the session variable space
      ModelHandle handle = (ModelHandle) session.getAttribute(HANDLE_NAME);

      if (handle == null)
         {
         handle = new ModelHandle();
         session.setAttribute(HANDLE_NAME, handle);
         MsgCenter.putMsg("Created model handle in the session");
         }
      else
         MsgCenter.putMsg("Found model model in the session");

      // initialize the model
      handle.initModel(request);

      return handle;
      }

   public ReservationModel getModel()
      {
      return model;
      }

   private void initModel(HttpServletRequest request)
      {
      if (model == null)
         model = new ReservationModel(true);

      request.setAttribute(ReservationServletConstants.MODEL_NAME, model);
      }

   // required by HttpSessionBindingListener
   // used for debugging only
   public void valueBound(HttpSessionBindingEvent e)
      {
      // debugging
      MsgCenter.putMsg("ModelHandle (" + System.identityHashCode(this) +
         ") placed in session (" + System.identityHashCode(e.getSession()) +
         ")");
      }

   public void valueUnbound(HttpSessionBindingEvent e)
      {
      // debugging
      MsgCenter.putMsg("ModelHandle (" + System.identityHashCode(this) +
         ") removed from session (" + System.identityHashCode(e.getSession()) +
         ")");
      }

   // hook into the serialization mechanism and report any errors
   // used for debugging
   private void writeObject(java.io.ObjectOutputStream stream)
         throws IOException
      {
      try
         {
         MsgCenter.putMsg("Serializing the ModelHandle");

         // default serialization action
         stream.defaultWriteObject();
         }
      catch (IOException e)
         {
         MsgCenter.putException("caught during model serialization", e);
         throw e;
         }
      catch (RuntimeException e)
         {
         MsgCenter.putException("caught during model serialization", e);
         throw e;
         }
      }
   }


/*
  for debugging
class TestSerializable
   {
   public static void test(Serializable o)
      {
      try
         {
         ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("temp.ser"));
         out.writeObject(o);
         out.close();
         MsgCenter.putMsg("Serialized model to file temp.ser");
         }
      catch (Exception e)
         {
         MsgCenter.putException("Caught exception serializing the model", e);
         }

      try
         {
         ObjectInputStream in = new ObjectInputStream(new FileInputStream("temp.ser"));
         Object obj = in.readObject();
         MsgCenter.putMsg("Read model from file temp.ser");
         in.close();
         }
      catch (Exception e)
         {
         MsgCenter.putException("Caught exception deserializing the model", e);
         }
      }
   }
*/

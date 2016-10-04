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
David Ezzio       10/01/02   Created
*/
package com.ysoft.jdo.book.rental.servlet.util;

import java.io.Serializable;
import java.util.*;

import javax.servlet.ServletContext;
import javax.servlet.http.*;

import com.ysoft.jdo.book.common.MsgCenter;


/**
 * The SessionLock bean allows the servlet response thread to lock access to
 * the session.   As long as the response thread holds the lock,
 * no other response thread can lock the session.
 * This insures single-threaded behavior within the session.
 * <p>
 * The lock is automatically
 * place into the session name space.
 * <p>
 * The request to lock has a time out associated with it.
 * If the lock cannot be claimed within the timeout, or if anything else
 * prevents the lock from occurring, a SessionLockException is thrown.
 * <p>
 * The request to lock has an expiration associated with it.
 * After the lock is claimed, if it is not released within the
 * expiration period, then another thread may claim the lock.
 */
public class SessionLock implements Serializable
   {
   public static final int     DISPLAY_ACTION_LINE = 1000;
   public static final int     NO_ACTION         = 0;
   private static final String SESSION_LOCK_NAME = "com.ysoft.jdo.book.rental.servlet.util.SessionLock";

   // all of these values return to their
   // Java defaults if the SessionLock is serialized.
   // This won't happen unless the session is unused,
   // in which case since there is no thread executing
   // in the session, the lock should be available.
   private transient int    mCountWaiting;
   private transient int    mNestingCount;
   private transient long   mClaimExpiresOn;
   private transient Thread mLockingThread;

   //private transient HttpSession mSession;
   private transient int mAction;
   private int           mLockCount;

   // private constructor to enforce the factory pattern
   private SessionLock()
      {
      }

   /**
    * Returns a SessionLock that is found or placed in the session variable space.
    */
   public static SessionLock getInstance(HttpSession session)
         throws SessionLockException
      {
      try
         {
         SessionLock lock = null;

         //debugging
         //boolean new_lock = false;
         synchronized (session)
            {
            lock = (SessionLock) session.getAttribute(SESSION_LOCK_NAME);

            if (lock == null)
               {
               lock = new SessionLock();

               //new_lock = true;
               session.setAttribute(SESSION_LOCK_NAME, lock);
               }

            // always set this as it may be erased by serialization
            //lock.mSession = session;
            }

         //lock.log("returning " + (new_lock ? "new" : "existing") + " lock instance");
         return lock;
         }
      catch (RuntimeException e)
         {
         throw new SessionLockException("could not obtain session lock", e);
         }
      }

   /**
    * Returns a SessionLock that is found or placed in the session variable space.
    */
   public static SessionLock getInstance(HttpServletRequest request)
         throws SessionLockException
      {
      return getInstance(request.getSession());
      }

   /**
    * Claim the session lock for the thread processing the request.
    * The thread with the lock may lock the thread any number of times.
    * The thread must call unlock as many times as it calls
    * lock; otherwise, the session will remain locked until the
    * lock expires.
    *
    * @param action The action that the lock holder will take if the lock
    *                is uncontested.  The action that subsequent lock holders
    *                will adopt if they find themselves contesting the lock.
    * @param expiresMs How long the lock is good for after it is acquired.
    *                This must be > 0.  It should be set to a value large
    *                enough for the work to be done, but small enough
    *                to make the session eventually accessible before the
    *                session times out, if something bad happens to the
    *                lock holder.
    * @return The action that the lock holder should execute.
    *
    * @exception SessionLockException when a runtime exception prevents
    *            obtaining the lock, or when the wait is interrupted.
    */
   public synchronized int lock(int action, int expiresSeconds)
         throws SessionLockException
      {
      if ((expiresSeconds <= 0) || (action < 0) ||
               (action >= DISPLAY_ACTION_LINE))
         throw new IllegalArgumentException();

      long expiresMs = expiresSeconds * 1000;

      try
         {
         int count = 0; // if greater than zero then we have waited

         for (;; count++)
            {
            if (mLockingThread != null)
               {
               // if our thread has previously locked,
               // then we simply bump the nesting count
               if (mLockingThread == Thread.currentThread())
                  {
                  mNestingCount++;
                  mLockCount++;
                  return action;
                  }

               // check the expiration
               if (mClaimExpiresOn < System.currentTimeMillis())
                  {
                  log("claimed session lock has expired");
                  mLockCount++;
                  return claim(expiresMs, action);
                  }

               // otherwise we bump the waiting count and wait
               mCountWaiting++;
               log("Thread: " + Thread.currentThread().hashCode() +
                  ", joining wait " +
                  ((mCountWaiting > 1)
                  ? ("with " + (mCountWaiting - 1) + " others") : "") +
                  " for the " + (count + 1) + " time");

               try
                  {
                  // wait for five seconds or until notified
                  wait(5000);
                  }
               catch (InterruptedException e)
                  {
                  throw new SessionLockException("interrupted", e);
                  }

               mCountWaiting--;
               }

            // otherwise, take the available lock
            else
               {
               log("Thread: " + Thread.currentThread().hashCode() +
                  " takes the lock");
               mLockCount++;

               // if we have waited and the last action is not NO_ACTION,
               //  return last action + DISPLAY_ACTION_LINE
               if (count > 0)
                  {
                  if ((mAction < DISPLAY_ACTION_LINE) &&
                           (mAction != NO_ACTION))
                     action = mAction + DISPLAY_ACTION_LINE;
                  else
                     action = mAction;
                  }

               return claim(expiresMs, action);
               }
            }
         }
      catch (RuntimeException e)
         {
         throw new SessionLockException("could not obtain session lock", e);
         }
      }

   /**
    * Unlocks the lock.  If the caller has called
    * lock n times, then this method will unlock
    * the lock only on the nth call.
    */
   public synchronized void unlock()
      {
      // we do nothing if the current thread is not the owner
      // because the current thread may have held the lock
      // beyond the expiration period.
      if (mLockingThread == Thread.currentThread())
         {
         if (mNestingCount > 0)
            {
            mNestingCount--;
            }
         else
            {
            log("Thread: " + Thread.currentThread().hashCode() +
               " releases the lock");
            release();
            notifyAll();
            }
         }
      }

   /**
    * Returns the number of threads that are waiting to
    * lock the lock.
    */
   public synchronized int getCountWaiting()
      {
      return mCountWaiting;
      }

   /**
    * Returns the maximum remaining time that the current owning
    * thread will keep the session locked.
    */
   public synchronized long getLockRemainingMs()
      {
      return mClaimExpiresOn - System.currentTimeMillis();
      }

   /**
    * Returns the number of times that this lock has been locked.
    */
   public synchronized int getLockCount()
      {
      return mLockCount;
      }

   /**
    * Allows the caller to set the action after claiming the lock.
    * This is useful when the caller needs the lock to before he can
    * determine what action he will take.
    */
   public synchronized void setAction(int action)
      {
      mAction = action;
      }

   private int claim(long expiration_time, int action)
      {
      mLockingThread     = Thread.currentThread();
      mClaimExpiresOn    = System.currentTimeMillis() + expiration_time;
      mAction            = action;
      return mAction;
      }

   private void release()
      {
      mLockingThread     = null;
      mClaimExpiresOn    = 0;
      }

   private void log(String msg)
      {
      //if (msg != null && mSession != null)
      if (msg != null)
         {
         //ServletContext sc = mSession.getServletContext();
         //sc.log(msg);
         MsgCenter.putMsg("" + System.currentTimeMillis() + ": " + msg);
         }
      }
   }

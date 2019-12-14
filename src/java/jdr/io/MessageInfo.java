// File          : MessageInfo.java
// Purpose       : Information for message system
// Creation Date : 2015-10-02
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006 Nicola L.C. Talbot

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
*/
package com.dickimawbooks.jdr.io;

import java.io.*;

import com.dickimawbooks.jdr.*;

public class MessageInfo
{
   private MessageInfo()
   {
   }

   public MessageInfo(String action, Object value)
   {
      if (action == null) throw new NullPointerException();

      this.action = action;
      this.value = value;
   }

   public String getAction()
   {
      return action;
   }

   public Object getValue()
   {
      return value;
   }

   public static MessageInfo createMessage(String message)
   {
      return createMessage(message, true);
   }

   public static MessageInfo createMessage(String message, boolean newline)
   {
      return new MessageInfo(MESSAGE, 
         newline ? String.format("%s%n", message) : message);
   }

   public static MessageInfo createProgress(int max)
   {
      return new MessageInfo(PROGRESS, new Integer(max));
   }

   public static MessageInfo createIncProgress()
   {
      return createIncProgress(1);
   }

   public static MessageInfo createIncProgress(int increment)
   {
      return new MessageInfo(INCREMENT_PROGRESS, new Integer(increment));
   }

   public static MessageInfo createSetProgress(int progress)
   {
      return new MessageInfo(SET_PROGRESS, new Integer(progress));
   }

   public static MessageInfo createMaxProgress(int max)
   {
      return new MessageInfo(MAX_PROGRESS, new Integer(max));
   }

   public static MessageInfo createIndeterminate(boolean isOn)
   {
      return new MessageInfo(INDETERMINATE, new Boolean(isOn));
   }

   public static MessageInfo createWarning(String message)
   {
      return new MessageInfo(WARNING, message);
   }
   
   public static MessageInfo createWarning(Throwable cause)
   {
      return new MessageInfo(WARNING, cause);
   }
   
   public static MessageInfo createError(String message)
   {
      return new MessageInfo(ERROR, message);
   }
   
   public static MessageInfo createError(Throwable cause)
   {
      return new MessageInfo(ERROR, cause);
   }
   
   public static MessageInfo createFatalError(Throwable cause)
   {
      return new MessageInfo(FATAL_ERROR, cause);
   }
   
   public static MessageInfo createInternalError(String message)
   {
      return new MessageInfo(INTERNAL_ERROR, message);
   }
   
   public static MessageInfo createInternalError(Throwable cause)
   {
      return new MessageInfo(INTERNAL_ERROR, cause);
   }
   
   public static MessageInfo createVerbose(int level, String message)
   {
      return createVerbose(level, message, true);
   }

   public static MessageInfo createVerbose(int level, String message,
     boolean newline)
   {
      return new MessageInfo(String.format(
        newline ? "%s%d%n" : "%s%d", VERBOSE, level), message);
   }
   
   public static MessageInfo createVerbose(int level, Throwable cause)
   {
      return new MessageInfo(String.format("%s%d", VERBOSE, level), cause);
   }

   public static MessageInfo createSetActive(boolean isActive)
   {
      return new MessageInfo(ACTIVE, new Boolean(isActive));
   }
   
   public static MessageInfo createSetVisible(boolean isVisible)
   {
      return new MessageInfo(VISIBLE, new Boolean(isVisible));
   }
   
   private String action;
   private Object value;

   public static final String MESSAGE = "message";
   public static final String PROGRESS = "progress";
   public static final String INCREMENT_PROGRESS = "incprogress";
   public static final String SET_PROGRESS = "setprogress";
   public static final String MAX_PROGRESS = "maxprogress";
   public static final String INDETERMINATE = "indeterminate";
   public static final String WARNING = "warning";
   public static final String ERROR = "error";
   public static final String FATAL_ERROR = "fatalerror";
   public static final String INTERNAL_ERROR = "internalerror";
   public static final String ACTIVE = "active";
   public static final String VISIBLE = "visible";
   public static final String VERBOSE = "verbose";
}

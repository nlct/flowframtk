// File          : JDRDefaultMessage.java
// Purpose       : Default message system
// Creation Date : 12th June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2026 Nicola L.C. Talbot

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

import java.text.MessageFormat;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

/**
 * Default message system. This just prints messages to STDOUT and
 * warnings/errors to STDERR.
 * @author Nicola L C Talbot
 */

public class JDRDefaultMessage extends JDRMessagePublisher
{
   public JDRDefaultMessage()
   {
      this(null);
   }

   public JDRDefaultMessage(TeXJavaHelpLib helpLib)
   {
      publisher = this;
      helpLib = helpLib;

      appVersion = "??";

      if (helpLib == null)
      {
         appName = "jdr/io";
      }
      else
      {
         appName = helpLib.getApplicationName();
      }
   }

   public JDRDefaultMessage(TeXJavaHelpLib helpLib, String appName, String appVersion)
   {
      publisher = this;
      helpLib = helpLib;
      this.appName = appName;
      this.appVersion = appVersion;
   }

   @Override
   public void postMessage(MessageInfo info)
   {
      String action = info.getAction();

      if (action.equals(MessageInfo.PROGRESS))
      {
         resetProgress(((Integer)info.getValue()).intValue());
      }
      else if (action.equals(MessageInfo.INCREMENT_PROGRESS))
      {
         incrementProgress(((Integer)info.getValue()).intValue());
      }
      else if (action.equals(MessageInfo.SET_PROGRESS))
      {
         setProgress(((Integer)info.getValue()).intValue());
      }
      else if (action.equals(MessageInfo.MAX_PROGRESS))
      {
         setMaxProgress(((Integer)info.getValue()).intValue());
      }
      else if (action.equals(MessageInfo.INDETERMINATE))
      {
         setIndeterminate(((Boolean)info.getValue()).booleanValue());
      }
      else if (action.equals(MessageInfo.WARNING))
      {
         Object value = info.getValue();

         if (value instanceof Throwable)
         {
            warning((Throwable)value);
         }
         else
         {
            warning(value.toString());
         }
      }
      else if (action.equals(MessageInfo.ERROR))
      {
         Object value = info.getValue();

         if (value instanceof Throwable)
         {
            error((Throwable)value);
         }
         else
         {
            error(value.toString());
         }
      }
      else if (action.equals(MessageInfo.FATAL_ERROR))
      {
         Object value = info.getValue();

         if (value instanceof Throwable)
         {
            fatalerror((Throwable)value);
         }
         else
         {
            fatalerror(value.toString());
         }
      }
      else if (action.equals(MessageInfo.INTERNAL_ERROR))
      {
         Object value = info.getValue();

         if (value instanceof Throwable)
         {
            internalerror((Throwable)value);
         }
         else
         {
            internalerror(value.toString());
         }
      }
      else if (action.startsWith(MessageInfo.VERBOSE))
      {
         int level = 1;
         int idx = MessageInfo.VERBOSE.length();

         if (idx < action.length())
         {
            try
            {
               level = Integer.parseInt(action.substring(idx));
            }
            catch (NumberFormatException e)
            {
               debug(e);
            }
         }

         verbosenoln(level, info.getValue().toString());
      }
      else
      {
         message(info.getValue().toString());
      }
   }

   public void setIndeterminate(boolean indeterminate)
   {
      indeterminate_ = indeterminate;
   }

   @Override
   public boolean isIndeterminate()
   {
      return indeterminate_;
   }

   public void incrementProgress()
   {
      incrementProgress(1);
   }

   public void incrementProgress(int inc)
   {
      setProgress(progressValue+inc);
   }

   @Override
   public int getProgress()
   {
      return progressValue;
   }

   @Override
   public void shutdown()
   {
      clearEol();
   }

   public void clearEol()
   {
      if (eolRequired)
      {
         if (helpLib == null)
         {
            System.out.println();
         }

         eolRequired = false;
      }
   }

   public void setProgress(int value)
   {
      progressValue = value;

      if (showMessages)
      {
         if (isIndeterminate())
         {
            System.out.print(".");
         }
         else
         {
            int n = (int)Math.round(
               ((double)value/(double)progressMax)*100);

            System.out.format("%3d%%", n);

            System.out.print("\b\b\b\b");
         }

         eolRequired = true;
      }
   }

   public void setMaxProgress(int maxValue)
   {
      progressMax = maxValue;
   }

   public int getMaxProgress()
   {
      return progressMax;
   }

   public void resetProgress(int maxValue)
   {
      progressMax = maxValue;
      progressValue = 0;
   }

   public void resetProgress()
   {
      resetProgress(progressMax);
   }

   public void hideMessages()
   {
      if (!suspended)
      {
         showMessages = false;
      }
   }

   public void displayMessages()
   {
      if (!suspended)
      {
         showMessages = true;
      }
   }

   public void suspend()
   {
      suspended = true;
   }

   public void resume()
   {
      suspended = false;
   }

   public void message(String messageText)
   {
      clearEol();

      if (showMessages && !suspended)
      {
         if (helpLib == null)
         {
            System.out.print(messageText);
         }
         else
         {
            helpLib.message(messageText);
         }
      }
   }

   public void message(Exception excp)
   {
      clearEol();

      if (showMessages && !suspended)
      {
         if (helpLib == null)
         {
            System.out.print(excp.getLocalizedMessage());
         }
         else
         {
            helpLib.message(excp.getLocalizedMessage());
         }
      }
   }

   public void messageln(String messageText)
   {
      clearEol();

      if (showMessages && !suspended)
      {
         if (helpLib == null)
         {
            System.out.println(messageText);
         }
         else
         {
            helpLib.message(messageText);
         }
      }
   }

   public void messageln(Exception excp)
   {
      clearEol();

      if (showMessages && !suspended)
      {
         if (helpLib == null)
         {
            System.out.println(excp.getLocalizedMessage());
         }
         else
         {
            helpLib.message(excp.getLocalizedMessage());
         }
      }
   }

   public void warningnoln(String messageText)
   {
      clearEol();

      String warnMsg = 
        getMessageWithFallback("warning.tag", "Warning: {0}", messageText);

      if (helpLib == null)
      {
         System.err.print(warnMsg);
      }
      else
      {
         helpLib.warning(warnMsg);
      }
   }

   public void warningnoln(Throwable cause)
   {
      warningnoln(cause.getLocalizedMessage());
      debug(cause);
   }

   public void warning(String messageText)
   {
      clearEol();

      String warnMsg = 
        getMessageWithFallback("warning.tag", "Warning: {0}", messageText);

      if (helpLib == null)
      {
         System.err.println(warnMsg);
      }
      else
      {
         helpLib.warning(warnMsg);
      }
   }

   public void warning(Throwable cause)
   {
      warning(getMessage(cause));
      debug(cause);
   }

   public void errornoln(String messageText)
   {
      clearEol();

      String errMsg = 
        getMessageWithFallback("error.tag", "Error: {0}", messageText);

      if (helpLib == null)
      {
         System.err.print(errMsg);
      }
      else
      {
         helpLib.error(errMsg);
      }
   }

   public void errornoln(Throwable cause)
   {
      errornoln(cause.getLocalizedMessage());
      debug(cause);
   }

   public void error(String messageText)
   {
      clearEol();

      String errMsg = 
        getMessageWithFallback("error.tag", "Error: {0}", messageText);

      if (helpLib == null)
      {
         System.err.println(errMsg);
      }
      else
      {
         helpLib.error(errMsg);
      }
   }

   public void error(Throwable cause)
   {
      error(getMessage(cause));
      debug(cause);
   }

   public void internalerror(String messageText)
   {
      clearEol();

      String msg = getMessageWithFallback("internal_error.tag",
         "Internal error: {0}", messageText);

      if (helpLib == null)
      {
         System.err.println(msg);
      }
      else
      {
         helpLib.error(msg);
      }
   }

   public void internalerror(Throwable cause)
   {
      internalerror(getMessage(cause));
      debug(cause);
   }

   public void fatalerror(String messageText)
   {
      clearEol();

      String errMsg = getMessageWithFallback("error.fatal.tag", "Fatal error: {0}", messageText);

      if (helpLib == null)
      {
         System.err.println(errMsg);
      }
      else
      {
         helpLib.error(errMsg);
      }
   }

   public void fatalerror(Throwable cause)
   {
      fatalerror(getMessage(cause));
   }

   @Override
   public boolean isDebuggingOn()
   {
      return verbosity >= debugVerbosityThreshold;
   }

   public void debug(Throwable excp)
   {
      if (isDebuggingOn())
      {
         clearEol();
         excp.printStackTrace();
      }
   }

   public void debug(String msg)
   {
      if (isDebuggingOn())
      {
         clearEol();
         System.err.println(msg);
      }
   }

   public void verbosenoln(int level, String msg)
   {
      if (level <= verbosity)
      {
         message(msg);
      }
   }

   public void verbose(int level, String msg)
   {
      if (level <= verbosity)
      {
         messageln(msg);
      }
   }

   public String getMessage(Throwable e)
   {
      String msg = e.getLocalizedMessage();

      return msg == null ? e.getClass().getSimpleName() : msg;
   }

   @Override
   public String getMessageWithFallback(String label, String altFormat, 
     Object... params)
   {
      if (helpLib != null)
      {
         return helpLib.getMessageWithFallback(label, altFormat, params);
      }
      else if (altFormat != null)
      {
         return MessageFormat.format(altFormat, params);
      }
      else
      {
         if (params.length == 0)
         {
            return label;
         }

         String msg = label;

         String pre = "[";

         for (int i = 0; i < params.length; i++)
         {
            msg += pre + (String)params[i];
            pre = ",";
         }

         msg += "]";

         return msg;
      }
   }

   @Override
   public void setVerbosity(int level)
   {
      verbosity = level;
   }

   @Override
   public int getVerbosity()
   {
      return verbosity;
   }

   public void setDebugVerbosityThreshold(int level)
   {
      debugVerbosityThreshold = level;
   }

   public int getDebugVerbosityThreshold()
   {
      return debugVerbosityThreshold;
   }

   @Override
   public MessageInfoPublisher getPublisher()
   {
      return publisher;
   }

   @Override
   public void setPublisher(MessageInfoPublisher publisher)
   {
      this.publisher = publisher;
   }

   @Override
   public void publishMessages(MessageInfo... chunks)
   {
      for (MessageInfo info : chunks)
      {
         postMessage(info);
      }
   }

   @Override
   public String getApplicationName()
   {
      return appName;
   }

   @Override
   public String getApplicationVersion()
   {
      return appVersion;
   }

   private int progressMax=100;
   private int progressValue=0;
   private boolean indeterminate_=true;

   private boolean showMessages=false;
   private boolean suspended=false;
   private boolean eolRequired = false;

   private int verbosity=1;
   private int debugVerbosityThreshold = 2;

   private MessageInfoPublisher publisher;
   TeXJavaHelpLib helpLib;

   String appName, appVersion;
}

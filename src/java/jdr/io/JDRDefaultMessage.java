// File          : JDRDefaultMessage.java
// Purpose       : Default message system
// Creation Date : 12th June 2008
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

/**
 * Default message system. This just prints messages to STDOUT and
 * warnings/errors to STDERR.
 * @author Nicola L C Talbot
 */

public class JDRDefaultMessage 
  implements JDRMessage,MessageInfoPublisher
{
   public JDRDefaultMessage()
   {
      publisher = this;
   }

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

         verbose(level, info.getValue().toString());
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

   public int getProgress()
   {
      return progressValue;
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

            if (n < 10)
            {
               System.out.print("  "+n+"%");
            }
            else if (n < 99)
            { 
              System.out.print(" "+n+"%");
            }
            else
            {
               System.out.print(""+n+"%");
            }

            System.out.print("\b\b\b\b");
         }
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
      if (showMessages && !suspended)
      {
         System.out.print(messageText);
      }
   }

   public void message(Exception excp)
   {
      if (showMessages && !suspended)
      {
         System.out.print(excp.getMessage());
      }
   }

   public void messageln(String messageText)
   {
      if (showMessages && !suspended)
      {
         System.out.println(messageText);
      }
   }

   public void messageln(Exception excp)
   {
      if (showMessages && !suspended)
      {
         System.out.println(excp.getMessage());
      }
   }

   public void warning(String messageText)
   {
      System.err.println("Warning: "+messageText);
   }

   public void warning(Throwable cause)
   {
      System.err.println("Warning: "+cause.getMessage());
   }

   public void error(String messageText)
   {
      System.err.println("Error: "+messageText);
   }

   public void error(Throwable cause)
   {
      System.err.println("Error: "+cause.getMessage());
   }

   public void internalerror(String messageText)
   {
      System.err.println("Internal error: "+messageText);
   }

   public void internalerror(Throwable cause)
   {
      System.err.println("Internal error: "+cause.getMessage());
      cause.printStackTrace();
   }

   public void fatalerror(String messageText)
   {
      System.err.println("Fatal error: "+messageText);
   }

   public void fatalerror(Throwable cause)
   {
      System.err.println("Fatal error: "+cause.getMessage());
   }

   public void debug(Exception excp)
   {
      excp.printStackTrace();
   }

   public void debug(String msg)
   {
      System.err.println(msg);
   }

   public void verbose(int level, String msg)
   {
      if (level <= verbosity)
      {
         System.out.println(msg);
      }
   }

   public String getString(String tag, String alt)
   {
      return alt;
   }

   public String getStringWithValues(String tag,
     String[] values, String alt)
   {
      return alt;
   }

   public void setVerbosity(int level)
   {
      verbosity = level;
   }

   public int getVerbosity()
   {
      return verbosity;
   }

   public MessageInfoPublisher getPublisher()
   {
      return publisher;
   }

   public void setPublisher(MessageInfoPublisher publisher)
   {
      this.publisher = publisher;
   }

   public void publishMessages(MessageInfo... chunks)
   {
      for (MessageInfo info : chunks)
      {
         postMessage(info);
      }
   }

   private int progressMax=100;
   private int progressValue=0;
   private boolean indeterminate_=true;

   private boolean showMessages=false;
   private boolean suspended=false;

   private int verbosity=1;

   private MessageInfoPublisher publisher;
}

// File          : JDRGuiMessage.java
// Purpose       : GUI message system
// Creation Date : 12th June 2008
// Author        : Nicola L.C. Talbot
//               http://www.dickimaw-books.com/

/*
    Copyright (C) 2006-2025 Nicola L.C. Talbot

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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;

import javax.swing.*;
import javax.swing.text.*;

import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;
import com.dickimawbooks.texjavahelplib.UserCancellationListener;
import com.dickimawbooks.texjavahelplib.UserCancelledException;

import com.dickimawbooks.jdr.io.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * GUI message system. 
 * @author Nicola L C Talbot
 */

public class JDRGuiMessage extends JDRMessagePublisher
   implements UserCancellationListener,JDRMessage,MessageInfoPublisher,ActionListener
{
   protected JDRGuiMessage(JDRResources resources)
   {
      warningFlag = false;
      isInitialised = false;
      isSuspended = true;
      errorBuffer = new StringBuffer();
      this.resources = resources;
      publisher = this;
   }

   public static JDRGuiMessage create(JDRResources resources)
   {
      JDRGuiMessage gm = new JDRGuiMessage(resources);
      resources.setMessageSystem(gm);
      gm.createFrame();
      return gm;
   }

   protected void createFrame()
   {
      frame = new JFrame(resources.getMessageWithFallback(
         "messages.title", "Messages"));

      TeXJavaHelpLib helpLib = resources.getHelpLib();

      frame.setIconImage(resources.getSmallAppIcon().getImage());

/*
      frame.addWindowListener(new WindowAdapter()
      {
         public void windowClosing(WindowEvent evt)
         {
            debug("Message frame closing");
            warningFlag = false;
            errorBuffer.setLength(0);
         }
      });
*/

      document = new DefaultStyledDocument();

      StyleContext context = StyleContext.getDefaultStyleContext();

      attrPlain = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, Color.BLACK);
      attrWarning = context.addAttribute(context.getEmptySet(),
         StyleConstants.Foreground, Color.RED);

      attrError = new SimpleAttributeSet();
      StyleConstants.setForeground(attrError, Color.RED);
      StyleConstants.setBold(attrError, true);

      messageArea = new JTextPane(document);
      messageArea.setEditable(false);
      messageArea.setBackground(Color.white);

      messageScrollPane = new JScrollPane(messageArea);

      frame.getContentPane().add(messageScrollPane, "Center");

      progressBar = new JProgressBar();

      frame.getContentPane().add(progressBar, "North");

      JPanel buttonPanel = new JPanel(new BorderLayout());
      frame.getContentPane().add(buttonPanel, "South");

      processInfo = new JLabel();
      buttonPanel.add(processInfo, "Center");

      JPanel lowerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
      buttonPanel.add(lowerPanel, "South");

      abortButton = helpLib.createJButton("button", "abort", this);
      abortButton.setActionCommand("confirmabort");
      buttonPanel.add(abortButton, "East");

      enableAbort(false);

      lowerPanel.add(resources.createCloseButton(frame.getRootPane(), this), "South");

      int width = 400;
      int height = 200;

      frame.setSize(width,height);

      Toolkit tk = Toolkit.getDefaultToolkit();
      Dimension d = tk.getScreenSize();
      int screenHeight = d.height;
      int screenWidth  = d.width;

      int x = (screenWidth-width)/2;
      int y = (screenHeight-height)/2;

      frame.setLocation(x, y);

      isInitialised = true;
      isSuspended = false;
   }

   @Override
   public void setProcess(Process process)
   {
      registerProcess(process);
   }

   public JFrame getFrame()
   {
      return frame;
   }

   @Override
   public String getApplicationName()
   {
      return getResources().getApplicationName();
   }

   @Override
   public String getApplicationVersion()
   {
      return getResources().getApplicationVersion();
   }

   private void doShow()
   {
      if (!isInitialised)
      {
         debug("Message frame not initialised");
      }

      if (!frame.isVisible())
      {
         debug("Showing message frame");
         frame.setVisible(true);
         frame.toFront();
      }
   }

   private void doHide()
   {
      if (isInitialised && frame.isVisible())
      {
         frame.setVisible(false);
      }

      warningFlag = false;
      errorBuffer.setLength(0);
   }

   @Override
   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("close"))
      {
         doHide();
      }
      else if (action.equals("confirmabort"))
      {
         if (getResources().confirm(frame,
             resources.getMessage("process.confirm.abort"))
         == JOptionPane.YES_OPTION)
         {
            abortButton.setActionCommand("abort");

            if (process != null)
            {
               process.destroy();
               process = null;
            }
         }
      }
   }

   /**
    * Displays message dialogue.
    */
   public void displayMessages()
   {
      if (!isSuspended)
      {
         doShow();
      }
   }

   /**
    * Hides message dialogue.
    */
   public void hideMessages()
   {
      if (!isSuspended)
      {
         doHide();
      }
   }

   public void suspend()
   {
      isSuspended = true;
   }

   public void resume()
   {
      isSuspended = false;
   }

   public void setIndeterminate(boolean indeterminate)
   {
      if (isSuspended || !isInitialised) return;

      progressBar.setIndeterminate(indeterminate);
   }

   public boolean isIndeterminate()
   {
      if (isSuspended || !isInitialised) return true;

      return progressBar.isIndeterminate();
   }

   public void incrementProgress()
   {
      incrementProgress(1);
   }

   public void incrementProgress(int increment)
   {
      if (isSuspended || !isInitialised) return;

      setProgress(progressBar.getValue()+increment);
   }

   public void setMaxProgress(int maxValue)
   {
      if (isSuspended || !isInitialised) return;

      progressBar.setMaximum(maxValue);
   }

   public int getMaxProgress()
   {
      if (isSuspended || !isInitialised) return 0;

      return progressBar.getMaximum();
   }

   public void resetProgress(int maxValue)
   {
      if (isSuspended || !isInitialised) return;

      progressBar.setValue(0);
      progressBar.setMaximum(maxValue);
   }

   public void resetProgress()
   {
      if (isSuspended || !isInitialised) return;

      progressBar.setValue(0);
   }

   public int getProgress()
   {
      if (isSuspended || !isInitialised) return 0;

      return progressBar.getValue();
   }

   public void setProgress(int value)
   {
      if (isSuspended || !isInitialised) return;

      progressBar.setValue(value);
   }

   protected void append(String text, AttributeSet attrs)
   {
      if (isSuspended || !isInitialised) return;

      try
      {
         document.insertString(document.getLength(), text, attrs);
      }
      catch (BadLocationException e)
      {// shouldn't happen
         resources.debugMessage(e);
      }
   }

   public void message(String messageText)
   {
      append(messageText, attrPlain);
   }

   public void message(Exception excp)
   {
      message(excp.getLocalizedMessage());
   }

   public void messageln(String messageText)
   {
      message(String.format("%s%n", messageText));
   }

   public void messageln(Exception excp)
   {
      message(String.format("%s%n", excp.getLocalizedMessage()));
   }

   public void warning(String messageText)
   {
      if (!isSuspended)
      {
         append(String.format("%s%n", 
           resources.getMessage("warning.tag", messageText)), attrWarning);

         displayMessages();
         warningFlag = true;
      }
   }

   public void warning(Throwable excp)
   {
      if (!isSuspended)
      {
         String msg = excp.getLocalizedMessage();

         String classname = excp.getClass().getSimpleName();
         String text = resources.getMessageIfExists("error.throwable."+classname);

         if (text == null)
         {
            if (msg == null)
            {
               text = classname;
            }
            else
            {
               text = msg;
            }
         }
         else if (msg != null)
         {
            text += ": " + msg;
         }

         warning(text);
         resources.debugMessage(excp);
      }
   }

   public void error(String messageText)
   {
      errorBuffer.append(messageText);

      if (!isSuspended)
      {
         append(messageText, attrError);
         displayMessages();
      }
   }

   public void error(Throwable excp)
   {
      String msg;

      if (excp instanceof InvalidFormatException)
      {
         msg = ((InvalidFormatException)excp).getMessageWithIndex(
                 resources.getMessageDictionary());
      }
      else
      {
         msg = excp.getLocalizedMessage();
      }

      resources.debugMessage(excp);
      error(msg);
   }

   public void internalerror(String messageText)
   {
      resources.internalError(null, messageText);

      append(messageText, attrError);
   }

   public void internalerror(Throwable excp)
   {
      resources.internalError(null, excp);

      append(excp.getLocalizedMessage(), attrError);
   }

   public void fatalerror(Throwable excp)
   {
      resources.fatalError(excp.getLocalizedMessage(), excp);
   }

   public String getMessageWithFallback(String tag, String altFormat, 
     Object... values)
   {
      return resources.getMessageWithFallback(tag, altFormat, values);
   }

   public boolean warningFlagged()
   {
      return warningFlag;
   }

   public void enableAbort(boolean enabled)
   {
      if (isSuspended || !isInitialised) return;

      abortButton.setActionCommand("confirmabort");
      abortButton.setEnabled(enabled);
      processInfo.setEnabled(enabled);
      processInfo.setText("");
   }

   public void checkForInterrupt() throws UserCancelledException
   {
      if (isSuspended || !isInitialised) return;

      if ("abort".equals(abortButton.getActionCommand()))
      {
         abortButton.setActionCommand("confirmabort");

         throw new UserCancelledException(resources.getHelpLib());
      }
   }

   public void setProcessInfo(String text)
   {
      if (processInfo != null)
      {
         processInfo.setText(text);
      }
   }

   public void debug(Exception e)
   {
      resources.debugMessage(e);
   }

   public void debug(String msg)
   {
      if (resources == null)
      {
         System.err.println(msg);
         return;
      }

      resources.debugMessage(msg);
   }

   public void registerProcess(Process process)
   {
      this.process = process;
   }

   public void processDone()
   {
      this.process = null;
   }

   public int getVerbosity()
   {
      return verbosity;
   }

   public void setVerbosity(int level)
   {
      verbosity = level;
   }

   public JDRResources getResources()
   {
      return resources;
   }

   public void postMessage(MessageInfo info)
   {
      if (isSuspended || !isInitialised) return;

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
      else if (action.equals(MessageInfo.VISIBLE))
      {// ignore
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
         fatalerror((Throwable)info.getValue());
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

   public void verbose(int level, String mess)
   {
      if (isSuspended) return;

      if (level <= verbosity)
      {
         message(mess);
      }
   }

   public MessageInfoPublisher getPublisher()
   {
      return publisher;
   }

   public void setPublisher(MessageInfoPublisher publisher)
   {
      if (isSuspended) return;

      this.publisher = publisher;
      warningFlag = false;
      errorBuffer.setLength(0);
   }

   public void publishMessages(MessageInfo... chunks)
   {
      if (isSuspended || !isInitialised) return;

      int max = getMaxProgress();
      int progress = getProgress();
      boolean indeterminate = isIndeterminate();

      for (MessageInfo info : chunks)
      {
         String action = info.getAction();

         if (action == MessageInfo.PROGRESS)
         {
            max = ((Integer)info.getValue()).intValue();
            progress = 0;
            indeterminate = false;
         }
         else if (action == MessageInfo.MAX_PROGRESS)
         {
            max = ((Integer)info.getValue()).intValue();
         }
         else if (action == MessageInfo.SET_PROGRESS)
         {
            progress = ((Integer)info.getValue()).intValue();
         }
         else if (action == MessageInfo.INCREMENT_PROGRESS)
         {
            progress += ((Integer)info.getValue()).intValue();
         }
         else if (action == MessageInfo.INDETERMINATE)
         {
            indeterminate = ((Boolean)info.getValue()).booleanValue();
         }
         else
         {
            postMessage(info);
         }
      }

      if (progressBar.isIndeterminate() != indeterminate)
      {
         progressBar.setIndeterminate(indeterminate);
      }

      if (progressBar.getMaximum() != max)
      {
         progressBar.setMaximum(max);
      }

      if (progressBar.getValue() != progress)
      {
         progressBar.setValue(progress);
      }
   }

   public void finished(JComponent comp)
   {
      if (isSuspended || !isInitialised) return;

      if (!warningFlag && errorBuffer.length() == 0)
      {
         doHide();
         //comp.requestFocusInWindow();
      }

      setPublisher(this);
   }

   private JFrame frame;
   private JProgressBar progressBar;

   private StringBuffer errorBuffer;

   private DefaultStyledDocument document;

   private AttributeSet attrPlain, attrWarning;
   private SimpleAttributeSet attrError;

   private JTextPane messageArea;
   private JScrollPane messageScrollPane;

   private JLabel processInfo;
   private AbstractButton abortButton;

   private boolean warningFlag;
   private boolean isSuspended;

   private boolean isInitialised;

   private Process process;

   private int verbosity = 1;

   private MessageInfoPublisher publisher;

   private JDRResources resources;
}

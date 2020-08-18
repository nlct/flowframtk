// File          : JDRResources.java
// Purpose       : Resources required by FlowframTk and associated
//                 applications
// Creation Date : 4th June 2008
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

package com.dickimawbooks.jdrresources;

import java.io.*;
import java.net.*;  
import java.beans.*;
import java.util.*;
import java.text.MessageFormat;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;
import javax.swing.event.ChangeListener;
import javax.help.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.io.JDRMessage;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Resources required by FlowframTk and JDRView.
 */
public class JDRResources
{
   public JDRResources() throws IOException,URISyntaxException
   {
      initStackTracePane();
      initUserConfigDir();
      createIconNameMap();
   }

   public static String getIconDir()
   {
      return "icons";
   }

   public ImageIcon getLargeAppIcon()
   {
      String filename = getIconDir()+"/flowframtklogolarge.png";

      java.net.URL imgURL = getClass().getResource(filename);

      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }

      warning(getString("error.file_not_found")+": "+filename);
      return null;
   }

   public ImageIcon getSmallAppIcon()
   {
      String filename = getIconDir() + "/flowframtklogosmall.png";

      java.net.URL imgURL = getClass().getResource(filename);

      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }

      warning(getString("error.file_not_found")+": "+filename);
      return null;
   }

   public static JDRButtonStyle getButtonStyle()
   {
      return BUTTON_STYLES[buttonStyle];
   }

   public static String getButtonStyleName()
   {
      return getButtonStyle().getName();
   }

   public static JDRButtonStyle getButtonStyle(String name)
   {
      for (int i = 0; i < BUTTON_STYLES.length; i++)
      {
         if (BUTTON_STYLES[i].getName().equals(name))
         {
            return BUTTON_STYLES[i];
         }
      }

      return BUTTON_STYLES[0];
   }

   public static void setButtonStyle(String name)
    throws IllegalArgumentException
   {
      for (int i = 0; i < BUTTON_STYLES.length; i++)
      {
         if (BUTTON_STYLES[i].getName().equals(name))
         {
            buttonStyle = i;
            return;
         }
      }

      throw new IllegalArgumentException("Invalid button style name '"
        +name+"'");
   }

   public static void setDialogButtonStyle(int style)
   {
      if (style != DIALOG_BUTTON_AS_GENERAL
       && (style < 0 || style >= DIALOG_BUTTON_STYLES.length))
      {
         throw new IllegalArgumentException("Invalid argument "+style
           +" to setDialogStyle");
      }

      dialogButtonStyle = style;
   }

   public static int getDialogButtonStyle()
   {
      return dialogButtonStyle;
   }

   /**
    * Gets an application icon from the resource icon directory.
    * @param name the name of the icon
    * @return the icon as an ImageIcon
    */
   public ImageIcon appIcon(String name)
   {
      String filename = getIconDir()+"/"+name;

      java.net.URL imgURL = getClass().getResource(filename);

      if (imgURL != null)
      {
         return new ImageIcon(imgURL);
      }

      warning(getString("error.file_not_found")+": "+filename);
      return null;
   } 

   public ImageIcon appButtonIcon(String name)
   {
      return appIcon(getButtonStyle().getLocation()+"/"+name);
   }

   /**
    * Gets an application icon from the resource icon directory.
    * @param name the name of the icon
    * @return the icon as an Image
    */
   public static Image getImage(String name)
   {
      String filename = getIconDir()+"/"+name;

      Toolkit tk = Toolkit.getDefaultToolkit();

      return tk.getImage(filename);
   }

   /**
    * Displays stack trace in a dialog box with the option for the
    * user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param frameTitle the title for the dialog box
    * @param e the exception with the required stack trace
    */
   public void displayStackTrace(Component parent,
       String frameTitle, Throwable e)
   {
      if (e instanceof InvalidFormatException)
      {
         displayStackTrace(parent, frameTitle, 
           ((InvalidFormatException)e).getMessageWithIndex(getMessageSystem()), e);
      }
      else
      {
         Throwable cause = e.getCause();

         if (cause == null)
         {
            displayStackTrace(parent, frameTitle, ""+e, e);
         }
         else
         {
            displayStackTrace(parent, frameTitle, ""+e+"\n"+cause, e);
         }
      }
   }

   private void initStackTracePane()
   {
      stackTracePane = new JTabbedPane();
      String title = getString("stacktrace.message","Error Message");

      messageArea = new JTextArea(20,50);
      messageArea.setEditable(false);
      messageArea.setLineWrap(true);
      messageArea.setWrapStyleWord(true);

      messageSP = new JScrollPane(messageArea);

      stackTracePane.addTab(title, null, messageSP, title);

      JPanel p2 = new JPanel();

      stackTraceDetails = new JTextArea(20,50);
      stackTraceDetails.setEditable(false);

      JScrollPane scrollPane = new JScrollPane(stackTraceDetails);
      p2.add(scrollPane, "Center");

      JButton copyButton =
         new JButton(getString("stacktrace.copy","Copy"));
      copyButton.setMnemonic(getCodePoint("stacktrace.copy.mnemonic",'C'));
      copyButton.addActionListener(new CopyAction(stackTraceDetails));

      p2.add(copyButton,"South");

      title = getString("stacktrace.details","Details");
      stackTracePane.addTab(title, null, p2, title);

      okayOption = getString("label.okay", "Okay");
      exitOption = getString("label.quit_without_saving",
         "Quit Without Saving");

   }

   /**
    * Displays stack trace in a dialog box with the option for the
    * user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param frameTitle the title for the dialog box
    * @param e the exception with the required stack trace
    */
   public void displayStackTrace(Component parent,
       String frameTitle, String message, Throwable e)
   {
      messageArea.setText(message);

      StackTraceElement[] trace = e.getStackTrace();
      String stackTrace = "";
      for (int i = 0, n=trace.length; i < n; i++)
      {
         stackTrace += trace[i]+"\n";
      }

      stackTraceDetails.setText(stackTrace);

      int result = JOptionPane.showOptionDialog(parent, stackTracePane,
         frameTitle,
         JOptionPane.YES_NO_OPTION,
         JOptionPane.ERROR_MESSAGE, null,
         new String[] {okayOption, exitOption}, okayOption);

      if (result == JOptionPane.NO_OPTION)
      {
         System.exit(EXIT_INTERNAL_ERROR);
      }
   }

   /**
    * Displays stack trace in a dialog box with the option for the
    * user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param e the exception with the required stack trace
    */
   public void internalError(Component parent, Throwable e)
   {
      if (debugMode)
      {
         e.printStackTrace();
         System.exit(1);
      }
      else
      {
         displayStackTrace(parent,
            getString("internal_error.title", "Internal Error"),
           e);
      }
   }

   /**
    * Displays stack trace in a dialog box.
    * @param parent the parent for the dialog box
    * @param message the error message
    * @param e the exception with the required stack trace
    */
   public void internalError(Component parent,
      String message, Throwable e)
   {
      displayStackTrace(parent,
         getString("internal_error.title", "Internal Error"),
         message, e);
   }

   /**
    * Displays a dialog box with the given message and the option
    * for the user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param message the error message
    */
   public void internalError(Component parent, String message)
   {
      String okayOption = getString("label.okay", "Okay");
      String exitOption = getString("label.quit_without_saving",
         "Quit Without Saving");

      int result = JOptionPane.showOptionDialog(parent, message,
         getString("internal_error.title","Internal Error"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.ERROR_MESSAGE, null,
         new String[] {okayOption, exitOption}, okayOption);

      if (result == JOptionPane.NO_OPTION)
      {
         System.exit(EXIT_INTERNAL_ERROR);
      }
   }

   /**
    * Displays a dialog box with the given message and the option
    * for the user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param message the error message
    */
   public void internalError(Component parent, String[] message)
   {
      String okayOption = getString("label.okay", "Okay");
      String exitOption = getString("label.quit_without_saving",
         "Quit Without Saving");

      int result = JOptionPane.showOptionDialog(parent, message,
         getString("internal_error.title","Internal Error"),
         JOptionPane.YES_NO_OPTION,
         JOptionPane.ERROR_MESSAGE, null,
         new String[] {okayOption, exitOption}, okayOption);

      if (result == JOptionPane.NO_OPTION)
      {
         System.exit(EXIT_INTERNAL_ERROR);
      }
   }

   /**
    * Displays a dialog box with the given message and the option
    * for the user to continue or quit the application. The dialog's
    * parent is set to null.
    * @param message the error message
    */
   public void internalError(String message)
   {
      System.err.println(message);
      internalError(null, message);
   }

   /**
    * Displays an error message in a dialog box.
    * @param parent the dialog box's parent
    * @param message the error message
    */
   public void error(Component parent, String message)
   {
      if (message == null)
      {
         try
         {
            throw new NullPointerException();
         }
         catch (NullPointerException npe)
         {
            System.err.println(npe);
         }
      }

      messageArea.setText(message);

      JOptionPane.showMessageDialog(parent,
      messageSP,
      getString("error.title", "Error"),
      JOptionPane.ERROR_MESSAGE);
   }

   /**
    * Displays an error message in a dialog box.
    * @param parent the dialog box's parent
    * @param message the error message
    */
   public void error(Component parent, String[] message)
   {
      if (message == null)
      {
         try
         {
            throw new NullPointerException();
         }
         catch (NullPointerException npe)
         {
            System.err.println(npe);
         }
      }

      messageArea.setText(String.join("\n", message));

      JOptionPane.showMessageDialog(parent,
      messageSP,
      getString("error.title", "Error"),
      JOptionPane.ERROR_MESSAGE);
   }

   /**
    * Displays an error message in a dialog box. The parent component
    * is set to null.
    * @param message the error message
    */
   public void error(String message)
   {
      error(null, message);
   }

   public void error(Exception e)
   {
      error(null, e);
   }

   /**
    * Displays an error message in a dialog box. The parent component
    * is set to null.
    * @param message the error message
    */
   public void error(String[] message)
   {
      error(null, message);
   }

   /**
    * Displays the stack trace in a dialog box where the user
    * has the option to continue or quit the application.
    * @param parent the parent component
    * @param e the exception
    */
   public void error(Component parent, Throwable e)
   {
      if (e instanceof UserCancelledException
        || e instanceof java.util.concurrent.CancellationException)
      {
         String msg = e.getMessage();

         if (msg == null || msg.isEmpty())
         {
            msg = getString("process.aborted", "Process Aborted");
         }

         JOptionPane.showMessageDialog(parent, msg);
      }
      else if (e.getCause() instanceof UserCancelledException)
      {
         String msg = e.getMessage();
         String msg2 = e.getCause().getMessage();

         if (msg2 == null || msg2.isEmpty())
         {
            msg2 = getString("process.aborted", "Process Aborted");
         }

         if (msg == null || msg.isEmpty();)
         {
            msg = msg2;
         }
         else
         {
            msg = String.format("%s%n%s", msg, msg2);
         }

         JOptionPane.showMessageDialog(parent, msg);
      }
      else
      {
         displayStackTrace(parent,
                           getString("error.title", "Error"),e);
      }
   }

   /**
    * Displays the stack trace in a dialog box where the user
    * has the option to continue or quit the application.
    * @param parent the parent component
    * @param e the exception
    */
   public void error(Component parent, String message,
      Throwable e)
   {
      if (message == null)
      {
         try
         {
            throw new NullPointerException();
         }
         catch (NullPointerException npe)
         {
            System.err.println(npe);
         }
      }

      displayStackTrace(parent,
                        getString("error.title", "Error"),
                        message, e);
   }

   /**
    * Displays message and stack trace in a dialog box and
    * exits the application when the dialog box is dismissed.
    * @param message the error message
    * @param e the exception
    */
   public void fatalError(String message, Throwable e)
   {
      if (message == null)
      {
         try
         {
            throw new NullPointerException();
         }
         catch (NullPointerException npe)
         {
            System.err.println(npe);
         }
      }

      messageArea.setText(message);

      StackTraceElement[] trace = e.getStackTrace();
      String stackTrace = "";
      for (int i = 0, n=trace.length; i < n; i++)
      {
         stackTrace += trace[i]+"\n";
      }

      stackTraceDetails.setText(stackTrace);

      JOptionPane.showMessageDialog(null,
      stackTracePane,
      getString("error.fatal", "Fatal Error"),
      JOptionPane.ERROR_MESSAGE);

      System.exit(EXIT_FATAL_ERROR);
   }

   /**
    * Displays a warning message in a dialog box.
    * @param parent the dialog's parent
    * @param message the warning message
    */
   public void warning(Component parent, String[] message)
   {
      messageArea.setText(String.join("\n", message));

      JOptionPane.showMessageDialog(parent,
      messageSP,
      getString("warning.title"),
      JOptionPane.WARNING_MESSAGE);
   }

   /**
    * Displays a warning message in a dialog box.
    * @param parent the dialog's parent
    * @param message the warning message
    */
   public void warning(Component parent, String message)
   {
      JOptionPane.showMessageDialog(parent,
      message,
      getString("warning.title"),
      JOptionPane.WARNING_MESSAGE);
   }

   /**
    * Displays a warning message in a dialog box.
    * @param message the warning message
    */
   public void warning(String message)
   {
      warning(null, message);
   }

   public int confirm(Component parent, String message)
   {
      return confirm(parent, message, getString("process.confirm"), JOptionPane.YES_NO_OPTION);
   }

   public int confirm(Component parent, String message, String title)
   {
      return confirm(parent, message, title, JOptionPane.YES_NO_OPTION);
   }

   public int confirm(Component parent, String message, int options)
   {
      return confirm(parent, message, getString("process.confirm"), options);
   }

   public int confirm(Component parent, String message, int options, int type)
   {
      return confirm(parent, message, getString("process.confirm"), options);
   }

   public int confirm(Component parent, String message, String title, int options)
   {
      return confirm(parent, message, title, options, JOptionPane.QUESTION_MESSAGE);
   }

   public int confirm(Component parent, String[] message, String title, int options, int type)
   {
      return confirm(parent, String.join("\n", message), title, options, type);
   }

   public int confirm(Component parent, String message, String title, int options, int type)
   {
      if (message == null)
      {
         try
         {
            throw new NullPointerException();
         }
         catch (NullPointerException npe)
         {
            System.err.println(npe);
         }
      }

      messageArea.setText(message);

      return JOptionPane.showConfirmDialog(parent, messageSP, title,
        options, type);
   }

   /**
    * Gets an integer associated with the given key in the
    * resources dictionary.
    */
   public int getInt(String key)
      throws NumberFormatException
   {
      if (dictionary == null)
      {
         return -1;
      }

      return Integer.parseInt(dictionary.getString(key));
   }

   public String applyMessagePattern(String key, Number value)
   {
      if (dictionary == null)
      {
         return key;
      }

      return dictionary.applyMessagePattern(key, value);
   }

   public String formatMessageChoice(Number value, String key)
   {
      if (dictionary == null)
      {
         return key;
      }

      return MessageFormat.format(dictionary.applyMessagePattern(key, value), value);
   }

   public String formatMessageChoice(Number value, String key, Object... params)
   {
      if (dictionary == null)
      {
         return key;
      }

      return MessageFormat.format(dictionary.applyMessagePattern(key, value), params);
   }

   public String getMessage(String key, Object... values)
   {
      if (dictionary == null)
      {
         return key;
      }

      return dictionary.getMessage(key, values);
   }

   public String getMessageWithAlt(String altFormat, String key, Object... values)
   {
      if (dictionary == null)
      {
         return MessageFormat.format(altFormat, values);
      }

      return dictionary.getMessageWithAlt(altFormat, key, values);
   }

   public String getMessage(int lineNum, String key, Object... values)
   {
      if (dictionary == null)
      {
         return String.format("%d: %s", lineNum, key);
      }

      return getMessageWithAlt("Line {0}: {1}", "error.with_line",
        lineNum, getMessage(key, values));
   }

   public String getMessageWithAlt(int lineNum, String altFormat, 
     String key, Object... values)
   {
      if (dictionary == null)
      {
         return String.format("%d: %s", lineNum, 
            MessageFormat.format(altFormat, values));
      }

      return getMessageWithAlt("%d: %s", "error.with_line", lineNum,
       dictionary.getMessageWithAlt(altFormat, key, values));
   }

   /**
    * Gets the string associated with the given key in the
    * resource dictionary or the default value if not found.
    * @param key the key identifying the required string
    * @param defVal the default value
    * @return the required string or default value if not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()}
    */
   public String getString(String key, String defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      return dictionary.getString(key, defVal);
   }

   /**
    * Gets the string associated with the given key in the
    * resource dictionary. If not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()},
    * the key is returned instead.
    * @param key the key identifying the required string
    * @return the required string or the key if not found
    */
   public String getString(String key)
   {
      if (dictionary == null) return key;

      return dictionary.getString(key);
   }

   /**
    * Gets the first character of the string associated with the 
    * given key in the resource dictionary, or the default value 
    * if not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()}.
    * This method is now obsolete. Use {@link #getCodePoint(String,int)} instead.
    * @param key the key identifying the required string
    * @param defVal the default value
    * @return the first character of the required string 
    * or default value if not found
    */
   @Deprecated
   public char getChar(String key, char defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      return dictionary.getChar(key, defVal);
   }


   /**
    * Gets the first character of the string associated with the 
    * given key in the resource dictionary. If not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()}, the
    * first character of the key is used instead.
    * This method is now obsolete. Use {@link #getCodePoint(String)} instead.
    * @param key the key identifying the required string
    * @return the first character of the required string 
    * or the first character of the key if not found
    */
   @Deprecated
   public char getChar(String key)
   {
      return getChar(key, key.charAt(0));
   }

   /**
    * Gets the code point of the first character of the string associated with the 
    * given key in the resource dictionary, or the default value 
    * if not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()}.
    * @param key the key identifying the required string
    * @param defVal the default value
    * @return the first character of the required string 
    * or default value if not found
    */
   public int getCodePoint(String key, int defVal)
   {
      if (dictionary == null)
      {
         return defVal;
      }

      return dictionary.getCodePoint(key, defVal);
   }

   /**
    * Gets the code point of the first character of the string associated with the 
    * given key in the resource dictionary. If not found or 
    * if the dictionary has not been initialised using
    * {@link #initialiseDictionary()}, the
    * first character of the key is used instead.
    * @param key the key identifying the required string
    * @return the first character of the required string 
    * or the first character of the key if not found
    */
   public int getCodePoint(String key)
   {
      return getCodePoint(key, key.codePointAt(0));
   }

   public String getToolTipText(String id)
   {
      return getString(id+".tooltip", getString("tooltip."+id, null));
   }

   public String getDefaultDescription(JDRCompleteObject object)
   {
      String description = "";

      FlowFrame flowframe = object.getFlowFrame();

      if (flowframe == null)
      {
         String tag = object.getClass().getName().toLowerCase()
            .substring(object.getClass().getPackage().getName().length()+4);

         description = getMessage("findbydescription."+tag,
                  object.getDescriptionInfo());
      }
      else
      {
         switch (flowframe.getType())
         {
            case FlowFrame.STATIC:
               description = getString("flowframe.static");
            break;
            case FlowFrame.FLOW:
               description = getString("flowframe.flow");
            break;
            case FlowFrame.DYNAMIC:
               description = getString("flowframe.dynamic");
            break;
         }

         description += " \""+flowframe.getLabel()+"\"";
      }

      return description;
   }

   public Enumeration<Object> getAcceleratorPropertyNames()
   {
      return accelerators.keys();
   }

   public String getAcceleratorString(String propName)
   {
      if (accelerators == null) return null;

      return accelerators.getProperty(propName);
   }

   public KeyStroke getAccelerator(String propName)
   {
      if (accelerators == null) return null;

      return accelerators.getAccelerator(propName);
   }

   public KeyStroke getAccelerator(String propName, String defValue)
   {
      if (accelerators == null) return null;

      return accelerators.getAccelerator(propName, defValue);
   }

   public void setAccelerator(String propName, String keystroke)
   {
      accelerators.setProperty(propName, keystroke);
   }

   public boolean areAcceleratorsInitialised()
   {
      return accelerators != null;
   }

   public void initialiseAccelerators()
   {
      accelerators = Accelerators.createDefaultAccelerators();
   }

   public void initialiseAccelerators(boolean upgrade,
      Reader reader)
      throws IOException
   {
      if (upgrade)
      {
         // Create the defaults first in case new accelerators
         // have been added to the application.
         accelerators = Accelerators.createDefaultAccelerators();
      }
      else
      {
         accelerators = new Accelerators();
      }

      accelerators.load(reader);
   }

   public void saveAccelerators(Writer writer)
      throws IOException
   {
      if (accelerators != null)
      {
         accelerators.store(writer, "Accelerators");
      }
   }

   public boolean isDictInitialised()
   {
      return dictionary != null;
   }

   /**
    * Initialises dictionary using the language resources.
    */
   public void initialiseDictionary()
   throws IOException
   {
      dictionary = new JDRDictionary(this);
   }

   /**
    * Initialises dictionary.
    * @param dictionaryInputStream the dictionary input stream
    * @param licenceInputStream the licence input stream
    */
   public void initialiseDictionary(
      InputStream dictionaryInputStream,
      InputStream licenceInputStream)
   throws IOException
   {
      dictionary = new JDRDictionary(this, dictionaryInputStream,
         licenceInputStream);
   }

   /**
    * Gets user's configuration directory.
    * If the environment variable <TT>JDRSETTINGS</TT> exists
    * and is a directory, then that value is returned. If the
    * environment variable <TT>HOME</TT> exists and contains
    * a directory called <TT>.flowframtk</TT> or the file system
    * is able to create the directory <TT>.flowframtk</TT> then that 
    * is returned, unless the OS is Windows, in which case 
    * <TT>flowframtk-settings</TT> is returned if it can be created.
    * If <TT>HOME</TT> exists and contains a directory
    * called <TT>flowframtk-settings</TT> or the file system is
    * able to create that directory, then that directory is returned.
    * If neither <TT>HOME</TT> nor <TT>JDRSETTINGS</TT> are defined
    * then a directory with the user's name is created in FlowframTk's
    * application directory and that is used.
    * @return path name of user configuration directory or null
    * if configuration directory can't be created
    */

   public String getUserConfigDirName()
   {
      return usersettings;
   }

   private void initUserConfigDir()
    throws IOException,URISyntaxException
   {
      // does JDRSETTINGS exist?

      String jdrsettings = System.getenv("JDRSETTINGS");
      usersettings=null;
      File file=null;

      if (jdrsettings != null)
      {
         file = new File(jdrsettings);

         if (file.exists() && file.isDirectory())
         {
            usersettings = file.getCanonicalPath();
            return;
         }
      }

      String homeName = System.getProperty("user.home");

      File homeDir = (homeName == null ? null : new File(homeName));

      // does HOME exist?

      if (homeDir != null && homeDir.exists() && homeDir.isDirectory())
      {
         // First try original "jpgfdraw" name

         String dirname = ".jpgfdraw";

         if (System.getProperty("os.name").contains("Win"))
         {
            dirname = "jpgfdraw-settings";
         }

         file = new File(homeDir, dirname);

         if (file.exists() && file.isDirectory())
         {
            usersettings = file.getCanonicalPath();
            return;
         }

         file = new File(homeDir, "jpgfdraw-settings");

         if (file.exists() && file.isDirectory())
         {
            usersettings = file.getCanonicalPath();
            return;
         }

         // Now try new "flowframtk" name

         dirname = ".flowframtk";

         if (System.getProperty("os.name").contains("Win"))
         {
            dirname = "flowframtk-settings";
         }

         file = new File(homeDir, dirname);

         if (file.exists() && file.isDirectory())
         {
            usersettings = file.getCanonicalPath();
            return;
         }

         if (file.mkdir())
         {
            usersettings = file.getCanonicalPath();
            return;
         }

         if (!dirname.equals("flowframtk-settings"))
         {
            file = new File(homeDir, "flowframtk-settings");

            if (file.exists() && file.isDirectory())
            {
               usersettings = file.getCanonicalPath();
               return;
            }

            if (file.mkdir())
            {
               usersettings = file.getCanonicalPath();
               return;
            }
         }
      }

      // neither "user.home" nor JDRSETTINGS are defined, so
      // assume settings are in "settings" subdirectory of
      // FlowframTk's installation directory.

      String dir = "settings";
      String user = System.getenv("USER");
      if (user == null) user = System.getenv("USERNAME");

      // Using URI, so forward slash required regardless of OS

      if (user != null) dir += "/"+user;

      URL url = getClass().getResource(dir);

      if (url != null)
      {
         String path = url.toURI().getPath();

         file = new File(path);

         if (file.isDirectory())
         {
            usersettings = path;
         }
         else if (file.exists())
         {
            if (dictionary == null)
            {
               throw new IOException(
                 String.format("'%s' is not a directory", file.toString()));
            }
            else
            {
               throw new IOException(
                 getMessage("error.not_a_directory", file.toString()));
            }
         }
      }
      else
      {
         url = getClass().getResource(".");

         if (url != null)
         {
            file = new File(url.toURI().getPath(), dir);

            if ((file.exists() && file.isDirectory())
              || file.mkdirs())
            {
               url = getClass().getResource(dir);

               if (url != null)
               {
                  usersettings = url.toURI().getPath();
               }
            }
            else
            {
               if (dictionary == null)
               {
                  throw new IOException(
                    String.format("Can't create config directory '%s'",
                     file.toString()));
               }
               else
               {
                  throw new IOException(
                        getMessage("error.config_cant_create",
                           file.getCanonicalPath()));
               }
            }
         }
      }
   }

   public void debugMessage(String message)
   {
      if (debugMode)
      {
         System.out.println("debug message: "+message);
      }
   }

   public void debugMessage(Throwable e)
   {
      if (debugMode)
      {
         System.err.println("Debug mode stack trace");
         String msg = e.getMessage();

         if (msg != null)
         {
            System.err.println(msg);
         }

         e.printStackTrace();
      }
   }

   private URL getHelpSetLocation(String appName)
     throws IOException
   {
      String helpsetLocation = "/resources/helpsets/";

      String hsLocation;

      URL hsURL;

      if (helpLocaleId != null)
      {
         hsLocation = String.format("%s%s/%s/%s.hs",
           helpsetLocation, appName, helpLocaleId,
              appName, helpLocaleId);

         hsURL = getClass().getResource(hsLocation);

         if (hsURL == null)
         {
            warning("Can't find helpset for language '"+helpLocaleId+"'");
            helpLocaleId = null;
         }

         return hsURL;
      }

      Locale locale = Locale.getDefault();

      String localeId = locale.getLanguage()+"-"+locale.getCountry();

      helpLocaleId = localeId;

      hsLocation = String.format("%s%s/%s/%s.hs",
        helpsetLocation, appName, localeId,
           appName, localeId);

      hsURL = getClass().getResource(hsLocation);

      if (hsURL == null)
      {
         String tried = hsLocation;

         helpLocaleId = locale.getLanguage();

         hsLocation = String.format("%s%s/%s/%s.hs",
           helpsetLocation, appName, helpLocaleId,
              appName, helpLocaleId);

         hsURL = getClass().getResource(hsLocation);

         if (hsURL == null)
         {
            tried += "\n"+hsLocation;

            if (!localeId.equals("en-GB"))
            {
               helpLocaleId = "en-GB";

               hsLocation = String.format("%s%s/%s/%s.hs",
                 helpsetLocation, appName, helpLocaleId,
                    appName, helpLocaleId);

               hsURL = getClass().getResource(hsLocation);

               if (hsURL == null)
               {
                  tried += "\n"+hsLocation;
               }
            }

            if (hsURL == null)
            {
               throw new IOException("Can't find helpset. Tried:\n"+tried);
            }
         }
      }

      return hsURL;
   }

   public String[] getAvailableDictLanguages()
   {
      URL url = getClass().getResource("/resources/dictionaries/");

      File parent;

      try
      {
         parent = new File(url.toURI());
      }
      catch (URISyntaxException e)
      {
         // this shouldn't happen!

         e.printStackTrace();
         return new String[] {"en-GB"};
      }

      File[] files = parent.listFiles(dictionaryFilter);

      if (files == null)
      {
         debugMessage("no dictionaries found");
         return new String[] {"en-GB"};
      }

      String[] lang = new String[files.length];

      for (int i = 0; i < files.length; i++)
      {
         String name = files[i].getName();

         lang[i] = name.substring(name.indexOf("-")+1, name.lastIndexOf("."));
      }

      return lang;
   }

   public String[] getAvailableHelpLanguages(String appName)
   {
      URL url = getClass().getResource("/resources/helpsets/"+appName);

      File parent;

      try
      {
         parent = new File(url.toURI());
      }
      catch (URISyntaxException e)
      {
         // this shouldn't happen!

         e.printStackTrace();
         return new String[] {"en-GB"};
      }

      File[] files = parent.listFiles(directoryFilter);

      if (files == null)
      {
         debugMessage("no dictionaries found");
         return new String[] {"en-GB"};
      }

      String[] lang = new String[files.length];

      for (int i = 0; i < files.length; i++)
      {
         lang[i] = files[i].getName();
      }

      return lang;
   }

   public void initialiseHelp(JFrame parent, String appName)
   {
      if (mainHelpBroker == null)
      {
         HelpSet mainHelpSet = null;

         try
         {
            URL hsURL = getHelpSetLocation(appName);

            mainHelpSet = new HelpSet(null, hsURL);
         }
         catch (Exception e)
         {
            error(parent, e);
         }

         if (mainHelpSet != null)
         {
            mainHelpBroker = mainHelpSet.createHelpBroker();
         }

         if (mainHelpBroker != null)
         {
            csh = new CSH.DisplayHelpFromSource(mainHelpBroker);
         }
      }
   }

   public void enableHelpOnButton(AbstractButton button, String id)
   {
      enableHelpOnButton(button, id, getAccelerator("label.contexthelp"));
   }

   public void enableHelpOnButton(AbstractButton button, String id,
     KeyStroke keyStroke)
   {
      if (mainHelpBroker != null)
      {
         try
         {
            mainHelpBroker.enableHelpOnButton(button, id,
               mainHelpBroker.getHelpSet());

            csh = new CSH.DisplayHelpFromSource(mainHelpBroker);

            button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
             put(keyStroke, button.getActionCommand());

            button.getActionMap().put(button.getActionCommand(), 
             new AbstractAction(button.getActionCommand())
             {
                public void actionPerformed(ActionEvent evt)
                {
                   csh.actionPerformed(evt);
                }
             }
             );
         }
         catch (BadIDException e)
         {
            internalError(null, e);
         }
      }
      else
      {
         internalError(getString("internal_error.no_helpset"));
      }
   }

   public JMenuItem addHelpItem(JMenu helpM, String appName)
   {
      JMenuItem helpItem = new JMenuItem(
         getMessage("help.handbook", appName));

      helpM.add(helpItem);
      helpItem.setAccelerator(getAccelerator("label.help"));

      helpItem.setMnemonic(getCodePoint("help.handbook.mnemonic"));

      if (csh != null)
      {
         helpItem.addActionListener(csh);
      }
      else
      {
         internalError(getString("internal_error.no_helpset"));
      }

      return helpItem;
   }

   public JDRButton createMainHelpButton()
   {
      return createMainHelpButton(getString("help.label"));
   }

   public JDRButton createMainHelpButton(String tooltipText)
   {
      JDRButton helpButton = createAppButton("help", csh, null, 
         tooltipText);

      return helpButton;
   }

   public JDRButton createHelpButton(String id)
   {
      return createHelpButton(id, getString("help.label"));
   }

   public JDRButton createHelpButton(String id, String tooltipText)
   {
      JDRButton helpButton = createDialogButton("label.help",
      "help", null, null, tooltipText);

      enableHelpOnButton(helpButton, id);

      return helpButton;
   }

   public JDRButton createDialogButton(String tag,
     ActionListener listener, KeyStroke keyStroke)
   {
      return createDialogButton(tag, tag, listener,
       keyStroke, getString(tag+".tooltip", null));
   }

   public JDRButton createDialogButton(String tag,
     ActionListener listener, KeyStroke keyStroke,
     String tooltipText)
   {
      return createDialogButton(tag, tag, listener,
       keyStroke, tooltipText);
   }

   public JDRButton createDialogButton(String tag,
     String actionName, ActionListener listener,
     KeyStroke keyStroke)
   {
      String tooltip = getToolTipText(tag+"."+actionName);

      if (tooltip == null)
      {
         tooltip = getToolTipText(tag);
      }

      return createDialogButton(tag, actionName, listener,
       keyStroke, tooltip);
   }

   public JDRButton createDialogButton(String tag,
     String actionName, ActionListener listener,
     KeyStroke keyStroke, String tooltipText)
   {
      return createDialogButton(dialogButtonStyle, tag,
       actionName, listener, keyStroke, tooltipText);
   }

   public JDRButton createDialogButton(int style,
     String tag, String actionName, ActionListener listener,
     KeyStroke keyStroke, String tooltipText)
   {
      String buttonText = getString("label."+actionName, null);

      if (buttonText == null)
      {
         buttonText = getString(tag, null);

         if (buttonText == null)
         {
            buttonText = getString(tag+"."+actionName, null);

            if (buttonText != null)
            {
               tag += "."+actionName;
            }
         }
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = mapIconBaseName(actionName);

      JDRButtonStyle buttonStyle;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         buttonStyle = getButtonStyle();
      }
      else
      {
         buttonStyle = DIALOG_BUTTON_STYLES[style];
      }

      JDRButton button = buttonStyle.createButton(this,
           buttonText, base, listener, tooltipText);

      button.setActionCommand(actionName);

      if (buttonText != null && buttonMnemonic != 0)
      {
         button.setMnemonic(buttonMnemonic);
      }

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, actionName);
         button.getActionMap().put(actionName, 
            new ButtonAction(button, listener));
      }

      return button;
   }

   public JDRToggleButton createDialogToggle(String tag,
     String actionName, ActionListener listener)
   {
      return createDialogToggle(getDialogButtonStyle(), 
       tag, actionName, listener,
       null, getString(tag+".tooltip", null));
   }

   public JDRToggleButton createDialogToggle(String tag,
     String actionName, ActionListener listener, boolean selected)
   {
      return createDialogToggle(getDialogButtonStyle(), 
       tag, actionName, listener,
       null, getString(tag+".tooltip", null), selected);
   }

   public JDRToggleButton createDialogToggle(String tag,
     String actionName, ActionListener listener,
     KeyStroke keyStroke)
   {
      return createDialogToggle(getDialogButtonStyle(), 
       tag, actionName, listener,
       keyStroke, getString(tag+".tooltip", null));
   }

   public JDRToggleButton createDialogToggle(int style,
     String tag, String actionName, ActionListener listener,
     KeyStroke keyStroke, String tooltipText)
   {
      return createDialogToggle(style, tag, actionName, listener, keyStroke,
        tooltipText, false);
   }

   public JDRToggleButton createDialogToggle(int style,
     String tag, String actionName, ActionListener listener,
     KeyStroke keyStroke, String tooltipText, boolean selected)
   {
      String buttonText = getString("label."+actionName, null);

      if (buttonText == null)
      {
         buttonText = getString(tag, null);
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = mapIconBaseName(actionName);

      JDRToggleButton button = null;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         button = getButtonStyle().createToggle(this,
           buttonText, base, listener, tooltipText);
      }
      else
      {
         button = DIALOG_BUTTON_STYLES[style].createToggle(this,
           buttonText, base, listener, tooltipText);
      }

      button.setSelected(selected);
      button.setActionCommand(actionName);

      if (buttonText != null && buttonMnemonic != 0)
      {
         button.setMnemonic(buttonMnemonic);
      }

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, actionName);
         button.getActionMap().put(actionName,
            new ButtonAction(button, listener));
      }

      return button;
   }

   public JDRToolButton createDialogRadio(String tag,
     String actionName, ActionListener listener, 
     ButtonGroup bg, boolean selected)
   {
      return createDialogRadio(getDialogButtonStyle(), 
       tag, actionName, listener, bg, selected,
       null, getString(tag+".tooltip", null));
   }

   public JDRToolButton createDialogRadio(String tag,
     String actionName, ActionListener listener,
     ButtonGroup bg, boolean selected, KeyStroke keyStroke)
   {
      return createDialogRadio(getDialogButtonStyle(), 
       tag, actionName, listener, bg, selected,
       keyStroke, getString(tag+".tooltip", null));
   }

   public JDRToolButton createDialogRadio(int style,
     String tag, String actionName,
     ActionListener listener, ButtonGroup bg, boolean selected,
     KeyStroke keyStroke, String tooltipText)
   {
      String buttonText = getString("label."+actionName, null);

      if (buttonText == null)
      {
         buttonText = getString(tag, null);
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = mapIconBaseName(actionName);

      JDRToolButton button = null;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         button = getButtonStyle().createTool(this,
           buttonText, base, listener, bg, selected, tooltipText);
      }
      else
      {
         button = DIALOG_BUTTON_STYLES[style].createTool(this,
           buttonText, base, listener, bg, selected, tooltipText);
      }

      button.setActionCommand(actionName);

      if (buttonText != null && buttonMnemonic != 0)
      {
         button.setMnemonic(buttonMnemonic);
      }

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, actionName);
         button.getActionMap().put(actionName,
            new ButtonAction(button, listener));
      }

      return button;
   }

   public DirectionButton createDirectionButton(String tag, int direction)
   {
      return getButtonStyle().createDirectionButton(this, tag, direction);
   }

   public JDRButton createAppButton(String buttonText, String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      String base = mapIconBaseName(name);

      JDRButton button = getButtonStyle().createButton(this,
         buttonText, base, listener, tooltipText);

      button.setActionCommand(name);

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, name);
         button.getActionMap().put(name,
            new ButtonAction(button, listener));
      }

      return button;
   }

   public JDRButton createAppButton(String parentId, 
      String name, ActionListener listener)
   {
      String id = parentId+"."+name;
      String text = getString(id);

      return createAppButton(text, name, listener,
         getAccelerator(id), getString(id+".tooltip", text));
   }

   public JDRButton createAppButton(String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      return createAppButton(getString("label."+name), name, listener,
         keyStroke, tooltipText);
   }

   public JDRToggleButton createToggleButton(String parentId, 
      String name, ActionListener listener)
   {
      String id = parentId+"."+name;
      String text = getString(id);

      return createToggleButton(text, name, listener,
         getAccelerator(id), getString(id+".tooltip", text));
   }

   public JDRToggleButton createToggleButton(String buttonText, String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      String base = mapIconBaseName(name);

      JDRToggleButton button = getButtonStyle().createToggle(this,
        buttonText, base, listener, tooltipText);

      button.setActionCommand(name);

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, name);
         button.getActionMap().put(name, 
            new ButtonAction(button, listener));
      }

      return button;
   }

   public JDRToolButton createToolButton(
      String buttonText, String name, 
      ActionListener listener, KeyStroke keyStroke,
      ButtonGroup g, boolean selected, String tooltipText)
   {
      String base = mapIconBaseName(name);

      JDRToolButton button = getButtonStyle().createTool(this,
         buttonText, base, listener, g, selected, tooltipText);

      if (g != null)
      {
         g.add(button);
      }

      button.setActionCommand(name);

      if (keyStroke != null && listener != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
           .put(keyStroke, name);
         button.getActionMap().put(name, 
            new ButtonAction(button, listener));
      }

      return button;
   }

   public JDRButton createOkayButton(ActionListener listener)
   {
      return createOkayButton(listener, getString("label.okay"));
   }

   public JDRButton createOkayButton(ActionListener listener, 
     String tooltipText)
   {
      JDRButton button = createDialogButton("label.okay", "okay", 
       listener, getAccelerator("label.okay"), tooltipText);

      button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
        put(getAccelerator("label.alt_okay"), "okay");

      return button;
   }

   public JDRButton createCancelButton(ActionListener listener)
   {
      return createCancelButton(listener, getString("label.cancel"));
   }

   public JDRButton createCancelButton(ActionListener listener,
      String tooltipText)
   {
      return createDialogButton("label.cancel", "cancel", 
       listener, getAccelerator("label.cancel"), tooltipText);
   }

   public JDRButton createCloseButton(ActionListener listener)
   {
      return createCloseButton(listener, getString("label.close"));
   }

   public JDRButton createCloseButton(ActionListener listener, 
      String tooltipText)
   {
      return createDialogButton("label.close", "close", 
       listener, getAccelerator("label.close"), tooltipText);
   }

   public JDRButton createDefaultButton(ActionListener listener)
   {
      return createDefaultButton(listener, getString("label.default.tooltip"));
   }

   public JDRButton createDefaultButton(ActionListener listener,
      String tooltipText)
   {
      return createDialogButton("label.default", "default", 
       listener, getAccelerator("label.default"), tooltipText);
   }

   public JDRButtonItem createButtonItem(String parentId, String action,
     ActionListener listener, JComponent comp, JComponent menu)
   {
      String menuId = (parentId == null ? action : parentId+"."+action);

      KeyStroke keyStroke = getAccelerator(menuId);

      JDRButtonItem button = new JDRButtonItem(this, menuId, action,
        keyStroke, listener, 
        getString(menuId+".tooltip", getString(parentId+".tooltip", null)),
        comp, menu);

      return button;
   }


   public JRadioButton createAppRadioButton(
      String parentId, String action,
      ButtonGroup bg, boolean selected, ActionListener listener)
   {
      String label = (parentId == null ? action : parentId+"."+action);

      JRadioButton button = new JRadioButton(getString(label), selected);

      int mnemonic = getCodePoint(label+".mnemonic", 0);

      if (mnemonic != 0)
      {
         button.setMnemonic(mnemonic);
      }

      bg.add(button);

      if (listener != null)
      {
         button.addActionListener(listener);
      }

      button.setActionCommand(action);

      return button;
   }

   public JRadioButton createAppRadioButton(
      String label, ButtonGroup bg, boolean selected, ChangeListener listener)
   {
      JRadioButton button = new JRadioButton(getString(label), selected);

      int mnemonic = getCodePoint(label+".mnemonic", 0);

      if (mnemonic != 0)
      {
         button.setMnemonic(mnemonic);
      }

      bg.add(button);

      if (listener != null)
      {
         button.addChangeListener(listener);
      }

      return button;
   }

   public JCheckBox createAppCheckBox(
      String parentId, String action,
      boolean selected, ActionListener listener)
   {
      String label = (parentId == null ? action : parentId+"."+action);

      JCheckBox button = new JCheckBox(getString(label), selected);

      int mnemonic = getCodePoint(label+".mnemonic", 0);

      if (mnemonic != 0)
      {
         button.setMnemonic(mnemonic);
      }

      if (listener != null)
      {
         button.addActionListener(listener);
      }

      button.setActionCommand(action);

      button.setToolTipText(getToolTipText(label));

      return button;
   }

   public JCheckBox createAppCheckBox(String label,
      boolean selected, ChangeListener listener)
   {
      JCheckBox button = new JCheckBox(getString(label), selected);

      int mnemonic = getCodePoint(label+".mnemonic", 0);

      if (mnemonic != 0)
      {
         button.setMnemonic(mnemonic);
      }

      if (listener != null)
      {
         button.addChangeListener(listener);
      }

      button.setToolTipText(getToolTipText(label));

      return button;
   }

   public JButton createAppJButton(
      String parentId, String action, ActionListener listener)
   {
      String label = (parentId == null ? action : parentId+"."+action);

      JButton button = new JButton(getString(label));

      int mnemonic = getCodePoint(label+".mnemonic", 0);

      if (mnemonic != 0)
      {
         button.setMnemonic(mnemonic);
      }

      if (listener != null)
      {
         button.addActionListener(listener);
      }

      button.setActionCommand(action);

      return button;
   }

   public JLabel createAppLabel(String id)
   {
      JLabel label = new JLabel(getString(id));

      int c = getCodePoint(id+".mnemonic", 0);

      if (c != 0)
      {
         label.setDisplayedMnemonic(c);
      }

      String tooltip = getString(id+".tooltip", null);

      if (tooltip != null)
      {
         label.setToolTipText(tooltip);
      }

      return label;
   }

   public JTextField createAppInfoField(int cols)
   {
      JTextField field = new JTextField(cols);

      field.setEditable(false);
      field.setBorder(null);
      field.setOpaque(false);

      return field;
   }

   public JTextField createAppInfoField(String id)
   {
      JTextField field = new JTextField(getString(id));

      field.setEditable(false);
      field.setBorder(null);
      field.setOpaque(false);

      return field;
   }

   public JTextField createAppInfoField(String id, Object... params)
   {
      JTextField field = new JTextField(getMessage(id, params));

      field.setEditable(false);
      field.setBorder(null);
      field.setOpaque(false);

      return field;
   }

   public JTextArea createAppInfoArea()
   {
      JTextArea textArea = new JTextArea();

      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      return textArea;
   }

   public JTextArea createAppInfoArea(int cols)
   {
      JTextArea textArea = createAppInfoArea();

      textArea.setColumns(cols);

      return textArea;
   }

   public JTextArea createAppInfoArea(int cols, String id)
   {
      JTextArea textArea = new JTextArea(getString(id));

      textArea.setColumns(cols);
      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      return textArea;
   }

   public JTextArea createAppInfoArea(String id)
   {
      JTextArea textArea = new JTextArea(getString(id));

      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      return textArea;
   }

   public JTextArea createAppInfoArea(String id, Object... values)
   {
      JTextArea textArea = new JTextArea(getMessage(id, values));

      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      return textArea;
   }

   public void addTab(JTabbedPane tabbedPane, String id, Component comp)
   {
      tabbedPane.addTab(getString(id), comp);

      int index = tabbedPane.getTabCount()-1;

      int mnemonic = getCodePoint(id+".mnemonic", -1);

      if (mnemonic != -1)
      {
         tabbedPane.setMnemonicAt(index, mnemonic);
      }

      String tooltipText = getString(id+".tooltip", null);

      if (tooltipText != null)
      {
         tabbedPane.setToolTipTextAt(index, tooltipText);
      }
   }

   public JMenu createAppMenu(String menuId)
   {
      String text = getString(menuId, null);

      if (text == null)
      {
         text = getString(menuId+".label");
      }

      JMenu menu = new JMenu(text);

      menu.setMnemonic(getCodePoint(menuId+".mnemonic"));

      return menu;
   }

   public JMenuItem createAppMenuItem(String parentId, 
      String actionName, ActionListener listener)
   {
      String id = (parentId == null ? actionName
                   : parentId+"."+actionName);

      return createAppMenuItem(id, actionName,
         getAccelerator(id), listener, 
         getString("tooltip."+actionName, null));
   }

   public JMenuItem createAppMenuItem(String id, 
      String actionName, KeyStroke keyStroke, ActionListener listener,
      String tooltipText)
   {
      JMenuItem item = new JMenuItem(getString(id),
         getCodePoint(id+".mnemonic"));

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      if (listener != null)
      {
         item.addActionListener(listener);
      }

      if (actionName != null)
      {
         item.setActionCommand(actionName);
      }

      if (tooltipText != null)
      {
         item.setToolTipText(tooltipText);
      }

      return item;
   }

   public JCheckBoxMenuItem createToggleMenuItem(String id, 
      String actionName, KeyStroke keyStroke, ActionListener listener,
      String tooltipText)
   {
       return createToggleMenuItem(id, actionName, false, keyStroke,
          listener, tooltipText);
   }

   public JCheckBoxMenuItem createToggleMenuItem(String id, 
      String actionName, boolean selected, KeyStroke keyStroke, 
      ActionListener listener, String tooltipText)
   {
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(getString(id), selected);

      item.setMnemonic(getCodePoint(id+".mnemonic"));

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      if (listener != null)
      {
         item.addActionListener(listener);
      }

      if (actionName != null)
      {
         item.setActionCommand(actionName);
      }

      if (tooltipText != null)
      {
         item.setToolTipText(tooltipText);
      }

      return item;
   }

   public JRadioButtonMenuItem createToolMenuItem(String id, 
      String actionName, KeyStroke keyStroke, ButtonGroup group,
      boolean selected, ActionListener listener, String tooltipText)
   {
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(getString(id),
         selected);

      group.add(item);

      item.setMnemonic(getCodePoint(id+".mnemonic"));

      if (keyStroke != null)
      {
         item.setAccelerator(keyStroke);
      }

      if (listener != null)
      {
         item.addActionListener(listener);
      }

      if (actionName != null)
      {
         item.setActionCommand(actionName);
      }

      if (tooltipText != null)
      {
         item.setToolTipText(tooltipText);
      }

      return item;
   }

   public LengthPanel createLengthPanel(String tag)
   {
      LengthPanel panel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         panel = new LengthPanel(getMessageSystem(), getString(tag));
      }
      else
      {
         panel = new LengthPanel(getMessageSystem(), getString(tag), mnemonic);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         panel.setToolTipText(tooltip);
      }

      return panel;
   }

   public LengthPanel createLengthPanel(String tag, NumberField numField)
   {
      LengthPanel panel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         panel = new LengthPanel(getMessageSystem(), getString(tag), numField);
      }
      else
      {
         panel = new LengthPanel(getMessageSystem(), getString(tag), mnemonic, numField);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         panel.setToolTipText(tooltip);
      }

      return panel;
   }

   public LengthPanel createLengthPanel(String tag,
     SamplePanel samplePanel, NumberField numField)
   {
      LengthPanel lengthPanel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getString(tag), samplePanel, numField);
      }
      else
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getString(tag), mnemonic, samplePanel, numField);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         lengthPanel.setToolTipText(tooltip);
      }

      return lengthPanel;
   }

   public LengthPanel createLengthPanel(String tag,
     SamplePanel samplePanel)
   {
      LengthPanel lengthPanel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getString(tag), samplePanel);
      }
      else
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getString(tag), mnemonic, samplePanel);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         lengthPanel.setToolTipText(tooltip);
      }

      return lengthPanel;
   }

   public LengthPanel createLengthPanel(SamplePanel samplePanel)
   {
      return new LengthPanel(getMessageSystem(), samplePanel);
   }

   public LengthPanel createLengthPanel(NumberField numField)
   {
      return new LengthPanel(getMessageSystem(), numField);
   }

   public LengthPanel createLengthPanel(SamplePanel samplePanel, NumberField numField)
   {
      return new LengthPanel(getMessageSystem(), samplePanel, numField);
   }

   public LengthPanel createLengthPanel()
   {
      return new LengthPanel(getMessageSystem());
   }

   public NonNegativeLengthPanel createNonNegativeLengthPanel(String tag)
   {
      NonNegativeLengthPanel panel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         panel = new NonNegativeLengthPanel(getMessageSystem(), getString(tag));
      }
      else
      {
         panel = new NonNegativeLengthPanel(getMessageSystem(), getString(tag), mnemonic);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         panel.setToolTipText(tooltip);
      }

      return panel;
   }

   public NonNegativeLengthPanel createNonNegativeLengthPanel(String tag, SamplePanel samplePanel)
   {
      NonNegativeLengthPanel lengthPanel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         lengthPanel = new NonNegativeLengthPanel(getMessageSystem(), getString(tag), samplePanel);
      }
      else
      {
         lengthPanel = new NonNegativeLengthPanel(getMessageSystem(), getString(tag), mnemonic, samplePanel);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         lengthPanel.setToolTipText(tooltip);
      }

      return lengthPanel;
   }

   public NonNegativeLengthPanel createNonNegativeLengthPanel()
   {
      return new NonNegativeLengthPanel(getMessageSystem());
   }

   public NonNegativeLengthPanel createNonNegativeLengthPanel(SamplePanel samplePanel)
   {
      return new NonNegativeLengthPanel(getMessageSystem(), samplePanel);
   }

   public NonNegativeDoublePanel createNonNegativeDoublePanel(String tag, double value)
   {
      return new NonNegativeDoublePanel(this, tag, value);
   }

   public NonNegativeIntPanel createNonNegativeIntPanel(String tag, int value)
   {
      return new NonNegativeIntPanel(this, tag, value);
   }

   public AnglePanel createAnglePanel(String tag, NumberField numField)
   {
      AnglePanel anglePanel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         anglePanel = new AnglePanel(getMessageSystem(), getString(tag), numField);
      }
      else
      {
         anglePanel = new AnglePanel(getMessageSystem(), getString(tag), mnemonic, numField);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         anglePanel.setToolTipText(tooltip);
      }

      return anglePanel;
   }

   public AnglePanel createAnglePanel(String tag)
   {
      AnglePanel anglePanel;

      int mnemonic = getCodePoint(tag+".mnemonic", 0);

      if (mnemonic == 0)
      {
         anglePanel = new AnglePanel(getMessageSystem(), getString(tag));
      }
      else
      {
         anglePanel = new AnglePanel(getMessageSystem(), getString(tag), mnemonic);
      }

      String tooltip = getToolTipText(tag);

      if (tooltip != null)
      {
         anglePanel.setToolTipText(tooltip);
      }

      return anglePanel;
   }

   public AnglePanel createAnglePanel(double radius, byte unitId)
   {
      return new AnglePanel(getMessageSystem(), radius, unitId);
   }

   public void createIconNameMap()
   {
      iconnamemap = new Properties();

      iconnamemap.setProperty("grid.show", "showgrid");
      iconnamemap.setProperty("grid.lock", "lockgrid");
      iconnamemap.setProperty("path.edit", "editPath");
      iconnamemap.setProperty("select_all", "selectAll");
      iconnamemap.setProperty("front", "movetofront");
      iconnamemap.setProperty("back", "movetoback");
      iconnamemap.setProperty("pattern.set", "pattern");
      iconnamemap.setProperty("open_line", "openline");
      iconnamemap.setProperty("closed_line", "closedline");
      iconnamemap.setProperty("open_curve", "opencurve");
      iconnamemap.setProperty("closed_curve", "closedcurve");
      iconnamemap.setProperty("manual", "help");
      iconnamemap.setProperty("remove.textmap", "remove");
      iconnamemap.setProperty("add.textmap", "add");
      iconnamemap.setProperty("remove.mathmap", "remove");
      iconnamemap.setProperty("add.mathmap", "add");
      iconnamemap.setProperty("remove.unicode_block", "remove");
      iconnamemap.setProperty("add.unicode_block", "add");
      iconnamemap.setProperty("find_again", "findAgain");
      iconnamemap.setProperty("replace_all", "replaceAll");
      iconnamemap.setProperty("close", "cancel");
      iconnamemap.setProperty("textmappings.import", "import");
      iconnamemap.setProperty("mathmappings.import", "import");
   }

   public String mapIconBaseName(String propName)
   {
      if (iconnamemap == null) return propName;

      return iconnamemap.getProperty(propName, propName);
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return dictionary;
   }

   public JDRMessage getMessageSystem()
   {
      return messageSystem;
   }

   public void setMessageSystem(JDRMessage msgSys)
   {
      this.messageSystem = msgSys;
   }

   public String getDictLocaleId()
   {
      return dictLocaleId;
   }

   public String getHelpLocaleId()
   {
      return helpLocaleId;
   }

   public void setDictLocaleId(String id)
   {
      dictLocaleId = id;
   }

   public void setHelpLocaleId(String id)
   {
      helpLocaleId = id;
   }

   public Font getStartUpInfoFont()
   {
      return startupInfoFont;
   }

   public Font getStartUpVersionFont()
   {
      return startupVersionFont;
   }

   public void setStartUpInfoFont(String font)
   {
      startupInfoFont = Font.decode(font);
   }

   public void setStartUpVersionFont(String font)
   {
      startupVersionFont = Font.decode(font);
   }

   public void setStartUpInfoFont(Font font)
   {
      startupInfoFont = font;
   }

   public void setStartUpVersionFont(Font font)
   {
      startupVersionFont = font;
   }

   private JDRMessage messageSystem;

   private JDRDictionary dictionary;

   private static HelpBroker mainHelpBroker = null;
   private static CSH.DisplayHelpFromSource csh = null;

   private String dictLocaleId, helpLocaleId;

   public Accelerators accelerators = null;

   public Properties iconnamemap;

   public boolean debugMode = false;

   private String usersettings = null;

   private Font startupInfoFont = new Font("Serif", Font.PLAIN, 10);
   private Font startupVersionFont = new Font("Serif", Font.BOLD, 20);

   private static DirectoryFilter directoryFilter = new DirectoryFilter();
   private static DictionaryFilter dictionaryFilter = new DictionaryFilter();

   private JTabbedPane stackTracePane;
   private JTextArea messageArea, stackTraceDetails;
   private JScrollPane messageSP;

   private String okayOption, exitOption;

   public static final int EXIT_SYNTAX = 1;

   public static final int EXIT_INTERNAL_ERROR = 2;

   public static final int EXIT_FATAL_ERROR = 3;

   public static final JDRButtonStyle[] BUTTON_STYLES = new JDRButtonStyle[]
   {
      new JDRButtonStyle("default", "buttons"),
      new JDRButtonStyle("small", "smallbuttons", true),
      new JDRButtonStyle("plain", "plain", false, true, true, false, false),
      new JDRButtonStyle("highlights", "plain", false, true, true, true, true),
      new JDRButtonStyle("bordered", "buttons", "plain",
          false, true, true, true, true),
      new JDRButtonStyle("smallplain", "smallplain",
          true, true, true, false, false),
      new JDRButtonStyle("smallhighlights", "smallplain",
          true, true, true, true, true),
      new JDRButtonStyle("smallbordered", "smallbuttons", "smallplain",
          true, true, true, true, true),
      new JDRButtonStyle("textsmallplain", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textsmallhighlights", "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textsmallbordered", "smallbuttons", "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textsmallplainleading", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallhighlightsleading", "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallborderedleading", "smallbuttons",
          "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallplaintop", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallhighlightstop", "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallborderedtop", "smallbuttons",
          "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallplainbottom", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallhighlightsbottom", "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallborderedbottom", "smallbuttons",
          "smallplain",
          false, true, true, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("text", "smallplain",
          false, false, false, false, false, JDRButtonStyle.TEXT_ONLY),
   };

   private static int buttonStyle = 0;

   public static final int DIALOG_BUTTON_AS_GENERAL=-1;
   public static final int DIALOG_BUTTON_DEFAULT=0;
   public static final int DIALOG_BUTTON_TEXT_ONLY=1;
   public static final int DIALOG_BUTTON_LARGE_ICONS_AND_TEXT_TRAILING=2;
   public static final int DIALOG_BUTTON_SMALL_ICONS_AND_TEXT_TRAILING=3;
   public static final int DIALOG_BUTTON_SMALL_BORDERED_ICONS_AND_TEXT_TRAILING=4;
   public static final int DIALOG_BUTTON_LARGE_ICONS_AND_TEXT_LEADING=5;
   public static final int DIALOG_BUTTON_SMALL_ICONS_AND_TEXT_LEADING=6;
   public static final int DIALOG_BUTTON_SMALL_BORDERED_ICONS_AND_TEXT_LEADING=7;
   public static final int DIALOG_BUTTON_LARGE_ICONS_AND_TEXT_TOP=8;
   public static final int DIALOG_BUTTON_SMALL_ICONS_AND_TEXT_TOP=9;
   public static final int DIALOG_BUTTON_SMALL_BORDERED_ICONS_AND_TEXT_TOP=10;
   public static final int DIALOG_BUTTON_LARGE_ICONS_AND_TEXT_BOTTOM=11;
   public static final int DIALOG_BUTTON_SMALL_ICONS_AND_TEXT_BOTTOM=12;
   public static final int DIALOG_BUTTON_SMALL_BORDERED_ICONS_AND_TEXT_BOTTOM=13;

   private static int dialogButtonStyle = DIALOG_BUTTON_AS_GENERAL;

   public static final String[] DIALOG_BUTTON_TAGS = new String[]
   {
     "dialog_button_default",
     "dialog_button_text_only",
     "dialog_button_large_icons_and_text_trailing",
     "dialog_button_small_icons_and_text_trailing",
     "dialog_button_small_bordered_icons_and_text_trailing",
     "dialog_button_large_icons_and_text_leading",
     "dialog_button_small_icons_and_text_leading",
     "dialog_button_small_bordered_icons_and_text_leading",
     "dialog_button_large_icons_and_text_top",
     "dialog_button_small_icons_and_text_top",
     "dialog_button_small_bordered_icons_and_text_top",
     "dialog_button_large_icons_and_text_bottom",
     "dialog_button_small_icons_and_text_bottom",
     "dialog_button_small_bordered_icons_and_text_bottom"
   };

   public static final JDRButtonStyle[] DIALOG_BUTTON_STYLES = new JDRButtonStyle[]
   {
      new JDRButtonStyle("default", "buttons"),
      new JDRButtonStyle("text", "smallplain",
          false, true, true, false, false, JDRButtonStyle.TEXT_ONLY),
      new JDRButtonStyle("textplaintrailing", "plain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textsmallplaintrailing", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textsmallborderedtrailing", "smallbuttons",
          false, false, false, true, true, JDRButtonStyle.ICON_TEXT),
      new JDRButtonStyle("textplainleading", "plain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallplainleading", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallborderedleading", "smallbuttons",
          false, false, false, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textplaintop", "plain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallplaintop", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallborderedtop", "smallbuttons",
          false, false, false, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textplainbottom", "plain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallplainbottom", "smallplain",
          false, true, true, false, false, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallborderedbottom", "smallbuttons",
          false, false, false, true, true, JDRButtonStyle.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
   };

}

class DirectoryFilter implements java.io.FilenameFilter
{
   public boolean accept(File dir, String name)
   {
      return (new File(dir, name)).isDirectory();
   }
}

class DictionaryFilter implements java.io.FilenameFilter
{
   public boolean accept(File dir, String name)
   {
      return name.contains("-") && name.endsWith(".prop");
   }
}

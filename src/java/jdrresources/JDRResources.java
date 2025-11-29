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
import java.nio.file.Path;
import java.net.*;  
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;
import javax.swing.event.ChangeListener;

import org.xml.sax.SAXException;

import com.dickimawbooks.texjavahelplib.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;
import com.dickimawbooks.jdr.io.JDRMessage;
import com.dickimawbooks.jdr.io.JDRMessagePublisher;
import com.dickimawbooks.jdr.exceptions.*;
import com.dickimawbooks.jdrresources.filter.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Resources required by FlowframTk and JDRView.
 */
public class JDRResources
 extends TeXJavaHelpLibAppAdapter
 implements JDRMessageDictionary,Serializable
{
   public JDRResources()
   {
      this("jdrresources");
   }

   public JDRResources(String appname)
   {
      this.appname = appname;
   }

   public void initialise()
   throws IOException,URISyntaxException,InvalidFormatException
   {
      initialise("jdrcommon", appname.toLowerCase().replaceAll(" ", ""));
   }

   public void initialise(String... dictPrefixes)
   throws IOException,URISyntaxException,InvalidFormatException
   {
      this.fatalErrorExitCode = EXIT_INTERNAL_ERROR;
      initUserConfigDir();
      initLocalisation(dictPrefixes);
      initStackTracePane();
      createIconNameMap();
   }

   protected void initLocalisation(String... dictPrefixes)
     throws IOException, InvalidFormatException
   {
      loadLanguageSettings();

      if (dictLocale == null)
      {
         dictLocale = new HelpSetLocale(Locale.getDefault());
      }

      if (helpSetLocale == null)
      {
         helpSetLocale = new HelpSetLocale(Locale.getDefault());
      }

      helpLib = new TeXJavaHelpLib(this,
       appname, "/resources", "/resources/dictionaries",
       dictLocale, helpSetLocale, dictPrefixes);

      helpLib.setDefaultButtonOmitTextIfIcon(true);
   }

   public String getDictionaryTag()
   {
      return dictLocale.getTag();
   }

   public HelpSetLocale getDictionaryLocale()
   {
      return dictLocale;
   }

   public void setDictionary(HelpSetLocale hsLocale)
   {
      dictLocale = hsLocale;
   }

   public void setDictionary(String tag)
   {
      setDictionary(new HelpSetLocale(tag));
   }

   public String getHelpSetTag()
   {
      return helpSetLocale.getTag();
   }

   public HelpSetLocale getHelpSetLocale()
   {
      return helpSetLocale;
   }

   public void setHelpSet(HelpSetLocale hsLocale)
   {
      helpSetLocale = hsLocale;
      helpLib.setHelpSetLocale(hsLocale);
   }

   public void setHelpSet(String tag)
   {
      setHelpSet(new HelpSetLocale(tag));
   }

   @Override
   public String getApplicationName()
   {
      return appname;
   }

   @Override
   public String getApplicationVersion()
   {
      return APP_VERSION;
   }

   @Override
   public boolean isGUI()
   {
      return true;
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

      try
      {
         throw new FileNotFoundException(
           getMessage("error.file_not_found_with_name", filename));
      }
      catch (FileNotFoundException e)
      {
         warning(e.getMessage(), e);
      }

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

      try
      {
         throw new FileNotFoundException(
           getMessage("error.file_not_found_with_name", filename));
      }
      catch (FileNotFoundException e)
      {
         warning(e.getMessage(), e);
      }

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

   public void setButtonStyle(String name)
    throws IllegalArgumentException
   {
      for (int i = 0; i < BUTTON_STYLES.length; i++)
      {
         if (BUTTON_STYLES[i].getName().equals(name))
         {
            buttonStyle = i;
            helpLib.setDefaultButtonOmitTextIfIcon(BUTTON_STYLES[i].isIconOnly());
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

   @Override
   public ImageIcon getSmallIcon(String base, String... extensions)
   {
      for (String ext : extensions)
      {
         String filename = getIconDir() + "/" + base
          + helpLib.getSmallIconSuffix() + "." + ext;

         java.net.URL imgURL = getClass().getResource(filename);

         if (imgURL == null)
         {
            filename = getIconDir() + "/" + base
             + "." + ext;

            imgURL = getClass().getResource(filename);
         }

         if (imgURL != null)
         {
            return new ImageIcon(imgURL);
         }
      }

      return null;
   }
  
   @Override
   public ImageIcon getLargeIcon(String base, String... extensions)
   {
      for (String ext : extensions)
      {
         String filename = getIconDir() + "/" + base
           + helpLib.getLargeIconSuffix() + "." + ext;

         java.net.URL imgURL = getClass().getResource(filename);

         if (imgURL == null)
         {
            filename = getIconDir() + "/" + base
             + "." + ext;

            imgURL = getClass().getResource(filename);
         }

         if (imgURL != null)
         {
            return new ImageIcon(imgURL);
         }
      }

      return null;
   }
  
   public ImageIcon getButtonIcon(String base)
   {
      String location = getButtonStyle().getLocation();

      return helpLib.getLargeIcon(location+"/"+base);
   }

   public ImageIcon getSmallButtonIcon(String base)
   {
      String location = getButtonStyle().getLocation();

      return helpLib.getSmallIcon(location+"/"+base);
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

      try
      {
         throw new FileNotFoundException(
           getMessage("error.file_not_found_with_name", filename));
      }
      catch (FileNotFoundException e)
      {
         warning(e.getMessage(), e);
      }

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
            displayStackTrace(parent, frameTitle, e.toString(), e);
         }
         else
         {
            displayStackTrace(parent, frameTitle, String.format("%s%n%s", e, cause), e);
         }
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
      internalError(parent, null, e);
   }

   /**
    * Displays a dialog box with the given message and the option
    * for the user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param message the error message
    */
   public void internalError(Component parent, String message)
   {
      internalError(parent, message, null);
   }

   /**
    * Displays a dialog box with the given message and the option
    * for the user to continue or quit the application.
    * @param parent the parent for the dialog box
    * @param message the error message
    */
   public void internalError(Component parent, String[] message)
   {
      internalError(parent, String.join(String.format("%n"), message), null);
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
   public void error(Component parent, String[] message)
   {
      error(parent, String.join(String.format("%n"), message));
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
   @Override
   public void error(Component parent, Throwable e)
   {
      if (e instanceof UserCancelledException
        || e instanceof java.util.concurrent.CancellationException)
      {
         String msg = e.getMessage();

         if (msg == null || msg.isEmpty())
         {
            msg = getMessageWithFallback("process.aborted", "Process Aborted");
         }

         error(parent, msg, null);
      }
      else if (e.getCause() instanceof UserCancelledException)
      {
         String msg = e.getMessage();
         String msg2 = e.getCause().getMessage();

         if (msg2 == null || msg2.isEmpty())
         {
            msg2 = getMessageWithFallback("process.aborted", "Process Aborted");
         }

         if (msg == null || msg.isEmpty())
         {
            msg = msg2;
         }
         else
         {
            msg = String.format("%s%n%s", msg, msg2);
         }

         error(parent, msg, null);
      }
      else
      {
         error(parent, null, e);
      }
   }

   /**
    * Displays message and stack trace in a dialog box and
    * exits the application when the dialog box is dismissed.
    * @param message the error message
    * @param e the exception
    */
   public void fatalError(String message, Throwable e)
   {
      fatalError(message, e, EXIT_FATAL_ERROR);
   }

   /**
    * Displays a warning message in a dialog box.
    * @param parent the dialog's parent
    * @param message the warning message
    */
   public void warning(Component parent, String[] message)
   {
      warning(parent, String.join(String.format("%n"), message));
   }

   public int confirm(Component parent, String message)
   {
      return confirm(parent, message, getMessage("process.confirm"), JOptionPane.YES_NO_OPTION);
   }

   public int confirm(Component parent, String message, String title)
   {
      return confirm(parent, message, title, JOptionPane.YES_NO_OPTION);
   }

   public int confirm(Component parent, String message, int options)
   {
      return confirm(parent, message, getMessage("process.confirm"), options);
   }

   public int confirm(Component parent, String message, int options, int type)
   {
      return confirm(parent, message, getMessage("process.confirm"), options);
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
      return showConfirmDialog(parent, message, title,
        options, type);
   }

   public void message(Component parent, String message, String title)
   {
      message(parent, message, title, JOptionPane.PLAIN_MESSAGE);
   }

   public void message(Component parent, String message, String title, int type)
   {
      showMessageDialog(parent, message, title, type);
   }

   /**
    * Gets an integer associated with the given key in the
    * resources dictionary.
    */
   public int getInt(String key)
      throws NumberFormatException
   {
      String msg = getMessageIfExists(key);

      if (msg == null || msg.isEmpty())
      {
         return -1;
      }

      return Integer.parseInt(msg);
   }

   @Deprecated
   public String applyMessagePattern(String key, Number value)
   {
      return key;
   }

   @Deprecated
   public String formatMessageChoice(Number value, String key)
   {
      return key;
   }

   @Deprecated
   public String formatMessageChoice(Number value, String key, Object... params)
   {
      return key;
   }

   public String getMessage(String key, Object... values)
   {
      return helpLib.getMessage(key, values);
   }

   public String getMessage(String key, int value)
   {
      return helpLib.getMessage(key, ""+value);
   }

   public String getMessageIfExists(String key, Object... values)
   {
      return helpLib.getMessageIfExists(key, values);
   }

   @Override
   public String getMessageWithFallback(String key, String altFormat, Object... values)
   {
      return helpLib.getMessageWithFallback(key, altFormat, values);
   }

   @Deprecated
   public String getMessageWithAlt(String altFormat, String key, Object... values)
   {
      return getMessageWithFallback(key, altFormat, values);
   }

   public String getMessage(int lineNum, String key, Object... values)
   {
      return getMessageWithFallback("error.with_line", "Line {0}: {1}", 
        lineNum, getMessage(key, values));
   }

   @Deprecated
   public String getMessageWithAlt(int lineNum, String altFormat, 
     String key, Object... values)
   {
      return getMessageWithAlt("%d: %s", "error.with_line", lineNum,
       getMessageWithAlt(altFormat, key, values));
   }

   public String getMessageWithFallback(int lineNum, 
     String key, String fallbackFormat, Object... values)
   {
      return getMessageWithFallback("error.with_line", "{0}: {1}", lineNum,
       getMessageWithFallback(key, fallbackFormat, values));
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
   @Deprecated
   public String getString(String key, String defVal)
   {
      return getMessageWithFallback(key, defVal);
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
   @Deprecated
   public String getString(String key)
   {
      return getMessage(key);
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
      String text = getMessageIfExists(key);
   
      if (text == null || text.isEmpty()) return defVal;
   
      return text.charAt(0);
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
    * if not found.
    * @param key the key identifying the required string
    * @param defVal the default value
    * @return the first character of the required string 
    * or default value if not found
    */
   public int getCodePoint(String key, int defVal)
   {
      String text = getMessageIfExists(key);
   
      if (text == null || text.isEmpty()) return defVal;
   
      return text.codePointAt(0);
   }

   /**
    * Gets the code point of the first character of the string associated with the 
    * given key in the resource dictionary. If not found the 
    * first character of the key is used instead.
    * @param key the key identifying the required string
    * @return the first character of the required string 
    * or the first character of the key if not found
    */
   public int getCodePoint(String key)
   {
      return getCodePoint(key, key.codePointAt(0));
   }

   public int getMnemonic(String label)
   {
      if (!label.endsWith(".mnemonic"))
      {
         label += ".mnemonic";
      }

      return helpLib.getMnemonic(label);
   }

   public String getToolTipText(String id)
   {
      if (id.startsWith("tooltip.") || id.endsWith(".tooltip"))
      {
         return getMessageIfExists(id);
      }

      String tooltip = getMessageIfExists(id+".tooltip");

      if (tooltip == null)
      {
         tooltip = getMessageIfExists("tooltip."+id);
      }

      return tooltip;
   }

   public String getDefaultDescription(JDRCompleteObject object)
   {
      String description = "";

      FlowFrame flowframe = object.getFlowFrame();

      if (flowframe == null)
      {
         String name = object.getClass().getSimpleName();

         description = getMessage("findbydescription."+name,
                  object.getDescriptionInfo());

         String tag = object.getTag();

         if (tag != null && !tag.isEmpty())
         {
            description += " [" + tag + "]";
         }
      }
      else
      {
         switch (flowframe.getType())
         {
            case FlowFrame.STATIC:
               description = getMessage("flowframe.static");
            break;
            case FlowFrame.FLOW:
               description = getMessage("flowframe.flow");
            break;
            case FlowFrame.DYNAMIC:
               description = getMessage("flowframe.dynamic");
            break;
         }

         description += " \""+flowframe.getLabel()+"\"";
      }

      return description;
   }

   @Deprecated
   public Enumeration<Object> getAcceleratorPropertyNames()
   {
      return null;
   }

   public String getAcceleratorString(String propName)
   {
      KeyStroke k = getAccelerator(propName);

      return k == null ? null : k.toString();
   }

   public KeyStroke getAccelerator(String propName)
   {
      return getAccelerator(propName, null);
   }

   public KeyStroke getAccelerator(String propName, String defValue)
   {
      KeyStroke keyStroke = null;

      if (keyStrokes == null)
      {
         keyStrokes = new HashMap<String,KeyStroke>();
      }

      keyStroke = keyStrokes.get(propName);

      if (keyStroke == null)
      {
         keyStroke = helpLib.getKeyStroke(propName);

         if (keyStroke == null && defValue != null)
         {
            keyStroke = KeyStroke.getKeyStroke(defValue);
         }

         if (keyStroke != null)
         {
            keyStrokes.put(propName, keyStroke);
         }
      }

      return keyStroke;
   }

   public void setAccelerator(String propName, String keystrokeName)
   {
      KeyStroke k = null;

      if (keystrokeName != null)
      {
         k = KeyStroke.getKeyStroke(keystrokeName);
      }

      setAccelerator(propName, k);
   }

   public void setAccelerator(String propName, KeyStroke k)
   {
      if (keyStrokes == null)
      {
         keyStrokes = new HashMap<String,KeyStroke>();
      }

      if (k == null)
      {
         keyStrokes.remove(propName);
      }
      else
      {
         keyStrokes.put(propName, k);
      }

      helpLib.setKeyStrokeProperty(propName, k);
   }

   @Deprecated
   public boolean areAcceleratorsInitialised()
   {
      return false;
   }

   @Deprecated
   public void initialiseAccelerators()
   {
   }

   public void initialiseAccelerators(boolean upgrade,
      BufferedReader reader)
      throws IOException
   {
      if (upgrade)
      {
         setAccelerator("menu.help.manual", getAccelerator("menu.help.manual"));
         setAccelerator("button.help", getAccelerator("button.help"));
      }

      String line;

      while ((line = reader.readLine()) != null)
      {
         if (line.startsWith("#") || line.isEmpty())
         {
            continue;
         }

         String[] split = line.split("=", 1);

         if (split.length == 2)
         {
            String propName = split[0];
            KeyStroke keyStroke = KeyStroke.getKeyStroke(split[1]);

            if (upgrade)
            {
               // take renaming into account

               if (propName.startsWith("text."))
               {
                  propName = "menu.textarea." + propName.substring(5);
               }
               else if (propName.startsWith("label."))
               {
                  propName = "button." + propName.substring(7);
               }
               else if (propName.equals("accelerator.alt_popup"))
               {
                  propName = "action.context_menu";
               }
               else if (propName.startsWith("accelerator."))
               {
                  propName = "action." + propName.substring(7);
               }
               else if (propName.startsWith("tools.")
                 || propName.startsWith("file.")
                 || propName.startsWith("edit.")
                 || propName.startsWith("editpath.")
                 || propName.startsWith("transform.")
                 || propName.startsWith("navigate.")
                 || propName.startsWith("settings.")
                 || propName.startsWith("texeditor.")
                 || propName.startsWith("debug.")
                 )
               {
                  propName = "menu." + propName;
               }
            }

            setAccelerator(propName, keyStroke);
         }
      }
   }

   public void saveAccelerators(PrintWriter writer)
      throws IOException
   {
      writer.println("#Accelerators");
      writer.print("#");
      writer.println(new Date());

      if (keyStrokes != null)
      {
         for (Iterator<String> it = keyStrokes.keySet().iterator(); it.hasNext(); )
         {
            String propName = it.next();
            KeyStroke k = keyStrokes.get(propName);

            if (k != null)
            {
               writer.print(propName);
               writer.print("=");
               writer.println(k);
            }
         }
      }
   }

   public Iterator<String> getKeyStrokeIterator()
   {
      if (keyStrokes != null)
      {
         return keyStrokes.keySet().iterator();
      }

      return null;
   }

   @Deprecated
   public boolean isDictInitialised()
   {
      return helpLib.getMessageSystem() != null;
   }

   /**
    * Initialises dictionary using the language resources.
    * No op now that TeXJavaHelpLib is used.
    */
   @Deprecated
   public void initialiseDictionary()
   throws IOException
   {
   }

   /**
    * Initialises dictionary.
    * @param dictionaryInputStream the dictionary input stream
    * @param licenseInputStream the license input stream
    * No op now that TeXJavaHelpLib is used.
    */
   @Deprecated
   public void initialiseDictionary(
      InputStream dictionaryInputStream,
      InputStream licenseInputStream)
   throws IOException
   {
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

   protected void setUserSettings(File dir)
   {
      try
      {
         usersettings = dir.getCanonicalPath().toString();
      }
      catch (IOException e)
      {
         usersettings = dir.getAbsolutePath().toString();
      }

      setLogFile(new File(dir, "errors.log"));
   }

   protected void setUserSettings(Path path)
   {
      usersettings = path.toString();
      setLogFile(new File(path.toFile(), "errors.log"));
   }

   protected void setUserSettings(String path)
   {
      setUserSettings(new File(path));
   }

   private void initUserConfigDir()
    throws IOException,URISyntaxException
   {
      usersettings=null;
      File file = findUserConfigDir(true);

      if (file != null)
      {
         setUserSettings(file);
      }
      else
      {
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
               setUserSettings(file);
               return;
            }
            else if (file.exists())
            {
               throw new IOException(
                 getMessageWithFallback("error.not_a_directory",
                  "''{0}'' is not a directory", file));
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
                     setUserSettings(url.toURI().getPath());
                     return;
                  }
               }
               else
               {
                  throw new IOException(
                     getMessageWithFallback("error.config_cant_create",
                       "Can''t create config directory ''{0}''",
                       file));
               }
            }
         }
      }
   }

   public static File findUserConfigDir(boolean mkdirIfNotExist)
    throws IOException
   {
      // does JDRSETTINGS exist?

      String jdrsettings = System.getenv("JDRSETTINGS");
      File file=null;

      if (jdrsettings != null)
      {
         file = new File(jdrsettings);

         if (file.exists() && file.isDirectory())
         {
            return file;
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
            return file;
         }

         file = new File(homeDir, "jpgfdraw-settings");

         if (file.exists() && file.isDirectory())
         {
            return file;
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
            return file;
         }

         if (mkdirIfNotExist && file.mkdir())
         {
            return file;
         }

         if (!dirname.equals("flowframtk-settings"))
         {
            file = new File(homeDir, "flowframtk-settings");

            if (file.exists() && file.isDirectory())
            {
               return file;
            }

            if (mkdirIfNotExist && file.mkdir())
            {
               return file;
            }
         }
      }

      return null;
   }

   public void saveLanguageSettings() throws IOException
   {
      File file = new File(usersettings, "languages.conf");

      PrintWriter out = null;

      try
      {
          out = new PrintWriter(new FileWriter(file));
          debugMessage("writing: "+file);

          if (dictLocale != null)
          {
             out.println("dict_lang="+dictLocale.getTag());
          }

          if (helpSetLocale != null)
          {
             out.println("help_lang="+helpSetLocale.getTag());
          }
      }
      finally
      {
         if (out != null)
         {
            out.close();
         }
      }
   }

   protected void loadLanguageSettings()
      throws IOException,InvalidFormatException
   {
      String helpsetTag = null;
      String dictTag = null;

      File file = new File(usersettings, "languages.conf");

      if (file.exists())
      {
         debugMessage("reading: "+file);

         BufferedReader in = null;
         int lineNum = 0;

         try
         {
             in = new BufferedReader(new FileReader(file));

             String line;

             while ((line = in.readLine()) != null)
             {
                lineNum++;

                line = line.trim();

                if (line.isEmpty() || line.startsWith("#"))
                {
                   continue;
                }

                String[] split = line.split(" *= *", 2);

                if (split == null || split.length < 2)
                {
                    throw new InvalidFormatException(
                      String.format("%s:%d: key=value pair expected, '%s' found ",
                        file.toString(), lineNum, line));
                }

                String key = split[0];
                String value = split[1];

                if (key.equals("dict_lang"))
                {
                   dictTag = value.trim();
                }
                else if (key.equals("help_lang"))
                {
                   helpsetTag = value.trim();
                }
             }
         }
         finally
         {
            if (in != null)
            {
               in.close();
            }
         }
      }

      if (dictTag != null && !dictTag.isEmpty())
      {
         dictLocale = new HelpSetLocale(dictTag);
      }

      if (helpsetTag != null && !helpsetTag.isEmpty())
      {
         helpSetLocale = new HelpSetLocale(helpsetTag);
      }
   }

   @Override
   public void debug(String message)
   {
      debugMessage(message);
   }

   @Override
   public void debug(Component owner, String message)
   {
      debugMessage(message);
   }

   @Override
   public void debug(Throwable e)
   {
      debugMessage(e);
   }

   @Override
   public void debug(Component owner, Throwable e)
   {
      debugMessage(e);
   }

   @Override
   public void debug(Component owner, String message, Throwable e)
   {
      debugMessage(message);
      debugMessage(e);
   }

   @Override
   public void debug(String message, Throwable e)
   {
      debugMessage(message);
      debugMessage(e);
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

   @Override
   public boolean isDebuggingOn()
   {
      return debugMode;
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
         return new String[] {"en"};
      }

      File[] files = parent.listFiles(dictionaryFilter);

      if (files == null)
      {
         debugMessage("no dictionaries found");
         return new String[] {"en"};
      }

      Vector<String> lang = new Vector<String>(files.length);

      for (int i = 0; i < files.length; i++)
      {
         String name = files[i].getName();

         String l = name.substring(name.indexOf("-")+1, name.lastIndexOf("."));

         if (!lang.contains(l))
         {
            lang.add(l);
         }
      }

      return lang.toArray(new String[lang.size()]);
   }

   public String[] getAvailableHelpLanguages(String dirBase)
   {
      URL url = getClass().getResource("/resources/helpsets/"+dirBase);

      if (url == null)
      {
         debugMessage("no helpsets found");
         return new String[] { "en" };
      }

      File parent;

      try
      {
         parent = new File(url.toURI());
      }
      catch (URISyntaxException e)
      {
         // this shouldn't happen!

         e.printStackTrace();
         return new String[] {"en"};
      }

      File[] files = parent.listFiles(helpsetDirectoryFilter);

      if (files == null)
      {
         debugMessage("no helpsets found");
         return new String[] {"en"};
      }

      String[] lang = new String[files.length];

      for (int i = 0; i < files.length; i++)
      {
         lang[i] = files[i].getName();
      }

      return lang;
   }

   /**
    * Initialises the GUI help components.
    */
   public void initialiseHelp(JFrame parent) throws IOException,SAXException
   {
      initialiseHelp(parent,
        "helpsets/"+appname.toLowerCase().replaceAll(" ", ""));
   }

   public void initialiseHelp(JFrame parent, String helpSetBase)
     throws IOException,SAXException
   {
      helpLib.initHelpSet(helpSetBase, "navigation");
      HelpFrame helpFrame = helpLib.getHelpFrame();

      Image img = parent.getIconImage();

      if (img != null)
      {
         helpFrame.setIconImage(img);
      }

      helpFrame.setLocationRelativeTo(parent);
   }

   /**
    * Creates a menu item and tool bar button to open manual.
    * @param helpM the menu to add the item to
    * @param toolBar the component to add the button to
    */
   public JDRButtonItem createHelpButtonItem(JMenu helpM, JComponent toolBar)
   {
      IconSet icSet = getButtonStyle().getIconSet(this, 
        helpLib.getIconPrefix("menu.help.manual", "help"));

      TJHAbstractAction helpAction = helpLib.createHelpManualAction(icSet, null);

      return new JDRButtonItem(this, helpAction, toolBar, helpM);
   }

   /**
    * Creates a menu item to open manual.
    * @param helpM menu to add the new menu item to
    * @return the new menu item
    */
   public JMenuItem addHelpItem(JMenu helpM)
   {
      TJHAbstractAction helpAction = helpLib.createHelpManualAction("help");

      JMenuItem helpItem = new JMenuItem(helpAction);
      helpItem.setText(helpAction.getDefaultName());
      helpM.add(helpItem);

      return helpItem;
   }

   /**
    * Creates an action to open a secondary help window at the
    * identified identified node.
    * @param id node identified (corresponds to \label argument)
    */
   public TJHAbstractAction createHelpAction(String id, JComponent comp)
   {
      return helpLib.createHelpAction(id, comp);
   }

   public JButton createHelpButton(String id, JComponent comp)
   {
      return new JButton(helpLib.createHelpAction(id, comp));
   }

   /**
    * Creates an action to open a secondary help window at the
    * identified node for a modal dialog.
    * @param dialog the dialog window
    * @param id node identified (corresponds to \label argument)
    */
   public JButton createHelpDialogButton(JDialog dialog, String id)
   {
      return createHelpDialogButton(dialogButtonStyle, dialog, id);
   }

   public JButton createHelpDialogButton(int style, JDialog dialog, String id)
   {
      JDRButtonStyle buttonStyle;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         buttonStyle = getButtonStyle();
      }
      else
      {
         buttonStyle = DIALOG_BUTTON_STYLES[style];
      }

      IconSet icSet = buttonStyle.getIconSet(this, 
        helpLib.getIconPrefix("button.help", "help"));

      TJHAbstractAction helpAction = helpLib.createHelpDialogAction(
        dialog, id, icSet, (IconSet)null);

      return createButton(buttonStyle, helpAction);
   }

   public HelpDialogAction createHelpDialogAction(JDialog dialog, String id)
   {
      return createHelpDialogAction(dialogButtonStyle, dialog, id);
   }

   public HelpDialogAction createHelpDialogAction(int style, JDialog dialog, String id)
   {
      JDRButtonStyle buttonStyle;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         buttonStyle = getButtonStyle();
      }
      else
      {
         buttonStyle = DIALOG_BUTTON_STYLES[style];
      }

      IconSet icSet = buttonStyle.getIconSet(this, 
        helpLib.getIconPrefix("button.help", "help"));

      return helpLib.createHelpDialogAction(dialog, id, icSet, (IconSet)null);
   }

   public JButton createButton(TJHAbstractAction action)
   {
      return createButton(getButtonStyle(), action);
   }

   public JButton createButton(JDRButtonStyle buttonStyle, TJHAbstractAction action)
   {
      JButton button = buttonStyle.createButton(this, action);

      KeyStroke ks = (KeyStroke)action.getValue(Action.ACCELERATOR_KEY);
      String actionName = (String)action.getValue(Action.ACTION_COMMAND_KEY);

      if (ks != null && actionName != null)
      {
         // TJHAbstractAction already sets up the mappings on the
         // dialog's root pane but the accelerator doesn't seem to show 
         // up in the tooltip unless the keystroke is also added
         // to the button's input map.

         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(ks, actionName);
      }

      return button;
   }

   /**
    * Creates an action to open a secondary help window at the
    * identified node for a frame that behaves like a modeless
    * dialog.
    * @param dialog the dialog window
    * @param id node identified (corresponds to \label argument)
    */
   public JButton createHelpDialogButton(JFrame dialog, String id)
   {
      return createHelpDialogButton(dialogButtonStyle, dialog, id);
   }

   public JButton createHelpDialogButton(int style, JFrame dialog, String id)
   {
      JDRButtonStyle buttonStyle;

      if (style == DIALOG_BUTTON_AS_GENERAL)
      {
         buttonStyle = getButtonStyle();
      }
      else
      {
         buttonStyle = DIALOG_BUTTON_STYLES[style];
      }

      IconSet icSet = buttonStyle.getIconSet(this, 
        helpLib.getIconPrefix("button.help", "help"));

      TJHAbstractAction helpAction = helpLib.createHelpDialogAction(
        dialog, id, icSet, null);

      JButton button = buttonStyle.createButton(this, helpAction);

      KeyStroke ks = (KeyStroke)helpAction.getValue(Action.ACCELERATOR_KEY);
      String actionName = (String)helpAction.getValue(Action.ACTION_COMMAND_KEY);

      if (ks != null && actionName != null)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
          .put(ks, actionName);
      }

      return button;
   }

   /**
    * Creates the license dialog (if not already created) 
    * and a menu item that may be used to open it.
    * @param parent parent frame
    * @param menu the menu to which the item should be added (may be
    * null if not required)
    */ 
   public JMenuItem createLicenceItem(JFrame parent, JMenu menu)
   {
      if (licenseDialog == null)
      {
         try
         {
            URL url = getClass().getResource(LICENSE_PATH);

            if (url == null)
            {
               throw new FileNotFoundException(helpLib.getMessage(
                 "error.resource_not_found", LICENSE_PATH));
            }

            licenseDialog = new MessageDialog(parent,
              helpLib.getMessage("license.title"), true,
              helpLib, url);

            if (parent != null)
            {
               licenseDialog.setSize(parent.getSize());
            }
         }
         catch (IOException e)
         {
            debug(e);
         }
      }

      JMenuItem item = helpLib.createJMenuItem("menu.help", "license", 
       new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent evt)
           {
              if (licenseDialog == null)
              {
                 error("Failed to open resource " + LICENSE_PATH);
              }
              else
              {
                 licenseDialog.setVisible(true);
              }
           }
        });


      if (menu != null)
      {
         menu.add(item);
      }

      return item;
   }

   /**
    * Creates the about dialog (if not already created) 
    * and a menu item that may be used to open it.
    * @param parent the parent frame
    * @param menu the menu to which the item should be added (may be
    * null if not required)
    */ 
   public JMenuItem createAboutItem(JFrame parent, JMenu menu)
   {
      if (aboutDialog == null)
      {
         aboutDialog = new MessageDialog(parent,
          helpLib.getMessage("about.title", getApplicationName()),
          true, helpLib, getAppInfo(true));
      }

      JMenuItem item = helpLib.createJMenuItem("menu.help", "about", 
       new ActionListener()
        {
           @Override
           public void actionPerformed(ActionEvent evt)
           {
              aboutDialog.setVisible(true);
           }
        });


      if (menu != null)
      {
         menu.add(item);
      }

      return item;
   }

   /**
    * Gets formatted information about the application for the about
    * dialog or to print to STDOUT.
    * @param html true if HTML output required otherwise returns
    * plain text
    */
   public String getAppInfo(boolean html)
   {
      return helpLib.getAboutInfo(html, APP_VERSION, APP_DATE,
       String.format(
        "Copyright (C) %s-%s Nicola L. C. Talbot (%s)",
        START_COPYRIGHT_YEAR, COPYRIGHT_YEAR,
        helpLib.getInfoUrl(html, "www.dickimaw-books.com")),
        TeXJavaHelpLib.LICENSE_GPL3,
        false, null
      );
   }

   /**
    * Formats URL for about information.
    * @param html true if HTML output required otherwise returns
    * plain text
    * @param url the URL (leading https:// may be omitted)
    */
   public String getInfoUrl(boolean html, String url)
   {
      return helpLib.getInfoUrl(html, url);
   }


   @Deprecated
   public void enableHelpOnButton(AbstractButton button, String id)
   {
      enableHelpOnButton(button, id, getAccelerator("label.contexthelp"));
   }

   @Deprecated
   public void enableHelpOnButton(AbstractButton button, String id,
     KeyStroke keyStroke)
   {
      System.err.println("JavaHelp no longer supported");
   }

   @Deprecated
   public JMenuItem addHelpItem(JMenu helpM, String appName)
   {
      return addHelpItem(helpM);
   }

   @Deprecated
   public JDRButton createMainHelpButton()
   {
      return createMainHelpButton(getString("button.help"));
   }

   @Deprecated
   public JDRButton createMainHelpButton(String tooltipText)
   {
      return new JDRButton(helpLib.createHelpManualAction("help"));
   }

   @Deprecated
   public JDRButton createHelpButton(String id)
   {
      return createHelpButton(id, getString("button.help"));
   }

   @Deprecated
   public JDRButton createHelpButton(String id, String tooltipText)
   {
      JDRButton helpButton = createDialogButton("button.help",
      "help", null, null, tooltipText);

      enableHelpOnButton(helpButton, id);

      return helpButton;
   }

   public JButton createSmallHelpButton(JRootPane rootPane, Action action)
   {
      return createSmallHelpButton(false, rootPane, action);
   }

   public JButton createSmallHelpButton(boolean setMax, JRootPane rootPane, Action action)
   {
      Icon ic = appIcon("statushelp.png");

      JButton button = new JButton(action);

      button.setIcon(ic);
      button.setMargin(new Insets(0,0,0,0));
      button.setContentAreaFilled(false);

      if (setMax)
      {
         Dimension dim = new Dimension(ic.getIconWidth(), ic.getIconHeight());
         button.setPreferredSize(dim);
         button.setMaximumSize(dim);
      }

      if (rootPane != null)
      {
         String actionName = "contexthelp";
         KeyStroke keyStroke = getAccelerator("info.help");

         if (keyStroke != null)
         {
            rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
                   .put(keyStroke, actionName);
            rootPane.getActionMap().put(actionName, action);
         }
      }

      return button;
   }

   public JDRButton createDialogButton(String tag,
     ActionListener listener, KeyStroke keyStroke)
   {
      return createDialogButton(tag, tag, listener,
       keyStroke, getToolTipText(tag));
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
      String buttonText;

      if (tag.equals("button"))
      {
         tag += "."+actionName;
         buttonText = getMessage(tag);
      }
      else
      {
         buttonText = getMessageIfExists("button."+actionName);

         if (buttonText == null)
         {
            buttonText = getMessageIfExists("label."+actionName);
         }

         if (buttonText == null)
         {
            if (buttonText == null)
            {
               buttonText = getMessageIfExists(tag+"."+actionName);

               if (buttonText != null)
               {
                  tag += "."+actionName;
               }
            }
         }
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = helpLib.getIconPrefix(tag, null);

      if (base == null)
      {
         base = mapIconBaseName(actionName);
      }

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
       null, getToolTipText(tag));
   }

   public JDRToggleButton createDialogToggle(String tag,
     String actionName, ActionListener listener, boolean selected)
   {
      return createDialogToggle(getDialogButtonStyle(), 
       tag, actionName, listener,
       null, getToolTipText(tag), selected);
   }

   public JDRToggleButton createDialogToggle(String tag,
     String actionName, ActionListener listener,
     KeyStroke keyStroke)
   {
      return createDialogToggle(getDialogButtonStyle(), 
       tag, actionName, listener,
       keyStroke, getToolTipText(tag));
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
      String buttonText = getMessageIfExists("label."+actionName);

      if (buttonText == null)
      {
         buttonText = getMessageIfExists("button."+actionName);
      }

      if (buttonText == null)
      {
         buttonText = getMessageIfExists(tag);
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = helpLib.getIconPrefix(tag, null);

      if (base == null)
      {
         base = mapIconBaseName(actionName);
      }

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
       null, getToolTipText(tag));
   }

   public JDRToolButton createDialogRadio(String tag,
     String actionName, ActionListener listener,
     ButtonGroup bg, boolean selected, KeyStroke keyStroke)
   {
      return createDialogRadio(getDialogButtonStyle(), 
       tag, actionName, listener, bg, selected,
       keyStroke, getToolTipText(tag));
   }

   public JDRToolButton createDialogRadio(int style,
     String tag, String actionName,
     ActionListener listener, ButtonGroup bg, boolean selected,
     KeyStroke keyStroke, String tooltipText)
   {
      String buttonText = getMessageIfExists("label."+actionName);

      if (buttonText == null)
      {
         buttonText = getMessageIfExists("button."+actionName);
      }

      if (buttonText == null)
      {
         buttonText = getMessageIfExists(tag);
      }

      int buttonMnemonic = getCodePoint(tag+".mnemonic", 0);
      String base = helpLib.getIconPrefix(tag, null);

      if (base == null)
      {
         base = mapIconBaseName(actionName);
      }

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

   public JDRButton createAppButton(TJHAbstractAction action)
   {
      return getButtonStyle().createButton(this, action);
   }

   public JDRButton createAppButton(String buttonText, String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      return createAppButton(buttonText, name, 
      listener, null, keyStroke, tooltipText);
   }

   public JDRButton createAppButton(String buttonText, String name, 
      ActionListener listener, String base, KeyStroke keyStroke, String tooltipText)
   {
      if (base == null)
      {
         base = mapIconBaseName(name);
      }

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
      String text = getMessage(id);

      return createAppButton(text, name, listener,
         helpLib.getIconPrefix(id, null),
         getAccelerator(id), getMessageWithFallback(id+".tooltip", text));
   }

   public JDRButton createAppButton(String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      String text = getMessageIfExists("button."+name);

      if (text == null)
      {
         text = getMessage("label."+name);
      }

      return createAppButton(text, name, listener,
         keyStroke, tooltipText);
   }

   public JDRToggleButton createToggleButton(String parentId, 
      String name, ActionListener listener)
   {
      String id = parentId+"."+name;
      String text = getMessage(id);

      return createToggleButton(text, name, listener, 
         helpLib.getIconPrefix(id, null),
         getAccelerator(id), getMessageWithFallback(id+".tooltip", text));
   }

   public JDRToggleButton createToggleButton(String buttonText, String name, 
      ActionListener listener, KeyStroke keyStroke, String tooltipText)
   {
      return createToggleButton(buttonText, name, 
      listener, null, keyStroke, tooltipText);
   }

   public JDRToggleButton createToggleButton(String buttonText, String name, 
      ActionListener listener, String base, KeyStroke keyStroke, String tooltipText)
   {
      if (base == null)
      {
         base = mapIconBaseName(name);
      }

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
      return createToolButton(buttonText, name, 
      listener, null, keyStroke,
      g, selected, tooltipText);
   }

   public JDRToolButton createToolButton(
      String buttonText, String name, 
      ActionListener listener, String base, KeyStroke keyStroke,
      ButtonGroup g, boolean selected, String tooltipText)
   {
      if (base == null)
      {
         base = mapIconBaseName(name);
      }

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
      return createOkayButton(listener, getMessage("button.okay"));
   }

   public JDRButton createOkayButton(ActionListener listener, 
     String tooltipText)
   {
      return createOkayButton(listener, tooltipText, true);
   }

   public JDRButton createOkayButton(ActionListener listener, 
     String tooltipText, boolean bothAccelerators)
   {
      JDRButton button = createDialogButton("button.okay", "okay", 
          listener, getAccelerator("button.okay"), tooltipText);

      if (bothAccelerators)
      {
         button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
           put(getAccelerator("button.alt_okay"), "okay");
      }

      return button;
   }

   public static Component createLabelSpacer()
   {
      return Box.createHorizontalStrut(LABEL_SPACER);
   }

   public static Component createButtonSpacer()
   {
      return Box.createHorizontalStrut(BUTTON_SPACER);
   }

   public void clampCompMaxHeight(JComponent comp, int xpad, int ypad)
   {
      Dimension dim = comp.getPreferredSize();
      dim.width = (int)comp.getMaximumSize().getWidth() + xpad;
      dim.height += ypad;
      comp.setMaximumSize(dim);
   }  

   public JDRButton createOkayCancelHelpButtons(JDialog dialog, JComponent comp,
      ActionListener listener, String helpId, boolean bothOkayAccelerators)
   {
      JDRButton okayButton = createOkayButton(listener, getMessage("button.okay"),
        bothOkayAccelerators);

      comp.add(okayButton);
      comp.add(createButtonSpacer());
      comp.add(createCancelButton(listener));

      if (helpId != null)
      {
         comp.add(createButtonSpacer());

         try
         {
            comp.add(createHelpDialogButton(dialog, helpId));
         }
         catch (HelpSetNotInitialisedException e)
         {
            internalError(null, e);
         }
      }

      return okayButton;
   }

   public JDRButton createOkayCancelButtons(JDialog dialog, JComponent comp,
      ActionListener listener)
   {
      return createOkayCancelHelpButtons(dialog, comp, listener, null);
   }

   public JDRButton createOkayCancelHelpButtons(JDialog dialog, JComponent comp,
      ActionListener listener, String helpId)
   {
      JDRButton okayButton = createOkayButton(dialog.getRootPane(), listener);

      comp.add(okayButton);
      comp.add(createButtonSpacer());
      comp.add(createCancelButton(listener));

      if (helpId != null)
      {
         comp.add(createButtonSpacer());

         try
         {
            comp.add(createHelpDialogButton(dialog, helpId));
         }
         catch (HelpSetNotInitialisedException e)
         {
            internalError(null, e);
         }
      }

      return okayButton;
   }

   public JDRButton createOkayButton(JRootPane rootPane, ActionListener listener)
   {
      return createOkayButton(rootPane, listener, getMessage("button.okay"));
   }

   public JDRButton createOkayButton(JRootPane rootPane, ActionListener listener, 
     String tooltipText)
   {
      JDRButton button = createDialogButton("button.okay", "okay", 
       listener, getAccelerator("button.okay"), tooltipText);

      rootPane.setDefaultButton(button);

      button.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).
        put(getAccelerator("button.alt_okay"), "okay");

      return button;
   }

   public JDRButton createCancelButton(ActionListener listener)
   {
      return createCancelButton(listener, getMessage("button.cancel"));
   }

   public JDRButton createCancelButton(ActionListener listener,
      String tooltipText)
   {
      return createDialogButton("button.cancel", "cancel", 
       listener, getAccelerator("button.cancel"), tooltipText);
   }

   public JDRButton createCloseButton(ActionListener listener)
   {
      return createCloseButton(listener, getMessage("button.close"));
   }

   public JDRButton createCloseButton(ActionListener listener, 
      String tooltipText)
   {
      return createDialogButton("button.close", "close", 
       listener, getAccelerator("button.close"), tooltipText);
   }

   public JDRButton createCloseButton(JRootPane rootPane, ActionListener listener)
   {
      return createCloseButton(rootPane, listener, getMessage("button.close"));
   }

   public JDRButton createCloseButton(JRootPane rootPane, ActionListener listener, 
     String tooltipText)
   {
      JDRButton button = createDialogButton("button.close", "close", 
       listener, getAccelerator("button.close"), tooltipText);

      rootPane.setDefaultButton(button);

      return button;
   }

   public JDRButton createDefaultButton(ActionListener listener)
   {
      return createDefaultButton(listener, getToolTipText("button.default"));
   }

   public JDRButton createDefaultButton(ActionListener listener,
      String tooltipText)
   {
      return createDialogButton("button.default", "default", 
       listener, getAccelerator("button.default"), tooltipText);
   }

   public JDRButtonItem createButtonItem(String parentId, String action,
     ActionListener listener, JComponent comp, JComponent menu)
   {
      String menuId = (parentId == null ? action : parentId+"."+action);

      KeyStroke keyStroke = getAccelerator(menuId);

      String tooltip = getMessageIfExists(menuId+".tooltip");

      if (tooltip == null)
      {
         tooltip = getMessageIfExists(parentId+".tooltip");
      }

      JDRButtonItem button = new JDRButtonItem(this, menuId, action,
        keyStroke, listener, tooltip, comp, menu);

      return button;
   }


   public JRadioButton createAppRadioButton(
      String parentId, String action,
      ButtonGroup bg, boolean selected, ActionListener listener)
   {
      String label = (parentId == null ? action : parentId+"."+action);

      JRadioButton button = new JRadioButton(getMessage(label), selected);

      button.setOpaque(false);

      int mnemonic = getMnemonic(label);

      if (mnemonic > 0)
      {
         button.setMnemonic(mnemonic);
      }

      String tooltip = getToolTipText(label);

      if (tooltip != null)
      {
         button.setToolTipText(tooltip);
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
      JRadioButton button = new JRadioButton(getMessage(label), selected);

      button.setOpaque(false);

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

      JCheckBox button = new JCheckBox(getMessage(label), selected);

      button.setOpaque(false);

      int mnemonic = getMnemonic(label);

      if (mnemonic > 0)
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
      JCheckBox button = new JCheckBox(getMessage(label), selected);

      button.setOpaque(false);

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

   public JButton createJButton(String parent, String action, ActionListener listener)
   {
      return helpLib.createJButton(parent, action, listener, true, false);
   }

   public JButton createAppJButton(
      String parentId, String action, ActionListener listener)
   {
      String label = (parentId == null ? action : parentId+"."+action);

      JButton button = new JButton(getMessage(label));

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

   public JLabel createAppLabel(String id)
   {
      JLabel label = new JLabel(getMessage(id));

      int c = getCodePoint(id+".mnemonic", 0);

      if (c != 0)
      {
         label.setDisplayedMnemonic(c);
      }

      String tooltip = getMessageIfExists(id+".tooltip");

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
      JTextField field = new JTextField(getMessage(id));

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
      JTextArea textArea = new JTextArea(getMessage(id));

      textArea.setColumns(cols);
      textArea.setEditable(false);
      textArea.setOpaque(false);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);

      return textArea;
   }

   public JTextArea createAppInfoArea(String id)
   {
      JTextArea textArea = new JTextArea(getMessage(id));

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
      tabbedPane.addTab(getMessage(id), comp);

      int index = tabbedPane.getTabCount()-1;

      int mnemonic = getCodePoint(id+".mnemonic", -1);

      if (mnemonic != -1)
      {
         tabbedPane.setMnemonicAt(index, mnemonic);
      }

      String tooltipText = getToolTipText(id);

      if (tooltipText != null)
      {
         tabbedPane.setToolTipTextAt(index, tooltipText);
      }
   }

   public JMenu createAppMenu(String menuId)
   {
      JMenu menu = new JMenu(getMessage(menuId));

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
         getToolTipText(actionName));
   }

   public JMenuItem createAppMenuItem(String id, 
      String actionName, KeyStroke keyStroke, ActionListener listener,
      String tooltipText)
   {
      JMenuItem item = new JMenuItem(getMessage(id),
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
      JCheckBoxMenuItem item = new JCheckBoxMenuItem(getMessage(id), selected);

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
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(getMessage(id),
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
         panel = new LengthPanel(getMessageSystem(), getMessage(tag));
      }
      else
      {
         panel = new LengthPanel(getMessageSystem(), getMessage(tag), mnemonic);
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
         panel = new LengthPanel(getMessageSystem(), getMessage(tag), numField);
      }
      else
      {
         panel = new LengthPanel(getMessageSystem(), getMessage(tag), mnemonic, numField);
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
         lengthPanel = new LengthPanel(getMessageSystem(), getMessage(tag), samplePanel, numField);
      }
      else
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getMessage(tag), mnemonic, samplePanel, numField);
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
         lengthPanel = new LengthPanel(getMessageSystem(), getMessage(tag), samplePanel);
      }
      else
      {
         lengthPanel = new LengthPanel(getMessageSystem(), getMessage(tag), mnemonic, samplePanel);
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
         panel = new NonNegativeLengthPanel(getMessageSystem(), getMessage(tag));
      }
      else
      {
         panel = new NonNegativeLengthPanel(getMessageSystem(), getMessage(tag), mnemonic);
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
         lengthPanel = new NonNegativeLengthPanel(getMessageSystem(), getMessage(tag), samplePanel);
      }
      else
      {
         lengthPanel = new NonNegativeLengthPanel(getMessageSystem(), getMessage(tag), mnemonic, samplePanel);
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
         anglePanel = new AnglePanel(getMessageSystem(), getMessage(tag), numField);
      }
      else
      {
         anglePanel = new AnglePanel(getMessageSystem(), getMessage(tag), mnemonic, numField);
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
         anglePanel = new AnglePanel(getMessageSystem(), getMessage(tag));
      }
      else
      {
         anglePanel = new AnglePanel(getMessageSystem(), getMessage(tag), mnemonic);
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

      //iconnamemap.setProperty("grid.show", "showgrid");
      //iconnamemap.setProperty("grid.lock", "lockgrid");
      //iconnamemap.setProperty("path.edit", "editPath");
      //iconnamemap.setProperty("select_all", "selectAll");
      //iconnamemap.setProperty("front", "movetofront");
      //iconnamemap.setProperty("back", "movetoback");
      //iconnamemap.setProperty("pattern.set", "pattern");
      //iconnamemap.setProperty("open_line", "openline");
      //iconnamemap.setProperty("closed_line", "closedline");
      //iconnamemap.setProperty("open_curve", "opencurve");
      //iconnamemap.setProperty("closed_curve", "closedcurve");
      //iconnamemap.setProperty("manual", "help");
      //iconnamemap.setProperty("remove.textmap", "remove");
      //iconnamemap.setProperty("add.textmap", "add");
      //iconnamemap.setProperty("remove.mathmap", "remove");
      //iconnamemap.setProperty("add.mathmap", "add");
      iconnamemap.setProperty("remove.unicode_block", "remove");
      iconnamemap.setProperty("add.unicode_block", "add");
      //iconnamemap.setProperty("find_again", "findAgain");
      //iconnamemap.setProperty("replace_all", "replaceAll");
      iconnamemap.setProperty("close", "cancel");
      //iconnamemap.setProperty("textmappings.import", "import");
      //iconnamemap.setProperty("mathmappings.import", "import");
   }

   public String mapIconBaseName(String propName)
   {
      if (iconnamemap == null) return propName;

      return iconnamemap.getProperty(propName, propName);
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return this;
   }

   public JDRMessage getMessageSystem()
   {
      return jdrMessageSystem;
   }

   public void setMessageSystem(JDRMessagePublisher msgSys)
   {
      jdrMessageSystem = msgSys;
   }

   @Deprecated
   public String getDictLocaleId()
   {
      return getDictionaryTag();
   }

   @Deprecated
   public String getHelpLocaleId()
   {
      return getHelpSetTag();
   }

   @Deprecated
   public void setDictLocaleId(String id)
   {
      setDictionary(id);
   }

   @Deprecated
   public void setHelpLocaleId(String id)
   {
      setHelpSet(id);
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

   @Override
   public void message(String message)
   {
      if (jdrMessageSystem == null)
      {
         if (debugMode)
         {
            System.out.println(message);
         }
      }
      else
      {
         jdrMessageSystem.message(message);
      }
   }

   @Override
   public void dictionaryLoaded(URL url)
   {
   }

   public static final String LICENSE_PATH = "/gpl-3.0-standalone.html";

   private String appname="jdrresources";
   private HelpSetLocale dictLocale, helpSetLocale;
   private MessageDialog licenseDialog, aboutDialog;

   private JDRMessagePublisher jdrMessageSystem;

   public HashMap<String,KeyStroke> keyStrokes = null;

   public Properties iconnamemap;

   public boolean debugMode = false;

   private String usersettings = null;

   private Font startupInfoFont = new Font("Serif", Font.PLAIN, 10);
   private Font startupVersionFont = new Font("Serif", Font.BOLD, 20);

   private static DirectoryFilter helpsetDirectoryFilter = new DirectoryFilter();
   private static DictionaryFilter dictionaryFilter = new DictionaryFilter();

   public static final int LABEL_SPACER=5;
   public static final int BUTTON_SPACER=10;

   public static final String APP_VERSION = "0.8.9.20251129";
   public static final String APP_DATE = "2025-11-29";
   public static final String START_COPYRIGHT_YEAR = "2005";
   public static final String COPYRIGHT_YEAR
    = APP_DATE.substring(0,4);

   public static final int EXIT_SYNTAX = 1;

   public static final int EXIT_INTERNAL_ERROR = 2;

   public static final int EXIT_FATAL_ERROR = 3;

   public static final JDRButtonStyle[] BUTTON_STYLES = new JDRButtonStyle[]
   {
      new JDRButtonStyle("default", "bevel"),
      new JDRButtonStyle("small", true, "bevel", true),
      new JDRButtonStyle("plain", "plain", false, true, true, false, false),
      new JDRButtonStyle("highlights", "plain", false, true, true, true, true),
      new JDRButtonStyle("bordered", "bevel", "plain",
          false, true, true, true, true),
      new JDRButtonStyle("smallplain", true, "plain",
          true, true, true, false, false),
      new JDRButtonStyle("smallhighlights", true, "plain",
          true, true, true, true, true),
      new JDRButtonStyle("smallbordered", true, "bevel", "plain",
          true, true, true, true, true),
      new JDRButtonStyle("textsmallplain", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textsmallhighlights", true, "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textsmallbordered", true, "bevel", "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textsmallplainleading", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallhighlightsleading", true, "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallborderedleading", true, "bevel", "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallplaintop", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallhighlightstop", true, "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallborderedtop", true, "bevel", "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallplainbottom", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallhighlightsbottom", true, "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallborderedbottom", true, "bevel", "plain",
          false, true, true, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("text", true, "plain",
          false, false, false, false, false, JDRButtonStyleDisplayType.TEXT_ONLY),
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
      new JDRButtonStyle("default", "bevel"),
      new JDRButtonStyle("text", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.TEXT_ONLY),
      new JDRButtonStyle("textplaintrailing", "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textsmallplaintrailing", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textsmallborderedtrailing", true, "bevel",
          false, false, false, true, true, JDRButtonStyleDisplayType.ICON_TEXT),
      new JDRButtonStyle("textplainleading", "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallplainleading", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textsmallborderedleading", true, "bevel",
          false, false, false, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.LEADING, SwingConstants.CENTER),
      new JDRButtonStyle("textplaintop", "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallplaintop", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textsmallborderedtop", true, "bevel",
          false, false, false, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.TOP),
      new JDRButtonStyle("textplainbottom", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallplainbottom", true, "plain",
          false, true, true, false, false, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
      new JDRButtonStyle("textsmallborderedbottom", true, "bevel",
          false, false, false, true, true, JDRButtonStyleDisplayType.ICON_TEXT,
          SwingConstants.CENTER, SwingConstants.BOTTOM),
   };

}

class DirectoryFilter implements java.io.FilenameFilter
{
   public boolean accept(File dir, String name)
   {
      return !name.contains("image") && (new File(dir, name)).isDirectory();
   }
}

class DictionaryFilter implements java.io.FilenameFilter
{
   public boolean accept(File dir, String name)
   {
      return name.contains("-") && name.endsWith(".xml");
   }
}

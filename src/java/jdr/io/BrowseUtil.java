// File          : BrowseUtil.java
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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

import javax.swing.*;

import com.dickimawbooks.jdr.*;

/**
 * Provides dialogs for {@link JDRBitmap} in the event that a link 
 * to a bitmap can't be found. This supplies two option dialogs,
 * which are identical, except for the title.
 * @see JDRBitmap#setBrowseUtil(BrowseUtil)
 * @version 0.3.0b 28 Jul 2007
 * @author Nicola L C Talbot
 */
public class BrowseUtil
{
   /**
    * Constructs utility using default labels.
    */
   public BrowseUtil()
   {
      this("Browse", "File Not found",
           "Unrecognised image format", "Can't Refresh", "Browse",
           "Invalid Bitmap", "Cancel");
   }
   /**
    * Constructs utility.
    * @param browseLabel the label to use for the browse button
    * in {@link #getCantRefreshDialog(String)}
    * @param notFoundLabel an informative message displayed below
    * the name of the file which can't be found (for example,
    * "File Not Found")
    * @param cantRefreshTitle the title for the dialog produced
    * by {@link #getCantRefreshDialog(String)}
    * @param invalidTitle the title for the dialog produced by
    * {@link #getInvalidFormatDialog(String)}
    * @param invalidLabel message indicating that the image has an unrecognised or invalid format
    * @param browseTitle the title for the dialog produced by
    * {@link #getBrowseDialog}
    * @param cancelLabel the label for the cancel button
    */
   public BrowseUtil(String browseLabel, String notFoundLabel,
      String invalidLabel, String cantRefreshTitle,
      String browseTitle, String invalidTitle, String cancelLabel)
   {
      browseLabel_ = browseLabel;
      notFoundLabel_ = notFoundLabel;
      cantRefreshTitle_ = cantRefreshTitle;
      invalidLabel_ = invalidLabel;
      browseTitle_ = browseTitle;
      invalidTitle_ = invalidTitle;
      cancelLabel_ = cancelLabel;
   }

   /**
    * Displays an option dialog to indicate that a bitmap 
    * can't be refreshed. The user is given two choices: browse
    * for a new file, or cancel.
    * @param filename the name of the file which can't be found
    * @return either <code>JOptionPane.YES_OPTION</code> 
    * (browse for a new file) or 
    * <code>JOptionPane.NO_OPTION</code> (cancel)
    */
   public int getCantRefreshDialog(String filename)
   {
      return JOptionPane.showOptionDialog(
                   null,
                   new String[]{filename, notFoundLabel_},
                   cantRefreshTitle_,
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.QUESTION_MESSAGE, null,
                   new String[]{browseLabel_, cancelLabel_},
                   browseLabel_); 
   }

   /**
    * Displays an option dialog to indicate that a bitmap 
    * can't be found. The user is given two choices: browse
    * for a new file, or cancel.
    * @param filename the name of the file which can't be found
    * @return either <code>JOptionPane.YES_OPTION</code> 
    * (browse for a new file) or 
    * <code>JOptionPane.NO_OPTION</code> (cancel)
    */
   public int getBrowseDialog(String filename)
   {
      return JOptionPane.showOptionDialog(
                   null,
                   new String[]{filename, notFoundLabel_},
                   browseTitle_,
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.QUESTION_MESSAGE, null,
                   new String[]{browseLabel_, cancelLabel_},
                   browseLabel_); 
   }

   /**
    * Displays an option dialog to indicate that a bitmap 
    * has an invalid or unrecognised file format. 
    * The user is given two choices: browse
    * for a new file, or cancel.
    * @param filename the name of the file which can't be found
    * @return either <code>JOptionPane.YES_OPTION</code> 
    * (browse for a new file) or 
    * <code>JOptionPane.NO_OPTION</code> (cancel)
    */
   public int getInvalidFormatDialog(String filename)
   {
      return JOptionPane.showOptionDialog(
                   null,
                   new String[]{filename, invalidLabel_},
                   invalidTitle_,
                   JOptionPane.YES_NO_OPTION,
                   JOptionPane.QUESTION_MESSAGE, null,
                   new String[]{browseLabel_, cancelLabel_},
                   browseLabel_); 
   }

   private String browseLabel_;
   private String cantRefreshTitle_;
   private String browseTitle_;
   private String notFoundLabel_;
   private String cancelLabel_;
   private String invalidLabel_;
   private String invalidTitle_;
}


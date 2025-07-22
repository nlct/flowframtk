// File          : LoadAction.java
// Description   : Action for loading JDR/AJR files
// Creation Date : 1st February 2006
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

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
package com.dickimawbooks.flowframtk;

import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Action for loading JDR/AJR files.
 * @author Nicola L C Talbot
 */
public class LoadAction extends AbstractAction
{
   public LoadAction(FlowframTk application, File file, 
      Vector<File> recentFiles)
   {
      this.application_ = application;
      this.file = file;
      this.list = recentFiles;
   }

   public void actionPerformed(ActionEvent evt)
   {
      JDRFrame frame = application_.getCurrentFrame();

      if (!file.exists())
      {
         getResources().error(application_, 
                       getResources().getMessage("error.io.not_exists",
                       file.toString()));

         list.remove(file);
         application_.setRecentFiles();

         return;
      }

      if (frame == null || !frame.isNewImage())
      {
         application_.addFrame(file);
      }
      else
      {
         frame.load(file);
      }
   }

   public JDRResources getResources()
   {
      return application_.getResources();
   }

   private FlowframTk application_;
   private File file;
   private Vector<File> list;
}

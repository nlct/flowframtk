// File          : JDRFileChooser.java
// Purpose       : File chooser
// Date          : 4th June 2008
// Last Modified : 4th June 2008
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

import java.io.File;
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.basic.*;
import java.beans.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.filter.*;

/**
 * File chooser modified from the workaround described in
   <a href="http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4678049">Java Bug ID 4678049</a>.
*/
public class JDRFileChooser extends JFileChooser
  implements PropertyChangeListener
{
   public JDRFileChooser(JDRResources resources)
   {
      this(resources, true);
   }

   public JDRFileChooser(JDRResources resources, boolean useExtBox)
   {
      super();

      addExtBox = resources.createAppCheckBox("fc", "append_ext", 
         useExtBox, null);
      setAccessory(addExtBox);

      addExtBox.setEnabled(useExtBox);
      addExtBox.setVisible(useExtBox);

      addPropertyChangeListener(this);
   }

   public File getSelectedFile()
   {
      File file = super.getSelectedFile();

      if (addExtBox == null || !addExtBox.isSelected())
      {
         return file;
      }

      if (file != null)
      {
         FileFilter filter = getFileFilter();

         if (filter instanceof JDRFileFilterInterface)
         {
            if (!filter.accept(file))
            {
               return new File(file.getParentFile(), 
                 file.getName()+"."
                 +((JDRFileFilterInterface)filter).getDefaultExtension());
            }
         }
      }

      return file;
   }

   private JTextComponent getTextComponent(Container container)
   {
      if (container instanceof JTextComponent
       && ((JTextComponent)container).isEditable())
      {
         JTextComponent textComp = (JTextComponent)container;

         // Some UI's may have a text component for the current
         // directory, so check this isn't it.

         File dir = getCurrentDirectory();
         String text = textComp.getText();

         if (dir != null && dir.getName().equals(text))
         {
            return null;
         }

         File file = new File(dir, text);

         if (!file.isDirectory())
         {
            return textComp;
         }
      }

      int n = container.getComponentCount();

      if (n == 0)
      {
         return null;
      }

      for (int i = 0; i < n; i++)
      {
         Component comp = container.getComponent(i);

         if (!(comp instanceof Container)
           || comp instanceof JList
           || comp instanceof JTable
           || comp instanceof CellRendererPane)
         {
            continue;
         }

         JTextComponent textComp = getTextComponent((Container)comp);

         if (textComp != null)
         {
            return textComp;
         }
      }

      return null;
   }

   /*
    * getSelectedFile() only returns the file the user has selected
    * from the list. There doesn't seem to be a convenient way of 
    * finding out what the user has typed into the box. This is a bit
    * of a fudge to find it.
    */
   public String getFileName()
   {
      JTextComponent textComp = getTextComponent(this);

      if (textComp == null)
      {
         return null;
      }

      return textComp.getText();
   }

   public void propertyChange(PropertyChangeEvent evt)
   {
       if (JFileChooser.FILE_FILTER_CHANGED_PROPERTY.equals(
          evt.getPropertyName()))
       {
          Object value = evt.getNewValue();

          if (value instanceof JDRFileFilterInterface)
          {
             JDRFileFilterInterface filter = (JDRFileFilterInterface)value;

             String name = getFileName();

             if (name == null)
             {
                return;
             }

             int idx = name.lastIndexOf(".");

             if (idx > -1 && 
                !filter.accept(new File(getCurrentDirectory(),name)))
             {
                name = name.substring(0, idx)
                     + "." + filter.getDefaultExtension();

                setSelectedFile(new File(name));
             }
          }
       }
   }

   /**
    * Sets the file filter for this file chooser.
    * @param filter the filter to apply to this file chooser
    */
   public void setFileFilter(FileFilter filter)
   {
      super.setFileFilter(filter);

      if (!(getUI() instanceof BasicFileChooserUI))
      {
         return;
      }

      final BasicFileChooserUI ui = (BasicFileChooserUI) getUI();
      final String name = ui.getFileName().trim();

      if ((name == null) || (name.length() == 0))
      {
         return;
      }
	
      EventQueue.invokeLater(new Thread()
      {
         public void run()
         {
            String currentName = ui.getFileName();

            if ((currentName == null) || (currentName.length() == 0))
            {
               FileFilter filter = getFileFilter();

               if (filter.accept(new File(name)))
               {
                  ui.setFileName(name);
               }
               else
               {
                  String base = name;

                  if (filter instanceof JDRFileFilterInterface)
                  {
                     base = base + "."
                          + ((JDRFileFilterInterface)filter)
                              .getDefaultExtension();
                  }

                  ui.setFileName(base);
               }
            }
         }
      });
   }

   private JCheckBox addExtBox;
}

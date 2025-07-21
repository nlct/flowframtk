// File          : ImagePreview.java
// Purpose       : Image preview component
// Date          : 4th June 2008
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
import java.beans.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;

import javax.swing.*;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.filter.*;

/**
 * Image preview component.
 This code is modified from 
<a href="http://java.sun.com/docs/books/tutorial/uiswing/examples/components/FileChooserDemo2Project/src/components/ImagePreview.java">Sun's example</a>
(Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved)
*/
public class ImagePreview extends JComponent
   implements PropertyChangeListener,ActionListener
{
   public ImagePreview(JDRResources resources, JFileChooser fc)
   {
      super();

      setLayout(new BorderLayout());

      imagePanel = new JPanel()
      {
         public void paintComponent(Graphics g)
         {
            super.paintComponent(g);

            if (thumbnail == null)
            {
               loadImage();
            }

            if (thumbnail != null)
            {
               int x = getWidth()/2 - thumbnail.getIconWidth()/2;
               int y = getHeight()/2 - thumbnail.getIconHeight()/2;

               if (y < 0)
               {
                  y = 0;
               }

               if (x < 5)
               {
                  x = 5;
               }

               thumbnail.paintIcon(this, g, x, y);
            }
         }
      };

      add(imagePanel, "Center");

      previewBox = new JCheckBox(
         resources.getMessage("image.preview"));
      previewBox.setMnemonic(
         resources.getCodePoint("image.preview.mnemonic"));
      previewBox.addActionListener(this);

      add(previewBox, "South");

      setPreferredSize(new Dimension(100,50));
      fc.addPropertyChangeListener(this);
   }

   public void loadImage()
   {
      if (file == null || !previewBox.isSelected())
      {
         thumbnail = null;
         return;
      }

      ImageIcon tmpIcon = new ImageIcon(file.getPath());

      if (tmpIcon != null)
      {
         if (tmpIcon.getIconWidth() > 90)
         {
            thumbnail = new ImageIcon(tmpIcon.getImage().
                                      getScaledInstance(90, -1,
                                         Image.SCALE_DEFAULT));
         }
         else
         {
            thumbnail = tmpIcon;
         }
      }
   }

   public void propertyChange(PropertyChangeEvent e)
   {
      boolean update = false;
      String prop = e.getPropertyName();

      if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop))
      {
         file = null;
         update = true;
      }
      else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop))
      {
         file = (File)e.getNewValue();
         update = true;
      }

      if (update)
      {
         thumbnail = null;

         if (imagePanel.isShowing())
         {
            loadImage();
            imagePanel.repaint();
         }
      }
   }

   public void actionPerformed(ActionEvent evt)
   {
      if (evt.getSource() == previewBox)
      {
         loadImage();
         imagePanel.repaint();
      }
   }

   public void addPreviewBoxActionListener(ActionListener list)
   {
      previewBox.addActionListener(list);
   }

   public boolean isPreviewSelected()
   {
      return previewBox.isSelected();
   }

   public void setPreviewSelected(boolean selected)
   {
      previewBox.setSelected(selected);
   }

   private JCheckBox previewBox;
   private ImageIcon thumbnail = null;
   private File file = null;
   private JPanel imagePanel;
}


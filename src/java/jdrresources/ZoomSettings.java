// File          : ZoomSettings.java
// Description   : Provides magnification dialog
// Date          : 1st February 2006
// Last Modified : 9th June 2008
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
package com.dickimawbooks.jdrresources;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.BasicComboBoxEditor;

import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Magnification dialog box.
 * @author Nicola L C Talbot
 */

public class ZoomSettings extends JDialog
   implements ActionListener
{
   public ZoomSettings(JDRApp application, Frame parent)
   {
      super(parent, application.getResources().getString("zoom.title"),true);
      this.application = application;

      JPanel p1 = new JPanel();

      JLabel label = new JLabel(
         application.getResources().getString("zoom.magnification"));
      label.setDisplayedMnemonic(
         application.getResources().getChar("zoom.magnification.mnemonic"));

      p1.add(label);

      values = createZoomArray();

      magBox = new JComboBox<ZoomValue>(values);
      magBox.setEditable(true);

      magBox.setEditor(new BasicComboBoxEditor()
      {
         public Object getItem()
         {
            String item = super.getItem().toString();

            for (int i = 0; i < values.length; i++)
            {
               if (item.equals(values[i].toString()))
               {
                  return values[i];
               }
            }

            try
            {
               return PercentageZoomValue.parse(item);
            }
            catch (NumberFormatException e)
            {
               return new InvalidZoomValue(item,
                  getResources().getStringWithValue("zoom.invalid", item));
            }
         }
      });

      setMag(1.0);

      label.setLabelFor(magBox);

      p1.add(magBox);

      getContentPane().add(p1, "Center");

      JPanel p2 = new JPanel();

      p2.add(application.getResources().createOkayButton(this));
      p2.add(application.getResources().createCancelButton(this));
      p2.add(application.getResources().createHelpButton("zoommenu"));

      getContentPane().add(p2, "South");

      addWindowListener(new WindowAdapter()
         {
            public void windowActivated(WindowEvent e)
            {
               magBox.requestFocusInWindow();
            }
         });

      pack();
      setLocationRelativeTo(parent);
   }

   public ZoomValue[] createZoomArray()
   {
      return createZoomArray(application.getResources());
   }

   public static ZoomValue[] createZoomArray(JDRResources resources)
   {
      ZoomValue[] array = new ZoomValue[RELATIVE_ZOOM_CHOICE.length+
        ZOOM_CHOICE.length];

      for (int i = 0; i < RELATIVE_ZOOM_CHOICE.length; i++)
      {
         array[i] = new RelativeZoomValue(RELATIVE_ZOOM_CHOICE[i],
          resources.getString("settings."+RELATIVE_ZOOM_CHOICE[i]));
      }

      for (int i = 0; i < ZOOM_CHOICE.length; i++)
      {
         array[RELATIVE_ZOOM_CHOICE.length+i] = new PercentageZoomValue(
           ZOOM_CHOICE[i]);
      }

      return array;
   }

   public void display()
   {
      setVisible(true);
   }

   public void okay()
   {
      ZoomValue obj = (ZoomValue)magBox.getSelectedItem();

      if (obj instanceof InvalidZoomValue)
      {
         application.getResources().error(this, ((InvalidZoomValue)obj).getMessage());
         return;
      }

      try
      {
         double val = application.zoomAction(obj);

         if (val > 0)
         {
            currentMagnification = val;
         }

         setVisible(false);
      }
      catch (IllegalArgumentException e)
      {
         application.getResources().error(this, e.getMessage());
      }
   }

   public void setMag(double mag)
   {
      currentMagnification = mag;

      for (int i = 0; i < magBox.getItemCount(); i++)
      {
         Object obj = magBox.getItemAt(i);

         if (obj instanceof PercentageZoomValue
          &&((PercentageZoomValue)obj).getValue() == mag)
         {
            magBox.setSelectedIndex(i);
            return;
         }
      }

      magBox.setSelectedItem(new PercentageZoomValue(mag));
   }

   public void setPercentageValue(PercentageZoomValue value)
   {
      magBox.setSelectedItem(value);
      currentMagnification = value.getValue();
   }

   public void setRelativeValue(String tag, double mag)
   {
      currentMagnification = mag;

      for (int i = 0; i < magBox.getItemCount(); i++)
      {
         Object obj = magBox.getItemAt(i);

         if (obj instanceof RelativeZoomValue
          &&((RelativeZoomValue)obj).getActionCommand().equals(tag))
         {
            magBox.setSelectedIndex(i);
            return;
         }
      }

      magBox.setSelectedItem(new PercentageZoomValue(mag));
   }

   public double getMag()
   {
      return currentMagnification;
   }

   public void actionPerformed(ActionEvent e)
   {
      String action = e.getActionCommand();

      if (action == null) return;

      if (action.equals("okay"))
      {
         okay();
      } 
      else if (action.equals("cancel"))
      {
         setVisible(false);
      }
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   private JDRApp application;
   private JComboBox<ZoomValue> magBox;

   private ZoomValue[] values;

   private double currentMagnification = 1.0;

   public static final String[] RELATIVE_ZOOM_CHOICE = new String[]
   {
      ZoomValue.ZOOM_PAGE_HEIGHT_ID,
      ZoomValue.ZOOM_PAGE_WIDTH_ID,
      ZoomValue.ZOOM_PAGE_ID
   };

   public static final double[] ZOOM_CHOICE = new double[]
   {
      0.25, 0.33, 0.5, 0.66, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 6.0, 8.0
   };
}

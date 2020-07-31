// File          : ZoomComponent.java
// Description   : Provides magnification component
// Date          : 31st July 2020
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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

import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Magnification Component.
 * @author Nicola L C Talbot
 */

public class ZoomComponent extends JPanel implements ActionListener
{
   public ZoomComponent(final JDRApp applicationListener)
   {
      this(applicationListener, null, 0, true);
   }

   public ZoomComponent(final JDRApp applicationListener, Font zoomFont, int height)
   {
      this(applicationListener, zoomFont, height, true);
   }

   public ZoomComponent(final JDRApp applicationListener, Font zoomFont, 
      int height, boolean convertRelativeSizes)
   {
      super(new BorderLayout());

      setOpaque(false);

      this.applicationListener = applicationListener;
      JDRResources resources = applicationListener.getResources();
      this.convertRelativeSizes = convertRelativeSizes;

      zoomArray = ZoomSettings.createZoomArray(resources);

      updateZoomFieldFontSize(zoomFont, height);

      if (height <= 0)
      {
         height = zoomFieldDim.height;
      }

      Icon zoomDownIcon = resources.appIcon("zoomdown.png");
      zoomControlDim = new Dimension(zoomDownIcon.getIconWidth(), height);

      zoomDown = new JButton(zoomDownIcon)
      {
         public Dimension getMinimumSize()
         {
            return zoomControlDim;
         }

         public Dimension getPreferredSize()
         {
            return zoomControlDim;
         }
      };

      zoomDown.setMargin(new Insets(0,0,0,0));
      zoomDown.setContentAreaFilled(false);
      zoomDown.setBorderPainted(false);
      zoomDown.setOpaque(false);
      zoomDown.addActionListener(this);
      add(zoomDown, "West");

      currentValue = getZoomValue(1.0);
      zoomField = new JLabel(currentValue.toString(), 
         SwingConstants.TRAILING)
      {
         public Dimension getMinimumSize()
         {
            return zoomFieldDim;
         }

         public Dimension getPreferredSize()
         {
            return zoomFieldDim;
         }
      };

      if (zoomFont != null)
      {
         zoomField.setFont(zoomFont);
      }

      zoomField.setOpaque(false);

      add(zoomField, "Center");

      zoomPopup = new JPopupMenu();

      for (int i = 0; i < zoomArray.length; i++)
      {
         JMenuItem item = new JMenuItem(zoomArray[i].toString());
         item.setActionCommand(item.getText());
         item.addActionListener(this);
         zoomPopup.add(item);
      }

      zoomField.addMouseListener(new MouseAdapter()
      {
         public void mousePressed(MouseEvent evt)
         {
            if (evt.isPopupTrigger())
            {
               zoomPopup.show(zoomField, evt.getX(), evt.getY());
            }
            else if (evt.getClickCount() > 1)
            {
               applicationListener.showZoomChooser();
            }
         }

         public void mouseReleased(MouseEvent evt)
         {
            if (evt.isPopupTrigger())
            {
               zoomPopup.show(zoomField, evt.getX(), evt.getY());
            }
            else if (evt.getClickCount() > 1)
            {
               applicationListener.showZoomChooser();
            }
         }
      });

      zoomUp = new JButton(resources.appIcon("zoomup.png"))
      {
         public Dimension getMinimumSize()
         {
            return zoomControlDim;
         }

         public Dimension getPreferredSize()
         {
            return zoomControlDim;
         }
      };

      zoomUp.setMargin(new Insets(0,0,0,0));
      zoomUp.setContentAreaFilled(false);
      zoomUp.setBorderPainted(false);
      zoomUp.setOpaque(false);
      zoomUp.addActionListener(this);
      add(zoomUp, "East");
   }

   public void setAlignmentY(float align)
   {
      super.setAlignmentY(align);
      zoomUp.setAlignmentY(align);
      zoomField.setAlignmentY(align);
      zoomDown.setAlignmentY(align);
   }

   public void setZoomFieldFont(Font f)
   {
      setZoomFieldFont(f, 0);
   }

   public void setZoomFieldFont(Font zoomFont, int height)
   {
      zoomField.setFont(zoomFont);
      updateZoomFieldFontSize(zoomFont, height);
      revalidate();
   }

   private void updateZoomFieldFontSize(Font zoomFont, int height)
   {
      JLabel tmp = new JLabel("");

      if (zoomFont != null)
      {
         tmp.setFont(zoomFont);
      }
      else if (zoomField != null)
      {
         tmp.setFont(zoomField.getFont());
      }

      zoomFieldDim = null;

      if (convertRelativeSizes)
      {
         for (int i = 0; i < ZoomSettings.ZOOM_CHOICE.length; i++)
         {
            tmp.setText(PercentageZoomValue.FORMAT.format(
               ZoomSettings.ZOOM_CHOICE[i]));

            Dimension dim = tmp.getPreferredSize();

            if (zoomFieldDim == null)
            {
               zoomFieldDim = dim;
            }
            else if (zoomFieldDim.width < dim.width)
            {
               zoomFieldDim.width = dim.width;
            }

            if (zoomFieldDim != null && zoomFieldDim.height < dim.height)
            {
               zoomFieldDim.height = dim.height;
            }
         }
      }
      else
      {
         for (int i = 0; i < zoomArray.length; i++)
         {
            tmp.setText(zoomArray[i].toString());

            Dimension dim = tmp.getPreferredSize();

            if (zoomFieldDim == null)
            {
               zoomFieldDim = dim;
            }
            else if (zoomFieldDim.width < dim.width)
            {
               zoomFieldDim.width = dim.width;
            }

            if (zoomFieldDim != null && zoomFieldDim.height < dim.height)
            {
               zoomFieldDim.height = dim.height;
            }
         }
      }

      if (height > 0)
      {
         zoomFieldDim.height = height;
      }
      else
      {
         height = zoomFieldDim.height;
      }

      if (zoomControlDim != null)
      {
         zoomControlDim.height = height;
      }
   }

   public Font getZoomFieldFont()
   {
      return zoomField.getFont();
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);
      zoomDown.setEnabled(enable);
      zoomField.setEnabled(enable);
      zoomUp.setEnabled(enable);
   }

   public void setZoom(double factor)
   {
      currentValue = getZoomValue(factor);
      zoomField.setText(currentValue.toString());
   }

   public void setZoom(ZoomValue value)
   {
      currentValue = value;
      zoomField.setText(currentValue.toString());
   }

   public void setZoom(ZoomValue value, double factor)
   {
      if (convertRelativeSizes && value instanceof RelativeZoomValue)
      {
         currentValue = new PercentageZoomValue(factor);
      }
      else
      {
         currentValue = value;
      }

      zoomField.setText(currentValue.toString());
   }

   public ZoomValue getZoomValue()
   {
      return currentValue;
   }

   private ZoomValue getZoomValue(double factor)
   {
      for (ZoomValue value : zoomArray)
      {
         if (value instanceof PercentageZoomValue 
              && ((PercentageZoomValue)value).getValue() == factor)
         {
            return value;
         }
      }

      return new PercentageZoomValue(factor);
   }

   public Dimension getZoomControlSize()
   {
      return zoomControlDim;
   }

   public Dimension getZoomFieldSize()
   {
      return zoomFieldDim;
   }

   public void actionPerformed(ActionEvent evt)
   {
      Object source = evt.getSource();

      if (source == zoomDown)
      {
         zoomDown();
      }
      else if (source == zoomUp)
      {
         zoomUp();
      }
      else if (source instanceof JMenuItem)
      {
         String action = evt.getActionCommand();

         for (int i = 0; i < zoomArray.length; i++)
         {
            if (zoomArray[i].toString().equals(action))
            {
               setZoom(zoomArray[i]);
               applicationListener.zoomAction(zoomArray[i]);
               return;
            }
         }
      }
   }

   public void zoomDown()
   {
      double current = applicationListener.getCurrentMagnification();

      for (int i = ZoomSettings.ZOOM_CHOICE.length-1; i >= 0; i--)
      {
         if (current > ZoomSettings.ZOOM_CHOICE[i])
         {
            setZoom(ZoomSettings.ZOOM_CHOICE[i]);
            applicationListener.setCurrentMagnification(ZoomSettings.ZOOM_CHOICE[i]);
            return;
         }
      }
   }

   public void zoomUp()
   {
      double current = applicationListener.getCurrentMagnification();

      for (int i = 0; i < ZoomSettings.ZOOM_CHOICE.length; i++)
      {
         if (current < ZoomSettings.ZOOM_CHOICE[i])
         {
            setZoom(ZoomSettings.ZOOM_CHOICE[i]);
            applicationListener.setCurrentMagnification(ZoomSettings.ZOOM_CHOICE[i]);
            return;
         }
      }
   }

   private JButton zoomDown, zoomUp;
   private JLabel zoomField;

   private ZoomValue[] zoomArray;
   private ZoomValue currentValue;

   private Dimension zoomControlDim, zoomFieldDim;
   private JPopupMenu zoomPopup;

   private boolean convertRelativeSizes;

   private JDRApp applicationListener;
}

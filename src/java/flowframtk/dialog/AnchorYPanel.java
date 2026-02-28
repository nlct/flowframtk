/*
    Copyright (C) 2026 Nicola L.C. Talbot

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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

import com.dickimawbooks.flowframtk.FlowframTk;

/**
 * Panel for selecting vertical anchor.
 * @author Nicola L C Talbot
 */

public class AnchorYPanel extends JPanel
{
   public AnchorYPanel(FlowframTk application)
   {
      this(application, false);
   }

   public AnchorYPanel(FlowframTk application, boolean incBase)
   {
      super(null);

      this.application = application;
      JDRResources resources = application.getResources();
      setAlignmentX(0.0f);

      BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
      setLayout(layout);

      JLabel label = resources.createAppLabel("anchor.y");

      add(label);
      add(resources.createLabelSpacer());

      if (incBase)
      {
         anchorYitems = new AnchorYItem[]
          {
            new AnchorYItem(AnchorY.TOP, resources.getMessage("anchor.y.top")),
            new AnchorYItem(AnchorY.MIDDLE, resources.getMessage("anchor.y.middle")),
            new AnchorYItem(AnchorY.BASE, resources.getMessage("anchor.y.base")),
            new AnchorYItem(AnchorY.BOTTOM, resources.getMessage("anchor.y.bottom"))
          };
      }
      else
      {
         anchorYitems = new AnchorYItem[]
          {
            new AnchorYItem(AnchorY.TOP, resources.getMessage("anchor.y.top")),
            new AnchorYItem(AnchorY.MIDDLE, resources.getMessage("anchor.y.middle")),
            new AnchorYItem(AnchorY.BOTTOM, resources.getMessage("anchor.y.bottom"))
          };
      }

      anchorBox = new JComboBox<AnchorYItem>(anchorYitems);

      label.setLabelFor(anchorBox);

      add(anchorBox);
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public void setSelectedAnchor(AnchorY anchory)
   {
      for (int i = 0; i < anchorYitems.length; i++)
      {
         if (anchorYitems[i].getValue() == anchory)
         {
            anchorBox.setSelectedIndex(i);
            break;
         }
      }
   }

   public AnchorY getSelectedAnchor()
   {
      Object item = anchorBox.getSelectedItem();

      if (item == null)
      {
         return null;
      }
      else
      {
         return ((AnchorYItem)item).getValue();
      }
   }

   private JComboBox<AnchorYItem> anchorBox;

   private AnchorYItem[] anchorYitems;

   private FlowframTk application;
}

class AnchorYItem
{
   AnchorYItem(AnchorY anchorY, String name)
   {
      this.anchorY = anchorY;
      this.name = name;
   }

   public String toString()
   {
      return name;
   }

   public AnchorY getValue()
   {
      return anchorY;
   }

   AnchorY anchorY;
   String name;
}

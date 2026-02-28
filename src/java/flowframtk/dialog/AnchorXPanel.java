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
 * Panel for selecting horizontal anchor.
 * @author Nicola L C Talbot
 */

public class AnchorXPanel extends JPanel
{
   public AnchorXPanel(FlowframTk application)
   {
      super(null);

      this.application = application;
      JDRResources resources = application.getResources();
      setAlignmentX(0.0f);

      BoxLayout layout = new BoxLayout(this, BoxLayout.X_AXIS);
      setLayout(layout);

      JLabel label = resources.createAppLabel("anchor.x");

      add(label);
      add(resources.createLabelSpacer());

      anchorXitems = new AnchorXItem[]
       {
         new AnchorXItem(AnchorX.LEFT, resources.getMessage("anchor.x.left")),
         new AnchorXItem(AnchorX.MIDDLE, resources.getMessage("anchor.x.middle")),
         new AnchorXItem(AnchorX.RIGHT, resources.getMessage("anchor.x.right"))
       };

      anchorBox = new JComboBox<AnchorXItem>(anchorXitems);

      label.setLabelFor(anchorBox);

      add(anchorBox);
   }

   public JDRResources getResources()
   {
      return application.getResources();
   }

   public void setSelectedAnchor(AnchorX anchorx)
   {
      for (int i = 0; i < anchorXitems.length; i++)
      {
         if (anchorXitems[i].getValue() == anchorx)
         {
            anchorBox.setSelectedIndex(i);
            break;
         }
      }
   }

   public AnchorX getSelectedAnchor()
   {
      Object item = anchorBox.getSelectedItem();

      if (item == null)
      {
         return null;
      }
      else
      {
         return ((AnchorXItem)item).getValue();
      }
   }

   private JComboBox<AnchorXItem> anchorBox;

   private AnchorXItem[] anchorXitems;

   private FlowframTk application;
}

class AnchorXItem
{
   AnchorXItem(AnchorX anchorX, String name)
   {
      this.anchorX = anchorX;
      this.name = name;
   }

   public String toString()
   {
      return name;
   }

   public AnchorX getValue()
   {
      return anchorX;
   }

   AnchorX anchorX;
   String name;
}

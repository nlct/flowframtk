// File          : StorageUnitPanel.java
// Description   : Component for setting storage unit
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import javax.swing.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.flowframtk.*;

public class StorageUnitPanel extends JPanel
{
   public StorageUnitPanel(JDRResources resources)
   {
      super();

      setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

      add(Box.createVerticalStrut(10));
      Box b = Box.createHorizontalBox();
      add(b);

      JLabel storageLabel = resources.createAppLabel("controls.storage_unit");
      b.add(storageLabel);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      storageLabel.setLabelFor(unitBox);
      b.add(unitBox);
      b.add(Box.createHorizontalGlue());
      b.add(Box.createHorizontalGlue());

      JTextArea textArea = resources.createAppInfoArea(
         "controls.storage_unit.note");

      Font font = textArea.getFont();

      FontMetrics fm = getFontMetrics(font);

      textArea.setPreferredSize(new Dimension(16*fm.getMaxAdvance(),
       8*fm.getHeight()));

      add(Box.createVerticalStrut(10));
      add(textArea);
   }

   public void initialise(FlowframTk application, CanvasGraphics cg)
   {
      originalUnitId = cg.getStorageUnitID();

      unitBox.setSelectedIndex(originalUnitId);
   }

   public void okay(FlowframTk application)
   {
      int unitId = unitBox.getSelectedIndex();

      if (unitId != originalUnitId)
      {
         application.setStorageUnit((byte)unitId);
      }
   }

   private JComboBox<String> unitBox;

   private int originalUnitId=-1;
}

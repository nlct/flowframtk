// File          : UnicodePanel.java
// Description   : Panel for selecting character by its unicode value
// Creation Date : 6th February 2006
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

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.font.*;
import java.awt.image.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Panel for selecting character from its unicode value.
 * @author Nicola L C Talbot
 */

public class UnicodePanel extends JPanel
   implements ActionListener
{
   public UnicodePanel(SymbolSelectorListener symbolListener,
                       CharacterSelector charSelector)
   {
      super(new BorderLayout());

      listener = symbolListener;
      selector = charSelector;

      samplePanel = new SampleCharPanel(this);

      add(samplePanel, "Center");

      JPanel topPanel = new JPanel();

      JLabel unicodeLabel = getResources().createAppLabel("symbolselector.unicode");

      topPanel.add(unicodeLabel);

      unicodeField = new HexField(20);
      unicodeField.setToolTipText(unicodeLabel.getToolTipText());

      unicodeField.setColumns(8);

      unicodeField.getDocument().addDocumentListener(
          new TextFieldSampleListener(samplePanel));
      unicodeLabel.setLabelFor(unicodeField);

      topPanel.add(unicodeField);

      selectButton = getResources().createDialogButton("symbolselector", "insertAtCaret", this, null);

      topPanel.add(selectButton);

      add(topPanel, "North");

      infoLabel = new JLabel();
      add(infoLabel, "South");
   }

   public boolean unicodeFieldHasFocus()
   {
      return unicodeField.hasFocus();
   }

   public void actionPerformed(ActionEvent evt)
   {
      String action = evt.getActionCommand();

      if (action == null) return;

      if (action.equals("insertAtCaret"))
      {
         selector.insert(getCodePoint());
      }

   }

   public void setSymbol(int value)
   {
      unicodeField.setValue(value);
   }

   public void setHexString(String hexString)
   {
      unicodeField.setText(hexString);
   }

   public void setInfo(String info)
   {
      infoLabel.setText(info);
   }

   public String getSymbol()
   {
      return new String(new int[]{getCodePoint()}, 0, 1);
   }

   public int getCodePoint()
   {
      return unicodeField.getInt();
   }

   public SymbolSelectorListener getSymbolSelectorListener()
   {
      return listener;
   }

   public RenderingHints getSymbolRenderingHints()
   {
      return listener.getRenderingHints();
   }

   public Font getSymbolFont()
   {
      return listener.getSymbolButtonFont();
   }

   public JDRResources getResources()
   {
      return selector.getResources();
   }

   private SymbolSelectorListener listener;
   private CharacterSelector selector;

   private HexField unicodeField;
   private JDRButton selectButton;
   private SampleCharPanel samplePanel;
   private JLabel infoLabel;
}

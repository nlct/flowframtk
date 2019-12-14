// File          : FontAnchorPanel.java
// Description   : Panel for selecting font anchor
// Creation Date : 2nd July 2008
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
package com.dickimawbooks.flowframtk.dialog;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

/**
 * Panel for selecting font anchor.
 * @author Nicola L C Talbot
 */

public class FontAnchorPanel extends JPanel
   implements ItemListener
{
   private FontAnchorPanel()
   {
   }

   public FontAnchorPanel(JDRSelector selector)
   {
      selector_ = selector;

      BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
      setLayout(layout);

      JPanel p1 = new JPanel();

      JLabel pgfTextLabel = getResources().createAppLabel("font.anchor");

      p1.add(pgfTextLabel);

      pgfHalign = new JComboBox<String>(
         new String[] {getResources().getString("font.anchor.left"),
                       getResources().getString("font.anchor.hcentre"),
                       getResources().getString("font.anchor.right")});

      pgfTextLabel.setLabelFor(pgfHalign);
      pgfHalign.addItemListener(this);

      p1.add(pgfHalign);

      pgfValign = new JComboBox<String>(
         new String[] {getResources().getString("font.anchor.top"),
                       getResources().getString("font.anchor.vcentre"),
                       getResources().getString("font.anchor.base"),
                       getResources().getString("font.anchor.bottom")});
      pgfValign.addItemListener(this);

      p1.add(pgfValign);

      add(p1);

      message = getResources().createAppInfoArea("font.anchor_message");
      add(message);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         selector_.repaintSample();
      }
   }

   public void setHalign(int align)
   {
      pgfHalign.setSelectedIndex(align);
   }

   public void setValign(int align)
   {
      pgfValign.setSelectedIndex(align);
   }

   public int getHalign()
   {
      return pgfHalign.getSelectedIndex();
   }

   public int getValign()
   {
      return pgfValign.getSelectedIndex();
   }

   public void setDefaults()
   {
      setHalign(JDRText.PGF_HALIGN_LEFT);
      setValign(JDRText.PGF_VALIGN_BASE);
   }

   public JDRResources getResources()
   {
      return selector_.getResources();
   }

   private JComboBox<String> pgfValign, pgfHalign;

   private JDRSelector selector_;

   private JTextArea message;
}

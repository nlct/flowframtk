// File          : SizePanel.java
// Description   : Panel for specifying a size in pt
// Creation Date : 6th February 2006
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
package com.dickimawbooks.jdrresources.numfield;

import java.text.DecimalFormat;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

public class SizePanel extends JPanel
{
   public SizePanel(JDRResources resources, SamplePanel panel)
   {
      super();

      sizeLabel = new JLabel(resources.getMessage("size.label"));
      sizeLabel.setDisplayedMnemonic(resources.getCodePoint("size.mnemonic"));
      text      = new NonNegativeDoubleField(1.0F);
      text.getDocument().addDocumentListener(
          new TextFieldSampleListener(panel));
      ptLabel   = new JLabel("pt");
      sizeLabel.setLabelFor(text);

      add(sizeLabel);
      add(text);
      add(ptLabel);
   }

   public void setEnabled(boolean flag)
   {
      sizeLabel.setEnabled(flag);
      text.setEnabled(flag);
      ptLabel.setEnabled(flag);
   }

   public double getValue()
   {
      return text.getDouble();
   }

   public void setValue(double val)
   {
      text.setValue(val);
   }

   public void addKeyListener(KeyListener kl)
   {
      text.addKeyListener(kl);
   }

   private JLabel sizeLabel, ptLabel;
   private NonNegativeDoubleField text;
}

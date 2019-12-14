// File          : NonNegativeIntPanel.java
// Description   : Panel for specifying a non-negative integers
// Creation Date : 26th May 2011
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

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for specifying a non-negative integers.
 * @author Nicola L C Talbot
 */

public class NonNegativeIntPanel extends JPanel
{
   public NonNegativeIntPanel(JDRResources resources, 
      String jdrlabel, int defValue)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      label = resources.createAppLabel(jdrlabel);

      text = new NonNegativeIntField(defValue);

      add(label);
      add(text);

      label.setLabelFor(text);
   }

   public NonNegativeIntPanel(JDRResources resources, String jdrlabel)
   {
      this(resources, jdrlabel, 0);
   }

   public Document getDocument()
   {
      return text.getDocument();
   }

   public NonNegativeIntField getTextField()
   {
      return text;
   }

   public int getValue()
   {
      return text.getInt();
   }

   public void setValue(int val)
   {
      text.setValue(val);
      text.setCaretPosition(0);
   }

   public String getLabelText()
   {
      return label.getText();
   }

   public void setEnabled(boolean flag)
   {
      if (label != null) label.setEnabled(flag);
      text.setEnabled(flag);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "NonNegativeIntPanel:"+eol;

      str += "   value box:"+eol;
      str += "      value: "+text.getInt()+eol;
      str += "      has focus: "+text.hasFocus()+eol;

      return str;
   }

   private JLabel label;
   private NonNegativeIntField text;
}

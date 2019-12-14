// File          : NonNegativeDoublePanel.java
// Description   : Panel for specifying a non-negative doubles
// Date          : 4th June 2011
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
 * Panel for specifying a non-negative doubles.
 * @author Nicola L C Talbot
 */

public class NonNegativeDoublePanel extends JPanel
{
   public NonNegativeDoublePanel(JDRResources resources,
      String jdrlabel, double defValue)
   {
      super();
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      label = resources.createAppLabel(jdrlabel);

      text = new NonNegativeDoubleField(defValue);

      add(label);
      add(text);

      label.setLabelFor(text);
   }

   public NonNegativeDoublePanel(JDRResources resources, String jdrlabel)
   {
      this(resources, jdrlabel, 0);
   }

   public Document getDocument()
   {
      return text.getDocument();
   }

   public NonNegativeDoubleField getTextField()
   {
      return text;
   }

   public double getValue()
   {
      return text.getDouble();
   }

   public void setValue(double val)
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

      String str = "NonNegativeDoublePanel:"+eol;

      str += "   value box:"+eol;
      str += "      value: "+text.getDouble()+eol;
      str += "      has focus: "+text.hasFocus()+eol;

      return str;
   }

   private JLabel label;
   private NonNegativeDoubleField text;
}

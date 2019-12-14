// File          : AnglePanel.java
// Description   : Panel to specify an angle
// Creation Date : 2014-04-01
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
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;

import com.dickimawbooks.jdr.io.JDRMessageDictionary;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for specifying an angle.
 * @author Nicola L C Talbot
 */

public class AnglePanel extends JPanel
   implements ItemListener
{
   public AnglePanel(JDRMessageDictionary msgSys, String label, NumberField numField)
   {
      super();
      this.messageSystem = msgSys;
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      sizeLabel = new JLabel(label);
      text      = numField;
      text.setColumns(4);

      sizeLabel.setLabelFor(text);

      unitBox = new JComboBox<String>
      (
        new String[]
         {
            msgSys.getString("rotate.radians", "rad"),
            msgSys.getString("rotate.degrees", "deg")
         }
      );
      unitBox.addItemListener(this);
      setUnit(JDRAngle.DEGREE);

      add(sizeLabel);
      add(text);
      add(unitBox);
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label, char mnemonic, NumberField numField)
   {
      this(msgSys, label, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public AnglePanel(JDRMessageDictionary msgSys, NumberField numField)
   {
      super();
      this.messageSystem = msgSys;
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      sizeLabel = null;
      text      = numField;
      text.setColumns(4);

      unitBox = new JComboBox<String>
      (
        new String[]
         {
            msgSys.getString("rotate.radians", "rad"),
            msgSys.getString("rotate.degrees", "deg")
         }
      ); 
      unitBox.addItemListener(this);
      setUnit(JDRAngle.DEGREE);

      add(text);
      add(unitBox);
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label, SamplePanel panel, NumberField numField)
   {
      this(msgSys, label, numField);

      text.getDocument().addDocumentListener(
          new TextFieldSampleListener(panel));
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label, char mnemonic, SamplePanel panel, NumberField numField)
   {
      this(msgSys, label, panel, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public AnglePanel(JDRMessageDictionary msgSys, SamplePanel panel, NumberField numField)
   {
      this(msgSys, numField);

      text.getDocument().addDocumentListener(
          new TextFieldSampleListener(panel));
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label)
   {
      this(msgSys, label, new DoubleField(0.0));
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label, char mnemonic)
   {
      this(msgSys, label, mnemonic, new DoubleField(0.0));
   }

   public AnglePanel(JDRMessageDictionary msgSys)
   {
      this(msgSys, new DoubleField(0.0));
   }

   public AnglePanel(JDRMessageDictionary msgSys, double value, byte unitId)
   {
      this(msgSys, new DoubleField(value));

      setUnit(unitId);
   }

   public AnglePanel(JDRMessageDictionary msgSys, String label, SamplePanel panel)
   {
      this(msgSys, label, panel, new DoubleField(0.0));
   }

   public AnglePanel(JDRMessageDictionary msgSys, SamplePanel panel)
   {
      this(msgSys, panel, new DoubleField(0.0));
   }

   public NumberField getTextField()
   {
      return text;
   }

   public Document getDocument()
   {
      return text.getDocument();
   }

   public JDRAngle getValue()
   {
      return new JDRAngle(messageSystem, text.getDouble(), getUnit());
   }

   public void setValue(JDRAngle angle)
   {
      text.setValue(angle.getValue());
      setUnit(angle.getUnitId());
      text.setCaretPosition(0);
   }

   public void addKeyListener(KeyListener kl)
   {
      text.addKeyListener(kl);
      unitBox.addKeyListener(kl);
   }

   private void setUnit(byte id)
   {
      currentUnit = id;
      unitBox.setSelectedIndex(id);
   }

   public byte getUnit()
   {
      return (byte)unitBox.getSelectedIndex();
   }

   public void setDegrees(double value)
   {
      text.setValue(value);
      setUnit(JDRAngle.DEGREE);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == unitBox)
         {
            byte oldUnit = currentUnit;

            currentUnit = (byte)unitBox.getSelectedIndex();

            if (oldUnit != currentUnit)
            {
               JDRAngle angle = new JDRAngle(messageSystem,
                  text.getDouble(), oldUnit);

               text.setValue(angle.getValue(currentUnit));
            }
         }
      }
   }

   public void setEnabled(boolean flag)
   {
      if (sizeLabel != null) sizeLabel.setEnabled(flag);
      unitBox.setEnabled(flag);
      text.setEnabled(flag);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "AnglePanel:"+eol;

      str += "   unit box:"+eol;
      str += "      value: "+unitBox.getSelectedItem()+eol;
      str += "      has focus: "+unitBox.hasFocus()+eol;
      str += "   value box:"+eol;
      str += "      value: "+text.getDouble()+eol;
      str += "      has focus: "+text.hasFocus()+eol;

      return str;
   }

   public void requestValueFocus()
   {
      text.requestFocusInWindow();
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return messageSystem;
   }

   private JLabel sizeLabel;
   private JComboBox<String> unitBox;
   private NumberField text;
   private byte currentUnit=0;

   private JDRMessageDictionary messageSystem;
}

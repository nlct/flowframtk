// File          : LengthPanel.java
// Description   : Panel to specify a length
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
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for specifying a length.
 * @author Nicola L C Talbot
 */

public class LengthPanel extends JPanel
   implements ItemListener
{
   public LengthPanel(JDRMessageDictionary msgSys, String label,
      NumberComponent numField)
   {
      super();
      this.messageSystem = msgSys;
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      sizeLabel = new JLabel(label);
      numberField = numField;
      setColumns(4);

      sizeLabel.setLabelFor(numberField.getComponent());

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS);
      unitBox.addItemListener(this);
      currentUnit = BP;
      setUnit(JDRUnit.bp);

      add(sizeLabel);
      add(numberField.getComponent());
      add(unitBox);
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label,
      char mnemonic, NumberComponent numField)
   {
      this(msgSys, label, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label,
      int mnemonic, NumberComponent numField)
   {
      this(msgSys, label, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public LengthPanel(JDRMessageDictionary msgSys, NumberComponent numField)
   {
      super();
      this.messageSystem = msgSys;
      setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

      sizeLabel = null;
      numberField = numField;
      setColumns(4);

      unitBox = new JComboBox<String>(JDRUnit.UNIT_LABELS); 
      unitBox.addItemListener(this);
      currentUnit = BP;
      setUnit(JDRUnit.bp);

      add(numberField.getComponent());
      add(unitBox);
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, 
      SamplePanel panel, NumberComponent numField)
   {
      this(msgSys, label, numField);

      addChangeListener(new TextFieldSampleListener(panel));
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, char mnemonic, 
      SamplePanel panel, NumberComponent numField)
   {
      this(msgSys, label, panel, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, int mnemonic,
      SamplePanel panel, NumberComponent numField)
   {
      this(msgSys, label, panel, numField);
      sizeLabel.setDisplayedMnemonic(mnemonic);
   }

   public LengthPanel(JDRMessageDictionary msgSys, SamplePanel panel, 
      NumberComponent numField)
   {
      this(msgSys, numField);

      addChangeListener(new TextFieldSampleListener(panel));
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label)
   {
      this(msgSys, label, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, char mnemonic)
   {
      this(msgSys, label, mnemonic, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, int mnemonic)
   {
      this(msgSys, label, mnemonic, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys)
   {
      this(msgSys, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, SamplePanel panel)
   {
      this(msgSys, label, panel, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, char mnemonic,
     SamplePanel panel)
   {
      this(msgSys, label, mnemonic, panel, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, String label, int mnemonic, 
      SamplePanel panel)
   {
      this(msgSys, label, mnemonic, panel, new NumberSpinnerField());
   }

   public LengthPanel(JDRMessageDictionary msgSys, SamplePanel panel)
   {
      this(msgSys, panel, new NumberSpinnerField());
   }

   public NumberComponent getNumberComponent()
   {
      return numberField;
   }

   public JTextField getTextField()
   {
      return getNumberComponent().getTextField();
   }

   public Document getDocument()
   {
      return getTextField().getDocument();
   }

   public double getValue(JDRUnit otherUnit)
   {
      JDRUnit unit = getUnit();

      return unit.toUnit(getNumberComponent().getDouble(), otherUnit);
   }

   public JDRLength getLength()
   {
      return new JDRLength(messageSystem, getNumberComponent().getDouble(), getUnit());
   }

   public void setValue(double value, JDRUnit unit)
   {
      getNumberComponent().setValue(value);
      setUnit(unit);
   }

   public void setValue(float value, JDRUnit unit)
   {
      getNumberComponent().setValue(value);
      setUnit(unit);
   }

   public void setLength(JDRLength length)
   {
      getNumberComponent().setValue(length.getValue());
      setUnit(length.getUnit());
   }

   public void addKeyListener(KeyListener kl)
   {
      getTextField().addKeyListener(kl);
      unitBox.addKeyListener(kl);
   }

   public void setUnit(JDRUnit unit)
   {
      currentUnit = unit.getID();

      unitBox.setSelectedIndex(currentUnit);
   }

   private JDRUnit getUnit()
   {
      return JDRUnit.getUnit(unitBox.getSelectedIndex());
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == unitBox)
         {
            JDRUnit oldUnit = JDRUnit.getUnit(currentUnit);

            currentUnit = unitBox.getSelectedIndex();
            JDRUnit unit = getUnit();

            double val = getNumberComponent().getDouble();
            double newVal = unit.fromUnit(val, oldUnit);
            getNumberComponent().setValue(newVal);
         }
      }
   }

   public void setEnabled(boolean flag)
   {
      if (sizeLabel != null) sizeLabel.setEnabled(flag);
      unitBox.setEnabled(flag);
      numberField.getComponent().setEnabled(flag);
   }

   public void setColumns(int cols)
   {
      getTextField().setColumns(cols);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "LengthPanel:"+eol;

      str += "   unit box:"+eol;
      str += "      value: "+unitBox.getSelectedItem()+eol;
      str += "      has focus: "+unitBox.hasFocus()+eol;
      str += "   value box:"+eol;
      str += "      value: "+getNumberComponent().getNumber()+eol;
      str += "      has focus: "+numberField.getComponent().hasFocus()+eol;

      return str;
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return getMessageSystem();
   }

   public void addChangeListener(ChangeListener listener)
   {
      getNumberComponent().addChangeListener(listener);
   }

   private JLabel sizeLabel;
   private JComboBox<String> unitBox;
   private NumberComponent numberField;
   public static final int PT=0, IN=1, CM=2, BP=3;
   private int currentUnit=0;

   private JDRMessageDictionary messageSystem;
}

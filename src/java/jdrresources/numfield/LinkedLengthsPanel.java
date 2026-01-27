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
package com.dickimawbooks.jdrresources.numfield;

import java.text.DecimalFormat;
import java.util.Vector;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.dickimawbooks.texjavahelplib.IconSet;
import com.dickimawbooks.texjavahelplib.TeXJavaHelpLib;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.io.JDRMessageDictionary;

import com.dickimawbooks.jdrresources.*;

/**
 * Panel for specifying two (possibly linked) lengths.
 * @author Nicola L C Talbot
 */

public class LinkedLengthsPanel extends JPanel
   implements UnitChangeListener
{
   public LinkedLengthsPanel(JDRResources resources, String parentTag)
   {
      this(resources, parentTag, Double.valueOf(0.0), null, Double.valueOf(1.0));
   }

   public LinkedLengthsPanel(JDRResources resources, String parentTag,
       Number defVal, Comparable min, Number step)
   {
      super();
      this.resources = resources;

      TeXJavaHelpLib helpLib = resources.getHelpLib();

      IconSet iconSet = helpLib.getHelpIconSet("linked", true);

      JCheckBox linkedBox;

      if (iconSet == null)
      {
         linkedBox = new JCheckBox();
      }
      else
      {
         linkedBox = iconSet.createIconCheckBox();
      }

      initialise(
        helpLib.createJLabel(parentTag+".x"),
        new NumberSpinnerField(defVal, min, null, step),
        linkedBox,
        helpLib.createJLabel(parentTag+".y"),
        new NumberSpinnerField(defVal, min, null, step)
      );
   }

   public LinkedLengthsPanel(JDRResources resources,
     JLabel label1,
     NumberComponent numberField1,
     JCheckBox linkedBox,
     JLabel label2,
     NumberComponent numberField2
    )
   {
      super();
      this.resources = resources;

      initialise(label1, numberField1, linkedBox, label2, numberField2);
   }

   protected void initialise(JLabel label1,
     NumberComponent numberField1,
     JCheckBox linkedBox,
     JLabel label2,
     NumberComponent numberField2)
   {
      setLayout(new FlowLayout(FlowLayout.LEADING));

      this.numberLabel1 = label1;
      this.numberLabel2 = label2;
      this.linkedCheckBox = linkedBox;
      this.numberField1 = numberField1;
      this.numberField2 = numberField2;

      setColumns(4);

      unitBox = new UnitField(); 
      unitBox.addUnitChangeListener(this);

      if (numberLabel1 != null)
      {
         add(numberLabel1);
         numberLabel1.setLabelFor(numberField1.getComponent());
         add(resources.createLabelSpacer());
      }

      add(numberField1.getComponent());

      add(resources.createButtonSpacer());
      add(linkedCheckBox);
      add(resources.createButtonSpacer());

      if (numberLabel2 != null)
      {
         add(numberLabel2);
         numberLabel2.setLabelFor(numberField2.getComponent());
         add(resources.createLabelSpacer());
      }

      add(numberField2.getComponent());
      add(unitBox);

      linkedCheckBox.addChangeListener(new ChangeListener()
        {
           @Override
           public void stateChanged(ChangeEvent evt)
           {
              boolean notLinked = !isLinked();

              numberField2.getComponent().setEnabled(notLinked);

              if (numberLabel2 != null)
              {
                 numberLabel2.setEnabled(notLinked);
              }
           }
        });

      numberField1.addChangeListener(new ChangeListener()
        {
           @Override
           public void stateChanged(ChangeEvent evt)
           {
              if (linkedCheckBox.isSelected())
              {
                 numberField2.setNumber(numberField1.getNumber());
              }
           }
        });

      linkedCheckBox.setSelected(true);
   }

   public NumberComponent getNumberComponent1()
   {
      return numberField1;
   }

   public NumberComponent getNumberComponent2()
   {
      return numberField2;
   }

   public JTextField getTextField1()
   {
      return getNumberComponent1().getTextField();
   }

   public JTextField getTextField2()
   {
      return getNumberComponent2().getTextField();
   }

   public Document getDocument1()
   {
      return getTextField1().getDocument();
   }

   public Document getDocument2()
   {
      return getTextField2().getDocument();
   }

   public double getValue1()
   {
      return getNumberComponent1().getDouble();
   }

   public double getValue2()
   {
      return getNumberComponent2().getDouble();
   }

   public double getValue1(JDRUnit otherUnit)
   {
      JDRUnit unit = getUnit();

      return unit.toUnit(getNumberComponent1().getDouble(), otherUnit);
   }

   public double getValue2(JDRUnit otherUnit)
   {
      JDRUnit unit = getUnit();

      return unit.toUnit(getNumberComponent2().getDouble(), otherUnit);
   }

   public JDRLength getLength1()
   {
      return new JDRLength(getMessageDictionary(),
       getNumberComponent1().getDouble(), getUnit());
   }

   public JDRLength getLength2()
   {
      return new JDRLength(getMessageDictionary(),
        getNumberComponent2().getDouble(), getUnit());
   }

   public void setValue(double value1, double value2, JDRUnit unit)
   {
      lengthIsAdjusting = true;
      setUnit(unit);
      lengthIsAdjusting = false;

      getNumberComponent1().setValue(value1);
      getNumberComponent2().setValue(value2);

      if (linkedCheckBox.isSelected() && value1 != value2)
      {
         linkedCheckBox.setSelected(false);
      }
   }

   public void translate(JDRUnit unit, double offset1, double offset2)
   {
      double val1 = getNumberComponent1().getDouble()
         + unit.toUnit(offset1, getUnit());

      double val2 = val1;

      if (!linkedCheckBox.isSelected())
      {
         val2 = getNumberComponent2().getDouble()
            + unit.toUnit(offset2, getUnit());
      }

      setValue(val1, val2, getUnit());
   }

   public void setUnit(JDRUnit unit)
   {
      unitBox.setUnit(unit);
   }

   public JDRUnit getUnit()
   {
      return unitBox.getUnit();
   }

   public UnitField getUnitField()
   {
      return unitBox;
   }

   @Override
   public void unitChanged(UnitChangeEvent evt)
   {
      Object source = evt.getSource();

      if (source == unitBox && !lengthIsAdjusting)
      {
         JDRUnit oldUnit = evt.getOldUnit();
         JDRUnit unit = evt.getNewUnit();

         double val = getNumberComponent1().getDouble();
         double newVal = unit.fromUnit(val, oldUnit);
         getNumberComponent1().setValue(newVal);

         val = getNumberComponent2().getDouble();
         newVal = unit.fromUnit(val, oldUnit);
         getNumberComponent2().setValue(newVal);
      }
   }

   public void setEnabled(boolean flag)
   {
      if (numberLabel1 != null)
      {
         numberLabel1.setEnabled(flag);
      }

      if (numberLabel2 != null)
      {
         numberLabel2.setEnabled(flag && linkedCheckBox.isSelected());
      }

      linkedCheckBox.setEnabled(flag);
      unitBox.setEnabled(flag);

      numberField1.getComponent().setEnabled(flag);
      numberField2.getComponent().setEnabled(flag && linkedCheckBox.isSelected());
   }

   public void setColumns(int cols)
   {
      getTextField1().setColumns(cols);
      getTextField2().setColumns(cols);
   }

   public String info()
   {
      String eol = System.getProperty("line.separator", "\n");

      String str = "LinkedNumbersPanel:"+eol;

      str += "   unit box:"+eol;
      str += "      value: "+unitBox.getSelectedItem()+eol;
      str += "      has focus: "+unitBox.hasFocus()+eol;
      str += "   value1 box:"+eol;
      str += "      value1: "+getNumberComponent1().getNumber()+eol;
      str += "      has focus: "+numberField1.getComponent().hasFocus()+eol;
      str += "   value2 box:"+eol;
      str += "      value2: "+getNumberComponent2().getNumber()+eol;
      str += "      has focus: "+numberField2.getComponent().hasFocus()+eol;

      return str;
   }

   public JDRMessageDictionary getMessageSystem()
   {
      return getMessageSystem();
   }

   public void addChangeListener(ChangeListener listener)
   {
      getNumberComponent1().addChangeListener(listener);
      getNumberComponent2().addChangeListener(listener);
   }

   public boolean isLinked()
   {
      return linkedCheckBox.isSelected();
   }

   public void setLinked(boolean linked)
   {
      linkedCheckBox.setSelected(linked);
   }

   public JDRMessageDictionary getMessageDictionary()
   {
      return resources.getMessageDictionary();
   }

   public JLabel getNumberLabel1()
   {
      return numberLabel1;
   }

   public JLabel getNumberLabel2()
   {
      return numberLabel2;
   }

   private JLabel numberLabel1, numberLabel2;
   private UnitField unitBox;
   private NumberComponent numberField1, numberField2;
   boolean lengthIsAdjusting = false;

   JCheckBox linkedCheckBox;

   private JDRResources resources;
}

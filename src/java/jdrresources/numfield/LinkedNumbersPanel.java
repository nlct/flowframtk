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
 * Panel for specifying two (possibly linked) numbers.
 * @author Nicola L C Talbot
 */

public class LinkedNumbersPanel extends JPanel
{
   public LinkedNumbersPanel(JDRResources resources, String parentTag)
   {
      this(resources, parentTag, Integer.valueOf(0), Integer.valueOf(0),
           Integer.valueOf(1));
   }

   public LinkedNumbersPanel(JDRResources resources, String parentTag,
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

   public LinkedNumbersPanel(JDRResources resources,
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
         numberLabel1.setLabelFor(numberField2.getComponent());
         add(resources.createLabelSpacer());
      }

      add(numberField2.getComponent());

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

   public Number getValue1()
   {
      return getNumberComponent1().getNumber();
   }

   public Number getValue2()
   {
      return getNumberComponent2().getNumber();
   }

   public void setValue(Number value1, Number value2)
   {
      getNumberComponent1().setNumber(value1);
      getNumberComponent2().setNumber(value2);

      if (linkedCheckBox.isSelected() && !value1.equals(value2))
      {
         linkedCheckBox.setSelected(false);
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
   private NumberComponent numberField1, numberField2;
   boolean lengthIsAdjusting = false;

   JCheckBox linkedCheckBox;

   private JDRResources resources;
}

// File          : UnitField.java
// Description   : Selector for units
// Date          : 2020-09-02
// Author        : Nicola L.C. Talbot
//                 http://www.dickimaw-books.com/

/*
    Copyright (C) 2020 Nicola L.C. Talbot

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

import java.util.Vector;

import java.awt.event.ItemListener;
import java.awt.event.ItemEvent;

import javax.swing.JComboBox;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import com.dickimawbooks.jdr.JDRUnit;

public class UnitField extends JComboBox<String> implements ItemListener
{
   public UnitField()
   {
      super(JDRUnit.UNIT_LABELS);
      addItemListener(this);
      currentUnit = BP;
      setUnit(JDRUnit.bp);
   }

   public JDRUnit getUnit()
   {
      return JDRUnit.getUnit(getSelectedIndex());
   }

   public void setUnit(JDRUnit unit)
   {
      currentUnit = unit.getID();

      setSelectedIndex(currentUnit);
   }

   public void itemStateChanged(ItemEvent evt)
   {
      Object source = evt.getSource();

      if (evt.getStateChange() == ItemEvent.SELECTED)
      {
         if (source == this)
         {
            int oldUnitId = currentUnit;
            JDRUnit oldUnit = JDRUnit.getUnit(currentUnit);

            currentUnit = getSelectedIndex();
            JDRUnit unit = getUnit();

            if (oldUnitId != currentUnit && unitChangeListeners != null)
            {
               UnitChangeEvent unitEvent
                  = new UnitChangeEvent(this, oldUnit, unit);

               for (UnitChangeListener listener : unitChangeListeners)
               {
                  listener.unitChanged(unitEvent);
               }
            }
         }
      }
   }

   public void addUnitChangeListener(UnitChangeListener listener)
   {
      if (unitChangeListeners == null)
      {
         unitChangeListeners = new Vector<UnitChangeListener>();
      }

      unitChangeListeners.add(listener);
   }

   public static final int PT=0, IN=1, CM=2, BP=3;
   private int currentUnit=0;

   private Vector<UnitChangeListener> unitChangeListeners = null;
}

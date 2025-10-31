// File          : LocationPaneBox.java
// Description   : Tabbed pane in which to specify new location
// Creation Date : 2012-03-06
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

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

/**
 * Tabbed pane in which to specify a new location.
 * @author Nicola L C Talbot
 */
public class LocationPane extends JTabbedPane
   implements ChangeListener
{
   public LocationPane(JDRResources resources)
   {
      this(resources, true);
   }

   public LocationPane(JDRResources resources, boolean addMnemonics)
   {
      this(resources, "grid", addMnemonics);
   }

   public LocationPane(JDRResources resources, String baseId)
   {
      this(resources, baseId, true);
   }

   public LocationPane(JDRResources resources, String baseId, boolean addMnemonics)
   {
      super();

      rectCoordPanel = new RectangularCoordPanel(resources);

      add(rectCoordPanel, RECTANGULAR);

      if (addMnemonics)
      {
         setMnemonicAt(RECTANGULAR,
            resources.getCodePoint(baseId+".rectangular.mnemonic"));
      }

      radialCoordPanel = new RadialCoordPanel(resources);

      add(radialCoordPanel, RADIAL);

      if (addMnemonics)
      {
         setMnemonicAt(RADIAL,
            resources.getCodePoint(baseId+".radial.mnemonic"));
      }

      addChangeListener(this);
   }

   public void stateChanged(ChangeEvent evt)
   {
      int old = current;

      current = getSelectedIndex();

      if (current != old)
      {
         CoordPanel coordPanel = (CoordPanel)getComponentAt(old);

         JDRLength x = coordPanel.getXCoord();
         JDRLength y = coordPanel.getYCoord();

         coordPanel = (CoordPanel)getComponentAt(current);

         coordPanel.setCoords(x.getValue(unit), y.getValue(unit), unit, paper);
      }
   }

   public void setCoords(double x, double y, JDRUnit unit, JDRPaper paper,
      JDRGrid grid)
   {
      this.grid  = grid;
      this.paper = paper;
      this.unit  = unit;

      rectCoordPanel.setCoords(x, y, unit, paper);
      radialCoordPanel.setCoords(x, y, unit, paper);

      if (grid instanceof JDRRectangularGrid)
      {
         setSelectedComponent(rectCoordPanel);
         current = RECTANGULAR;
      }
      else if (grid instanceof JDRRadialGrid)
      {
         setSelectedComponent(radialCoordPanel);
         current = RADIAL;
         radialCoordPanel.setPageCentred(((JDRRadialGrid)grid).isPageCentred());
      }

      getCoordPanel().requestCoordFocus();
   }

   public void translate(JDRUnit unit, double dx, double dy)
   {
      rectCoordPanel.translate(unit, dx, dy);
      radialCoordPanel.translate(unit, dx, dy);
   }

   public JDRLength getXCoord()
   {
      return getCoordPanel().getXCoord();
   }

   public JDRLength getYCoord()
   {
      return getCoordPanel().getYCoord();
   }

   public CoordPanel getCoordPanel()
   {
      return (CoordPanel)getSelectedComponent();
   }

   public void addCoordinateChangeListener(ChangeListener listener)
   {
      rectCoordPanel.addCoordinateChangeListener(listener);
      radialCoordPanel.addCoordinateChangeListener(listener);
   }

   public void setPanelsEnabled(boolean enabled)
   {
      rectCoordPanel.setEnabled(enabled);
      radialCoordPanel.setEnabled(enabled);
   }

   public JDRPaper getPaper()
   {
      return paper;
   }

   public JDRUnit getUnit()
   {
      return unit;
   }

   public JDRGrid getGrid()
   {
      return grid;
   }

   private RectangularCoordPanel rectCoordPanel;
   private RadialCoordPanel radialCoordPanel;

   private JDRGrid grid;
   private JDRPaper paper;
   private JDRUnit unit;

   private static final int RECTANGULAR = 0;
   private static final int RADIAL = 1;

   private int current=0;
}

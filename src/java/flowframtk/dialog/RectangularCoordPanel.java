// File          : RectangularCoordPanel.java
// Description   : Panel for entering rectangular coordinates
// Creation Date : 2012-03-05
// Author        : Nicola L. C. Talbot
//               http://www.dickimaw-books.com/

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

import javax.swing.*;
import javax.swing.event.ChangeListener;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdrresources.*;
import com.dickimawbooks.jdrresources.numfield.*;

import com.dickimawbooks.flowframtk.*;

public class RectangularCoordPanel extends JPanel implements CoordPanel
{
   public RectangularCoordPanel(JDRResources resources)
   {
      super();

      xPanel = resources.createLengthPanel("coordinates.x");
      add(xPanel);

      yPanel = resources.createLengthPanel("coordinates.y");
      add(yPanel);

      setName(resources.getString("grid.rectangular"));
   }

   public void requestCoordFocus()
   {
      xPanel.getTextField().requestFocusInWindow();
   }

   public void setUnit(JDRUnit unit)
   {
      xPanel.setUnit(unit);
      yPanel.setUnit(unit);
   }

   public void setCoords(double x, double y, JDRUnit unit, JDRPaper paper)
   {
      xPanel.setValue(x, unit);
      yPanel.setValue(y, unit);
   }

   public JDRLength getXCoord()
   {
      return xPanel.getLength();
   }

   public JDRLength getYCoord()
   {
      return yPanel.getLength();
   }

   public void addCoordinateChangeListener(ChangeListener listener)
   {
      xPanel.addChangeListener(listener);
      yPanel.addChangeListener(listener);
   }

   public void setEnabled(boolean enable)
   {
      super.setEnabled(enable);
      xPanel.setEnabled(enable);
      yPanel.setEnabled(enable);
   }

   private LengthPanel xPanel, yPanel;
}

// File          : CoordPanel.java
// Description   : Interface for entering coordinates
// Creation Date : 2012-03-05
// Author        : Nicola L. C. Talbot
//               http://www.dickimaw-books.com/

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

import javax.swing.event.ChangeListener;

import com.dickimawbooks.jdr.*;

public interface CoordPanel
{
   public void requestCoordFocus();

   public void setCoords(double x, double y, JDRUnit unit, JDRPaper paper);

   public void translate(JDRUnit unit, double dx, double dy);

   public void setUnit(JDRUnit unit);

   public JDRLength getXCoord();

   public JDRLength getYCoord();

   public void addCoordinateChangeListener(ChangeListener listener);
}

// File          : JDRRadialGridListener.java
// Creation Date : 17th August 2010
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for radial grids.
 * @author Nicola L C Talbot
 */

public class JDRRadialGridListener implements JDRGridLoaderListener
{
   public byte getId(float version)
   {
      return 1;
   }

   public JDRGrid getGrid(JDRAJR jdr, JDRGrid grid, float version)
   {
      if (version < 1.6f)
      {
         return grid.getRectangularGrid();
      }

      return grid;
   }

   public void write(JDRAJR jdr, JDRGrid object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRRadialGrid grid = (JDRRadialGrid)object;

      if (version < 1.6f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            grid.getClass().getName()+" ("+version+")", jdr);
      }

      jdr.writeByte((byte)grid.getUnit().getID());
      jdr.writeDouble(grid.getMajorInterval());
      jdr.writeInt(grid.getSubDivisions());
      jdr.writeInt(grid.getSpokes());

      boolean pageDependent = grid.isPageCentred();

      if (version >= 2.1f)
      {
         jdr.writeBoolean(pageDependent);
      }
      else if (!pageDependent)
      {
         jdr.warningWithFallback("warning.save_unsupported_radial_origin_grid",
          "Origin-based radial grid type not supported in JDR/AJR version {0}",
          version);
      }
   }

   public JDRGrid read(JDRAJR jdr) throws InvalidFormatException
   {
      float version = jdr.getVersion();

      byte unitID = jdr.readByte(InvalidFormatException.UNIT_ID);

      JDRUnit unit = JDRUnit.getUnit(unitID);

      if (unit == null)
      {
         throw new InvalidValueException(
            InvalidFormatException.UNIT_ID, unitID, jdr);
      }

      double majorDivisions;

      if (version < 1.6f)
      {
         majorDivisions = jdr.readIntGt(InvalidFormatException.GRID_MAJOR, 0);
      }
      else
      {
         majorDivisions = jdr.readDoubleGt(InvalidFormatException.GRID_MAJOR, 0);
      }

      int subDivisions = jdr.readIntGe(InvalidFormatException.GRID_MINOR, 0);

      if (version < 1.6f)
      {
         return new JDRRectangularGrid(jdr.getCanvasGraphics(),
            unit, majorDivisions, subDivisions);
      }

      int spokes = jdr.readIntGe(InvalidFormatException.GRID_SPOKES, 0);

      JDRRadialGrid grid = new JDRRadialGrid(jdr.getCanvasGraphics(),
         unit, majorDivisions, subDivisions, spokes);

      if (version >= 2.1f)
      {
         grid.setPageCentred(
          jdr.readBoolean(InvalidFormatException.GRID_RADIAL_PAGE_DEPENDENT));
      }

      return grid;
   }

}

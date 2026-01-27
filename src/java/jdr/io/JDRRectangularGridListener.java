// File          : JDRRectangularGridListener.java
// Creation Date : 17th August 2010
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

package com.dickimawbooks.jdr.io;

import java.io.*;
import java.util.*;

import com.dickimawbooks.jdr.*;
import com.dickimawbooks.jdr.exceptions.*;

/**
 * Loader listener for rectangular grids.
 * @author Nicola L C Talbot
 */

public class JDRRectangularGridListener implements JDRGridLoaderListener
{
   public byte getId(float version)
   {
      return 0;
   }

   public JDRGrid getGrid(JDRAJR jdr, JDRGrid grid, float version)
   {
      return grid;
   }

   public void write(JDRAJR jdr, JDRGrid object)
      throws IOException
   {
      float version = jdr.getVersion();

      JDRRectangularGrid grid = (JDRRectangularGrid)object;

      jdr.writeByte((byte)grid.getUnit().getID());

      if (version < 1.6f)
      {
         jdr.writeInt((int)Math.round(grid.getMajorXInterval()));
      }
      else
      {
         jdr.writeDouble(grid.getMajorXInterval());

         if (version >= 2.2f)
         {
            jdr.writeDouble(grid.getMajorYInterval());
         }
      }

      jdr.writeInt(grid.getSubDivisionsX());

      if (version >= 2.2f)
      {
         jdr.writeInt(grid.getSubDivisionsY());
      }
   }

   public JDRGrid read(JDRAJR jdr)
      throws InvalidFormatException
   {
      float version = jdr.getVersion();

      byte unitID = jdr.readByte(InvalidFormatException.UNIT_ID);

      JDRUnit unit = JDRUnit.getUnit(unitID);

      if (unit == null)
      {
         throw new InvalidValueException(
            InvalidFormatException.UNIT_ID, unitID, jdr);
      }

      double majorDivisionsX;
      double majorDivisionsY;

      if (version < 1.6f)
      {
         majorDivisionsX = jdr.readIntGt(InvalidFormatException.GRID_MAJOR, 0);
         majorDivisionsY = majorDivisionsX;
      }
      else
      {
         majorDivisionsX = jdr.readDoubleGt(InvalidFormatException.GRID_MAJOR, 0);
         majorDivisionsY = majorDivisionsX;

         if (version >= 2.2f)
         {
            majorDivisionsY = jdr.readDoubleGt(InvalidFormatException.GRID_MAJOR, 0);
         }
      }

      int subDivisionsX = jdr.readIntGe(InvalidFormatException.GRID_MINOR, 0);
      int subDivisionsY = subDivisionsX;

      if (version >= 2.2f)
      {
         subDivisionsY = jdr.readIntGe(InvalidFormatException.GRID_MINOR, 0);
      }

      return new JDRRectangularGrid(jdr.getCanvasGraphics(),
         unit, majorDivisionsX, majorDivisionsY,
         subDivisionsX, subDivisionsY);
   }

}

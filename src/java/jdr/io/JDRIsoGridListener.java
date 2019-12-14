// File          : JDRIsoGridListener.java
// Creation Date : 2014-06-06
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
 * Loader listener for isometric grids.
 * @author Nicola L C Talbot
 */

public class JDRIsoGridListener implements JDRGridLoaderListener
{
   public byte getId(float version)
   {
      return 2;
   }

   public JDRGrid getGrid(JDRAJR jdr, JDRGrid grid, float version)
   {
      return (version < 1.8f ? ((JDRIsoGrid)grid).getRectangularGrid(): grid);
   }

   public void write(JDRAJR jdr, JDRGrid object)
      throws IOException
   {
      float version = jdr.getVersion();

      if (version < 1.8f)
      {
         throw new JdrIllegalArgumentException(
            JdrIllegalArgumentException.UNSUPPORTED_VERSION,
            object.getClass().getName()+" ("+version+")", jdr);
      }

      JDRIsoGrid grid = (JDRIsoGrid)object;

      jdr.writeByte((byte)grid.getUnit().getID());

      jdr.writeDouble(grid.getMajorInterval());

      jdr.writeInt(grid.getSubDivisions());
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

      double majorDivisions;

      majorDivisions = jdr.readDoubleGt(InvalidFormatException.GRID_MAJOR, 0);

      int subDivisions = jdr.readIntGe(InvalidFormatException.GRID_MINOR, 0);

      return new JDRIsoGrid(jdr.getCanvasGraphics(),
         unit, majorDivisions, subDivisions);
   }

}

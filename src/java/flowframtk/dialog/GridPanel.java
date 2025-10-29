// File          : GridPanel.java
// Description   : Panel in which to specify grid settings
// Date          : 14th Sept 2010
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

import java.awt.event.ItemListener;
import javax.swing.*;

import com.dickimawbooks.jdr.*;

/**
 * Panel in which to specify grid settings.
 * @author Nicola L C Talbot
 */
public abstract class GridPanel extends JPanel
{
   public abstract void requestDefaultFieldFocus();

   public abstract void addUnitChangeListener(ItemListener listener);

   public abstract void setGrid(JDRGrid grid);

   /**
    * Gets the grid for this panel. 
    * @param grid the old grid this is replacing.
    */
   public abstract JDRGrid getGrid(JDRGrid grid);

   /**
    * Set common shortcut.
    */
   public void setCommon(int setting)
   {
      switch (setting)
      {
         case GRID_1CM_X_2:
           setUnit(JDRUnit.cm);
           setMajor(1);
           setSubDivisions(2);
         break;
         case GRID_1CM_X_4:
           setUnit(JDRUnit.cm);
           setMajor(1);
           setSubDivisions(4);
         break;
         case GRID_1CM_X_10:
           setUnit(JDRUnit.cm);
           setMajor(1);
           setSubDivisions(10);
         break;
         case GRID_1IN_X_4:
           setUnit(JDRUnit.in);
           setMajor(1);
           setSubDivisions(4);
         break;
         case GRID_1IN_X_8:
           setUnit(JDRUnit.in);
           setMajor(1);
           setSubDivisions(8);
         break;
         case GRID_1IN_X_10:
           setUnit(JDRUnit.in);
           setMajor(1);
           setSubDivisions(10);
         break;
         case GRID_1IN_X_16:
           setUnit(JDRUnit.in);
           setMajor(1);
           setSubDivisions(16);
         break;
         case GRID_100BP_X_10:
           setUnit(JDRUnit.bp);
           setMajor(100);
           setSubDivisions(10);
         break;
         case GRID_100PT_X_10:
           setUnit(JDRUnit.pt);
           setMajor(100);
           setSubDivisions(10);
         break;
         default:
           throw new IllegalArgumentException("Unknown common setting: "+setting);
      }
   }

   public static String getCommonString(int setting)
   {
      switch (setting)
      {
         case GRID_1CM_X_2: return "1cm x 2";
         case GRID_1CM_X_4: return "1cm x 4";
         case GRID_1CM_X_10: return "1cm x 10";
         case GRID_1IN_X_4: return "1in x 4";
         case GRID_1IN_X_8: return "1in x 8";
         case GRID_1IN_X_10: return "1in x 10";
         case GRID_1IN_X_16: return "1in x 16";
         case GRID_100BP_X_10: return "100bp x 10";
         case GRID_100PT_X_10: return "100pt x 10";
         default:
           throw new IllegalArgumentException("Unknown common setting: "+setting);
      }
   }


   protected void setMajor(int value) { }
   protected void setSubDivisions(int value) { }
   public abstract void setUnit(JDRUnit unit);
   public abstract JDRUnit getUnit();

   public static final int GRID_1CM_X_2=0;
   public static final int GRID_1CM_X_4=1;
   public static final int GRID_1CM_X_10=2;
   public static final int GRID_1IN_X_4=3;
   public static final int GRID_1IN_X_8=4;
   public static final int GRID_1IN_X_10=5;
   public static final int GRID_1IN_X_16=6;
   public static final int GRID_100BP_X_10=7;
   public static final int GRID_100PT_X_10=8;

   public static final int GRID_MAX_COMMON=9;
}

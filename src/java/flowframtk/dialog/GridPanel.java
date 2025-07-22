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

import javax.swing.*;

import com.dickimawbooks.jdr.*;

/**
 * Panel in which to specify grid settings.
 * @author Nicola L C Talbot
 */
public abstract class GridPanel extends JPanel
{
   public abstract void requestDefaultFieldFocus();

   public abstract void setGrid(JDRGrid grid);

   /**
    * Gets the grid for this panel. 
    * @param grid the old grid this is replacing.
    */
   public abstract JDRGrid getGrid(JDRGrid grid);

}
